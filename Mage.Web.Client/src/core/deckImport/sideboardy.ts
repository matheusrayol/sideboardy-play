import type { DeckCardInfo } from '../decks/types';
import type { DeckReading } from './draft';
import { normalizeCardName } from './parseList';

const SIDEBOARDY_ORIGIN = 'https://sideboardy.app';

export interface SideboardySsoStatus {
  authenticated: boolean;
  playUsername: string | null;
  migratedCookie?: boolean;
  reachable?: boolean;
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

export function isSideboardyHost(): boolean {
  if (typeof window === 'undefined') return false;
  const host = window.location.hostname.toLowerCase();
  return host === 'sideboardy.app' || host.endsWith('.sideboardy.app');
}

export function toPlayUsername(username: string, userId?: number | string | null): string {
  let clean = String(username || '').trim().toLowerCase().replace(/[^a-z0-9_]/g, '_').replace(/_+/g, '_').replace(/^_|_$/g, '');
  if (clean.length < 3) clean = `sb_${clean || 'user'}_${userId || 0}`.replace(/[^a-z0-9_]/g, '');
  if (clean.length > 14) clean = clean.slice(0, 14);
  if (clean.toLowerCase() === 'admin') clean = `sb_${userId || 'user'}`.slice(0, 14);
  return clean;
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
  let localReachable = false;
  try {
    const localRes = await fetch('/sideboardy-api/auth/sso', { credentials: 'include', cache: 'no-store' });
    if ((localRes.headers.get('content-type') ?? '').includes('application/json')) {
      localReachable = true;
      if (localRes.ok) {
        const payload = (await localRes.json()) as Partial<SideboardySsoStatus>;
        if (payload.authenticated && payload.playUsername) {
          return { authenticated: true, playUsername: payload.playUsername, migratedCookie: false, reachable: true };
        }
      }
    }
  } catch {
    // ignore and check apex if on *.sideboardy.app
  }

  if (!isSideboardyHost()) {
    return { authenticated: false, playUsername: null, reachable: localReachable };
  }

  try {
    const apexRes = await fetch(`${SIDEBOARDY_ORIGIN}/api/auth/sso`, { credentials: 'include', cache: 'no-store' });
    if (apexRes.ok && (apexRes.headers.get('content-type') ?? '').includes('application/json')) {
      const payload = (await apexRes.json()) as Partial<SideboardySsoStatus>;
      if (payload.authenticated && payload.playUsername) {
        return { authenticated: true, playUsername: payload.playUsername, migratedCookie: true, reachable: true };
      }
    }
  } catch {
    // unreachable or network blocked
  }

  return { authenticated: false, playUsername: null, reachable: localReachable || isSideboardyHost() };
}

export async function loginSideboardy(login: string, password: string): Promise<string> {
  const result = await fetchApiJson('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ login: login.trim(), password }),
  });
  if (!result) {
    throw new Error('Não foi possível conectar ao Sideboardy para fazer login.');
  }
  const data = result.data as { error?: string; user?: { id?: number; username?: string } };
  if (!result.response.ok) {
    throw new Error(data?.error || 'Usuário ou senha incorretos.');
  }
  const sso = await syncSideboardySso();
  return sso.authenticated && sso.playUsername ? sso.playUsername : toPlayUsername(data?.user?.username || login, data?.user?.id);
}

export async function registerSideboardy(username: string, email: string, password: string): Promise<string> {
  const result = await fetchApiJson('/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: username.trim(),
      email: email.trim(),
      password,
      display_name: username.trim(),
      policies_read: true,
    }),
  });
  if (!result) {
    throw new Error('Não foi possível conectar ao Sideboardy para criar a conta.');
  }
  const data = result.data as { error?: string; user?: { id?: number; username?: string } };
  if (!result.response.ok) {
    throw new Error(data?.error || 'Erro ao criar conta.');
  }
  const sso = await syncSideboardySso();
  return sso.authenticated && sso.playUsername ? sso.playUsername : toPlayUsername(data?.user?.username || username, data?.user?.id);
}

export async function forgotPasswordSideboardy(email: string): Promise<string> {
  const result = await fetchApiJson('/auth/forgot-password', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: email.trim() }),
  });
  if (!result) {
    throw new Error('Não foi possível conectar ao Sideboardy.');
  }
  const data = result.data as { error?: string; message?: string };
  if (!result.response.ok) {
    throw new Error(data?.error || 'Não foi possível enviar o e-mail de recuperação.');
  }
  return data?.message || 'Se houver uma conta com esse e-mail, enviamos um link para redefinir a senha.';
}

export async function logoutSideboardy(): Promise<void> {
  if (!isSideboardyHost()) return;
  await fetchApiJson('/auth/logout', { method: 'POST' }).catch(() => null);
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
