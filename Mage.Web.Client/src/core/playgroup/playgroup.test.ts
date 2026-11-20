import { describe, expect, test, vi } from 'vitest';
import type { GameView } from '../../protocol/generated/views';
import {
  MatchEventAccumulator,
  PlaygroupApiError,
  createPlaygroupClient,
  findPlaygroupGame,
  nextPlaygroupAttempt,
  normalizePlaygroupKey,
  normalizePlaygroupLiveUrl,
  parseTime,
  playgroupSkippedKinds,
  recordCommanders,
  recordFromReplayFrames,
  resolveWinnerName,
  syncRecordToPlaygroup,
  toPlaygroupEvents,
  type PlaygroupMatchRecord,
} from './playgroup';

describe('playgroup key validation and matching', () => {
  test('normalizes bearer prefixes and rejects invalid keys', () => {
    expect(normalizePlaygroupKey('  Bearer pg_live_1234567890abcdef  ')).toBe('pg_live_1234567890abcdef');
    expect(() => normalizePlaygroupKey('short')).toThrow(PlaygroupApiError);
    expect(() => normalizePlaygroupKey('invalid key with spaces')).toThrow(PlaygroupApiError);
  });

  test('normalizes Playgroup Live session URLs across proxied origins and rejects invalid URLs', () => {
    expect(normalizePlaygroupLiveUrl('https://playgroup.gg/live_sessions/abc123XYZ')).toBe('https://playgroup.gg/live_sessions/abc123XYZ');
    expect(normalizePlaygroupLiveUrl('https://play.sideboardy.app/live_sessions/abc123XYZ?foo=bar')).toBe('https://playgroup.gg/live_sessions/abc123XYZ?foo=bar');
    expect(normalizePlaygroupLiveUrl('/live_sessions/abc123XYZ')).toBe('https://playgroup.gg/live_sessions/abc123XYZ');
    expect(normalizePlaygroupLiveUrl('https://playgroup.gg/not_live/abc123')).toBeNull();
    expect(normalizePlaygroupLiveUrl('javascript:alert(1)')).toBeNull();
    expect(normalizePlaygroupLiveUrl('')).toBeNull();
    expect(normalizePlaygroupLiveUrl(null)).toBeNull();
  });

  test('parses timestamps safely in UTC when timezone is omitted', () => {
    expect(parseTime('2026-11-07 20:00:00')).toBe(Date.parse('2026-11-07T20:00:00Z'));
    expect(parseTime('2026-11-07T20:00')).toBe(Date.parse('2026-11-07T20:00:00Z'));
    expect(parseTime(1700000000000)).toBe(1700000000000);
  });

  test('finds newest matching playgroup game after match start window', () => {
    const startedAt = Date.parse('2026-11-07T20:00:00Z');
    const games = [
      { id: 1, started_at: '2026-11-07T18:00:00Z', participations: [{ id: 1, user_id: 7 }] },
      { id: 2, started_at: '2026-11-07T20:05:00Z', participations: [{ id: 2, user_id: 8 }] },
      { id: 3, started_at: '2026-11-07T20:02:00Z', participations: [{ id: 3, user_id: 7 }] },
    ];
    expect(findPlaygroupGame(games, { playgroupUserId: 7, startedAt })?.id).toBe(3);
    expect(findPlaygroupGame([], { playgroupUserId: 7, startedAt })).toBeNull();
  });
});

