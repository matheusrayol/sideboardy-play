import { create } from 'zustand';
import type {
  PlaygroupMatchRecord,
  PlaygroupSummary,
  PlaygroupUser,
} from '../../core/playgroup/playgroup';
import { readJson, writeJson } from './persist';

const loadPlaygroup = () => import('../../core/playgroup/playgroup');

const STORAGE_KEY = 'playmat.playgroup.v1';
const TABLE_CONFIG_TTL_MS = 7 * 24 * 60 * 60 * 1000;

export interface PlaygroupTableConfig {
  playgroupId: number;
  playgroupName: string;
  leagueId: number | null;
  liveUrl: string | null;
  startedAt: number;
}

export interface PlaygroupSyncedGame {
  playgroupGameId: number;
  imported: number;
  skipped: string[];
  sentAt: number;
  playgroupName?: string;
}

interface SavedPlaygroupState {
  apiKey: string;
  user: PlaygroupUser | null;
  playgroups: PlaygroupSummary[];
  defaultPlaygroupId: number | null;
  defaultLeagueId: number | null;
  autoSync: boolean;
  tableConfigs: Record<string, PlaygroupTableConfig>;
  syncedGames: Record<string, PlaygroupSyncedGame>;
}

const DEFAULT_SAVED: SavedPlaygroupState = {
  apiKey: '',
  user: null,
  playgroups: [],
  defaultPlaygroupId: null,
  defaultLeagueId: null,
  autoSync: true,
  tableConfigs: {},
  syncedGames: {},
};

const learnedEventState: { choice: Record<string, number>; skip: string[] } = { choice: {}, skip: [] };
const inFlightSyncs = new Set<string>();

function pruneTableConfigs(configs: Record<string, PlaygroupTableConfig>, now = Date.now()): Record<string, PlaygroupTableConfig> {
  const out: Record<string, PlaygroupTableConfig> = {};
  for (const [id, entry] of Object.entries(configs)) {
    if (entry && (!entry.startedAt || now - entry.startedAt < TABLE_CONFIG_TTL_MS)) {
      out[id] = entry;
    }
  }
  return out;
}

function loadSaved(): SavedPlaygroupState {
  const raw = readJson<Partial<SavedPlaygroupState>>(STORAGE_KEY, {});
  return {
    ...DEFAULT_SAVED,
    ...raw,
    playgroups: Array.isArray(raw.playgroups) ? raw.playgroups : [],
    tableConfigs: raw.tableConfigs && typeof raw.tableConfigs === 'object' ? pruneTableConfigs(raw.tableConfigs) : {},
    syncedGames: raw.syncedGames && typeof raw.syncedGames === 'object' ? raw.syncedGames : {},
  };
}

function persist(state: SavedPlaygroupState): void {
  writeJson<SavedPlaygroupState>(STORAGE_KEY, {
    apiKey: state.apiKey,
    user: state.user,
    playgroups: state.playgroups,
    defaultPlaygroupId: state.defaultPlaygroupId,
    defaultLeagueId: state.defaultLeagueId,
    autoSync: state.autoSync,
    tableConfigs: pruneTableConfigs(state.tableConfigs),
    syncedGames: state.syncedGames,
  });
}

export interface PlaygroupState extends SavedPlaygroupState {
  syncingIds: Record<string, boolean>;
  matchRecords: Record<string, PlaygroupMatchRecord>;
  setMatchRecord(record: PlaygroupMatchRecord): void;
  connect(rawKey: string): Promise<void>;
  refresh(): Promise<void>;
  disconnect(): void;
  setDefaults(patch: { defaultPlaygroupId?: number | null; defaultLeagueId?: number | null; autoSync?: boolean }): void;
  setTablePlaygroup(
    tableId: string,
    config: { playgroupId: number; playgroupName: string; leagueId?: number | null; liveUrl?: string | null } | null,
  ): void;
  startTableLiveSession(
    tableId: string,
    opts: { playerAmount: number; lifeAmount: number; playgroupId: number; playgroupName: string; leagueId?: number | null },
  ): Promise<string | null>;
  syncGame(
    record: PlaygroupMatchRecord,
    opts?: { tableId?: string | null; playgroupId?: number | null; gameId?: number | null; force?: boolean },
  ): Promise<{ gameId: number; imported: number; skipped: string[] }>;
}

