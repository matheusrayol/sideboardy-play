import type { CardView, GameEndView, GameEventView, GameView, ReplayInfo } from '../../protocol/generated/views';
import { commandersByPlayer } from '../game/commander';
import { gameResult } from '../game/matchResult';
import { replayWinner } from '../replay/timeline';

export const PLAYGROUP_BASE = '/playgroup-api';

export type PlaygroupErrorCode =
  | 'invalidKey'
  | 'unreachable'
  | 'unauthorized'
  | 'forbidden'
  | 'notFound'
  | 'rateLimit'
  | 'rejected';

export interface PlaygroupErrorDetails {
  error?: string;
  failed_event_index?: number;
  validation_errors?: unknown;
}

export class PlaygroupApiError extends Error {
  readonly code: PlaygroupErrorCode;
  readonly status: number;
  readonly details?: PlaygroupErrorDetails;

  constructor(code: PlaygroupErrorCode, status: number, message: string, details?: PlaygroupErrorDetails) {
    super(message);
    this.name = 'PlaygroupApiError';
    this.code = code;
    this.status = status;
    this.details = details;
  }
}

export interface PlaygroupUser {
  id: number;
  username: string;
  games_played?: number;
  games_won?: number;
  global_winrate?: number;
}

export interface PlaygroupLeague {
  id: number;
  name: string;
  active?: boolean;
}

export interface PlaygroupSummary {
  id: number;
  name: string;
  member_count?: number;
  game_count?: number;
  leagues?: PlaygroupLeague[];
}

export interface PlaygroupMember {
  user_id: number;
  username?: string;
  admin?: boolean;
}

export interface PlaygroupParticipation {
  id: number;
  user_id?: number;
  deck_id?: number;
  winner?: boolean;
}

export interface PlaygroupGame {
  id: number;
  started_at?: string;
  ended_at?: string;
  playgroup_id?: number;
  participations?: PlaygroupParticipation[];
  events?: unknown[];
}

export interface PlaygroupBatchEvent {
  id?: number;
  name: string;
  source_player_id: string;
  target_player_id?: string;
  time?: number;
  turn?: number;
  amount?: number;
  commander_id?: number;
  metadata?: Record<string, unknown>;
}

export type RecordedMatchEventKind =
  | 'turn'
  | 'normal_damage'
  | 'commander_damage'
  | 'poison_tokens'
  | 'healing'
  | 'life_loss'
  | 'mill'
  | 'elimination';

export interface RecordedMatchEvent {
  kind: RecordedMatchEventKind;
  t: number;
  turn: number;
  source: number | null;
  target?: number | null;
  amount?: number;
  commander?: string;
  reason?: string;
}

export interface RecordedMatchPlayer {
  seat: number;
  playerId: string;
  name: string;
  human: boolean;
  commanders: string[];
}

export interface PlaygroupMatchRecord {
  gameId: string;
  startedAt: number;
  endedAt?: number;
  turns: number;
  starter: number | null;
  players: RecordedMatchPlayer[];
  events: RecordedMatchEvent[];
  result: { winner: number | null; draw?: boolean } | null;
}

export interface PlaygroupClient {
  me(): Promise<PlaygroupUser>;
  playgroups(userId: number): Promise<PlaygroupSummary[]>;
  members(playgroupId: number): Promise<PlaygroupMember[]>;
  games(playgroupId: number): Promise<PlaygroupGame[]>;
  createLive(body: {
    player_amount: number;
    life_amount: number;
    playgroup_id?: number;
    league_id?: number;
    bracket?: number;
    client_identifier?: string;
  }): Promise<{ url?: string }>;
  commanderByName(name: string): Promise<{ id?: number; name?: string }>;
  sendEvents(gameId: number, events: PlaygroupBatchEvent[]): Promise<{ status?: string; imported_count?: number }>;
}

