import { CheckCircle2, Loader2, Send } from 'lucide-react';
import { useState } from 'react';
import type { PlaygroupMatchRecord } from '../../core/playgroup/playgroup';
import { useT } from '../i18n';
import { usePlaygroup } from '../stores/playgroup';
import { Button } from '../ui/Button';
import { playgroupErrorText, playgroupSkippedText } from './playgroupUi';
import styles from './PlaygroupSyncBox.module.css';

export interface PlaygroupSyncBoxProps {
  record: PlaygroupMatchRecord;
  tableId?: string | null;
  compact?: boolean;
}

/** Shows Playgroup.gg synchronization status and lets a participant sync or resync manually. */
export function PlaygroupSyncBox({ record, tableId, compact = false }: PlaygroupSyncBoxProps) {
  const t = useT();
  const user = usePlaygroup((state) => state.user);
  const defaultPlaygroupId = usePlaygroup((state) => state.defaultPlaygroupId);
  const tableConfig = usePlaygroup((state) => (tableId ? state.tableConfigs[tableId] : undefined));
  const synced = usePlaygroup((state) => state.syncedGames[record.gameId]);
  const syncing = usePlaygroup((state) => Boolean(state.syncingIds[record.gameId]));
  const syncGame = usePlaygroup((state) => state.syncGame);

  const [gameId, setGameId] = useState('');
  const [error, setError] = useState<string | null>(null);

  if (compact && !user && !synced) return null;

  const canSync = Boolean(user && !syncing && (gameId || tableConfig?.playgroupId || defaultPlaygroupId));

  async function onSync() {
    if (synced && !window.confirm(t('match.playgroup.resyncConfirm'))) return;
    setError(null);
    try {
      await syncGame(record, {
        tableId,
        gameId: gameId ? Number(gameId) : null,
        force: Boolean(synced),
      });
    } catch (failure) {
      setError(playgroupErrorText(t, failure));
    }
  }

  return (
    <div className={[styles.box, compact ? styles.compact : ''].join(' ').trim()}>
      {syncing && (
        <div className={styles.status} role="status">
          <Loader2 size={16} className={styles.spin} aria-hidden="true" />
          <span>{t('match.playgroup.syncing')}</span>
        </div>
      )}
      {!syncing && synced && (
        <div className={styles.status} role="status">
          <CheckCircle2 size={16} className={styles.syncedIcon} aria-hidden="true" />
          <span>{t('match.playgroup.synced', { id: synced.playgroupGameId, count: synced.imported })}</span>
        </div>
      )}
      {synced && synced.skipped.length > 0 && (
        <p className={styles.skipped}>
          {t('match.playgroup.skipped', { kinds: playgroupSkippedText(t, synced.skipped) })}
        </p>
      )}
      {error && <p className={styles.error} role="alert">{error}</p>}

      {user && (
        <div className={styles.actions}>
          <input
            type="text"
            inputMode="numeric"
            value={gameId}
            onChange={(event) => setGameId(event.target.value.replace(/\D/g, '').slice(0, 12))}
            placeholder={t('match.playgroup.gameIdPlaceholder')}
            aria-label={t('match.playgroup.gameIdAria')}
            className={styles.input}
          />
          <Button
            size={compact ? 'sm' : 'md'}
            variant="print"
            busy={syncing}
            disabled={!canSync}
            icon={<Send size={15} />}
            onClick={() => void onSync()}
          >
            {t(synced ? 'match.playgroup.resync' : 'match.playgroup.syncNow')}
          </Button>
        </div>
      )}

      {!compact && <p className={styles.hint}>{t('match.playgroup.hintAuto')}</p>}
    </div>
  );
}
