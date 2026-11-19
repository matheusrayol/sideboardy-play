import { useEffect, useState } from 'react';
import type { Advice } from '../../protocol/generated/views';
import { api } from '../connection';
import { useT, type MessageKey } from '../i18n';
import './matchMessages';
import { Button } from '../ui/Button';
import { Dialog } from '../ui/Dialog';
import styles from './AdviceDialog.module.css';

const HEADING_KEYS: Record<string, MessageKey> = {
  PLAY: 'match.advice.heading.PLAY',
  PASS: 'match.advice.heading.PASS',
  ATTACK: 'match.advice.heading.ATTACK',
  NO_ATTACK: 'match.advice.heading.NO_ATTACK',
  BLOCK: 'match.advice.heading.BLOCK',
  NO_BLOCK: 'match.advice.heading.NO_BLOCK',
  NONE: 'match.advice.heading.NONE',
};

/**
 * "What would the AI do?": the computer takes your seat in a copy of the game and says what it would do now, without
 * doing it. It thinks while the game waits for you, so ask when it's your decision.
 */
export function AdviceDialog({ gameId, open, onOpenChange, counted, onUsed }: {
  gameId: string;
  open: boolean;
  onOpenChange(open: boolean): void;
  /** a Career game: hints are counted and halve the pay, so each one is asked for on purpose */
  counted?: { hintsLeft: number } | null;
  /** a hint was given */
  onUsed?(): void;
}) {
  const t = useT();
  const [advice, setAdvice] = useState<Advice | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [thinking, setThinking] = useState(false);

  async function ask() {
    setThinking(true);
    setError(null);
    setAdvice(null);
    try {
      setAdvice(await api.gameAdvise(gameId));
      onUsed?.();
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : String(failure));
    } finally {
      setThinking(false);
    }
  }

  useEffect(() => {
    if (!open) return;
    setAdvice(null);
    setError(null);
    // a counted hint is only spent when the player asks for it
    if (!counted) void ask();
    // a new question each time it opens
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  const steps = advice?.steps ?? [];
  const left = advice?.hintsLeft ?? counted?.hintsLeft;
  const canAsk = !counted || (left ?? 0) > 0;
  const askKey: MessageKey = counted ? (advice ? 'match.advice.another' : 'match.advice.useHint') : 'match.advice.askAgain';
  return (
    <Dialog
      open={open}
      onOpenChange={onOpenChange}
      title={t(counted ? 'match.advice.titleCounted' : 'match.advice.title')}
      description={t(counted ? 'match.advice.descCounted' : 'match.advice.desc')}
      width="sm"
      footer={(
        <>
          <Button variant="quiet" onClick={() => onOpenChange(false)}>{t('match.advice.close')}</Button>
          {canAsk && (
            <Button variant="print" busy={thinking} onClick={() => void ask()}>
              {t(askKey)}
            </Button>
          )}
        </>
      )}
    >
      <div className={styles.body} aria-live="polite">
        {counted && left !== undefined && (
          <p className={styles.quiet}>
            {left <= 0 ? t('match.advice.noneLeft') : t('match.advice.left', { count: left })}
          </p>
        )}
        {thinking && <p className={styles.quiet}>{t('match.advice.thinking')}</p>}
        {error && <p className={styles.error} role="alert">{error}</p>}
        {advice && (
          <>
            <p className={styles.heading}>
              {t(HEADING_KEYS[advice.kind ?? 'NONE'] ?? 'match.advice.defaultHeading')}
              {advice.when ? <small> · {advice.when}</small> : null}
            </p>
            {steps.length > 1 ? (
              <ol className={styles.steps}>
                {steps.map((step, index) => <li key={`${step.objectId ?? ''}-${index}`}>{step.text}</li>)}
              </ol>
            ) : (
              <p className={styles.text}>{advice.text}</p>
            )}
            {advice.reason && <p className={styles.reason}>{t('match.advice.why', { reason: advice.reason })}</p>}
            {advice.millis !== undefined && <p className={styles.quiet}>{t('match.advice.thoughtFor', { seconds: (advice.millis / 1000).toFixed(1) })}</p>}
          </>
        )}
      </div>
    </Dialog>
  );
}
