import { createHash, randomUUID } from 'node:crypto';
import { readFileSync, writeFileSync, mkdirSync, renameSync, rmSync, readdirSync, lstatSync, realpathSync, existsSync, openSync, fsyncSync, closeSync } from 'node:fs';
import { resolve, join, relative, isAbsolute, sep, dirname } from 'node:path';

const MARKER = '.android-design-exploration.json';
const SKIP = new Set(['build', '.gradle', '.kotlin', '.git', 'renders', 'local.properties', '__pycache__', 'screenshotTestDebug', 'node_modules', '.DS_Store']);
const PROPOSAL_ID = /^[a-z][a-z0-9-]{0,39}$/;
const hash = bytes => createHash('sha256').update(bytes).digest('hex');
export class GalleryError extends Error {
  constructor(message, status = 400) { super(message); this.status = status; }
}
function requireThat(condition, message, status) {
  if (!condition) throw new GalleryError(message, status);
}
function string(value, label, max = 200) {
  requireThat(typeof value === 'string' && value.trim().length > 0 && value.length <= max, `Invalid ${label}`);
  return value;
}
function inside(root, path) {
  requireThat(typeof path === 'string' && path && !isAbsolute(path), 'Expected relative path inside exploration');
  const full = resolve(root, path);
  const rel = relative(root, full);
  requireThat(rel && rel !== '..' && !rel.startsWith(`..${sep}`) && !isAbsolute(rel), 'Path must stay inside exploration');
  let current = root;
  for (const part of rel.split(sep)) {
    current = join(current, part);
    if (existsSync(current) || (() => { try { lstatSync(current); return true; } catch { return false; } })()) {
      requireThat(!lstatSync(current).isSymbolicLink(), 'Refusing symlink path');
    }
  }
  return full;
}
function readJSON(path) {
  try { return JSON.parse(readFileSync(path, 'utf8')); }
  catch { throw new GalleryError(`Cannot read valid JSON: ${path}`, 422); }
}
function atomicJSON(path, value) {
  const temporary = join(dirname(path), `.write-${randomUUID()}`);
  let fd;
  try {
    fd = openSync(temporary, 'wx', 0o600);
    writeFileSync(fd, `${JSON.stringify(value, null, 2)}\n`);
    fsyncSync(fd);
    closeSync(fd); fd = undefined;
    renameSync(temporary, path);
  } finally {
    if (fd !== undefined) closeSync(fd);
    rmSync(temporary, { force: true });
  }
}
function locked(root, action) {
  const lock = inside(root, '.gallery-write-lock');
  try { mkdirSync(lock); }
  catch (error) {
    if (error.code !== 'EEXIST') throw error;
    throw new GalleryError('Another writer is active. Retry; after a crashed writer, remove .gallery-write-lock only when no writer is running.', 409);
  }
  try { return action(); } finally { rmSync(lock, { recursive: true }); }
}
function skipped(name) {
  return SKIP.has(name) || name.startsWith('.env') || /\.(pem|key|keystore|jks)$/.test(name);
}
// Regular files of a proposal folder with their bytes. Build caches and secrets are skipped; symlinks and special files are refused.
function portableFiles(root, prefix = '') {
  return readdirSync(join(root, prefix), { withFileTypes: true }).sort((a, b) => a.name.localeCompare(b.name)).flatMap(entry => {
    if (skipped(entry.name)) return [];
    const path = prefix ? `${prefix}/${entry.name}` : entry.name;
    requireThat(!entry.isSymbolicLink(), 'Refusing symlink in proposal source');
    if (entry.isDirectory()) return portableFiles(root, path);
    requireThat(entry.isFile(), 'Proposal source must contain regular files');
    const bytes = readFileSync(join(root, path));
    return [{ path, sha256: hash(bytes), bytes }];
  });
}
const listed = files => files.map(({ path, sha256 }) => ({ path, sha256 }));

