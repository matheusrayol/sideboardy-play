#!/usr/bin/env node
/**
 * Scryfall audit, printing-sync, and XMage Java card scaffolding CLI.
 *
 * Usage:
 *   node Utils/scryfall-cards.mjs audit --set TDM [--refresh]
 *   node Utils/scryfall-cards.mjs audit [--format commander] [--refresh] [--limit 30]
 *   node Utils/scryfall-cards.mjs sync-printings --set TDM [--write]
 *   node Utils/scryfall-cards.mjs scaffold --card "Card Name" [--set CODE] [--write] [--register] [--with-test] [--force]
 *   node Utils/scryfall-cards.mjs scaffold --set TDM [--auto-only] [--write] [--register] [--force]
 */
import { existsSync, mkdirSync, readFileSync, readdirSync, statSync, writeFileSync } from 'node:fs';
import { dirname, join, relative } from 'node:path';
import { fileURLToPath } from 'node:url';
import { gunzipSync } from 'node:zlib';

const ROOT_DIR = join(dirname(fileURLToPath(import.meta.url)), '..');
const UTILS_DIR = join(ROOT_DIR, 'Utils');
const DATA_DIR = join(UTILS_DIR, 'data');
const SETS_DIR = join(ROOT_DIR, 'Mage.Sets/src/mage/sets');
const CARDS_DIR = join(ROOT_DIR, 'Mage.Sets/src/mage/cards');
const CORE_CARDS_DIR = join(ROOT_DIR, 'Mage/src/main/java/mage/cards');
const TESTS_SINGLE_DIR = join(ROOT_DIR, 'Mage.Tests/src/test/java/org/mage/test/cards/single');
const SUBTYPE_FILE = join(ROOT_DIR, 'Mage/src/main/java/mage/constants/SubType.java');
const SUPERTYPE_FILE = join(ROOT_DIR, 'Mage/src/main/java/mage/constants/SuperType.java');
const KEYWORDS_FILE = join(UTILS_DIR, 'keywords.txt');

const USER_AGENT = 'SideboardyPlay-ScryfallSync/1.0 (+https://sideboardy.app)';
const CACHE_TTL_MS = 24 * 60 * 60 * 1000;

const NON_PLAYABLE_LAYOUTS = new Set([
  'token', 'double_faced_token', 'emblem', 'art_series', 'planar', 'scheme', 'vanguard', 'reversible_card',
]);

const CARD_TYPES = new Map([
  ['Artifact', 'CardType.ARTIFACT'],
  ['Battle', 'CardType.BATTLE'],
  ['Conspiracy', 'CardType.CONSPIRACY'],
  ['Creature', 'CardType.CREATURE'],
  ['Enchantment', 'CardType.ENCHANTMENT'],
  ['Instant', 'CardType.INSTANT'],
  ['Kindred', 'CardType.KINDRED'],
  ['Land', 'CardType.LAND'],
  ['Phenomenon', 'CardType.PHENOMENON'],
  ['Plane', 'CardType.PLANE'],
  ['Planeswalker', 'CardType.PLANESWALKER'],
  ['Scheme', 'CardType.SCHEME'],
  ['Sorcery', 'CardType.SORCERY'],
  ['Tribal', 'CardType.KINDRED'],
  ['Vanguard', 'CardType.VANGUARD'],
]);

const RARITIES = {
  common: 'COMMON',
  uncommon: 'UNCOMMON',
  rare: 'RARE',
  mythic: 'MYTHIC',
  special: 'SPECIAL',
  bonus: 'BONUS',
};