describe('playgroup event export and adaptive retries', () => {
  const sampleRecord: PlaygroupMatchRecord = {
    gameId: 'g1',
    startedAt: 10_000,
    endedAt: 20_000,
    turns: 2,
    starter: 2,
    players: [
      { seat: 0, playerId: 'p0', name: 'Alice', human: true, commanders: [] },
      { seat: 1, playerId: 'p1', name: 'Bob', human: true, commanders: [] },
      { seat: 2, playerId: 'p2', name: 'Cara', human: true, commanders: ['Atraxa'] },
    ],
    events: [
      { kind: 'turn', t: 10, turn: 1, source: 2, target: null },
      { kind: 'normal_damage', t: 11, turn: 1, source: 2, target: 0, amount: 3 },
      { kind: 'commander_damage', t: 11, turn: 1, source: 2, target: 0, amount: 4, commander: 'Atraxa' },
      { kind: 'life_loss', t: 12, turn: 1, source: null, target: 1, amount: 2 },
      { kind: 'turn', t: 13, turn: 2, source: 0, target: null },
      { kind: 'healing', t: 14, turn: 2, source: 0, target: 0, amount: 4 },
      { kind: 'mill', t: 15, turn: 2, source: null, target: 1, amount: 3 },
      { kind: 'poison_tokens', t: 16, turn: 2, source: 0, target: 2, amount: 1 },
      { kind: 'elimination', t: 16, turn: 2, source: 0, target: 2, reason: 'life' },
    ],
    result: { winner: 0 },
  };

  test('converts match events into Playgroup batch format', () => {
    const events = toPlaygroupEvents(sampleRecord, { Atraxa: 99 });
    expect(events.map((event) => event.name)).toEqual([
      'StartingPlayer',
      'Damage',
      'CommanderDamage',
      'Damage',
      'PassTurn',
      'Healing',
      'MoveCard',
      'MoveCard',
      'MoveCard',
      'PoisonCounter',
      'Kill',
      'WinnerDeclared',
    ]);
    expect(events[0]).toMatchObject({ name: 'StartingPlayer', source_player_id: '2', turn: 1 });
    expect(events[2]).toMatchObject({ name: 'CommanderDamage', commander_id: 99, amount: 4, source_player_id: '2', target_player_id: '0' });
    expect(events[3]).toMatchObject({ name: 'Damage', source_player_id: '1', target_player_id: '1', metadata: { kind: 'life_loss' } });
    expect(events[6]).toMatchObject({ name: 'MoveCard', source_player_id: '1', target_player_id: '1', metadata: { kind: 'mill', from_zone: 'Library', to_zone: 'Graveyard' } });
    expect(events[6].amount).toBeUndefined();
    expect(events[11]).toMatchObject({ name: 'WinnerDeclared', source_player_id: '0' });
    expect(recordCommanders(sampleRecord)).toEqual(['Atraxa']);
  });

  test('falls back commander damage to Damage when commander lookup returned no ID', () => {
    const events = toPlaygroupEvents(sampleRecord, {});
    expect(events[2]).toMatchObject({
      name: 'Damage',
      amount: 4,
      source_player_id: '2',
      target_player_id: '0',
      metadata: { kind: 'commander_damage' },
    });
    expect(events[2].commander_id).toBeUndefined();
  });

  test('falls back across candidate names and skips rejected optional event kinds', () => {
    const cmdAttempt = nextPlaygroupAttempt({ name: 'CommanderDamage', source_player_id: '0', metadata: { kind: 'commander_damage' } }, {});
    expect(cmdAttempt).toEqual({ choice: { commander_damage: 1 }, skip: [] });
    const skipAttempt = nextPlaygroupAttempt({ name: 'Damage', source_player_id: '0', metadata: { kind: 'commander_damage' } }, cmdAttempt!);
    expect(skipAttempt).toEqual({ choice: { commander_damage: 1 }, skip: ['commander_damage'] });
    expect(nextPlaygroupAttempt({ name: 'PassTurn', source_player_id: '0', metadata: { kind: 'turn' } }, {})).toBeNull();
    expect(playgroupSkippedKinds(sampleRecord, ['commander_damage'])).toEqual(['commander_damage']);
  });

  test('syncRecordToPlaygroup resolves game, looks up commanders, and retries on 422', async () => {
    const sendEvents = vi.fn()
      .mockRejectedValueOnce(new PlaygroupApiError('rejected', 422, 'failed', { failed_event_index: 2 }))
      .mockResolvedValueOnce({ status: 'success', imported_count: 10 });
    const client = {
      me: vi.fn(),
      playgroups: vi.fn(),
      members: vi.fn(),
      games: vi.fn().mockResolvedValue([
        { id: 456, started_at: '2026-11-07T20:01:00Z', participations: [{ id: 1, user_id: 7 }] },
      ]),
      createLive: vi.fn(),
      commanderByName: vi.fn().mockResolvedValue({ id: 99, name: 'Atraxa' }),
      sendEvents,
    };
    const learnedState = { choice: {}, skip: [] };
    const result = await syncRecordToPlaygroup(
      client,
      { ...sampleRecord, startedAt: Date.parse('2026-11-07T20:00:00Z') },
      { playgroupId: 10, playgroupUserId: 7, learnedState },
    );

    expect(result).toEqual({ gameId: 456, imported: 10, skipped: [] });
    expect(sendEvents).toHaveBeenCalledTimes(2);
    expect(sendEvents.mock.calls[1][1][2].name).toBe('Damage');
    expect(learnedState.choice).toEqual({ commander_damage: 1 });
  });
});

