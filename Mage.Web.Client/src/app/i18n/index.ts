import { create } from 'zustand';
import { useSettings } from '../stores/settings';
import core from './en/core';
import { resolveLocale, type Catalog, type Locale, type MessageKey } from './locales';
import { createTranslator, type Messages, type Vars } from './translate';

export { LOCALES, type Locale, type LocalePreference, type MessageKey } from './locales';

export type Translate = (key: MessageKey, vars?: Vars) => string;
export type PhraseTranslator = (text: string) => string;

const IDENTITY_TX: PhraseTranslator = (text) => text;

/** English, inline: the entry's messages now, a lazy screen's when it loads (registerMessages). */
const english: Messages = { ...core };

/** Translations load on demand, one chunk per language. */
const LOADERS: Record<Exclude<Locale, 'en'>, () => Promise<{ default: Catalog; translatePtPhrase?: PhraseTranslator }>> = {
  pt: () => import('./pt'),
  es: () => import('./es'),
};

const warned = new Set<string>();
function missing(key: string) {
  if (!import.meta.env.DEV || warned.has(key)) return;
  warned.add(key);
  console.warn(`i18n: no message for "${key}" (is its English catalog registered?)`);
}

function translator(locale: Locale, messages: Messages): Translate {
  return createTranslator({ locale, messages, fallback: english, onMissing: missing });
}

interface I18nState {
  locale: Locale;
  /** replaced when the language changes, so components reading it with useT re-render */
  t: Translate;
  /** translates legacy unkeyed English phrases when Portuguese is active */
  tx: PhraseTranslator;
}

export const useI18n = create<I18nState>(() => ({ locale: 'en', t: translator('en', {}), tx: IDENTITY_TX }));

/** The translate function, for components: they re-render in the new language when it changes. */
export function useT(): Translate {
  return useI18n((state) => state.t);
}

/** Translate unkeyed UI phrases in components: re-renders when the language changes. */
export function useTx(): PhraseTranslator {
  return useI18n((state) => state.tx);
}

/** Translate outside React (toasts, store messages): the language at the moment of the call. */
export const t: Translate = (key, vars) => useI18n.getState().t(key, vars);

/** Translate an unkeyed English phrase outside React. */
export const tx: PhraseTranslator = (text) => useI18n.getState().tx(text);

/** A screen that loads later adds its English messages here, at module load, before it first renders. */
export function registerMessages(messages: Messages): void {
  Object.assign(english, messages);
}

export function formatNumber(value: number, options?: Intl.NumberFormatOptions): string {
  return new Intl.NumberFormat(useI18n.getState().locale, options).format(value);
}

export function formatDate(value: Date | number, options?: Intl.DateTimeFormatOptions): string {
  return new Intl.DateTimeFormat(useI18n.getState().locale, options).format(value);
}

function browserLanguages(): readonly string[] {
  if (typeof navigator === 'undefined') return [];
  return navigator.languages?.length ? navigator.languages : [navigator.language];
}

/** The language to show now: the setting, or the browser's when the setting says so. */
export function preferredLocale(): Locale {
  return resolveLocale(useSettings.getState().settings.locale, browserLanguages());
}

let latest = 0;
async function apply(locale: Locale): Promise<void> {
  const request = ++latest;
  let messages: Messages = {};
  let phraseTx: PhraseTranslator = IDENTITY_TX;
  if (locale !== 'en') {
    try {
      const mod = await LOADERS[locale]();
      messages = mod.default;
      if (locale === 'pt' && mod.translatePtPhrase) phraseTx = mod.translatePtPhrase;
    } catch {
      // offline or a stale deploy: stay in English rather than show keys
      locale = 'en';
    }
  }
  // a later change won the race
  if (request !== latest) return;
  if (typeof document !== 'undefined') document.documentElement.lang = locale;
  useI18n.setState({ locale, t: translator(locale, messages), tx: phraseTx });
}

/** Resolves once the first language is in place; the app renders after it, so nobody sees English flash by. */
export const i18nReady: Promise<void> = apply(preferredLocale());

useSettings.subscribe((state, previous) => {
  if (state.settings.locale !== previous.settings.locale) void apply(preferredLocale());
});
if (typeof window !== 'undefined') {
  window.addEventListener('languagechange', () => {
    if (useI18n.getState().locale !== preferredLocale()) void apply(preferredLocale());
  });
}