// Strict styling lint. Each rule keeps a proposal a lookup into Compose tokens (see assets/proposal-starter/README.md).
const NAMED_COLORS = ('aliceblue antiquewhite aqua aquamarine azure beige bisque black blanchedalmond blue blueviolet brown burlywood cadetblue chartreuse chocolate coral cornflowerblue cornsilk crimson cyan darkblue darkcyan darkgoldenrod darkgray darkgreen darkgrey darkkhaki darkmagenta darkolivegreen darkorange darkorchid darkred darksalmon darkseagreen darkslateblue darkslategray darkslategrey darkturquoise darkviolet deeppink deepskyblue dimgray dimgrey dodgerblue firebrick floralwhite forestgreen fuchsia gainsboro ghostwhite gold goldenrod gray green greenyellow grey honeydew hotpink indianred indigo ivory khaki lavender lavenderblush lawngreen lemonchiffon lightblue lightcoral lightcyan lightgoldenrodyellow lightgray lightgreen lightgrey lightpink lightsalmon lightseagreen lightskyblue lightslategray lightslategrey lightsteelblue lightyellow lime limegreen linen magenta maroon mediumaquamarine mediumblue mediumorchid mediumpurple mediumseagreen mediumslateblue mediumspringgreen mediumturquoise mediumvioletred midnightblue mintcream mistyrose moccasin navajowhite navy oldlace olive olivedrab orange orangered orchid palegoldenrod palegreen paleturquoise palevioletred papayawhip peachpuff peru pink plum powderblue purple rebeccapurple red rosybrown royalblue saddlebrown salmon sandybrown seagreen seashell sienna silver skyblue slateblue slategray slategrey snow springgreen steelblue tan teal thistle tomato turquoise violet wheat white whitesmoke yellow yellowgreen').split(' ');
const COLOR_WORD = new RegExp(`(?<![\\w-])(?:${NAMED_COLORS.join('|')})(?![\\w-])`, 'i');
const COLOR_FUNCTION = /(?<![\w-])(?:rgba?|hsla?|hwb|lab|lch|oklab|oklch|color)\(/i;
const HEX_COLOR = /#[0-9a-f]{3,8}(?![\w-])/i;
// The bundled typefaces (runtime-src/fonts.json, the same list the runtime fonts check reads). A brand face is for headline small (24px) and up,
// Doto for display small (36px) and up, Google Sans Code for numbers and data at any size, Google Sans Flex everywhere.
const FONTS = JSON.parse(readFileSync(new URL('./runtime-src/fonts.json', import.meta.url), 'utf8'));
const BUNDLED = [FONTS.plain, ...FONTS.faces.map(face => face.family)];
// Every Material Symbols name (public/runtime/symbols.json, emitted by the runtime build from the pinned `material-symbols` package).
// <p-icon name> draws the name as a font ligature, so an unknown name would render as plain text; publishing rejects it instead.
const SYMBOLS = new Set(JSON.parse(readFileSync(new URL('./public/runtime/symbols.json', import.meta.url), 'utf8')));
const TYPE_PROPERTIES = new Set(['font', 'font-size', 'font-weight', 'line-height', 'letter-spacing', 'font-variation-settings']);
const blank = text => text.replace(/[^\n]/g, ' ');
const lineAt = (text, index) => text.slice(0, index).split('\n').length;

function lintCss(file, source) {
  const fail = (index, message) => { throw new GalleryError(`${file}:${lineAt(source, index)}: ${message}`, 422); };
  const text = source.replace(/\/\*[\s\S]*?\*\//g, blank);
  let staticStart = -1, staticEnd = -1;
  const marker = /\/\*\s*static colors\s*\*\//.exec(source);
  if (marker) {
    const open = text.indexOf('{', marker.index);
    let depth = 0, close = -1;
    for (let i = open; open >= 0 && i < text.length; i++) {
      if (text[i] === '{') depth++;
      else if (text[i] === '}' && --depth === 0) { close = i; break; }
    }
    if (open >= 0 && close >= 0) { staticStart = open; staticEnd = close; }
  }
  const stack = [];
  const voices = new Map(); // selector -> { family, min, at, sizes } across every rule that styles that voice
  const frames = []; // one per open block: the family and font-size declared directly in it
  let start = 0;
  const voice = selector => {
    if (!voices.has(selector)) voices.set(selector, { min: 0, sizes: [] });
    return voices.get(selector);
  };
  const declaration = (end) => {
    const raw = text.slice(start, end);
    const colon = raw.indexOf(':');
    if (!stack.length || colon < 0) return;
    const property = raw.slice(0, colon).trim().toLowerCase();
    const value = raw.slice(colon + 1).replace(/url\([^)]*\)/gi, '').replace(/"[^"]*"|'[^']*'/g, '');
    const at = start + raw.search(/\S/);
    const inStatic = start > staticStart && end <= staticEnd;
    if (inStatic) {
      if (!/^--static-[\w-]+$/.test(property)) fail(at, 'The /* static colors */ block may only declare --static-* custom properties');
      return;
    }
    if (HEX_COLOR.test(value) || COLOR_FUNCTION.test(value) || COLOR_WORD.test(value)) {
      fail(at, `Raw color in ${property}. Use var(--md-sys-color-*), or declare a named color once in a /* static colors */ block as --static-*`);
    }
    // The theme emits primary, secondary, tertiary, and error *-dim roles that Compose's ColorScheme lacks; a proposal using one could not be ported (see compose-mapping.md).
    const dim = /--md-sys-color-((?:primary|secondary|tertiary|error)-dim)\b/.exec(value);
    if (dim) fail(at, `${dim[1]} has no Compose counterpart. Use another --md-sys-color-* role`);
    const rule = [...stack].reverse().find(prelude => !prelude.startsWith('@'));
    const isVoice = rule && /^\.voice-[\w-]+$/.test(rule);
    if (property === 'font-family') {
      if (!isVoice) fail(at, 'font-family is not allowed outside a single-class .voice-* rule. Plain text uses Google Sans Flex from the runtime');
      const names = raw.slice(colon + 1).split(',').map(name => name.trim().replace(/^(["'])(.*)\1$/, '$2'));
      const named = names.filter(name => !FONTS.generics.includes(name.toLowerCase()));
      const unknown = named.find(name => !BUNDLED.includes(name));
      if (unknown !== undefined) fail(at, `font-family is not allowed: "${unknown}" is not bundled. Use ${BUNDLED.join(', ')}, optionally followed by ${FONTS.generics.join(', ')} as a fallback`);
      if (named.length !== 1) fail(at, `font-family is not allowed: a voice names exactly one bundled family (got ${named.length}). Use ${BUNDLED.join(', ')}`);
      const face = FONTS.faces.find(item => item.family === named[0]);
      if (face?.minPx) {
        const info = voice(rule);
        info.min = face.minPx; info.family = face.family; info.use = face.use; info.at = at;
        frames.at(-1).needsSize = info;
      }
    }
    if (property === 'font-size' && isVoice) {
      const px = /^\s*(\d+(?:\.\d+)?)px\s*$/.exec(value);
      voice(rule).sizes.push({ px: px ? Number(px[1]) : null, at });
      frames.at(-1).sized = true;
    }
    if (TYPE_PROPERTIES.has(property) && !isVoice) fail(at, `${property} is only allowed in a single-class .voice-* rule. Use a .t-* type role class instead`);
    if (property === 'font' && isVoice) fail(at, 'The font shorthand is not allowed. Set font-size, font-weight, line-height, and font-variation-settings separately');
  };
  for (let i = 0; i < text.length; i++) {
    const char = text[i];
    if (char === '{') { stack.push(text.slice(start, i).trim().replace(/\s+/g, ' ')); frames.push({}); start = i + 1; }
    else if (char === ';') { declaration(i); start = i + 1; }
    else if (char === '}') {
      declaration(i);
      const frame = frames.pop();
      if (frame?.needsSize && !frame.sized) fail(frame.needsSize.at, `${frame.needsSize.family} needs a font-size of at least ${frame.needsSize.min}px in the same rule (${frame.needsSize.use === 'display' ? 'display small and up' : 'headline small and up'})`);
      stack.pop(); start = i + 1;
    }
  }
  for (const info of voices.values()) {
    if (!info.min) continue;
    for (const { px, at } of info.sizes) {
      if (px === null) fail(at, `${info.family} voices need a font-size in px so the minimum can be checked`);
      if (px < info.min) fail(at, `${info.family} is only for ${info.use === 'display' ? 'display small (36px)' : 'headline small (24px)'} and up. This voice sets font-size ${px}px; the minimum is ${info.min}px. Use Google Sans Flex for smaller text`);
    }
  }
}

function lintHtml(file, source) {
  const text = source.replace(/<!--[\s\S]*?-->/g, blank);
  const fail = (index, message) => { throw new GalleryError(`${file}:${lineAt(text, index)}: ${message}`, 422); };
  for (const tag of text.matchAll(/<([a-zA-Z][\w-]*)\b[^>]*>/g)) {
    const name = tag[1].toLowerCase();
    if (name === 'style') fail(tag.index, 'Inline <style> is not allowed. Put styles in a .css file');
    if (name === 'script' && !/\ssrc\s*=/i.test(tag[0])) fail(tag.index, 'Inline <script> is not allowed. Use <script src="...">');
    if (name === 'p-icon') {
      const icon = /\sname\s*=\s*(?:"([^"]*)"|'([^']*)')/i.exec(tag[0]);
      const symbol = icon ? icon[1] ?? icon[2] : undefined;
      if (symbol === undefined) fail(tag.index, '<p-icon> needs a quoted name="..." attribute that is a Material Symbols name from fonts.google.com/icons (name=home without quotes is not accepted)');
      else if (!SYMBOLS.has(symbol)) fail(tag.index, `<p-icon name="${symbol}"> is not a Material Symbols name. Pick one from fonts.google.com/icons, written in snake_case`);
    }
    const attribute = /\sstyle\s*=/i.exec(tag[0]);
    if (attribute) fail(tag.index + attribute.index, 'style="" attributes are not allowed. Put styles in a .css file');
  }
}

function lintProposal(id, files) {
  for (const file of files) {
    const label = `proposals/${id}/${file.path}`;
    if (/\.css$/i.test(file.path)) lintCss(label, file.bytes.toString('utf8'));
    else if (/\.html?$/i.test(file.path)) lintHtml(label, file.bytes.toString('utf8'));
  }
}

export class GalleryStore {
  constructor(root) {
    requireThat(!lstatSync(resolve(root)).isSymbolicLink(), 'Refusing symlink exploration');
    this.root = realpathSync(root);
    const marker = readJSON(inside(this.root, MARKER));
    requireThat(marker.version === 1 && marker.path === this.root, 'Exploration marker does not match');
  }
  #file(name) { return inside(this.root, name); }
  #manifest() {
    const value = readJSON(this.#file('exploration.json'));
    requireThat(value.version === 1 && Array.isArray(value.rounds), 'Invalid exploration manifest', 422);
    string(value.title, 'exploration title');
    const ids = new Set();
    for (const id of value.rounds) {
      requireThat(typeof id === 'string' && /^\d{3,}$/.test(id) && !ids.has(id), 'Invalid round reference', 422);
      ids.add(id);
    }
    return value;
  }
  #feedback() {
    const value = readJSON(this.#file('feedback.json'));
    requireThat(Number.isSafeInteger(value.revision) && value.revision >= 0 && value.rounds && typeof value.rounds === 'object' && !Array.isArray(value.rounds) && (value.approved === null || typeof value.approved === 'object'), 'Invalid feedback file', 422);
    return value;
  }
  read() {
    const manifest = this.#manifest();
    const rounds = manifest.rounds.map(id => {
      const round = readJSON(this.#file(`rounds/${id}/round.json`));
      requireThat(round.id === id && Array.isArray(round.proposals), 'Invalid round record', 422);
      return round;
    });
    const data = { title: manifest.title, rounds, feedback: this.#feedback() };
    return { ...data, pending: pendingRequests(data), session: this.#session() };
  }
  // Session status is advisory and expires on its own, so a missing, corrupt, or
  // stale file reads as no session instead of breaking the gallery.
  #session() {
    try {
      const value = JSON.parse(readFileSync(this.#file('session.json'), 'utf8'));
      const valid = value && ['waiting', 'working'].includes(value.state) && Number.isFinite(Date.parse(value.since)) && Number.isFinite(Date.parse(value.until));
      return valid && Date.parse(value.until) > Date.now() ? { state: value.state, since: value.since, until: value.until } : null;
    } catch { return null; }
  }
  // Session writes skip the feedback write lock and never touch feedback.json.
  writeSession(state, until) {
    requireThat(['waiting', 'working'].includes(state), 'Invalid session state');
    requireThat(until instanceof Date && Number.isFinite(until.getTime()), 'Invalid session expiry');
    atomicJSON(this.#file('session.json'), { state, since: new Date().toISOString(), until: until.toISOString() });
  }
  clearSession() { rmSync(this.#file('session.json'), { force: true }); }
  proposal(key) {
    requireThat(typeof key === 'string', 'Unknown proposal');
    const [roundId, id, extra] = key.split('/');
    requireThat(!extra, 'Unknown proposal');
    const round = this.read().rounds.find(r => r.id === roundId);
    const proposal = round?.proposals.find(p => p.id === id);
    requireThat(proposal, 'Unknown proposal', 404);
    return { round, proposal };
  }
  #verifyRound(round) {
    for (const proposal of round.proposals) {
      const files = listed(portableFiles(this.#file(`rounds/${round.id}/proposals/${proposal.id}`)));
      requireThat(JSON.stringify(files) === JSON.stringify(proposal.files), 'Published proposal changed; do not approve stale files', 409);
    }
  }
  // A published file, verified against round.json on every read.
  publishedFile(roundId, proposalId, path) {
    const { proposal } = this.proposal(`${roundId}/${proposalId}`);
    const file = proposal.files.find(f => f.path === (path || proposal.entry));
    requireThat(file, 'Unknown file', 404);
    const bytes = readFileSync(this.#file(`rounds/${roundId}/proposals/${proposalId}/${file.path}`));
    requireThat(hash(bytes) === file.sha256, 'Published file changed', 409);
    return { path: file.path, bytes };
  }
  // A working draft file for the agent's review; no hash check.
  draftFile(proposalId, path) {
    requireThat(PROPOSAL_ID.test(proposalId), 'Unknown file', 404);
    const parts = (path || 'index.html').split('/');
    requireThat(parts.every(part => part && part !== '.' && part !== '..' && !part.includes('\\') && !part.includes('\0') && !skipped(part)), 'Unknown file', 404);
    let full;
    try { full = inside(this.root, `project/proposals/${proposalId}/${parts.join('/')}`); }
    catch { throw new GalleryError('Unknown file', 404); }
    let stat;
    try { stat = lstatSync(full); } catch { throw new GalleryError('Unknown file', 404); }
    requireThat(stat.isFile(), 'Unknown file', 404);
    return { path: parts.join('/'), bytes: readFileSync(full) };
  }
  #update(revision, action) {
    return locked(this.root, () => {
      const state = this.#feedback();
      requireThat(state.approved === null, 'Exploration is already approved', 409);
      requireThat(Number.isSafeInteger(revision) && revision === state.revision, 'Feedback changed in another tab. Reload before retrying.', 409);
      action(state);
      state.revision += 1;
      atomicJSON(this.#file('feedback.json'), state);
      return state;
    });
  }
  #round(id) {
    const round = this.read().rounds.find(round => round.id === id);
    requireThat(round, 'Unknown round', 404);
    return round;
  }
  save({ revision, roundId, choice, note }) {
    const round = this.#round(roundId);
    requireThat(choice === null || choice === 'none' || round.proposals.some(p => choice === roundId + '/' + p.id), 'Choose a proposal from this round');
    requireThat(typeof note === 'string' && note.length <= 10000, 'Invalid feedback');
    return this.#update(revision, state => {
      const requests = state.rounds[roundId]?.requests || [];
      state.rounds[roundId] = { choice, note, requests };
    });
  }
  refine({ revision, roundId }) {
    this.#round(roundId);
    return this.#update(revision, state => {
      const entry = state.rounds[roundId];
      requireThat(entry && entry.choice !== null, 'Select a design or None of these');
      requireThat(entry.choice !== 'none' || entry.note.trim(), 'Describe what to try instead');
      entry.requests.push({ choice: entry.choice, note: entry.note, at: new Date().toISOString() });
    });
  }
  approve({ revision, key }) {
    const { round, proposal } = this.proposal(key);
    return this.#update(revision, state => {
      requireThat(state.rounds[round.id]?.choice === key, 'Finish with the selected design', 409);
      this.#verifyRound(round);
      state.approved = { key, name: proposal.name, roundId: round.id, sourceHash: round.sourceHash, at: new Date().toISOString() };
    });
  }
  publish(draft) {
    return locked(this.root, () => {
      const feedback = this.#feedback();
      requireThat(feedback.approved === null, 'Exploration is already approved', 409);
      requireThat(draft && typeof draft === 'object' && !Array.isArray(draft), 'Invalid draft');
      string(draft.title, 'round title');
      requireThat(Array.isArray(draft.proposals) && draft.proposals.length > 0 && draft.proposals.length <= 12, 'Provide 1 to 12 proposals');
      requireThat(!('images' in draft), 'Drafts no longer take images; each proposal is the folder project/proposals/<id>/');
      const answers = this.#answers(draft.answers, feedback);
      const manifest = this.#manifest();
      const id = String(manifest.rounds.length + 1).padStart(3, '0');
      const destination = this.#file(`rounds/${id}`);
      requireThat(!existsSync(destination), 'Round already exists; refusing to overwrite', 409);
      const ids = new Set();
      const snapshots = draft.proposals.map(proposal => {
        requireThat(proposal && typeof proposal === 'object', 'Invalid proposal');
        requireThat(typeof proposal.id === 'string' && PROPOSAL_ID.test(proposal.id), 'Invalid proposal id');
        requireThat(!ids.has(proposal.id), 'Duplicate proposal id'); ids.add(proposal.id);
        string(proposal.name, 'proposal name'); string(proposal.rationale, 'rationale', 1000);
        requireThat(!('images' in proposal), `Proposal ${proposal.id}: drafts no longer take images; the proposal is the folder project/proposals/${proposal.id}/`);
        let folder;
        try { folder = this.#file(`project/proposals/${proposal.id}`); lstatSync(folder); }
        catch (error) { if (error instanceof GalleryError) throw error; throw new GalleryError(`Missing proposal folder project/proposals/${proposal.id}/`); }
        requireThat(lstatSync(folder).isDirectory(), `project/proposals/${proposal.id} must be a folder`);
        const files = portableFiles(folder);
        requireThat(files.some(f => f.path === 'index.html'), `Proposal ${proposal.id} needs project/proposals/${proposal.id}/index.html`);
        lintProposal(proposal.id, files);
        return { proposal, files };
      });
      const proposals = snapshots.map(({ proposal, files }) => ({ id: proposal.id, name: proposal.name, rationale: proposal.rationale, entry: 'index.html', files: listed(files) }));
      const staging = this.#file(`.publish-${randomUUID()}`);
      try {
        for (const { proposal, files } of snapshots) {
          for (const file of files) {
            const target = join(staging, 'proposals', proposal.id, file.path);
            mkdirSync(dirname(target), { recursive: true });
            writeFileSync(target, file.bytes, { mode: 0o644 });
          }
        }
        const sourceHash = hash(JSON.stringify(proposals.map(({ id, files }) => ({ id, files }))));
        const round = { id, title: draft.title, createdAt: new Date().toISOString(), sourceHash, proposals, ...(answers && { answers }) };
        atomicJSON(join(staging, 'round.json'), round);
        mkdirSync(this.#file('rounds'), { recursive: true });
        renameSync(staging, destination);
        try { atomicJSON(this.#file('exploration.json'), { ...manifest, rounds: [...manifest.rounds, id] }); }
        catch (error) { rmSync(destination, { recursive: true }); throw error; }
        return round;
      } finally { rmSync(staging, { recursive: true, force: true }); }
    });
  }
  #answers(value, feedback) {
    if (value === undefined) return undefined;
    requireThat(Array.isArray(value) && value.length <= 50, 'Invalid answers');
    const seen = new Set();
    const manifest = this.#manifest();
    return value.map(answer => {
      requireThat(answer && typeof answer === 'object' && typeof answer.roundId === 'string' && typeof answer.at === 'string', 'Invalid answer');
      const entry = manifest.rounds.includes(answer.roundId) ? feedback.rounds[answer.roundId] : undefined;
      requireThat(entry, `Unknown answered round: ${answer.roundId}`);
      requireThat(Array.isArray(entry.requests) && entry.requests.some(request => request.at === answer.at), `Unknown answered request: ${answer.roundId} ${answer.at}`);
      const id = `${answer.roundId} ${answer.at}`;
      requireThat(!seen.has(id), 'Duplicate answer'); seen.add(id);
      return { roundId: answer.roundId, at: answer.at };
    });
  }
}

export function initializeExploration(root, title) {
  requireThat(typeof title === 'string' && title.trim().length > 0 && title.length <= 200, 'Title must contain 1 to 200 characters');
  atomicJSON(join(root, MARKER), { version: 1, path: root });
  atomicJSON(join(root, 'exploration.json'), { version: 1, title, rounds: [] });
  atomicJSON(join(root, 'feedback.json'), { revision: 0, rounds: {}, approved: null });
}

export function pendingRequests(data) {
  const unanswered = data.rounds.filter(round => !round.answers);
  const named = new Set(data.rounds.flatMap(round => (round.answers || []).map(a => `${a.roundId}\n${a.at}`)));
  return Object.entries(data.feedback.rounds)
    .flatMap(([roundId, entry]) => (entry.requests || []).map(request => ({ roundId, choice: request.choice, note: request.note, at: request.at })))
    .filter(request => !named.has(`${request.roundId}\n${request.at}`) && !unanswered.some(round => Date.parse(round.createdAt) > Date.parse(request.at)))
    .sort((a, b) => Date.parse(b.at) - Date.parse(a.at));
}