export const PLAYGROUP_NAME_CANDIDATES: Record<string, readonly { name: string }[]> = {
  starter: [{ name: 'StartingPlayer' }],
  normal_damage: [{ name: 'Damage' }],
  commander_damage: [{ name: 'CommanderDamage' }, { name: 'Damage' }],
  poison_tokens: [{ name: 'PoisonCounter' }],
  healing: [{ name: 'Healing' }],
  life_loss: [{ name: 'Damage' }],
  mill: [{ name: 'MoveCard' }],
  elimination: [{ name: 'Kill' }],
  winner: [{ name: 'WinnerDeclared' }],
};

export interface PlaygroupAttemptState {
  choice?: Record<string, number>;
  skip?: string[];
}

export function normalizePlaygroupKey(value: string): string {
  const key = String(value || '').trim().replace(/^Bearer\s+/i, '');
  if (!/^[A-Za-z0-9._~+/=-]{16,256}$/.test(key)) {
    throw new PlaygroupApiError('invalidKey', 400, 'Invalid Playgroup.gg API key.');
  }
  return key;
}

/** Validates a Playgroup Live URL returned by `/live_sessions` and canonicalizes its origin to `https://playgroup.gg`. */
export function normalizePlaygroupLiveUrl(rawUrl: string | null | undefined): string | null {
  const value = String(rawUrl ?? '').trim();
  if (!value) return null;
  try {
    const url = value.startsWith('/') ? new URL(value, 'https://playgroup.gg') : new URL(value);
    if (url.protocol !== 'https:' && url.protocol !== 'http:') return null;
    if (!/^\/live_sessions\/[A-Za-z0-9_-]+(?:\/.*)?$/.test(url.pathname)) return null;
    return `https://playgroup.gg${url.pathname}${url.search}${url.hash}`;
  } catch {
    return null;
  }
}

export function validationText(errors: unknown, max = 300): string {
  if (!errors) return '';
  let text: string;
  if (typeof errors === 'string') {
    text = errors;
  } else if (Array.isArray(errors)) {
    text = errors.map((entry) => validationText(entry, Infinity)).filter(Boolean).join('; ');
  } else if (typeof errors === 'object') {
    text = Object.entries(errors as Record<string, unknown>)
      .map(([k, v]) => `${k} ${Array.isArray(v) ? v.join(', ') : typeof v === 'object' ? JSON.stringify(v) : String(v)}`)
      .join('; ');
  } else {
    text = String(errors);
  }
  return Number.isFinite(max) ? text.slice(0, max) : text;
}

