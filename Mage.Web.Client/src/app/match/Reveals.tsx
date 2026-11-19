import { X } from 'lucide-react';
import { useEffect, useMemo, useRef, useState } from 'react';
import { revealSeat, type RevealGroup } from '../../core/game/reveals';
import { useT, type Translate } from '../i18n';
import './matchMessages';
import { CardFace } from '../ui/CardFace';
import { useMatchUi } from './matchUi';
import styles from './Reveals.module.css';

function formatRevealLabel(t: Translate, group: RevealGroup): string {
  if (group.kind === 'revealed') return group.title ? t('match.reveals.revealedNamed', { title: group.title }) : t('match.reveals.revealed');
  return group.title ? t('match.reveals.lookedAtNamed', { title: group.title }) : t('match.reveals.lookedAt');
}

/**
 * This turn's revealed and looked-at cards (useReveals), as a strip at the seat of the player whose cards they are, next to the seam. The
 * newest reveal shows; click it to read the cards, dismiss it once seen. Fresh reveals are marked until opened. With
 * `autoDismiss` (Settings: Hide revealed cards after a moment), a strip goes away by itself unless pointed at.
 */
export function Reveals({ groups, myPlayerId, opponentIds, autoDismiss = false }: { groups: RevealGroup[]; myPlayerId: string | null; opponentIds: string[]; autoDismiss?: boolean }) {
  const t = useT();
  const [seen, setSeen] = useState<ReadonlySet<string>>(() => new Set());
  const [dismissed, setDismissed] = useState<ReadonlySet<string>>(() => new Set());
  const openViewer = useMatchUi((state) => state.openViewer);

  const bySide = useMemo(() => {
    const seats = new Set([...(myPlayerId ? [myPlayerId] : []), ...opponentIds]);
    const mine: RevealGroup[] = [];
    const theirs: RevealGroup[] = [];
    for (const group of groups) {
      if (dismissed.has(group.key)) continue;
      (revealSeat(group, seats, myPlayerId) === myPlayerId ? mine : theirs).push(group);
    }
    return { mine, theirs };
  }, [groups, dismissed, myPlayerId, opponentIds]);

  const markSeen = (keys: string[]) => setSeen((current) => new Set([...current, ...keys]));
  const open = (shown: RevealGroup[]) => {
    markSeen(shown.map((group) => group.key));
    const newest = shown[shown.length - 1];
    openViewer(shown.length === 1
      ? { title: formatRevealLabel(t, newest), cards: newest.cards }
      : { title: t('match.reveals.turnTitle'), cards: shown.flatMap((group) => group.cards) });
  };
  const dismiss = (shown: RevealGroup[]) => {
    markSeen(shown.map((group) => group.key));
    setDismissed((current) => new Set([...current, ...shown.map((group) => group.key)]));
  };

  return (
    <>
      {bySide.theirs.length > 0 && <Strip key={bySide.theirs[bySide.theirs.length - 1].key} side="theirs" groups={bySide.theirs} seen={seen} autoDismiss={autoDismiss} onOpen={open} onDismiss={dismiss} />}
      {bySide.mine.length > 0 && <Strip key={bySide.mine[bySide.mine.length - 1].key} side="mine" groups={bySide.mine} seen={seen} autoDismiss={autoDismiss} onOpen={open} onDismiss={dismiss} />}
    </>
  );
}

/** How long a reveal stays up by itself when it hides after a moment. */
const REVEAL_MS = 4000;

function Strip({ side, groups, seen, autoDismiss, onOpen, onDismiss }: {
  side: 'mine' | 'theirs';
  groups: RevealGroup[];
  seen: ReadonlySet<string>;
  autoDismiss: boolean;
  onOpen(groups: RevealGroup[]): void;
  onDismiss(groups: RevealGroup[]): void;
}) {
  const t = useT();
  const newest = groups[groups.length - 1];
  // pointed at or focused: it stays while the player reads it
  const [held, setHeld] = useState(false);
  // the board updates often; the countdown runs from when this reveal (the strip's key) came up
  const dismissNow = useRef(() => onDismiss(groups));
  useEffect(() => {
    dismissNow.current = () => onDismiss(groups);
  }, [groups, onDismiss]);
  useEffect(() => {
    if (!autoDismiss || held) return;
    const timer = setTimeout(() => dismissNow.current(), REVEAL_MS);
    return () => clearTimeout(timer);
  }, [autoDismiss, held]);
  const fresh = groups.some((group) => !seen.has(group.key));
  const label = formatRevealLabel(t, newest);
  const thumbs = newest.cards.slice(0, 3);
  return (
    <div
      className={[styles.strip, side === 'mine' ? styles.mine : styles.theirs, fresh ? styles.fresh : ''].join(' ')}
      onPointerEnter={() => setHeld(true)}
      onPointerLeave={() => setHeld(false)}
      onFocus={() => setHeld(true)}
      onBlur={() => setHeld(false)}
    >
      <button type="button" className={styles.open} onClick={() => onOpen([newest])} aria-label={`${t('match.reveals.openAria', { label, count: newest.cards.length })}${fresh ? t('match.reveals.newSuffix') : ''}`}>
        <span className={styles.thumbs} aria-hidden="true">
          {thumbs.map((card, index) => (
            <span key={card.id ?? index} className={styles.thumb} style={{ ['--i' as string]: index }}>
              <CardFace card={card} size="small" />
            </span>
          ))}
        </span>
        <span className={styles.label}>{label}</span>
        {fresh && <span className={styles.new} aria-hidden="true">{t('match.reveals.newBadge')}</span>}
      </button>
      {groups.length > 1 && (
        <button type="button" className={styles.more} onClick={() => onOpen(groups)} aria-label={t('match.reveals.allAria', { count: groups.length })}>
          +{groups.length - 1}
        </button>
      )}
      <button type="button" className={styles.dismiss} onClick={() => onDismiss(groups)} aria-label={t('match.reveals.dismiss')}>
        <X size={20} />
      </button>
    </div>
  );
}