describe('MatchEventAccumulator and client', () => {
  test('extracts turns, combat/commander damage, deduplicated life loss, healing, poison, mill, and elimination', () => {
    const acc = new MatchEventAccumulator('game-1', 1_000_000);
    const view1: GameView = {
      turn: 1,
      activePlayerId: 'p1',
      players: [
        {
          playerId: 'p1',
          name: 'Alice',
          life: 40,
          isHuman: true,
          battlefield: {
            cmd1: { id: 'cmd1', name: 'Isshin, Two Heavens as One', controllerId: 'p1', ownerId: 'p1', rules: ['<b>Commander</b> 1 time played from the command zone.'] },
            card1: { id: 'card1', name: 'Shock', controllerId: 'p1', ownerId: 'p1' },
          },
          commandList: [],
        },
        {
          playerId: 'p2',
          name: 'Bob',
          life: 40,
          isHuman: true,
          battlefield: {},
          commandList: [],
        },
      ],
      events: [
        // Combat damage by commander -> commander_damage, and its coupled LIFE_LOSS should be deduplicated
        { seq: 1, kind: 'DAMAGE', combat: true, sourceId: 'cmd1', sourceName: 'Isshin, Two Heavens as One', targetId: 'p2', amount: 3 },
        { seq: 2, kind: 'LIFE_LOSS', playerId: 'p2', targetId: 'p2', amount: 3 },
        // Standalone life loss (e.g. fetchland / spell) -> emitted as life_loss
        { seq: 3, kind: 'LIFE_LOSS', playerId: 'p1', targetId: 'p1', amount: 1 },
        // Healing and poison
        { seq: 4, kind: 'LIFE_GAIN', playerId: 'p1', targetId: 'p1', amount: 2 },
        { seq: 5, kind: 'COUNTERS_ADDED', counter: 'poison', targetId: 'p2', amount: 2 },
        // Mill from library to graveyard
        { seq: 6, kind: 'ZONE', playerId: 'p2', targetId: 'lib-card', fromZone: 'LIBRARY', toZone: 'GRAVEYARD' },
      ],
    };
    acc.applyView(view1, 1_001_000);

    // Next turn with repeated seq 6 (must be ignored) and Bob eliminated at 0 life
    const view2: GameView = {
      ...view1,
      turn: 2,
      activePlayerId: 'p2',
      players: [
        view1.players![0],
        { ...view1.players![1], life: 0 },
      ],
      events: [
        { seq: 6, kind: 'ZONE', playerId: 'p2', targetId: 'lib-card', fromZone: 'LIBRARY', toZone: 'GRAVEYARD' },
      ],
    };
    acc.applyView(view2, 1_005_000);

    const record = acc.finish('Alice', false, 1_010_000);
    expect(record.starter).toBe(0);
    expect(record.turns).toBe(2);
    expect(record.players[0].commanders).toEqual(['Isshin, Two Heavens as One']);
    expect(record.events.map((event) => event.kind)).toEqual([
      'turn',
      'commander_damage',
      'life_loss',
      'healing',
      'poison_tokens',
      'mill',
      'turn',
      'elimination',
    ]);
    expect(record.events[1]).toMatchObject({ kind: 'commander_damage', source: 0, target: 1, amount: 3, commander: 'Isshin, Two Heavens as One' });
    expect(record.events[2]).toMatchObject({ kind: 'life_loss', target: 0, amount: 1 });
    expect(record.events[7]).toMatchObject({ kind: 'elimination', source: 0, target: 1, reason: 'life' });
    expect(record.result).toEqual({ winner: 0 });
  });

  test('builds record from replay frames and resolves winner', () => {
    const view: GameView = {
      turn: 1,
      activePlayerId: 'p1',
      players: [
        { playerId: 'p1', name: 'Alice', life: 20, isHuman: true },
        { playerId: 'p2', name: 'Bob', life: 0, isHuman: true, hasLeft: true },
      ],
      events: [],
    };
    const record = recordFromReplayFrames('g-replay', [{ at: 0, view }], {
      gameId: 'g-replay',
      startedAt: 5000,
      endedAt: 9000,
      result: 'Player Alice is the winner',
    });
    expect(record.result).toEqual({ winner: 0 });
    expect(resolveWinnerName('Alice won the game!', { won: true }, view, 'Alice')).toBe('Alice');
    expect(resolveWinnerName('Alice is the winner', { won: false }, view, 'Bob')).toBe('Alice');
  });

  test('createPlaygroupClient maps HTTP error statuses cleanly', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ error: 'nope' }), { status: 401 }));
    const client = createPlaygroupClient('pg_test_1234567890abcdef', { fetchImpl: fetchMock });
    await expect(client.me()).rejects.toMatchObject({ code: 'unauthorized', status: 401 });
  });
});