export function createPlaygroupClient(
  apiKey: string,
  { base = PLAYGROUP_BASE, fetchImpl = fetch, timeoutMs = 15_000 }: { base?: string; fetchImpl?: typeof fetch; timeoutMs?: number } = {},
): PlaygroupClient {
  async function call<T>(path: string, { method = 'GET', body }: { method?: string; body?: unknown } = {}): Promise<T> {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);
    let response: Response;
    try {
      response = await fetchImpl(`${base}${path}`, {
        method,
        headers: {
          Accept: 'application/json',
          ...(apiKey ? { Authorization: `Bearer ${apiKey}` } : {}),
          ...(body ? { 'Content-Type': 'application/json' } : {}),
        },
        body: body ? JSON.stringify(body) : undefined,
        signal: controller.signal,
      });
    } catch {
      throw new PlaygroupApiError('unreachable', 502, 'Could not reach Playgroup.gg.');
    } finally {
      clearTimeout(timer);
    }

    const data = (await response.json().catch(() => ({}))) as PlaygroupErrorDetails;
    if (response.ok) return data as T;
    if (response.status === 401) throw new PlaygroupApiError('unauthorized', 401, 'Playgroup.gg rejected the API key.');
    if (response.status === 403) throw new PlaygroupApiError('forbidden', 403, 'Playgroup.gg denied access to this playgroup or game.');
    if (response.status === 404) throw new PlaygroupApiError('notFound', 404, 'Game or playgroup not found on Playgroup.gg.');
    if (response.status === 429) throw new PlaygroupApiError('rateLimit', 429, 'Playgroup.gg rate limit reached. Try again shortly.');

    const errors = validationText(data?.validation_errors);
    const detail = data?.error ? `: ${data.error}${errors ? ` (${errors})` : ''}` : errors ? `: ${errors}` : '';
    const idx = Number.isInteger(data?.failed_event_index) ? ` (event ${(data.failed_event_index as number) + 1})` : '';
    throw new PlaygroupApiError('rejected', response.status === 422 ? 422 : 502, `Playgroup.gg rejected the request${idx}${detail}`, data);
  }

  return {
    me: () => call<PlaygroupUser>('/me'),
    playgroups: (userId) => call<PlaygroupSummary[]>(`/users/${encodeURIComponent(userId)}/playgroups`),
    members: (playgroupId) => call<PlaygroupMember[]>(`/playgroups/${encodeURIComponent(playgroupId)}/members`),
    games: (playgroupId) => call<PlaygroupGame[]>(`/playgroups/${encodeURIComponent(playgroupId)}/games?limit=20`),
    createLive: (body) => call<{ url?: string }>('/live_sessions', { method: 'POST', body }),
    commanderByName: (name) => call<{ id?: number; name?: string }>(`/commanders/by_name/${encodeURIComponent(name)}`),
    sendEvents: (gameId, events) => call<{ status?: string; imported_count?: number }>(`/games/${encodeURIComponent(gameId)}/events/batch`, { method: 'POST', body: { events } }),
  };
}

/** Parses UTC timestamps without timezone suffix (e.g. SQL TIMESTAMP strings) as UTC, plus ISO/epoch inputs. */
export function parseTime(value: string | number | Date | null | undefined): number {
  if (!value) return 0;
  if (typeof value === 'number') return Number.isFinite(value) ? value : 0;
  if (value instanceof Date) return value.getTime() || 0;
  let text = String(value).trim().replace(' ', 'T');
  if (!/(Z|[+-]\d{2}:?\d{2})$/.test(text)) {
    if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(text)) text += ':00';
    text += 'Z';
  }
  const time = new Date(text).getTime();
  return Number.isNaN(time) ? 0 : time;
}

/** Finds the newest playgroup game where the user participated starting within 15m before `startedAt`. */
export function findPlaygroupGame(
  games: readonly PlaygroupGame[] | null | undefined,
  { playgroupUserId, startedAt }: { playgroupUserId?: number | null; startedAt?: string | number | Date | null },
): PlaygroupGame | null {
  const since = startedAt ? parseTime(startedAt) - 15 * 60 * 1000 : 0;
  const list: readonly PlaygroupGame[] = Array.isArray(games) ? games : [];
  return (
    list
      .filter((game) => game && game.id != null)
      .filter((game) => !playgroupUserId || (game.participations ?? []).some((p) => Number(p.user_id) === Number(playgroupUserId)))
      .filter((game) => !since || !game.started_at || parseTime(game.started_at) >= since)
      .sort((a, b) => parseTime(b.started_at) - parseTime(a.started_at))[0] ?? null
  );
}

function seatId(value: number | null | undefined): string | undefined {
  return value == null ? undefined : String(value);
}

