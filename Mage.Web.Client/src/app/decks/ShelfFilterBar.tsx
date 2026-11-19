import { Search, X } from 'lucide-react';
import { useMemo } from 'react';
import { tagCounts } from '../../core/decks/tags';
import { useT } from '../i18n';
import { useDecks, type RosterDeck } from '../stores/decks';
import styles from './DeckShelf.module.css';

/** The shelf's filter (#200): optional search input, tag chips, and drafts switch. */
export function ShelfFilterBar({
  decks,
  query = '',
  onQueryChange,
  compact = false,
}: {
  decks: RosterDeck[];
  query?: string;
  onQueryChange?(value: string): void;
  compact?: boolean;
}) {
  const t = useT();
  const filter = useDecks((state) => state.shelfFilter);
  const setFilter = useDecks((state) => state.setShelfFilter);
  const tags = useMemo(() => tagCounts(decks), [decks]);
  const drafts = decks.some((deck) => deck.draft);
  // a tag nobody uses any more doesn't keep hiding every deck
  const current = filter.tag && tags.some((entry) => entry.tag.toLowerCase() === filter.tag!.toLowerCase()) ? filter.tag : null;
  const hasChipControls = tags.length > 0 || drafts || filter.hideDrafts;
  if (!hasChipControls && (!onQueryChange || decks.length === 0)) return null;
  return (
    <div className={[styles.filterBar, compact ? styles.filterCompact : ''].join(' ')} role="group" aria-label={t('decks.filter.label')}>
      {onQueryChange && (
        <label className={[styles.search, styles.shelfSearch].join(' ')}>
          <Search size={15} aria-hidden="true" />
          <input
            value={query}
            onChange={(event) => onQueryChange(event.target.value)}
            placeholder={t('decks.filter.search')}
            aria-label={t('decks.filter.search')}
          />
          {query && (
            <button type="button" className={styles.searchClear} aria-label={t('ui.dismiss')} onClick={() => onQueryChange('')}>
              <X size={13} aria-hidden="true" />
            </button>
          )}
        </label>
      )}
      {hasChipControls && (
        <div className={styles.chips}>
          <button type="button" className={current === null ? styles.chipOn : styles.chip} aria-pressed={current === null} onClick={() => setFilter({ tag: null })}>
            {t('decks.filter.all')}
          </button>
          {tags.map(({ tag, count }) => {
            const on = current?.toLowerCase() === tag.toLowerCase();
            return (
              <button key={tag} type="button" className={on ? styles.chipOn : styles.chip} aria-pressed={on} onClick={() => setFilter({ tag: on ? null : tag })}>
                {tag}<small>{count}</small>
              </button>
            );
          })}
          {(drafts || filter.hideDrafts) && (
            <label className={styles.check}>
              <input type="checkbox" checked={filter.hideDrafts} onChange={(event) => setFilter({ hideDrafts: event.target.checked })} />
              {t('decks.filter.hideDrafts')}
            </label>
          )}
        </div>
      )}
    </div>
  );
}
