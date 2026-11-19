import { PlaygroupApiError } from '../../core/playgroup/playgroup';
import { registerMessages, type MessageKey, type Translate } from '../i18n';
import settingsMessages from '../i18n/en/settings';
import '../match/matchMessages';

registerMessages(settingsMessages);

const ERROR_KEYS: Record<PlaygroupApiError['code'], MessageKey> = {
  invalidKey: 'settings.playgroup.error.invalidKey',
  unauthorized: 'settings.playgroup.error.unauthorized',
  forbidden: 'settings.playgroup.error.forbidden',
  notFound: 'settings.playgroup.error.notFound',
  rateLimit: 'settings.playgroup.error.rateLimit',
  unreachable: 'settings.playgroup.error.unreachable',
  rejected: 'settings.playgroup.error.rejected',
};

const KIND_KEYS: Record<string, MessageKey> = {
  starter: 'match.playgroup.kind.starter',
  turn: 'match.playgroup.kind.turn',
  normal_damage: 'match.playgroup.kind.normal_damage',
  commander_damage: 'match.playgroup.kind.commander_damage',
  poison_tokens: 'match.playgroup.kind.poison_tokens',
  healing: 'match.playgroup.kind.healing',
  life_loss: 'match.playgroup.kind.life_loss',
  mill: 'match.playgroup.kind.mill',
  elimination: 'match.playgroup.kind.elimination',
  winner: 'match.playgroup.kind.winner',
};

export function playgroupErrorText(t: Translate, error: unknown): string {
  if (error instanceof PlaygroupApiError) {
    if (error.code === 'rejected') {
      const detail = error.details?.error ? `: ${error.details.error}` : '';
      return t('settings.playgroup.error.rejected', { detail });
    }
    return t(ERROR_KEYS[error.code]);
  }
  return error instanceof Error ? error.message : String(error);
}

export function playgroupSkippedText(t: Translate, skipped: readonly string[]): string {
  return skipped.map((kind) => (KIND_KEYS[kind] ? t(KIND_KEYS[kind]) : kind)).join(', ');
}