/** Converts a recorded match into Playgroup.gg `/games/{id}/events/batch` events. */
export function toPlaygroupEvents(
  record: PlaygroupMatchRecord | null | undefined,
  commanderIds: Record<string, number> = {},
  { choice = {}, skip = [] }: PlaygroupAttemptState = {},
): PlaygroupBatchEvent[] {
  if (!record || !Array.isArray(record.events)) return [];
  const skipped = new Set(skip);
  const out: PlaygroupBatchEvent[] = [];
  let id = 1;
  let previousTurn: RecordedMatchEvent | null = null;
  const firstTurn = record.events.find((event) => event.kind === 'turn');
  const starter = record.starter ?? firstTurn?.source ?? null;

  if (starter != null && !skipped.has('starter')) {
    out.push({
      id: id++,
      name: PLAYGROUP_NAME_CANDIDATES.starter[0].name,
      source_player_id: String(starter),
      time: firstTurn?.t,
      turn: 1,
      metadata: { kind: 'starter' },
    });
  }

  for (const event of record.events) {
    if (event.kind === 'turn') {
      if (previousTurn && previousTurn.source != null && !skipped.has('turn')) {
        out.push({
          id: id++,
          name: 'PassTurn',
          source_player_id: String(previousTurn.source),
          time: event.t,
          turn: previousTurn.turn,
          metadata: { kind: 'turn' },
        });
      }
      previousTurn = event;
      continue;
    }

    const candidates = PLAYGROUP_NAME_CANDIDATES[event.kind];
    if (!candidates?.length || skipped.has(event.kind)) continue;
    const pick = candidates[Math.min(choice[event.kind] ?? 0, candidates.length - 1)];
    const source = seatId(event.kind === 'life_loss' ? (event.target ?? event.source) : (event.source ?? event.target));
    if (source == null) continue;

    const hasCommanderId = event.kind === 'commander_damage' && Boolean(event.commander && commanderIds[event.commander]);
    const eventName = event.kind === 'commander_damage' && !hasCommanderId ? 'Damage' : pick.name;

    const batchEvent: PlaygroupBatchEvent = {
      id: id++,
      name: eventName,
      source_player_id: source,
      time: event.t,
      turn: event.turn,
      metadata: { kind: event.kind },
    };

    if (event.kind === 'life_loss') {
      batchEvent.target_player_id = source;
    } else if (event.target != null) {
      batchEvent.target_player_id = String(event.target);
    }
    if (event.amount) batchEvent.amount = event.amount;
    if (hasCommanderId && event.commander) {
      batchEvent.commander_id = commanderIds[event.commander];
    }
    if (event.kind === 'elimination' && event.reason && batchEvent.metadata) {
      batchEvent.metadata.reason = event.reason;
    }
    if (event.kind === 'mill') {
      delete batchEvent.amount;
      batchEvent.target_player_id = source;
      batchEvent.metadata = { kind: 'mill', from_zone: 'Library', to_zone: 'Graveyard' };
      const count = Math.min(Math.max(1, Number(event.amount) || 1), 60);
      out.push(batchEvent);
      for (let i = 1; i < count; i++) {
        out.push({ ...batchEvent, id: id++, metadata: { ...batchEvent.metadata } });
      }
      continue;
    }
    out.push(batchEvent);
  }

  const winner = record.result?.winner;
  if (winner != null && !skipped.has('winner')) {
    const last = record.events[record.events.length - 1];
    out.push({
      id: id++,
      name: PLAYGROUP_NAME_CANDIDATES.winner[0].name,
      source_player_id: String(winner),
      time: last?.t,
      turn: last?.turn,
      metadata: { kind: 'winner' },
    });
  }
  return out;
}

export function nextPlaygroupAttempt(
  failed: PlaygroupBatchEvent | undefined,
  { choice = {}, skip = [] }: PlaygroupAttemptState = {},
): PlaygroupAttemptState | null {
  const kind = typeof failed?.metadata?.kind === 'string' ? failed.metadata.kind : '';
  if (!kind || kind === 'turn' || kind === 'normal_damage') return null;
  const candidates = PLAYGROUP_NAME_CANDIDATES[kind];
  if (!candidates?.length) return null;
  const next = (choice[kind] ?? 0) + 1;
  if (next < candidates.length) {
    return { choice: { ...choice, [kind]: next }, skip: [...skip] };
  }
  return { choice: { ...choice }, skip: [...new Set([...skip, kind])] };
}

