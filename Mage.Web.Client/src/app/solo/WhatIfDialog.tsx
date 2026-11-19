import { GitBranch } from 'lucide-react';
import { useState } from 'react';
import { useT } from '../i18n';
import '../match/matchMessages';
import { notify } from '../stores/toasts';
import { Button } from '../ui/Button';
import { Dialog } from '../ui/Dialog';
import { playFromTurn, useSoloTurns } from './soloData';
import styles from './WhatIfDialog.module.css';

/**
 * What-if lines (S013): pick a turn of this game to play again from its start, at a new table. The game you're in is
 * saved as it stands, so you can come back to it from Home.
 */
export function WhatIfDialog({ gameId, open, onOpenChange }: { gameId: string; open: boolean; onOpenChange(open: boolean): void }) {
  const t = useT();
  const turns = useSoloTurns(gameId, open);
  const [busy, setBusy] = useState<number | null>(null);
  async function play(turn: number) {
    setBusy(turn);
    try {
      await playFromTurn(gameId, turn);
      notify(t('match.whatIf.toastTitle'), t('match.whatIf.toastBody', { turn }));
      onOpenChange(false);
    } catch (failure) {
      notify(t('match.whatIf.toastTitle'), failure instanceof Error ? failure.message : String(failure));
    } finally {
      setBusy(null);
    }
  }
  const list = [...(turns.data ?? [])].reverse();
  return (
    <Dialog
      open={open}
      onOpenChange={onOpenChange}
      title={t('match.whatIf.title')}
      description={t('match.whatIf.desc')}
      width="sm"
      footer={<Button variant="quiet" onClick={() => onOpenChange(false)}>{t('match.advice.close')}</Button>}
    >
      {turns.isPending && <p className={styles.quiet} role="status">{t('match.whatIf.loading')}</p>}
      {turns.isError && <p className={styles.quiet} role="alert">{t('match.whatIf.error')}</p>}
      {turns.data && list.length === 0 && <p className={styles.quiet}>{t('match.whatIf.empty')}</p>}
      {list.length > 0 && (
        <ul className={styles.turns}>
          {list.map((entry) => (
            <li key={entry.turn}>
              <span>{t('match.log.turn', { turn: entry.turn ?? 0 })}{entry.active ? <small> · {entry.active}</small> : null}</span>
              <Button size="sm" variant="print" icon={<GitBranch size={15} />} busy={busy === entry.turn} disabled={busy !== null} onClick={() => void play(entry.turn ?? 0)}>
                {t('match.whatIf.playFromHere')}
              </Button>
            </li>
          ))}
        </ul>
      )}
    </Dialog>
  );
}