export const usePlaygroup = create<PlaygroupState>((set, get) => ({
  ...loadSaved(),
  syncingIds: {},
  matchRecords: {},

  setMatchRecord(record) {
    set((state) => ({ matchRecords: { ...state.matchRecords, [record.gameId]: record } }));
  },

  async connect(rawKey) {
    const { createPlaygroupClient, normalizePlaygroupKey } = await loadPlaygroup();
    const apiKey = normalizePlaygroupKey(rawKey);
    const client = createPlaygroupClient(apiKey);
    const user = await client.me();
    const groups = await client.playgroups(user.id).catch(() => [] as PlaygroupSummary[]);
    const currentDefault = get().defaultPlaygroupId;
    const keepDefault = groups.some((group) => group.id === currentDefault) ? currentDefault : (groups[0]?.id ?? null);
    set((state) => {
      const next: SavedPlaygroupState = {
        ...state,
        apiKey,
        user,
        playgroups: groups,
        defaultPlaygroupId: keepDefault,
        defaultLeagueId: keepDefault === currentDefault ? state.defaultLeagueId : null,
      };
      persist(next);
      return next;
    });
  },

  async refresh() {
    const apiKey = get().apiKey;
    if (!apiKey) return;
    const { createPlaygroupClient } = await loadPlaygroup();
    const client = createPlaygroupClient(apiKey);
    const user = await client.me();
    const groups = await client.playgroups(user.id).catch(() => [] as PlaygroupSummary[]);
    set((state) => {
      const keepDefault = groups.some((group) => group.id === state.defaultPlaygroupId)
        ? state.defaultPlaygroupId
        : (groups[0]?.id ?? null);
      const next: SavedPlaygroupState = {
        ...state,
        user,
        playgroups: groups,
        defaultPlaygroupId: keepDefault,
      };
      persist(next);
      return next;
    });
  },

  disconnect() {
    set((state) => {
      const next: SavedPlaygroupState = {
        ...state,
        apiKey: '',
        user: null,
        playgroups: [],
        defaultPlaygroupId: null,
        defaultLeagueId: null,
      };
      persist(next);
      return next;
    });
  },

  setDefaults(patch) {
    set((state) => {
      const defaultPlaygroupId = patch.defaultPlaygroupId !== undefined ? patch.defaultPlaygroupId : state.defaultPlaygroupId;
      let defaultLeagueId = patch.defaultLeagueId !== undefined ? patch.defaultLeagueId : state.defaultLeagueId;
      if (patch.defaultPlaygroupId !== undefined && patch.defaultPlaygroupId !== state.defaultPlaygroupId && patch.defaultLeagueId === undefined) {
        defaultLeagueId = null;
      }
      const next: SavedPlaygroupState = {
        ...state,
        defaultPlaygroupId,
        defaultLeagueId,
        autoSync: patch.autoSync !== undefined ? patch.autoSync : state.autoSync,
      };
      persist(next);
      return next;
    });
  },

  setTablePlaygroup(tableId, config) {
    if (!tableId) return;
    set((state) => {
      const tableConfigs = { ...state.tableConfigs };
      if (!config) {
        delete tableConfigs[tableId];
      } else {
        tableConfigs[tableId] = {
          playgroupId: config.playgroupId,
          playgroupName: config.playgroupName,
          leagueId: config.leagueId ?? null,
          liveUrl: config.liveUrl ?? null,
          startedAt: Date.now(),
        };
      }
      const next: SavedPlaygroupState = { ...state, tableConfigs };
      persist(next);
      return next;
    });
  },

  async startTableLiveSession(tableId, opts) {
    const apiKey = get().apiKey;
    if (!apiKey) return null;
    const { createPlaygroupClient } = await loadPlaygroup();
    const client = createPlaygroupClient(apiKey);
    const live = await client.createLive({
      player_amount: Math.min(6, Math.max(1, opts.playerAmount)),
      life_amount: opts.lifeAmount,
      playgroup_id: opts.playgroupId,
      ...(opts.leagueId ? { league_id: opts.leagueId } : {}),
      client_identifier: 'sideboardy-play',
    });
    const liveUrl = live?.url && /^https:\/\/playgroup\.gg\//.test(live.url) ? live.url : null;
    get().setTablePlaygroup(tableId, {
      playgroupId: opts.playgroupId,
      playgroupName: opts.playgroupName,
      leagueId: opts.leagueId ?? null,
      liveUrl,
    });
    return liveUrl;
  },

  async syncGame(record, opts) {
    const { PlaygroupApiError, createPlaygroupClient, syncRecordToPlaygroup } = await loadPlaygroup();
    const state = get();
    if (!state.apiKey) {
      throw new PlaygroupApiError('unauthorized', 401, 'Playgroup.gg account is not connected.');
    }
    const existing = state.syncedGames[record.gameId];
    if (existing && !opts?.force) {
      return { gameId: existing.playgroupGameId, imported: existing.imported, skipped: existing.skipped };
    }
    if (inFlightSyncs.has(record.gameId)) {
      throw new PlaygroupApiError('rejected', 409, 'Sync already in progress for this match.');
    }

    const tableConfig = opts?.tableId ? state.tableConfigs[opts.tableId] : undefined;
    const playgroupId = opts?.playgroupId ?? tableConfig?.playgroupId ?? state.defaultPlaygroupId ?? null;
    const playgroupName = tableConfig?.playgroupName
      ?? state.playgroups.find((group) => group.id === playgroupId)?.name;

    inFlightSyncs.add(record.gameId);
    set((current) => ({ syncingIds: { ...current.syncingIds, [record.gameId]: true } }));
    try {
      const client = createPlaygroupClient(state.apiKey);
      const res = await syncRecordToPlaygroup(client, record, {
        playgroupId,
        playgroupUserId: state.user?.id ?? null,
        gameId: opts?.gameId ?? null,
        learnedState: learnedEventState,
      });
      set((current) => {
        const syncedGames: Record<string, PlaygroupSyncedGame> = {
          ...current.syncedGames,
          [record.gameId]: {
            playgroupGameId: res.gameId,
            imported: res.imported,
            skipped: res.skipped,
            sentAt: Date.now(),
            ...(playgroupName ? { playgroupName } : {}),
          },
        };
        const next: SavedPlaygroupState = { ...current, syncedGames };
        persist(next);
        return next;
      });
      return res;
    } finally {
      inFlightSyncs.delete(record.gameId);
      set((current) => {
        const syncingIds = { ...current.syncingIds };
        delete syncingIds[record.gameId];
        return { syncingIds };
      });
    }
  },
}));