export function playgroupSkippedKinds(record: PlaygroupMatchRecord | null | undefined, skip: readonly string[] = []): string[] {
  const present = new Set((record?.events ?? []).map((event) => event.kind));
  return [...new Set(skip)].filter((kind) => present.has(kind as RecordedMatchEventKind));
}

export function recordCommanders(record: PlaygroupMatchRecord | null | undefined): string[] {
  const names = new Set<string>();
  for (const event of record?.events ?? []) {
    if (event.kind === 'commander_damage' && event.commander) names.add(event.commander);
  }
  return [...names];
}

export async function syncRecordToPlaygroup(
  client: PlaygroupClient,
  record: PlaygroupMatchRecord,
  {
    playgroupId,
    playgroupUserId,
    gameId,
    learnedState,
  }: {
    playgroupId?: number | null;
    playgroupUserId?: number | null;
    gameId?: number | null;
    learnedState?: { choice: Record<string, number>; skip: string[] };
  } = {},
): Promise<{ gameId: number; imported: number; skipped: string[] }> {
  let targetGameId = Number(gameId) || 0;
  if (!targetGameId) {
    if (!playgroupId) {
      throw new PlaygroupApiError('notFound', 404, 'Provide a Playgroup game ID or select a playgroup.');
    }
    const found = findPlaygroupGame(await client.games(playgroupId), {
      playgroupUserId,
      startedAt: record.startedAt,
    });
    if (!found) {
      throw new PlaygroupApiError('notFound', 404, 'No matching Playgroup.gg game was found for this match yet.');
    }
    targetGameId = Number(found.id);
  }

  const commanderIds: Record<string, number> = {};
  for (const name of recordCommanders(record)) {
    try {
      const commander = await client.commanderByName(name);
      if (commander?.id) commanderIds[name] = Number(commander.id);
    } catch {
      // Fall back to sending commander damage without commander_id if lookup fails.
    }
  }

  let attempt: { choice: Record<string, number>; skip: string[] } = {
    choice: { ...(learnedState?.choice ?? {}) },
    skip: [...(learnedState?.skip ?? [])],
  };
  let lastError: unknown = null;

  for (let tries = 0; tries < 20; tries++) {
    const events = toPlaygroupEvents(record, commanderIds, attempt);
    if (events.length === 0) {
      if (lastError) throw lastError;
      throw new PlaygroupApiError('rejected', 422, 'There are no events to send for this match.');
    }
    try {
      const sent = await client.sendEvents(targetGameId, events);
      if (learnedState) {
        learnedState.choice = { ...attempt.choice };
        learnedState.skip = [...attempt.skip];
      }
      return {
        gameId: targetGameId,
        imported: Number(sent?.imported_count ?? events.length),
        skipped: playgroupSkippedKinds(record, attempt.skip),
      };
    } catch (error) {
      const index = error instanceof PlaygroupApiError ? error.details?.failed_event_index : undefined;
      const next = error instanceof PlaygroupApiError && error.status === 422 && Number.isInteger(index)
        ? nextPlaygroupAttempt(events[index as number], attempt)
        : null;
      if (!next) throw error;
      lastError = error;
      attempt = { choice: next.choice ?? {}, skip: next.skip ?? [] };
    }
  }

  throw lastError ?? new PlaygroupApiError('rejected', 422, 'Playgroup.gg rejected the event batch.');
}

/** Resolves the winner's display name at the end of a game, when there is a single winner. */
export function resolveWinnerName(
  message: string | null | undefined,
  endInfo: GameEndView | null | undefined,
  view: GameView | null | undefined,
  myName: string | null,
): string | null {
  const players = view?.players ?? [];
  const outcome = gameResult(message, endInfo, myName);
  if (outcome === 'draw') return null;
  if (outcome === 'won' && myName) return myName;
  if (outcome === 'lost' && players.length === 2 && myName) {
    const other = players.find((player) => player.name && player.name.toLowerCase() !== myName.toLowerCase());
    if (other?.name) return other.name;
  }
  const match = /^(?:player\s+)?(.+?)\s+(?:is the winner|won the (?:game|match))/im.exec(
    `${endInfo?.gameInfo ?? ''}\n${message ?? ''}`.trim(),
  )?.[1];
  if (match) return match.trim();
  const survivors = players.filter((player) => !player.hasLeft && (player.life ?? 1) > 0);
  return survivors.length === 1 ? survivors[0].name ?? null : null;
}

