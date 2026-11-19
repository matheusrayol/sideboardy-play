import type { DeckCardInfo } from '../decks/types';
import type { DeckReading } from './draft';
import { normalizeCardName } from './parseList';

const SIDEBOARDY_ORIGIN = 'https://sideboardy.app';

export interface SideboardySsoStatus {
  authenticated: boolean;
  playUsername: string | null;
  migratedCookie?: boolean;
}

export interface SideboardyDeckSummary {
  id: number;
  name: string;
  format: string | null;
  visibility: string;
  total_quantity: number;
  card_count: number;
  updated_at: string;
}

interface PlayExportCard {
  cardName?: string;
  amount?: number;
  setCode?: string | null;
  cardNumber?: string | null;
  section?: string;
}

interface PlayExportResponse {
  id: number;
  name?: string;
  author?: string | null;
  format?: string | null;
  updatedAt?: string | null;
  coverImage?: string | null;
  cards?: PlayExportCard[];
  error?: string;
}

const FORMAT_MAP: Record<string, string> = {
  commander: 'Variant Magic - Commander',
  edh: 'Variant Magic - Commander',
  cedh: 'Variant Magic - Commander',
  standard: 'Constructed - Standard',
  pioneer: 'Constructed - Pioneer',
  modern: 'Constructed - Modern',
  legacy: 'Constructed - Legacy',
  vintage: 'Constructed - Vintage',
  pauper: 'Constructed - Pauper',
  historic: 'Constructed - Historic',
  premodern: 'Constructed - Premodern',
  brawl: 'Variant Magic - Brawl',
  oathbreaker: 'Variant Magic - Oathbreaker',
  duel: 'Variant Magic - Duel Commander',
  'duel commander': 'Variant Magic - Duel Commander',
};

function mapFormat(value: string | null | undefined): string | undefined {
  if (!value) return undefined;
  return FORMAT_MAP[value.trim().toLowerCase().replace(/[_-]+/g, ' ')];
}

function isSideboardyHost(): boolean {
  if (typeof window === 'undefined') return false;
  const host = window.location.hostname.toLowerCase();
  return host === 'sideboardy.app' || host.endsWith('.sideboardy.app');
}

async function fetchApiJson(path: string, init?: RequestInit): Promise<{ response: Response; data: unknown } | null> {
  try {
    const response = await fetch(`/sideboardy-api${path}`, {
      credentials: 'include',
      cache: 'no-store',
      ...init,
    });
    const contentType = response.headers.get('content-type') ?? '';
    if (contentType.includes('application/json')) {
      return { response, data: await response.json() };
    }
  } catch {
    // fallback below when direct host is reachable
  }

  if (!isSideboardyHost()) return null;

  try {
    const response = await fetch(`${SIDEBOARDY_ORIGIN}/api${path}`, {
      credentials: 'include',
      cache: 'no-store',
      ...init,
    });
    const contentType = response.headers.get('content-type') ?? '';
    if (contentType.includes('application/json')) {
      return { response, data: await response.json() };
    }
  } catch {
    return null;
  }
  return null;
}

export async function syncSideboardySso(): Promise<SideboardySsoStatus> {
  try {
    const localRes = await fetch('/sideboardy-api/auth/sso', { credentials: 'include', cache: 'no-store' });
    if (localRes.ok && (localRes.headers.get('content-type') ?? '').includes('application/json')) {
      const payload = (await localRes.json()) as Partial<SideboardySsoStatus>;
      if (payload.authenticated && payload.playUsername) {
        return { authenticated: true, playUsername: payload.playUsername, migratedCookie: false };
      }
    }
  } catch {
    // ignore and check apex if on *.sideboardy.app
  }

  if (!isSideboardyHost()) {
    return { authenticated: false, playUsername: null };
  }

  try {
    const apexRes = await fetch(`${SIDEBOARDY_ORIGIN}/api/auth/sso`, { credentials: 'include', cache: 'no-store' });
    if (apexRes.ok && (apexRes.headers.get('content-type') ?? '').includes('application/json')) {
      const payload = (await apexRes.json()) as Partial<SideboardySsoStatus>;
      if (payload.authenticated && payload.playUsername) {
        return { authenticated: true, playUsername: payload.playUsername, migratedCookie: true };
      }
    }
  } catch {
    // unreachable or network blocked
  }

  return { authenticated: false, playUsername: null };
}

export async function fetchSideboardyMyDecks(): Promise<{ authenticated: boolean; decks: SideboardyDeckSummary[] }> {
  const result = await fetchApiJson('/decks');
  if (!result) return { authenticated: false, decks: [] };
  if (result.response.status === 401) return { authenticated: false, decks: [] };
  if (!result.response.ok) throw new Error('Não foi possível carregar seus decks do Sideboardy.');
  const payload = result.data as { decks?: SideboardyDeckSummary[] };
  return { authenticated: true, decks: Array.isArray(payload.decks) ? payload.decks : [] };
}

export async function fetchSideboardyDeckReading(deckId: string, fallbackUrl?: string): Promise<DeckReading> {
  const result = await fetchApiJson(`/decks/${encodeURIComponent(deckId)}/play-export`);
  if (!result) {
    throw new Error('Não foi possível conectar ao Sideboardy para importar este deck.');
  }
  const data = result.data as PlayExportResponse;
  if (!result.response.ok) {
    if (result.response.status === 403) {
      throw new Error('Este deck é privado no Sideboardy. Faça login na mesma conta ou torne-o público.');
    }
    if (result.response.status === 404) {
      throw new Error('Deck não encontrado no Sideboardy.');
    }
    throw new Error(data?.error || 'Não foi possível importar o deck do Sideboardy.');
  }

  const main: DeckCardInfo[] = [];
  const side: DeckCardInfo[] = [];
  const commanders: DeckCardInfo[] = [];
  const companion: DeckCardInfo[] = [];
  const maybe: DeckCardInfo[] = [];

  for (const raw of data.cards ?? []) {
    const cardName = normalizeCardName(raw.cardName ?? '');
    const amount = Math.max(0, Number(raw.amount ?? 0));
    if (!cardName || !amount) continue;
    const entry: DeckCardInfo = {
      cardName,
      amount,
      setCode: raw.setCode ? String(raw.setCode).trim().toUpperCase() : null,
      cardNumber: raw.cardNumber ? String(raw.cardNumber).trim() : null,
    };
    switch (raw.section) {
      case 'commander':
        commanders.push(entry);
        break;
      case 'companion':
        companion.push(entry);
        break;
      case 'side':
        side.push(entry);
        break;
      case 'maybe':
        maybe.push(entry);
        break;
      default:
        main.push(entry);
    }
  }

  return {
    list: { main, side, commanders, companion, maybe: [], skipped: [] },
    maybeCount: maybe.length,
    name: data.name || `Deck #${deckId}`,
    author: data.author ?? undefined,
    format: mapFormat(data.format),
    source: {
      site: 'sideboardy',
      url: fallbackUrl || `${SIDEBOARDY_ORIGIN}/decks/${deckId}`,
      remoteId: String(data.id ?? deckId),
      remoteUpdatedAt: data.updatedAt ? Date.parse(data.updatedAt) || undefined : undefined,
    },
  };
}
