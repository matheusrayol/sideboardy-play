import { Shuffle } from 'lucide-react';
import { useMemo, useState } from 'react';
import type { CardView } from '../../protocol/generated/views';
import { cardsSeen, deckStats, drawOdds, expandDeck, MANA_LETTERS, shuffle } from '../../core/decks/analysis';
import type { DeckCardInfo, DeckCardLists } from '../../core/decks/types';
import { useTx } from '../i18n';
import { Button } from '../ui/Button';
import { CardFace } from '../ui/CardFace';
import { Dialog } from '../ui/Dialog';
import { simulateDeck } from '../../core/decks/simulate';
import { entryKey } from './deckModel';
import styles from './DeckInsights.module.css';

type Tab = 'stats' | 'simulate' | 'hand';

const TURNS = [1, 2, 3, 4, 5, 6];

/** Numbers about the main deck and a sample opening hand. */
export function DeckInsights({ open, onOpenChange, deck, info }: {
  open: boolean;
  onOpenChange(open: boolean): void;
  deck: DeckCardLists;
  info: ReadonlyMap<string, CardView>;
}) {
  const tx = useTx();
  const [tab, setTab] = useState<Tab>('stats');
  return (
    <Dialog open={open} onOpenChange={onOpenChange} title={deck.name || tx('Deck')} width="xl">
      <div className={styles.tabs} role="tablist" aria-label={tx('Deck insights')}>
        <button type="button" role="tab" aria-selected={tab === 'stats'} className={tab === 'stats' ? styles.tabOn : styles.tab} onClick={() => setTab('stats')}>{tx('Numbers')}</button>
        <button type="button" role="tab" aria-selected={tab === 'simulate'} className={tab === 'simulate' ? styles.tabOn : styles.tab} onClick={() => setTab('simulate')}>{tx('Simulate')}</button>
        <button type="button" role="tab" aria-selected={tab === 'hand'} className={tab === 'hand' ? styles.tabOn : styles.tab} onClick={() => setTab('hand')}>{tx('Sample hand')}</button>
      </div>
      <div role="tabpanel">
        {tab === 'stats' && <Stats entries={deck.cards} info={info} />}
        {tab === 'simulate' && <Simulate entries={deck.cards} info={info} />}
        {tab === 'hand' && <SampleHand entries={deck.cards} info={info} />}
      </div>
    </Dialog>
  );
}