function normalizeName(name) {
  return String(name ?? '')
    .replace(/\\"/g, '"')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[’‘]/g, "'")
    .replace(/\uA789/g, '')
    .replace(/^"+|"+$/g, '')
    .trim()
    .toLowerCase();
}

function toClassName(name) {
  const ascii = String(name ?? '')
    .replace(/\/\/.*/g, '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/&/g, ' And ');
  const words = ascii.replace(/['’]/g, '').match(/[A-Za-z0-9]+/g) ?? [];
  let className = words.map((word) => word.charAt(0).toUpperCase() + word.slice(1)).join('');
  if (/^\d/.test(className)) className = `_${className}`;
  return className;
}

function toPackageLetter(name) {
  const ascii = String(name ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase();
  const match = /[a-z0-9]/.exec(ascii);
  return match ? match[0] : 'a';
}

function escapeJavaString(text) {
  return String(text).replace(/\\/g, '\\\\').replace(/"/g, '\\"');
}

function formatCollectorNumber(num) {
  const s = String(num).trim();
  return /^[1-9]\d*$/.test(s) ? s : `"${escapeJavaString(s)}"`;
}

function walkFiles(dir, ext) {
  const out = [];
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    const full = join(dir, entry.name);
    if (entry.isDirectory()) out.push(...walkFiles(full, ext));
    else if (entry.name.endsWith(ext)) out.push(full);
  }
  return out;
}

function parseEnumConstants(filePath) {
  const map = new Map();
  const src = readFileSync(filePath, 'utf8');
  for (const match of src.matchAll(/\b([A-Z0-9_]+)\("((?:[^"\\]|\\.)*)"/g)) {
    map.set(normalizeName(match[2]), match[1]);
    map.set(normalizeName(match[1].replace(/_/g, ' ')), match[1]);
  }
  return map;
}

function parseKeywords() {
  const rules = [];
  for (const line of readFileSync(KEYWORDS_FILE, 'utf8').split(/\r?\n/)) {
    const trimmed = line.trim();
    if (!trimmed) continue;
    const [rawName, mode] = trimmed.split('|');
    if (!rawName || !mode) continue;
    rules.push({
      name: rawName.trim(),
      lower: rawName.trim().toLowerCase(),
      abilityClass: `${toClassName(rawName)}Ability`,
      mode: mode.trim(),
    });
  }
  // Longer names first so "Double Strike" / "First strike" match accurately
  rules.sort((a, b) => b.lower.length - a.lower.length);
  return rules;
}

/**
 * Indexes all sets, SetCardInfo printings, and CardImpl Java files in Mage.Sets.
 */
function scanMageSets() {
  const setsByCode = new Map();
  const implementedByName = new Map();
  const cardsDirFiles = new Map();

  for (const dir of [CARDS_DIR, CORE_CARDS_DIR]) {
    if (!existsSync(dir)) continue;
    for (const cardPath of walkFiles(dir, '.java')) {
      const rel = relative(dir, cardPath).replace(/\\/g, '/').replace(/\.java$/, '');
      cardsDirFiles.set(`mage.cards.${rel.replace(/\//g, '.')}`, cardPath);
    }
  }

  const setCardRegex = /^[ \t]*cards\.add\(new SetCardInfo\(\s*"((?:[^"\\]|\\.)*)"\s*,\s*("?[^",\s)]+"?)\s*,\s*Rarity\.(\w+)\s*,\s*(mage\.cards\.[\w.]+)\.class/gm;

  for (const file of readdirSync(SETS_DIR).filter((f) => f.endsWith('.java'))) {
    const filePath = join(SETS_DIR, file);
    const src = readFileSync(filePath, 'utf8');
    const superMatch = /super\(\s*"((?:[^"\\]|\\.)*)"\s*,\s*"([A-Za-z0-9]+)"/.exec(src);
    if (!superMatch) continue;
    const setName = superMatch[1].replace(/\\"/g, '"');
    const code = superMatch[2].toUpperCase();

    const byName = new Map();
    const byNumber = new Set();
    let totalLines = 0;

    for (const m of src.matchAll(setCardRegex)) {
      totalLines++;
      const cardName = m[1].replace(/\\"/g, '"');
      const number = m[2].replace(/^"|"$/g, '').toLowerCase();
      const rarity = m[3];
      const classFqn = m[4];
      const norm = normalizeName(cardName);

      if (!byName.has(norm)) byName.set(norm, []);
      byName.get(norm).push({ name: cardName, number, rarity, classFqn });
      byNumber.add(`${norm}::${number}`);

      const implPath = cardsDirFiles.get(classFqn) ?? join(CARDS_DIR, `${classFqn.replace(/^mage\.cards\./, '').replace(/\./g, '/')}.java`);
      if (!implementedByName.has(norm) && existsSync(implPath)) {
        implementedByName.set(norm, { classFqn, filePath: implPath, canonicalName: cardName });
      }
    }

    setsByCode.set(code, { code, setName, filePath, byName, byNumber, totalLines });
  }

  return { setsByCode, implementedByName, cardsDirFiles };
}

function isFreshCache(filePath, refresh) {
  if (refresh || !existsSync(filePath)) return false;
  return Date.now() - statSync(filePath).mtimeMs < CACHE_TTL_MS;
}

async function fetchJson(url) {
  const res = await fetch(url, {
    headers: { 'User-Agent': USER_AGENT, Accept: 'application/json' },
    signal: AbortSignal.timeout(30_000),
  });
  if (!res.ok) {
    const body = await res.text().catch(() => '');
    throw new Error(`Scryfall HTTP ${res.status} for ${url}${body ? `: ${body.slice(0, 180)}` : ''}`);
  }
  return res.json();
}

async function loadScryfallSet(code, refresh = false) {
  const upper = code.toUpperCase();
  const cachePath = join(DATA_DIR, `${upper}-scryfall.json`);
  if (isFreshCache(cachePath, refresh)) {
    return JSON.parse(readFileSync(cachePath, 'utf8')).data;
  }
  const all = [];
  let url = `https://api.scryfall.com/cards/search?${new URLSearchParams({
    q: `set:${upper.toLowerCase()}`,
    order: 'set',
    unique: 'prints',
  })}`;
  while (url) {
    const page = await fetchJson(url);
    all.push(...(page.data ?? []));
    url = page.has_more ? page.next_page : null;
    if (url) await new Promise((resolve) => setTimeout(resolve, 110));
  }
  mkdirSync(DATA_DIR, { recursive: true });
  writeFileSync(cachePath, JSON.stringify({ fetchedAt: new Date().toISOString(), data: all }), 'utf8');
  return all;
}

function parseBulkText(text) {
  const trimmed = text.trimStart();
  if (trimmed.startsWith('[')) return JSON.parse(text);
  return text.split(/\r?\n/).filter(Boolean).map((line) => JSON.parse(line));
}

async function loadScryfallOracleBulk(refresh = false) {
  const cachePath = join(DATA_DIR, 'scryfall-oracle-cards.jsonl');
  const legacyCache = join(DATA_DIR, 'scryfall-oracle-cards.json');
  if (isFreshCache(cachePath, refresh)) {
    return parseBulkText(readFileSync(cachePath, 'utf8'));
  }
  if (!refresh && isFreshCache(legacyCache, false)) {
    return JSON.parse(readFileSync(legacyCache, 'utf8'));
  }
  console.error('Fetching Scryfall oracle-cards bulk metadata...');
  const list = await fetchJson('https://api.scryfall.com/bulk-data');
  const meta = (list.data ?? []).find((entry) => entry.type === 'oracle_cards');
  const uri = meta?.jsonl_download_uri ?? meta?.download_uri;
  if (!uri) throw new Error('Scryfall bulk metadata missing download URI');
  console.error(`Downloading ${uri} ...`);
  const res = await fetch(uri, {
    headers: { 'User-Agent': USER_AGENT },
    signal: AbortSignal.timeout(180_000),
  });
  if (!res.ok) throw new Error(`Scryfall bulk download HTTP ${res.status} for ${uri}`);
  const rawBuf = Buffer.from(await res.arrayBuffer());
  const text = uri.endsWith('.gz') ? gunzipSync(rawBuf).toString('utf8') : rawBuf.toString('utf8');
  mkdirSync(DATA_DIR, { recursive: true });
  writeFileSync(cachePath, text, 'utf8');
  return parseBulkText(text);
}

async function fetchScryfallCardByName(name, setCode) {
  const params = new URLSearchParams({ exact: name });
  if (setCode) params.set('set', setCode.toLowerCase());
  try {
    return await fetchJson(`https://api.scryfall.com/cards/named?${params}`);
  } catch {
    params.delete('exact');
    params.set('fuzzy', name);
    return fetchJson(`https://api.scryfall.com/cards/named?${params}`);
  }
}

function isPlayableScryfallCard(card, { allowDigital = false } = {}) {
  if (!card || !card.name) return false;
  if (NON_PLAYABLE_LAYOUTS.has(card.layout)) return false;
  if (card.oversized) return false;
  if (!allowDigital && card.digital) return false;
  if (card.set_type === 'memorabilia' || card.set_type === 'token') return false;
  if (Array.isArray(card.promo_types) && card.promo_types.includes('playtest')) return false;
  const tl = String(card.type_line ?? '');
  if (tl === 'Card' || tl === 'Stickers' || tl === 'Dungeon' || tl.startsWith('Token ')) return false;
  return true;
}

function cardNameVariants(card) {
  const out = [normalizeName(card.name)];
  if (card.layout !== 'split' && String(card.name).includes('//')) {
    const front = normalizeName(String(card.name).split('//')[0]);
    if (front && !out.includes(front)) out.push(front);
  }
  return out;
}

function resolveImplemented(card, implementedByName) {
  for (const variant of cardNameVariants(card)) {
    const found = implementedByName.get(variant);
    if (found) return found;
  }
  return null;
}

function stripReminderText(text) {
  return String(text ?? '').replace(/\s*\([^)]*\)/g, '').trim();
}

function parseSingleKeyword(segment, keywordRules) {
  const clean = stripReminderText(segment).replace(/\.$/, '').trim();
  if (!clean) return null;
  const cleanLower = clean.toLowerCase();

  for (const rule of keywordRules) {
    if (!cleanLower.startsWith(rule.lower)) continue;
    const rest = clean.slice(rule.lower.length).trim();
    if (clean.length > rule.lower.length && !/^[\s—\-0-9{]/.test(clean.slice(rule.lower.length))) continue;

    if (rule.mode === 'instance' && !rest) {
      return {
        importClass: `mage.abilities.keyword.${rule.abilityClass}`,
        statement: `this.addAbility(${rule.abilityClass}.getInstance());`,
      };
    }
    if (rule.mode === 'new' && !rest) {
      return {
        importClass: `mage.abilities.keyword.${rule.abilityClass}`,
        statement: `this.addAbility(new ${rule.abilityClass}());`,
      };
    }
    if (rule.mode === 'card' && !rest) {
      return {
        importClass: `mage.abilities.keyword.${rule.abilityClass}`,
        statement: `this.addAbility(new ${rule.abilityClass}(this));`,
      };
    }
    if (rule.mode === 'color' && !rest) {
      return {
        importClass: `mage.abilities.keyword.${rule.abilityClass}`,
        statement: `this.addAbility(new ${rule.abilityClass}(this.color));`,
      };
    }

    const manaMatch = /^(\{(?:[WUBRGCXSP0-9/]+)\}(?:\{(?:[WUBRGCXSP0-9/]+)\})*)$/.exec(rest.replace(/^—/, '').trim());
    const numMatch = /^(\d+)$/.exec(rest);

    // Special case: Equip {2} uses EquipAbility(2) when pure generic integer, or EquipAbility(Outcome.AddAbility, ManaCostsImpl, false) for colored equip
    if (rule.lower === 'equip') {
      const generic = /^\{(\d+)\}$/.exec(rest);
      if (generic) {
        return {
          importClass: 'mage.abilities.keyword.EquipAbility',
          statement: `this.addAbility(new EquipAbility(${generic[1]}));`,
        };
      }
      if (manaMatch) {
        return {
          importClasses: [
            'mage.abilities.costs.mana.ManaCostsImpl',
            'mage.abilities.keyword.EquipAbility',
            'mage.constants.Outcome',
          ],
          statement: `this.addAbility(new EquipAbility(Outcome.AddAbility, new ManaCostsImpl<>("${manaMatch[1]}"), false));`,
        };
      }
      return null;
    }

    if (rule.mode === 'number' && numMatch) {
      return {
        importClass: `mage.abilities.keyword.${rule.abilityClass}`,
        statement: `this.addAbility(new ${rule.abilityClass}(${numMatch[1]}));`,
      };
    }
    if (rule.mode === 'card, number' && numMatch) {
      return {
        importClass: `mage.abilities.keyword.${rule.abilityClass}`,
        statement: `this.addAbility(new ${rule.abilityClass}(this, ${numMatch[1]}));`,
      };
    }
    if (rule.mode === 'cost' && manaMatch) {
      return {
        importClasses: ['mage.abilities.costs.mana.ManaCostsImpl', `mage.abilities.keyword.${rule.abilityClass}`],
        statement: `this.addAbility(new ${rule.abilityClass}(new ManaCostsImpl<>("${manaMatch[1]}")));`,
      };
    }
    if (rule.mode === 'manaString' && manaMatch) {
      return {
        importClass: `mage.abilities.keyword.${rule.abilityClass}`,
        statement: `this.addAbility(new ${rule.abilityClass}("${manaMatch[1]}"));`,
      };
    }
    if (rule.mode === 'card, manaString' && manaMatch) {
      return {
        importClass: `mage.abilities.keyword.${rule.abilityClass}`,
        statement: `this.addAbility(new ${rule.abilityClass}(this, "${manaMatch[1]}"));`,
      };
    }
    if (rule.mode === 'card, cost' && manaMatch) {
      return {
        importClasses: ['mage.abilities.costs.mana.ManaCostsImpl', `mage.abilities.keyword.${rule.abilityClass}`],
        statement: `this.addAbility(new ${rule.abilityClass}(this, new ManaCostsImpl<>("${manaMatch[1]}")));`,
      };
    }
    if (rule.mode === 'cost, card' && manaMatch) {
      return {
        importClasses: ['mage.abilities.costs.mana.ManaCostsImpl', `mage.abilities.keyword.${rule.abilityClass}`],
        statement: `this.addAbility(new ${rule.abilityClass}(new ManaCostsImpl<>("${manaMatch[1]}"), this));`,
      };
    }
    if (rule.lower === 'suspend') {
      const susp = /^(\d+)\s*—\s*(\{(?:[WUBRGCXSP0-9/]+)\}(?:\{(?:[WUBRGCXSP0-9/]+)\})*)$/.exec(rest);
      if (susp) {
        return {
          importClasses: ['mage.abilities.costs.mana.ManaCostsImpl', 'mage.abilities.keyword.SuspendAbility'],
          statement: `this.addAbility(new SuspendAbility(${susp[1]}, new ManaCostsImpl<>("${susp[2]}"), this));`,
        };
      }
    }
  }
  return null;
}

/**
 * Classifies and compiles a card's Oracle text paragraphs into XMage keyword abilities + TODOs.
 */
function compileOracleLines(oracleText, keywordRules) {
  const lines = String(oracleText ?? '')
    .split(/\r?\n/)
    .map((l) => l.trim())
    .filter(Boolean);

  if (lines.length === 0) return { isAutoReady: true, blocks: [], todoCount: 0 };

  const blocks = [];
  let todoCount = 0;

  for (const line of lines) {
    const clean = stripReminderText(line);
    if (!clean) {
      // Pure reminder text line (e.g. flavorless vanilla/reminder)
      continue;
    }
    // Check comma-separated keywords (without splitting costs/clauses containing commas in full sentences)
    const parts = clean.includes(';') || /\./.test(clean.replace(/\.$/, ''))
      ? [clean]
      : clean.split(/,\s*/);

    const parsedParts = parts.map((p) => parseSingleKeyword(p, keywordRules));
    if (parsedParts.every(Boolean)) {
      blocks.push({ kind: 'keywords', comment: line, items: parsedParts });
    } else {
      todoCount++;
      blocks.push({ kind: 'todo', comment: line });
    }
  }

  return { isAutoReady: todoCount === 0, blocks, todoCount };
}

function parseTypeLine(typeLine, superTypesMap, subTypesMap) {
  const [left = '', right = ''] = String(typeLine ?? '').split('—').map((s) => s.trim());
  const superTypes = [];
  const cardTypes = [];
  const subTypes = [];
  const unknownTokens = [];

  for (const token of left.split(/\s+/).filter(Boolean)) {
    const ct = CARD_TYPES.get(token);
    if (ct) {
      cardTypes.push(ct);
      continue;
    }
    const st = superTypesMap.get(normalizeName(token));
    if (st) {
      superTypes.push(`SuperType.${st}`);
      continue;
    }
    unknownTokens.push(token);
  }

  // Handle multi-word subtypes like "Time Lord" before single-word split
  let remainingRight = right;
  const knownMulti = [...subTypesMap.keys()].filter((k) => k.includes(' ') || k.includes('-')).sort((a, b) => b.length - a.length);
  for (const multi of knownMulti) {
    const re = new RegExp(`(^|\\s)${multi.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}(?=\\s|$)`, 'i');
    const normRight = normalizeName(remainingRight);
    if (re.test(normRight)) {
      subTypes.push(`SubType.${subTypesMap.get(multi)}`);
      remainingRight = normRight.replace(re, ' ').trim();
    }
  }

  for (const token of remainingRight.split(/\s+/).filter(Boolean)) {
    const sub = subTypesMap.get(normalizeName(token));
    if (sub) subTypes.push(`SubType.${sub}`);
    else unknownTokens.push(token);
  }

  return { superTypes, cardTypes, subTypes, unknownTokens };
}

function generateCardJava(card, { keywordRules, superTypesMap, subTypesMap }) {
  const face = card.card_faces?.[0] && !card.oracle_text && card.layout !== 'normal' ? card.card_faces[0] : card;
  const isMultiFace = Array.isArray(card.card_faces) && card.card_faces.length > 1;
  const cardName = card.name;
  const className = toClassName(cardName);
  const pkgLetter = toPackageLetter(cardName);
  const manaCost = face.mana_cost ?? '';
  const typeLine = face.type_line ?? card.type_line ?? '';
  const { superTypes, cardTypes, subTypes, unknownTokens } = parseTypeLine(typeLine, superTypesMap, subTypesMap);
  const compiled = compileOracleLines(face.oracle_text ?? '', keywordRules);

  const imports = new Set([
    'java.util.UUID',
    'mage.cards.CardImpl',
    'mage.cards.CardSetInfo',
    'mage.constants.CardType',
  ]);
  if (superTypes.length > 0) imports.add('mage.constants.SuperType');
  if (subTypes.length > 0) imports.add('mage.constants.SubType');

  const isPlaneswalker = cardTypes.includes('CardType.PLANESWALKER');
  const isBattle = cardTypes.includes('CardType.BATTLE');
  const hasPT = face.power != null && face.toughness != null && !isPlaneswalker && !isBattle;
  if (hasPT) imports.add('mage.MageInt');

  for (const block of compiled.blocks) {
    if (block.kind !== 'keywords') continue;
    for (const item of block.items) {
      if (item.importClass) imports.add(item.importClass);
      for (const cls of item.importClasses ?? []) imports.add(cls);
    }
  }

  const bodyLines = [];
  for (const st of superTypes) bodyLines.push(`        this.supertype.add(${st});`);
  for (const sub of subTypes) bodyLines.push(`        this.subtype.add(${sub});`);
  for (const unk of unknownTokens) bodyLines.push(`        // TODO unknown type/subtype: ${unk}`);

  if (isPlaneswalker && face.loyalty != null) {
    const loy = /^\d+$/.test(String(face.loyalty)) ? face.loyalty : `0 /* ${face.loyalty} */`;
    bodyLines.push(`        this.setStartingLoyalty(${loy});`);
  } else if (isBattle && face.defense != null) {
    const def = /^\d+$/.test(String(face.defense)) ? face.defense : `0 /* ${face.defense} */`;
    bodyLines.push(`        this.setStartingDefense(${def});`);
  } else if (hasPT) {
    const pow = /^\d+$/.test(String(face.power)) ? face.power : `0 /* ${face.power} */`;
    const tou = /^\d+$/.test(String(face.toughness)) ? face.toughness : `0 /* ${face.toughness} */`;
    bodyLines.push(`        this.power = new MageInt(${pow});`);
    bodyLines.push(`        this.toughness = new MageInt(${tou});`);
  }

  if (isMultiFace) {
    bodyLines.push('');
    bodyLines.push(`        // TODO multi-face layout (${card.layout}): check TransformingDoubleFacedCard / ModalDoubleFacedCard / SplitCard / AdventureCard`);
  }

  for (const block of compiled.blocks) {
    bodyLines.push('');
    if (block.kind === 'keywords') {
      bodyLines.push(`        // ${block.comment}`);
      for (const item of block.items) bodyLines.push(`        ${item.statement}`);
    } else {
      bodyLines.push(`        // TODO: ${block.comment}`);
    }
  }

  const sortedImports = [...imports].sort((a, b) => {
    const aJava = a.startsWith('java.');
    const bJava = b.startsWith('java.');
    if (aJava !== bJava) return aJava ? -1 : 1;
    return a.localeCompare(b);
  });

  const javaSource = [
    `package mage.cards.${pkgLetter};`,
    '',
    ...sortedImports.map((imp) => `import ${imp};`),
    '',
    '/**',
    ` * Generated from Scryfall oracle data (${cardName}).`,
    ' */',
    `public final class ${className} extends CardImpl {`,
    '',
    `    public ${className}(UUID ownerId, CardSetInfo setInfo) {`,
    `        super(ownerId, setInfo, new CardType[]{${cardTypes.join(', ')}}, "${escapeJavaString(manaCost)}");`,
    ...bodyLines,
    '    }',
    '',
    `    private ${className}(final ${className} card) {`,
    '        super(card);',
    '    }',
    '',
    '    @Override',
    `    public ${className} copy() {`,
    `        return new ${className}(this);`,
    '    }',
    '}',
    '',
  ].join('\n');

  return {
    cardName,
    className,
    pkgLetter,
    classFqn: `mage.cards.${pkgLetter}.${className}`,
    relPath: `Mage.Sets/src/mage/cards/${pkgLetter}/${className}.java`,
    absPath: join(CARDS_DIR, pkgLetter, `${className}.java`),
    isAutoReady: compiled.isAutoReady && !isMultiFace && unknownTokens.length === 0,
    javaSource,
  };
}

function generateTestJava(card, setCode) {
  const className = toClassName(card.name);
  const pkg = String(setCode || 'custom').toLowerCase().replace(/[^a-z0-9]/g, '');
  const relPath = `Mage.Tests/src/test/java/org/mage/test/cards/single/${pkg}/${className}Test.java`;
  const source = [
    `package org.mage.test.cards.single.${pkg};`,
    '',
    'import mage.constants.PhaseStep;',
    'import mage.constants.Zone;',
    'import org.junit.Test;',
    'import org.mage.test.serverside.base.CardTestPlayerBase;',
    '',
    `public class ${className}Test extends CardTestPlayerBase {`,
    '',
    `    @Test`,
    `    public void test${className}() {`,
    `        addCard(Zone.HAND, playerA, "${escapeJavaString(card.name)}");`,
    '',
    '        setStrictChooseMode(true);',
    '        setStopAt(1, PhaseStep.BEGIN_COMBAT);',
    '        execute();',
    '    }',
    '}',
    '',
  ].join('\n');
  return { relPath, absPath: join(ROOT_DIR, relPath), source };
}

function insertSetCardLines(setInfo, additions) {
  if (additions.length === 0) return { changed: false, addedCount: 0, upgradedVarious: 0 };
  const raw = readFileSync(setInfo.filePath, 'utf8');
  const eol = raw.includes('\r\n') ? '\r\n' : '\n';
  let lines = raw.split(/\r?\n/);
  const existingCounts = new Map();
  for (const line of lines) {
    const m = /cards\.add\(new SetCardInfo\(\s*"((?:[^"\\]|\\.)*)"/.exec(line);
    if (m) {
      const k = normalizeName(m[1]);
      existingCounts.set(k, (existingCounts.get(k) ?? 0) + 1);
    }
  }
  const plannedCounts = new Map(existingCounts);
  const touchedNorms = new Set();
  for (const add of additions) {
    const k = normalizeName(add.name);
    touchedNorms.add(k);
    plannedCounts.set(k, (plannedCounts.get(k) ?? 0) + 1);
  }

  let upgradedVarious = 0;
  lines = lines.map((line) => {
    const m = /^(\s*cards\.add\(new SetCardInfo\(\s*"((?:[^"\\]|\\.)*)"[^)]+)(\)\);\s*)$/.exec(line);
    if (!m) return line;
    const norm = normalizeName(m[2]);
    if (
      touchedNorms.has(norm)
      && (plannedCounts.get(norm) ?? 0) > 1
      && /\.class\s*$/.test(m[1])
    ) {
      upgradedVarious++;
      return `${m[1]}, NON_FULL_USE_VARIOUS${m[3]}`;
    }
    return line;
  });

  const newEntries = additions.map((add) => {
    const multi = (plannedCounts.get(normalizeName(add.name)) ?? 0) > 1;
    const rarity = RARITIES[add.rarity] ?? 'SPECIAL';
    const num = formatCollectorNumber(add.collectorNumber);
    const suffix = multi ? ', NON_FULL_USE_VARIOUS' : '';
    return {
      sortName: add.name,
      line: `        cards.add(new SetCardInfo("${escapeJavaString(add.name)}", ${num}, Rarity.${rarity}, ${add.classFqn}.class${suffix}));`,
    };
  });

  // Insert sorted into the cards.add block
  const cardLineIndices = lines
    .map((line, index) => (/^\s*cards\.add\(new SetCardInfo\(/.test(line) ? index : -1))
    .filter((i) => i >= 0);

  if (cardLineIndices.length === 0) {
    throw new Error(`Could not find any cards.add(new SetCardInfo(...)) line in ${setInfo.filePath}`);
  }

  const firstIdx = cardLineIndices[0];
  const lastIdx = cardLineIndices[cardLineIndices.length - 1];
  const block = lines.slice(firstIdx, lastIdx + 1).map((line) => {
    const m = /cards\.add\(new SetCardInfo\(\s*"((?:[^"\\]|\\.)*)"/.exec(line);
    return { sortName: m ? m[1].replace(/\\"/g, '"') : '', line };
  });

  block.push(...newEntries);
  block.sort((a, b) => a.sortName.localeCompare(b.sortName, 'en', { sensitivity: 'base' }));

  const updated = [
    ...lines.slice(0, firstIdx),
    ...block.map((item) => item.line),
    ...lines.slice(lastIdx + 1),
  ].join(eol);

  writeFileSync(setInfo.filePath, updated, 'utf8');
  return { changed: true, addedCount: newEntries.length, upgradedVarious };
}

function parseArgs(argv) {
  const [command, ...rest] = argv;
  const opts = { command };
  for (let i = 0; i < rest.length; i++) {
    const token = rest[i];
    if (!token.startsWith('--')) continue;
    const key = token.slice(2);
    const next = rest[i + 1];
    if (!next || next.startsWith('--')) {
      opts[key] = true;
    } else {
      opts[key] = next;
      i++;
    }
  }
  return opts;
}

async function runAudit(opts, state) {
  const limit = Number(opts.limit ?? 25);
  const keywordRules = parseKeywords();

  if (opts.set) {
    const code = String(opts.set).toUpperCase();
    const setInfo = state.setsByCode.get(code);
    const scryfallCards = (await loadScryfallSet(code, !!opts.refresh)).filter((c) => isPlayableScryfallCard(c));

    const missingPrintings = [];
    const autoReady = [];
    const customNeeded = [];
    const seenMissingNames = new Set();

    for (const card of scryfallCards) {
      const impl = resolveImplemented(card, state.implementedByName);
      if (impl) {
        if (setInfo) {
          const num = String(card.collector_number).toLowerCase();
          const hasPrinting = cardNameVariants(card).some((v) => setInfo.byNumber.has(`${v}::${num}`));
          if (!hasPrinting) {
            missingPrintings.push({
              collectorNumber: card.collector_number,
              name: card.name,
              rarity: card.rarity,
              classFqn: impl.classFqn,
            });
          }
        }
        continue;
      }

      const norm = normalizeName(card.name);
      if (seenMissingNames.has(norm)) continue;
      seenMissingNames.add(norm);

      const face = card.card_faces?.[0] && !card.oracle_text ? card.card_faces[0] : card;
      const compiled = compileOracleLines(face.oracle_text ?? '', keywordRules);
      const isSimpleLayout = !Array.isArray(card.card_faces) || card.card_faces.length <= 1;
      if (compiled.isAutoReady && isSimpleLayout) {
        autoReady.push({ collectorNumber: card.collector_number, name: card.name, typeLine: card.type_line });
      } else {
        customNeeded.push({
          collectorNumber: card.collector_number,
          name: card.name,
          typeLine: card.type_line,
          todoCount: isSimpleLayout ? compiled.todoCount : (compiled.todoCount || 1),
        });
      }
    }

    console.log(`\n=== Audit Set ${code}${setInfo ? ` (${setInfo.setName})` : ' (SET CLASS NOT REGISTERED)'} ===`);
    console.log(`Scryfall playable printings: ${scryfallCards.length}`);
    console.log(`Missing printings of existing cards: ${missingPrintings.length}`);
    console.log(`Unimplemented unique cards: ${autoReady.length + customNeeded.length} (auto-ready: ${autoReady.length}, custom logic: ${customNeeded.length})`);

    if (missingPrintings.length > 0) {
      console.log(`\nFirst ${Math.min(limit, missingPrintings.length)} missing printings:`);
      for (const p of missingPrintings.slice(0, limit)) console.log(`  #${p.collectorNumber}\t${p.name} -> ${p.classFqn}`);
    }
    if (autoReady.length > 0) {
      console.log(`\nAuto-ready unimplemented cards (vanilla / supported keywords):`);
      for (const c of autoReady.slice(0, limit)) console.log(`  #${c.collectorNumber}\t${c.name} [${c.typeLine}]`);
    }
    if (customNeeded.length > 0) {
      console.log(`\nFirst ${Math.min(limit, customNeeded.length)} unimplemented cards needing custom rules:`);
      for (const c of customNeeded.slice(0, limit)) console.log(`  #${c.collectorNumber}\t${c.name} [${c.typeLine}] (${c.todoCount} TODO line(s))`);
    }
    return;
  }

  const format = opts.format ? String(opts.format).toLowerCase() : null;
  const oracleCards = await loadScryfallOracleBulk(!!opts.refresh);
  const filtered = oracleCards.filter((card) => {
    if (!isPlayableScryfallCard(card)) return false;
    if (format) return card.legalities?.[format] === 'legal';
    // When auditing all cards globally, exclude unreleased/funny/digital-only
    return card.set_type !== 'funny' && !card.promo;
  });

  let implementedCount = 0;
  const autoReady = [];
  const customNeeded = [];
  const missingBySet = new Map();

  for (const card of filtered) {
    if (resolveImplemented(card, state.implementedByName)) {
      implementedCount++;
      continue;
    }
    const face = card.card_faces?.[0] && !card.oracle_text ? card.card_faces[0] : card;
    const compiled = compileOracleLines(face.oracle_text ?? '', keywordRules);
    const isSimple = !Array.isArray(card.card_faces) || card.card_faces.length <= 1;
    const entry = { name: card.name, set: card.set.toUpperCase(), setName: card.set_name, typeLine: card.type_line, releasedAt: card.released_at };
    if (compiled.isAutoReady && isSimple) autoReady.push(entry);
    else customNeeded.push(entry);

    const bucket = missingBySet.get(entry.set) ?? { set: entry.set, setName: entry.setName, count: 0 };
    bucket.count++;
    missingBySet.set(entry.set, bucket);
  }

  const total = filtered.length;
  const missingTotal = autoReady.length + customNeeded.length;
  const pct = total > 0 ? ((implementedCount / total) * 100).toFixed(2) : '0.00';

  console.log(`\n=== Global Scryfall Audit (${format ? `format: ${format}` : 'all playable paper cards'}) ===`);
  console.log(`Eligible Scryfall unique cards: ${total}`);
  console.log(`Implemented in Mage.Sets: ${implementedCount} (${pct}%)`);
  console.log(`Unimplemented: ${missingTotal} (auto-ready: ${autoReady.length}, custom rules: ${customNeeded.length})`);

  const topSets = [...missingBySet.values()].sort((a, b) => b.count - a.count).slice(0, 15);
  console.log('\nTop sets with missing cards:');
  for (const s of topSets) console.log(`  ${s.set.padEnd(6)} ${String(s.count).padStart(4)}  ${s.setName}`);

  if (autoReady.length > 0) {
    console.log(`\nSample auto-ready cards (${Math.min(limit, autoReady.length)} of ${autoReady.length}):`);
    for (const c of autoReady.slice(0, limit)) console.log(`  [${c.set}] ${c.name} — ${c.typeLine}`);
  }
  if (customNeeded.length > 0) {
    const sortedRecent = [...customNeeded].sort((a, b) => String(b.releasedAt ?? '').localeCompare(String(a.releasedAt ?? '')));
    console.log(`\nMost recent missing custom cards (${Math.min(limit, sortedRecent.length)} of ${customNeeded.length}):`);
    for (const c of sortedRecent.slice(0, limit)) console.log(`  [${c.set} ${c.releasedAt ?? ''}] ${c.name} — ${c.typeLine}`);
  }
}

async function runSyncPrintings(opts, state) {
  if (!opts.set) throw new Error('sync-printings requires --set <CODE>');
  const code = String(opts.set).toUpperCase();
  const setInfo = state.setsByCode.get(code);
  if (!setInfo) throw new Error(`Set class for ${code} was not found in Mage.Sets/src/mage/sets/`);

  const scryfallCards = (await loadScryfallSet(code, !!opts.refresh)).filter((c) => isPlayableScryfallCard(c));
  const missing = [];
  for (const card of scryfallCards) {
    const impl = resolveImplemented(card, state.implementedByName);
    if (!impl) continue;
    const num = String(card.collector_number).toLowerCase();
    const exists = cardNameVariants(card).some((v) => setInfo.byNumber.has(`${v}::${num}`));
    if (exists) continue;
    missing.push({
      name: card.name,
      collectorNumber: card.collector_number,
      rarity: card.rarity,
      classFqn: impl.classFqn,
    });
  }

  console.log(`${code} (${setInfo.setName}): found ${missing.length} missing printing(s) for already-implemented cards.`);
  for (const item of missing) {
    console.log(`  + #${item.collectorNumber} ${item.name} (${item.classFqn})`);
  }

  if (opts.write && missing.length > 0) {
    const result = insertSetCardLines(setInfo, missing);
    console.log(`Updated ${relative(ROOT_DIR, setInfo.filePath)}: +${result.addedCount} SetCardInfo(s), upgraded ${result.upgradedVarious} to NON_FULL_USE_VARIOUS.`);
  } else if (missing.length > 0) {
    console.log('Dry run only. Re-run with --write to insert them.');
  }
}

async function runScaffold(opts, state) {
  const keywordRules = parseKeywords();
  const subTypesMap = parseEnumConstants(SUBTYPE_FILE);
  const superTypesMap = parseEnumConstants(SUPERTYPE_FILE);
  const genCtx = { keywordRules, subTypesMap, superTypesMap };

  const targets = [];
  let targetSetCode = opts.set ? String(opts.set).toUpperCase() : null;

  if (opts.card) {
    const card = await fetchScryfallCardByName(String(opts.card), targetSetCode);
    targetSetCode ??= card.set.toUpperCase();
    targets.push(card);
  } else if (targetSetCode) {
    const cards = (await loadScryfallSet(targetSetCode, !!opts.refresh)).filter((c) => isPlayableScryfallCard(c));
    const seen = new Set();
    for (const card of cards) {
      if (resolveImplemented(card, state.implementedByName)) continue;
      const norm = normalizeName(card.name);
      if (seen.has(norm)) continue;
      seen.add(norm);
      targets.push(card);
    }
  } else {
    throw new Error('scaffold requires --card "<Name>" or --set <CODE>');
  }

  const generated = [];
  for (const card of targets) {
    const spec = generateCardJava(card, genCtx);
    if (opts['auto-only'] && !spec.isAutoReady) continue;
    generated.push({ card, spec });
  }

  if (generated.length === 0) {
    console.log('No matching cards to scaffold.');
    return;
  }

  if (!opts.write) {
    console.log(`Dry run (${generated.length} card(s)); pass --write to create Java file(s).`);
    for (const { spec } of generated.slice(0, 3)) {
      console.log(`\n// === ${spec.relPath} (autoReady=${spec.isAutoReady}) ===`);
      console.log(spec.javaSource);
    }
    return;
  }

  const setRegistrations = [];
  for (const { card, spec } of generated) {
    if (existsSync(spec.absPath) && !opts.force) {
      console.log(`Skipped existing ${spec.relPath} (use --force to overwrite)`);
    } else {
      mkdirSync(dirname(spec.absPath), { recursive: true });
      writeFileSync(spec.absPath, spec.javaSource, 'utf8');
      console.log(`Wrote ${spec.relPath} (${spec.isAutoReady ? 'auto-ready' : 'has TODOs'})`);
    }
    setRegistrations.push({
      name: card.name,
      collectorNumber: card.collector_number,
      rarity: card.rarity,
      classFqn: spec.classFqn,
    });
    if (opts['with-test']) {
      const testSpec = generateTestJava(card, targetSetCode);
      if (!existsSync(testSpec.absPath) || opts.force) {
        mkdirSync(dirname(testSpec.absPath), { recursive: true });
        writeFileSync(testSpec.absPath, testSpec.source, 'utf8');
        console.log(`Wrote ${testSpec.relPath}`);
      }
    }
  }

  if (opts.register && targetSetCode) {
    const setInfo = state.setsByCode.get(targetSetCode);
    if (!setInfo) {
      console.error(`Set ${targetSetCode} not registered in Mage.Sets/src/mage/sets/; skipped --register.`);
    } else {
      const freshAdditions = setRegistrations.filter((item) => !setInfo.byNumber.has(`${normalizeName(item.name)}::${String(item.collectorNumber).toLowerCase()}`));
      if (freshAdditions.length > 0) {
        const res = insertSetCardLines(setInfo, freshAdditions);
        console.log(`Registered ${res.addedCount} card(s) in ${relative(ROOT_DIR, setInfo.filePath)}.`);
      }
    }
  }
}

async function main() {
  const opts = parseArgs(process.argv.slice(2));
  if (!opts.command || opts.help) {
    console.log([
      'Usage:',
      '  node Utils/scryfall-cards.mjs audit --set <CODE> [--refresh]',
      '  node Utils/scryfall-cards.mjs audit [--format commander|standard|pioneer|modern|legacy|pauper] [--limit 25]',
      '  node Utils/scryfall-cards.mjs sync-printings --set <CODE> [--write]',
      '  node Utils/scryfall-cards.mjs scaffold --card "<Name>" [--set <CODE>] [--write] [--register] [--with-test] [--force]',
      '  node Utils/scryfall-cards.mjs scaffold --set <CODE> [--auto-only] [--write] [--register] [--force]',
    ].join('\n'));
    return;
  }

  const state = scanMageSets();
  switch (opts.command) {
    case 'audit':
      await runAudit(opts, state);
      break;
    case 'sync-printings':
      await runSyncPrintings(opts, state);
      break;
    case 'scaffold':
      await runScaffold(opts, state);
      break;
    default:
      throw new Error(`Unknown command: ${opts.command}`);
  }
}

main().catch((error) => {
  console.error(error instanceof Error ? error.message : String(error));
  process.exit(1);
});