/**
 * Observes `GameView` updates during a live match or replay and builds a Playgroup-compatible timeline.
 */
export class MatchEventAccumulator {
  readonly gameId: string;
  private startedAt = 0;
  private turns = 0;
  private currentTurn = 0;
  private activeSeat: number | null = null;
  private starter: number | null = null;
  private readonly players: RecordedMatchPlayer[] = [];
  private readonly seatByPlayerId = new Map<string, number>();
  private readonly ownerSeatByObjectId = new Map<string, number>();
  private readonly commanderByCardId = new Map<string, string>();
  private readonly commanderNamesBySeat = new Map<number, Set<string>>();
  private readonly seenSeqs = new Set<number>();
  private readonly eliminatedSeats = new Set<number>();
  private readonly lastDamageByTargetSeat = new Map<number, number>();
  private readonly events: RecordedMatchEvent[] = [];

  constructor(gameId: string, startedAt = 0) {
    this.gameId = gameId;
    this.startedAt = startedAt;
  }

  applyView(view: GameView | null | undefined, atMs = Date.now()): void {
    if (!view) return;
    if (!this.startedAt) this.startedAt = atMs;
    const t = Math.floor(atMs / 1000);

    this.syncPlayersAndObjects(view);

    const viewTurn = view.turn ?? 0;
    const nextActiveSeat = view.activePlayerId ? (this.seatByPlayerId.get(view.activePlayerId) ?? null) : null;
    if (viewTurn > 0) this.turns = Math.max(this.turns, viewTurn);

    if (viewTurn > 0 && (viewTurn !== this.currentTurn || (nextActiveSeat !== null && nextActiveSeat !== this.activeSeat))) {
      if (this.starter === null && nextActiveSeat !== null) this.starter = nextActiveSeat;
      this.currentTurn = viewTurn;
      this.activeSeat = nextActiveSeat;
      if (nextActiveSeat !== null) {
        this.events.push({ kind: 'turn', t, turn: viewTurn, source: nextActiveSeat, target: null });
      }
    }

    const turnNumber = Math.max(1, this.currentTurn || viewTurn || 1);
    const fresh: GameEventView[] = [];
    for (const event of view.events ?? []) {
      if (event.seq !== undefined) {
        if (this.seenSeqs.has(event.seq)) continue;
        this.seenSeqs.add(event.seq);
      }
      fresh.push(event);
    }

    const batchDamageCounts = new Map<string, number>();
    for (const event of fresh) {
      if (event.kind !== 'DAMAGE') continue;
      const targetSeat = event.targetId ? this.seatByPlayerId.get(event.targetId) : undefined;
      const amount = event.amount ?? 0;
      if (targetSeat === undefined || amount <= 0) continue;

      const sourceSeat = this.resolveSourceSeat(event) ?? this.activeSeat ?? targetSeat;
      const commander = event.combat ? this.resolveCommander(event.sourceId, event.sourceName, sourceSeat) : null;
      this.lastDamageByTargetSeat.set(targetSeat, sourceSeat);
      const damageKey = `${targetSeat}:${amount}`;
      batchDamageCounts.set(damageKey, (batchDamageCounts.get(damageKey) ?? 0) + 1);

      this.events.push({
        kind: commander ? 'commander_damage' : 'normal_damage',
        t,
        turn: turnNumber,
        source: sourceSeat,
        target: targetSeat,
        amount,
        ...(commander ? { commander } : {}),
      });
    }

    for (const event of fresh) {
      switch (event.kind) {
        case 'LIFE_LOSS': {
          const targetSeat = this.resolvePlayerSeat(event.targetId, event.playerId);
          const amount = event.amount ?? 0;
          if (targetSeat === undefined || amount <= 0) break;
          const key = `${targetSeat}:${amount}`;
          const covered = batchDamageCounts.get(key) ?? 0;
          if (covered > 0) {
            batchDamageCounts.set(key, covered - 1);
            break;
          }
          this.events.push({ kind: 'life_loss', t, turn: turnNumber, source: targetSeat, target: targetSeat, amount });
          break;
        }
        case 'LIFE_GAIN': {
          const targetSeat = this.resolvePlayerSeat(event.targetId, event.playerId);
          const amount = event.amount ?? 0;
          if (targetSeat === undefined || amount <= 0) break;
          this.events.push({ kind: 'healing', t, turn: turnNumber, source: targetSeat, target: targetSeat, amount });
          break;
        }
        case 'COUNTERS_ADDED': {
          const targetSeat = this.resolvePlayerSeat(event.targetId, event.playerId);
          const amount = event.amount ?? 0;
          if (targetSeat === undefined || amount <= 0 || event.counter?.toLowerCase() !== 'poison') break;
          this.events.push({
            kind: 'poison_tokens',
            t,
            turn: turnNumber,
            source: this.activeSeat ?? targetSeat,
            target: targetSeat,
            amount,
          });
          break;
        }
        case 'ZONE': {
          if (event.fromZone !== 'LIBRARY' || event.toZone !== 'GRAVEYARD') break;
          const millSeat = this.resolvePlayerSeat(event.playerId, undefined)
            ?? (event.targetId ? this.ownerSeatByObjectId.get(event.targetId) : undefined);
          if (millSeat === undefined) break;
          this.events.push({
            kind: 'mill',
            t,
            turn: turnNumber,
            source: this.activeSeat ?? millSeat,
            target: millSeat,
            amount: 1,
          });
          break;
        }
      }
    }

    if (this.currentTurn > 0) {
      for (const player of view.players ?? []) {
        const seat = player.playerId ? this.seatByPlayerId.get(player.playerId) : undefined;
        if (seat === undefined || this.eliminatedSeats.has(seat)) continue;
        if (player.hasLeft || (player.life !== undefined && player.life <= 0)) {
          this.eliminatedSeats.add(seat);
          this.events.push({
            kind: 'elimination',
            t,
            turn: turnNumber,
            source: this.lastDamageByTargetSeat.get(seat) ?? seat,
            target: seat,
            reason: player.hasLeft ? 'concession' : 'life',
          });
        }
      }
    }
  }

