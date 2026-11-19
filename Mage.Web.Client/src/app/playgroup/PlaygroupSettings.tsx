import { CheckCircle2, ExternalLink, KeyRound, RefreshCw, Unlink } from 'lucide-react';
import { useState, type FormEvent } from 'react';
import { useT } from '../i18n';
import { usePlaygroup } from '../stores/playgroup';
import { Button } from '../ui/Button';
import { Field } from '../ui/Field';
import { Select } from '../ui/Select';
import { playgroupErrorText } from './playgroupUi';
import styles from './PlaygroupSettings.module.css';

/** Settings panel to connect Playgroup.gg via API key and choose default tracking preferences. */
export function PlaygroupSettings() {
  const t = useT();
  const user = usePlaygroup((state) => state.user);
  const playgroups = usePlaygroup((state) => state.playgroups);
  const defaultPlaygroupId = usePlaygroup((state) => state.defaultPlaygroupId);
  const defaultLeagueId = usePlaygroup((state) => state.defaultLeagueId);
  const autoSync = usePlaygroup((state) => state.autoSync);
  const connect = usePlaygroup((state) => state.connect);
  const refresh = usePlaygroup((state) => state.refresh);
  const disconnect = usePlaygroup((state) => state.disconnect);
  const setDefaults = usePlaygroup((state) => state.setDefaults);

  const [apiKey, setApiKey] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const selectedGroup = playgroups.find((group) => group.id === defaultPlaygroupId) ?? null;
  const activeLeagues = (selectedGroup?.leagues ?? []).filter((league) => league.active !== false);

  async function onConnect(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await connect(apiKey);
      setApiKey('');
    } catch (failure) {
      setError(playgroupErrorText(t, failure));
    } finally {
      setBusy(false);
    }
  }

  async function onRefresh() {
    setBusy(true);
    setError(null);
    try {
      await refresh();
    } catch (failure) {
      setError(playgroupErrorText(t, failure));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className={styles.section}>
      <p className={styles.intro}>{t('settings.playgroup.intro')}</p>
      <a className={styles.link} href="https://playgroup.gg" target="_blank" rel="noreferrer">
        {t('settings.playgroup.getKey')}
        <ExternalLink size={14} aria-hidden="true" />
      </a>

      {!user ? (
        <form className={styles.form} onSubmit={(event) => void onConnect(event)}>
          <Field
            type="password"
            autoComplete="off"
            spellCheck={false}
            label={t('settings.playgroup.keyLabel')}
            placeholder={t('settings.playgroup.keyPlaceholder')}
            hint={t('settings.playgroup.keyHint')}
            value={apiKey}
            onChange={(event) => setApiKey(event.target.value)}
            error={error}
          />
          <div className={styles.actions}>
            <Button type="submit" variant="decision" busy={busy} disabled={apiKey.trim().length < 16} icon={<KeyRound size={16} />}>
              {t('settings.playgroup.connect')}
            </Button>
          </div>
        </form>
      ) : (
        <>
          <div className={styles.card}>
            <div className={styles.identity}>
              <CheckCircle2 size={20} className={styles.identityIcon} aria-hidden="true" />
              <div className={styles.identityText}>
                <strong>{t('settings.playgroup.connectedAs', { name: user.username })}</strong>
                <span>{t('settings.playgroup.groupCount', { count: playgroups.length })}</span>
              </div>
            </div>
            <div className={styles.actions}>
              <Button size="sm" variant="print" busy={busy} icon={<RefreshCw size={15} />} onClick={() => void onRefresh()}>
                {t('settings.playgroup.refresh')}
              </Button>
              <Button size="sm" variant="quiet" disabled={busy} icon={<Unlink size={15} />} onClick={disconnect}>
                {t('settings.playgroup.disconnect')}
              </Button>
            </div>
          </div>

          {error && <p className={styles.error} role="alert">{error}</p>}

          <div className={styles.row}>
            <div>
              <div className={styles.rowLabel}>{t('settings.playgroup.defaultGroup')}</div>
              <div className={styles.rowDetail}>{t('settings.playgroup.defaultGroup.detail')}</div>
            </div>
            <Select
              className={styles.select}
              value={defaultPlaygroupId ?? ''}
              aria-label={t('settings.playgroup.defaultGroup')}
              onChange={(event) => setDefaults({ defaultPlaygroupId: event.target.value ? Number(event.target.value) : null })}
            >
              <option value="">{t('settings.playgroup.defaultGroup.none')}</option>
              {playgroups.map((group) => (
                <option key={group.id} value={group.id}>{group.name}</option>
              ))}
            </Select>
          </div>

          {selectedGroup && activeLeagues.length > 0 && (
            <div className={styles.row}>
              <div>
                <div className={styles.rowLabel}>{t('settings.playgroup.defaultLeague')}</div>
                <div className={styles.rowDetail}>{t('settings.playgroup.defaultLeague.detail')}</div>
              </div>
              <Select
                className={styles.select}
                value={defaultLeagueId ?? ''}
                aria-label={t('settings.playgroup.defaultLeague')}
                onChange={(event) => setDefaults({ defaultLeagueId: event.target.value ? Number(event.target.value) : null })}
              >
                <option value="">{t('settings.playgroup.defaultLeague.none')}</option>
                {activeLeagues.map((league) => (
                  <option key={league.id} value={league.id}>{league.name}</option>
                ))}
              </Select>
            </div>
          )}

          <div className={styles.row}>
            <div>
              <div className={styles.rowLabel}>{t('settings.playgroup.autoSync')}</div>
              <div className={styles.rowDetail}>{t('settings.playgroup.autoSync.detail')}</div>
            </div>
            <button
              type="button"
              role="switch"
              aria-checked={autoSync}
              aria-label={t('settings.playgroup.autoSync')}
              className={styles.switch}
              onClick={() => setDefaults({ autoSync: !autoSync })}
            >
              <span className={styles.knob} />
            </button>
          </div>
        </>
      )}
    </div>
  );
}