function Stats({ entries, info }: { entries: DeckCardInfo[]; info: ReadonlyMap<string, CardView> }) {
  const tx = useTx();
  const infoOf = (entry: DeckCardInfo) => info.get(entryKey(entry));
  const stats = deckStats(entries, infoOf);
  const [onThePlay, setOnThePlay] = useState(true);
  const totalPips = MANA_LETTERS.reduce((sum, letter) => sum + stats.pips[letter], 0);
  // the cards worth asking about: every nonland card, most copies first
  const rows = useMemo(() => {
    const byName = new Map<string, { name: string; copies: number }>();
    for (const entry of entries) {
      const card = info.get(entryKey(entry));
      if ((card?.cardTypes ?? []).includes('LAND')) continue;
      const row = byName.get(entry.cardName) ?? { name: entry.cardName, copies: 0 };
      row.copies += entry.amount;
      byName.set(entry.cardName, row);
    }
    return [...byName.values()].sort((a, b) => b.copies - a.copies || a.name.localeCompare(b.name)).slice(0, 12);
  }, [entries, info]);

  return (
    <div className={styles.stats}>
      <section className={styles.figures} aria-label={tx('Totals')}>
        <Figure value={stats.cards} label="Cards" />
        <Figure value={stats.lands} label="Lands" />
        <Figure value={stats.nonlands} label="Spells" />
        <Figure value={stats.averageValue.toFixed(2)} label="Average mana value" />
      </section>

      <section aria-label={tx('Card types')}>
        <h3 className={styles.head}>{tx('Types')}</h3>
        <ul className={styles.bars}>
          {stats.types.map((type) => (
            <li key={type.label}>
              <span>{tx(type.label)}</span>
              <span className={styles.track}><span style={{ width: `${(type.count / Math.max(1, stats.cards)) * 100}%` }} /></span>
              <b>{type.count}</b>
            </li>
          ))}
        </ul>
      </section>

      <section aria-label={tx('Colored mana symbols')}>
        <h3 className={styles.head}>{tx('Mana symbols')}</h3>
        {totalPips === 0 ? <p className={styles.note}>{tx('No colored symbols.')}</p> : (
          <ul className={styles.bars}>
            {MANA_LETTERS.filter((letter) => stats.pips[letter] > 0).map((letter) => (
              <li key={letter}>
                <span><i className={`ms ms-cost ms-${letter.toLowerCase()}`} aria-hidden="true" /> {letter}</span>
                <span className={styles.track}><span style={{ width: `${(stats.pips[letter] / totalPips) * 100}%` }} /></span>
                <b>{Math.round((stats.pips[letter] / totalPips) * 100)}%</b>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className={styles.odds} aria-label={tx('Draw odds')}>
        <header className={styles.oddsHead}>
          <h3 className={styles.head}>{tx('Chance to have drawn at least one, by turn')}</h3>
          <label className={styles.toggle}>
            <input type="checkbox" checked={onThePlay} onChange={(event) => setOnThePlay(event.target.checked)} />
            {tx('On the play')}
          </label>
        </header>
        <table className={styles.table}>
          <thead>
            <tr>
              <th scope="col">{tx('Card')}</th>
              {TURNS.map((turn) => <th key={turn} scope="col">T{turn}</th>)}
            </tr>
          </thead>
          <tbody>
            <tr>
              <th scope="row">{tx('A land')}</th>
              {TURNS.map((turn) => <td key={turn}>{percent(drawOdds(stats.lands, stats.cards, cardsSeen(turn, onThePlay), Math.min(turn, stats.lands)))}</td>)}
            </tr>
            {rows.map((row) => (
              <tr key={row.name}>
                <th scope="row">{row.copies}× {row.name}</th>
                {TURNS.map((turn) => <td key={turn}>{percent(drawOdds(row.copies, stats.cards, cardsSeen(turn, onThePlay)))}</td>)}
              </tr>
            ))}
          </tbody>
        </table>
        <p className={styles.note}>{tx('“A land” is the chance to have hit your land drop every turn so far.')}</p>
      </section>
    </div>
  );
}

const SIM_RUNS = 1000;
const SIM_TURNS = 7;

/** A thousand games played alone, a land a turn: what the odds tables can't say about colors and curve. */
function Simulate({ entries, info }: { entries: DeckCardInfo[]; info: ReadonlyMap<string, CardView> }) {
  const tx = useTx();
  const [onThePlay, setOnThePlay] = useState(true);
  const [round, setRound] = useState(0);
  const result = useMemo(
    () => simulateDeck(entries, (entry) => info.get(entryKey(entry as DeckCardInfo)), { runs: SIM_RUNS, turns: SIM_TURNS, onThePlay }),
    // a new round deals new games
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [entries, info, onThePlay, round],
  );
  if (entries.length === 0) return <p className={styles.note}>{tx('Add cards to simulate the deck.')}</p>;
  return (
    <div className={styles.stats}>
      <section className={styles.figures} aria-label={tx('Mana trouble')}>
        <Figure value={percent(result.screwed)} label="Stuck on 2 lands by turn 4" />
        <Figure value={percent(result.flooded)} label="3+ lands in hand on turn 6" />
        <Figure value={percent(result.byTurn[3]?.allDrops ?? 0)} label="Every land drop to turn 4" />
      </section>

      <section className={styles.odds} aria-label={tx('By turn')}>
        <header className={styles.oddsHead}>
          <h3 className={styles.head}>{tx(`${SIM_RUNS} games, a land a turn`)}</h3>
          <label className={styles.toggle}>
            <input type="checkbox" checked={onThePlay} onChange={(event) => setOnThePlay(event.target.checked)} />
            {tx('On the play')}
          </label>
          <Button size="sm" variant="quiet" icon={<Shuffle size={15} />} onClick={() => setRound((value) => value + 1)}>Deal again</Button>
        </header>
        <table className={styles.table}>
          <thead>
            <tr>
              <th scope="col">{tx('Turn')}</th>
              {result.byTurn.map((turn) => <th key={turn.turn} scope="col">T{turn.turn}</th>)}
            </tr>
          </thead>
          <tbody>
            <tr>
              <th scope="row">{tx('Lands in play')}</th>
              {result.byTurn.map((turn) => <td key={turn.turn}>{turn.averageLands.toFixed(1)}</td>)}
            </tr>
            <tr>
              <th scope="row">{tx('Every drop so far')}</th>
              {result.byTurn.map((turn) => <td key={turn.turn}>{percent(turn.allDrops)}</td>)}
            </tr>
            <tr>
              <th scope="row">{tx('A spell on curve')}</th>
              {result.byTurn.map((turn) => <td key={turn.turn}>{percent(turn.onCurve)}</td>)}
            </tr>
            <tr>
              <th scope="row">{tx('Anything to cast')}</th>
              {result.byTurn.map((turn) => <td key={turn.turn}>{percent(turn.anyPlay)}</td>)}
            </tr>
          </tbody>
        </table>
        <p className={styles.note}>{tx('“On curve” is a spell costing exactly the turn number, with the lands and colors to cast it.')}</p>
      </section>

      <section className={styles.odds} aria-label={tx('First seen')}>
        <h3 className={styles.head}>{tx('When each card shows up')}</h3>
        <table className={styles.table}>
          <thead>
            <tr>
              <th scope="col">{tx('Card')}</th>
              <th scope="col">{tx('Usually by')}</th>
              <th scope="col">{tx('By T3')}</th>
              <th scope="col">{tx(`By T${result.turns}`)}</th>
            </tr>
          </thead>
          <tbody>
            {result.cards.slice(0, 16).map((card) => (
              <tr key={card.name}>
                <th scope="row">{card.copies}× {card.name}</th>
                <td>{card.medianTurn === null ? tx(`after T${result.turns}`) : `T${card.medianTurn}`}</td>
                <td>{percent(card.byTurn3)}</td>
                <td>{percent(card.byLastTurn)}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <p className={styles.note}>{tx('“Usually by” is the turn half the games have the card in hand.')}</p>
      </section>
    </div>
  );
}

function Figure({ value, label }: { value: number | string; label: string }) {
  const tx = useTx();
  return (
    <div className={styles.figure}>
      <b>{value}</b>
      <span>{tx(label)}</span>
    </div>
  );
}

function percent(value: number): string {
  return `${Math.round(value * 100)}%`;
}

function SampleHand({ entries, info }: { entries: DeckCardInfo[]; info: ReadonlyMap<string, CardView> }) {
  const tx = useTx();
  const library = useMemo(() => expandDeck(entries), [entries]);
  const [deal, setDeal] = useState(() => ({ order: shuffle(library), drawn: 7 }));
  const cards = deal.order.slice(0, deal.drawn);
  if (library.length === 0) return <p className={styles.note}>{tx('Add cards to deal a hand.')}</p>;
  return (
    <div className={styles.hand}>
      <div className={styles.handCards}>
        {cards.map((entry, index) => {
          const card = info.get(entryKey(entry)) ?? { name: entry.cardName, expansionSetCode: entry.setCode ?? undefined, cardNumber: entry.cardNumber ?? undefined };
          return (
            <div key={index} className={[styles.handCard, index >= 7 ? styles.drawn : ''].join(' ')}>
              <CardFace card={card} size="normal" />
            </div>
          );
        })}
      </div>
      <div className={styles.handActions}>
        <Button variant="print" icon={<Shuffle size={16} />} onClick={() => setDeal({ order: shuffle(library), drawn: 7 })}>New hand</Button>
        <Button variant="quiet" disabled={deal.drawn >= library.length} onClick={() => setDeal((current) => ({ ...current, drawn: current.drawn + 1 }))}>Draw a card</Button>
        <span className={styles.note}>{tx(`${library.length - deal.drawn} cards left in the library`)}</span>
      </div>
    </div>
  );
}