  finish(winnerName: string | null, draw = false, endedAtMs = Date.now()): PlaygroupMatchRecord {
    const normalized = winnerName?.trim().toLowerCase() ?? '';
    const winnerPlayer = !draw && normalized
      ? this.players.find((player) => player.name.trim().toLowerCase() === normalized)
      : undefined;

    return {
      gameId: this.gameId,
      startedAt: this.startedAt || endedAtMs,
      endedAt: endedAtMs,
      turns: Math.max(this.turns, this.currentTurn),
      starter: this.starter,
      players: this.players.map((player) => ({ ...player, commanders: [...player.commanders] })),
      events: this.events.map((event) => ({ ...event })),
      result: {
        winner: draw ? null : (winnerPlayer?.seat ?? null),
        ...(draw ? { draw: true } : {}),
      },
    };
  }

  private syncPlayersAndObjects(view: GameView): void {
    const commanders = commandersByPlayer(view);
    for (const player of view.players ?? []) {
      const playerId = player.playerId;
      if (!playerId) continue;
      let seat = this.seatByPlayerId.get(playerId);
      if (seat === undefined) {
        seat = this.players.length;
        this.seatByPlayerId.set(playerId, seat);
        this.players.push({
          seat,
          playerId,
          name: player.name ?? `Player ${seat + 1}`,
          human: player.isHuman !== false,
          commanders: [],
        });
      } else {
        if (player.name) this.players[seat].name = player.name;
        if (player.isHuman !== undefined) this.players[seat].human = player.isHuman;
      }

      const cmdList = commanders.get(playerId) ?? [];
      if (cmdList.length > 0) {
        const names = this.commanderNamesBySeat.get(seat) ?? new Set<string>();
        for (const cmd of cmdList) {
          names.add(cmd.name);
          if (cmd.id) this.commanderByCardId.set(cmd.id, cmd.name);
          if (cmd.card.id) this.commanderByCardId.set(cmd.card.id, cmd.name);
        }
        this.commanderNamesBySeat.set(seat, names);
        this.players[seat].commanders = [...names];
      }

      this.indexCards(Object.values(player.battlefield ?? {}), seat);
      this.indexCards(Object.values(player.graveyard ?? {}), seat);
      this.indexCards(Object.values(player.exile ?? {}), seat);
      this.indexCards((player.commandList ?? []) as CardView[], seat);
    }

    for (const card of Object.values(view.stack ?? {})) {
      const ownerId = card.controllerId ?? card.ownerId;
      const seat = ownerId ? this.seatByPlayerId.get(ownerId) : undefined;
      if (seat !== undefined && card.id) this.ownerSeatByObjectId.set(card.id, seat);
    }
  }

