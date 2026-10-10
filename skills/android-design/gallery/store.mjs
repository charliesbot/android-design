import { createHash, randomUUID } from 'node:crypto';
import { readFileSync, writeFileSync, mkdirSync, renameSync, rmSync, readdirSync, lstatSync, realpathSync, existsSync, openSync, fsyncSync, closeSync } from 'node:fs';
import { resolve, join, relative, isAbsolute, sep, dirname } from 'node:path';

export const DEVICES = ['phone', 'tablet', 'desktop', 'foldable'];
const MARKER = '.android-design-exploration.json';
const SKIP = new Set(['build', '.gradle', '.kotlin', '.git', 'renders', 'local.properties', '__pycache__', 'screenshotTestDebug', 'node_modules', '.DS_Store']);
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
export function inside(root, path) {
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
export function atomicJSON(path, value) {
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
function portableFiles(root, prefix = '') {
  return readdirSync(join(root, prefix), { withFileTypes: true }).sort((a, b) => a.name.localeCompare(b.name)).flatMap(entry => {
    if (SKIP.has(entry.name) || entry.name.startsWith('.env') || /\.(pem|key|keystore|jks)$/.test(entry.name)) return [];
    const path = prefix ? `${prefix}/${entry.name}` : entry.name;
    requireThat(!entry.isSymbolicLink(), 'Refusing symlink in proposal source');
    if (entry.isDirectory()) return portableFiles(root, path);
    requireThat(entry.isFile(), 'Proposal source must contain regular files');
    return [{ path, sha256: hash(readFileSync(join(root, path))) }];
  });
}
function png(bytes) {
  requireThat(bytes.length >= 33 && bytes.subarray(0, 8).equals(Buffer.from([137, 80, 78, 71, 13, 10, 26, 10])) && bytes.toString('ascii', 12, 16) === 'IHDR', 'Expected a rendered PNG');
  const width = bytes.readUInt32BE(16), height = bytes.readUInt32BE(20);
  requireThat(width > 0 && height > 0 && width <= 20000 && height <= 20000, 'Invalid PNG dimensions');
  return { width, height, sha256: hash(bytes) };
}

export class GalleryStore {
  constructor(root) {
    requireThat(!lstatSync(resolve(root)).isSymbolicLink(), 'Refusing symlink exploration');
    this.root = realpathSync(root);
    const marker = readJSON(inside(this.root, MARKER));
    requireThat(marker.version === 1 && marker.path === this.root, 'Exploration marker does not match');
  }
  file(name) { return inside(this.root, name); }
  manifest() {
    const value = readJSON(this.file('exploration.json'));
    requireThat(value.version === 1 && Array.isArray(value.rounds), 'Invalid exploration manifest', 422);
    string(value.title, 'exploration title');
    const ids = new Set();
    for (const id of value.rounds) {
      requireThat(typeof id === 'string' && /^\d{3,}$/.test(id) && !ids.has(id), 'Invalid round reference', 422);
      ids.add(id);
    }
    return value;
  }
  feedback() {
    const value = readJSON(this.file('feedback.json'));
    requireThat(Number.isSafeInteger(value.revision) && value.revision >= 0 && value.rounds && typeof value.rounds === 'object' && !Array.isArray(value.rounds) && (value.approved === null || typeof value.approved === 'object'), 'Invalid feedback file', 422);
    return value;
  }
  read() {
    const manifest = this.manifest();
    const rounds = manifest.rounds.map(id => {
      const round = readJSON(this.file(`rounds/${id}/round.json`));
      requireThat(round.id === id && Array.isArray(round.proposals) && Array.isArray(round.sourceFiles), 'Invalid round record', 422);
      return round;
    });
    return { title: manifest.title, rounds, feedback: this.feedback() };
  }
  proposal(key) {
    requireThat(typeof key === 'string', 'Unknown proposal');
    const [roundId, id, extra] = key.split('/');
    requireThat(!extra, 'Unknown proposal');
    const round = this.read().rounds.find(r => r.id === roundId);
    const proposal = round?.proposals.find(p => p.id === id);
    requireThat(proposal, 'Unknown proposal', 404);
    return { round, proposal };
  }
  verifyRound(round) {
    const files = portableFiles(this.file(`rounds/${round.id}/source`));
    requireThat(JSON.stringify(files) === JSON.stringify(round.sourceFiles), 'Published source changed; do not approve stale images', 409);
    for (const proposal of round.proposals) {
      for (const device of DEVICES) this.image(`${round.id}/${proposal.id}`, device);
    }
  }
  image(key, device) {
    requireThat(DEVICES.includes(device), 'Unknown device', 404);
    const { proposal } = this.proposal(key);
    const image = proposal.images[device];
    requireThat(image, 'Missing image', 422);
    const bytes = readFileSync(this.file(image.path));
    requireThat(hash(bytes) === image.sha256, 'Published image changed', 409);
    return bytes;
  }
  update(revision, action) {
    return locked(this.root, () => {
      const state = this.feedback();
      requireThat(state.approved === null, 'Exploration is already approved', 409);
      requireThat(Number.isSafeInteger(revision) && revision === state.revision, 'Feedback changed in another tab. Reload before retrying.', 409);
      action(state);
      state.revision += 1;
      atomicJSON(this.file('feedback.json'), state);
      return state;
    });
  }
  round(id) {
    const round = this.read().rounds.find(round => round.id === id);
    requireThat(round, 'Unknown round', 404);
    return round;
  }
  save({ revision, roundId, choice, note }) {
    const round = this.round(roundId);
    requireThat(choice === null || choice === 'none' || round.proposals.some(p => choice === roundId + '/' + p.id), 'Choose a proposal from this round');
    requireThat(typeof note === 'string' && note.length <= 10000, 'Invalid feedback');
    return this.update(revision, state => {
      const requests = state.rounds[roundId]?.requests || [];
      state.rounds[roundId] = { choice, note, requests };
    });
  }
  refine({ revision, roundId }) {
    this.round(roundId);
    return this.update(revision, state => {
      const entry = state.rounds[roundId];
      requireThat(entry && entry.choice !== null, 'Select a design or None of these');
      requireThat(entry.choice !== 'none' || entry.note.trim(), 'Describe what to try instead');
      entry.requests.push({ choice: entry.choice, note: entry.note, at: new Date().toISOString() });
    });
  }
  approve({ revision, key }) {
    const { round, proposal } = this.proposal(key);
    return this.update(revision, state => {
      requireThat(state.rounds[round.id]?.choice === key, 'Finish with the selected design', 409);
      this.verifyRound(round);
      state.approved = { key, name: proposal.name, roundId: round.id, sourceHash: round.sourceHash, at: new Date().toISOString() };
    });
  }
}

export function publishRound(root, draft) {
  const store = new GalleryStore(root);
  return locked(store.root, () => {
    requireThat(store.feedback().approved === null, 'Exploration is already approved', 409);
    string(draft.title, 'round title');
    requireThat(Array.isArray(draft.proposals) && draft.proposals.length > 0 && draft.proposals.length <= 12, 'Provide 1 to 12 proposals');
    const project = store.file('project');
    const sourceFiles = portableFiles(project);
    requireThat(sourceFiles.some(f => f.path === 'settings.gradle.kts'), 'Missing Compose project');
    const manifest = store.manifest();
    const id = String(manifest.rounds.length + 1).padStart(3, '0');
    const destination = store.file(`rounds/${id}`);
    requireThat(!existsSync(destination), 'Round already exists; refusing to overwrite', 409);
    const ids = new Set();
    const proposals = draft.proposals.map(proposal => {
      requireThat(typeof proposal.id === 'string' && /^[a-z][a-z0-9-]{0,39}$/.test(proposal.id), 'Invalid proposal id');
      requireThat(!ids.has(proposal.id), 'Duplicate proposal id'); ids.add(proposal.id);
      string(proposal.name, 'proposal name'); string(proposal.rationale, 'rationale', 1000);
      requireThat(proposal.images && Object.keys(proposal.images).length === 4 && DEVICES.every(d => typeof proposal.images[d] === 'string'), 'Provide exactly four images: phone, tablet, desktop, foldable');
      const images = Object.fromEntries(DEVICES.map(device => {
        const source = inside(project, proposal.images[device]);
        const bytes = readFileSync(source);
        return [device, { path: `rounds/${id}/images/${proposal.id}-${device}.png`, ...png(bytes), bytes }];
      }));
      return { id: proposal.id, name: proposal.name, rationale: proposal.rationale, images };
    });
    const staging = store.file(`.publish-${randomUUID()}`);
    try {
      mkdirSync(join(staging, 'images'), { recursive: true });
      mkdirSync(join(staging, 'source'));
      for (const file of sourceFiles) {
        const target = join(staging, 'source', file.path);
        mkdirSync(dirname(target), { recursive: true });
        const bytes = readFileSync(inside(project, file.path));
        requireThat(hash(bytes) === file.sha256, 'Source changed during publication', 409);
        writeFileSync(target, bytes, { mode: lstatSync(join(project, file.path)).mode & 0o777 });
      }
      for (const proposal of proposals) for (const image of Object.values(proposal.images)) {
        writeFileSync(join(staging, 'images', image.path.split('/').at(-1)), image.bytes);
        delete image.bytes;
      }
      const round = { id, title: draft.title, createdAt: new Date().toISOString(), sourceHash: hash(JSON.stringify(sourceFiles)), sourceFiles, proposals };
      atomicJSON(join(staging, 'round.json'), round);
      mkdirSync(store.file('rounds'), { recursive: true });
      renameSync(staging, destination);
      try { atomicJSON(store.file('exploration.json'), { ...manifest, rounds: [...manifest.rounds, id] }); }
      catch (error) { rmSync(destination, { recursive: true }); throw error; }
      return round;
    } finally { rmSync(staging, { recursive: true, force: true }); }
  });
}