  private indexCards(cards: readonly CardView[], fallbackSeat: number): void {
    for (const card of cards) {
      if (!card?.id) continue;
      const ownerId = card.controllerId ?? card.ownerId;
      const seat = (ownerId ? this.seatByPlayerId.get(ownerId) : undefined) ?? fallbackSeat;
      this.ownerSeatByObjectId.set(card.id, seat);
    }
  }

  private resolvePlayerSeat(primaryId: string | undefined, fallbackId: string | undefined): number | undefined {
    if (primaryId && this.seatByPlayerId.has(primaryId)) return this.seatByPlayerId.get(primaryId);
    if (fallbackId && this.seatByPlayerId.has(fallbackId)) return this.seatByPlayerId.get(fallbackId);
    return undefined;
  }

  private resolveSourceSeat(event: GameEventView): number | undefined {
    if (event.sourceId) {
      if (this.seatByPlayerId.has(event.sourceId)) return this.seatByPlayerId.get(event.sourceId);
      if (this.ownerSeatByObjectId.has(event.sourceId)) return this.ownerSeatByObjectId.get(event.sourceId);
    }
    if (event.playerId && this.seatByPlayerId.has(event.playerId)) {
      return this.seatByPlayerId.get(event.playerId);
    }
    return undefined;
  }

  private resolveCommander(sourceId: string | undefined, sourceName: string | undefined, sourceSeat: number): string | null {
    if (sourceId && this.commanderByCardId.has(sourceId)) {
      return this.commanderByCardId.get(sourceId) ?? null;
    }
    if (sourceName) {
      const seatCommanders = this.commanderNamesBySeat.get(sourceSeat);
      if (seatCommanders?.has(sourceName)) return sourceName;
    }
    return null;
  }
}

export function recordFromReplayFrames(
  gameId: string,
  frames: readonly { at: number; view: GameView }[],
  info?: ReplayInfo | null,
): PlaygroupMatchRecord {
  const startedAt = info?.startedAt ?? frames[0]?.at ?? Date.now();
  const accumulator = new MatchEventAccumulator(gameId, startedAt);
  for (const frame of frames) {
    accumulator.applyView(frame.view, startedAt + Math.max(0, frame.at));
  }
  const winner = replayWinner(info?.result);
  const draw = !winner && /\bdraw\b/i.test(info?.result ?? '');
  const lastAt = frames.length > 0 ? startedAt + Math.max(0, frames[frames.length - 1].at) : Date.now();
  return accumulator.finish(winner, draw, info?.endedAt ?? lastAt);
}
