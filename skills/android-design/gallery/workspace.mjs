import { spawnSync } from 'node:child_process';
import { appendFileSync, cpSync, existsSync, lstatSync, mkdirSync, mkdtempSync, readFileSync, realpathSync, rmSync, statSync, writeFileSync } from 'node:fs';
import { basename, dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

export const PREFIX = 'android-design-explore-';
export const MARKER = '.android-design-exploration.json';
export const DIRECTORY = 'design-explorations';
const ASSETS = resolve(dirname(fileURLToPath(import.meta.url)), '../assets');
export const TEMPLATE = join(ASSETS, 'proposal-starter');
export const ANDROID_TEMPLATE = join(ASSETS, 'exploration-starter');
// Each starter names the file that proves a copy source is the right folder.
const STARTERS = { html: [TEMPLATE, 'proposals/a/index.html'], android: [ANDROID_TEMPLATE, 'settings.gradle.kts'] };
const SKIP = new Set(['build', '.gradle', '.kotlin', '.git', 'renders', 'local.properties', '__pycache__', 'screenshotTestDebug']);

function git(root, ...args) {
  const result = spawnSync('git', ['-C', root, ...args], { encoding: 'utf8' });
  if (result.error || result.status !== 0) {
    throw new Error(`Exploration requires an existing Git repository: ${(result.stderr || result.error?.message || '').trim()}`);
  }
  return result.stdout.trim();
}

function repository(root) {
  return realpathSync(git(realpathSync(resolve(root ?? process.cwd())), 'rev-parse', '--show-toplevel'));
}

function isSymlink(path) {
  try { return lstatSync(path).isSymbolicLink(); } catch { return false; }
}

function storage(root) {
  const destination = join(root, DIRECTORY);
  if (isSymlink(destination)) throw new Error('Refusing symlink exploration storage');
  return destination;
}

export function create({ starter = 'html', template, root, title = 'Design exploration' } = {}) {
  if (!Object.hasOwn(STARTERS, starter)) throw new Error(`Invalid --starter: expected html or android, got ${starter}`);
  const [defaultTemplate, entryFile] = STARTERS[starter];
  template = realpathSync(resolve(template ?? defaultTemplate));
  root = repository(root);
  if (typeof title !== 'string' || !title.trim() || title.length > 200) throw new Error('Title must contain 1 to 200 characters');
  const entry = join(template, entryFile);
  if (!existsSync(entry) || !statSync(entry).isFile()) throw new Error(`Missing starter: ${template}`);
  const base = storage(root);
  if (git(root, 'ls-files', '--', DIRECTORY)) throw new Error('Exploration storage is already tracked; leave it untouched');
  // Local Git metadata keeps generated proposals out of commits without editing .gitignore.
  const exclude = git(root, 'rev-parse', '--path-format=absolute', '--git-path', 'info/exclude');
  mkdirSync(dirname(exclude), { recursive: true });
  const existing = existsSync(exclude) ? readFileSync(exclude, 'utf8') : '';
  const rule = `/${DIRECTORY}/`;
  if (!existing.split('\n').includes(rule)) appendFileSync(exclude, `${existing && !existing.endsWith('\n') ? '\n' : ''}${rule}\n`);
  mkdirSync(base, { recursive: true });
  const destination = mkdtempSync(join(base, PREFIX));
  try {
    cpSync(template, join(destination, 'project'), { recursive: true, filter: source => !SKIP.has(basename(source)) });
    writeFileSync(join(destination, MARKER), `${JSON.stringify({ version: 1, path: destination })}\n`);
    writeFileSync(join(destination, 'exploration.json'), `${JSON.stringify({ version: 1, title })}\n`);
  } catch (error) {
    rmSync(destination, { recursive: true, force: true });
    throw error;
  }
  return destination;
}

export function cleanup(path, root) {
  root = repository(root);
  if (isSymlink(path)) throw new Error('Refusing to remove a symlink');
  path = resolve(path);
  if (!existsSync(path)) throw new Error(`Not a direct exploration directory in ${join(root, DIRECTORY)}: ${path}`);
  path = realpathSync(path);
  if (dirname(path) !== storage(root) || !basename(path).startsWith(PREFIX)) {
    throw new Error(`Not a direct exploration directory in ${join(root, DIRECTORY)}: ${path}`);
  }
  const marker = join(path, MARKER);
  if (isSymlink(marker) || !existsSync(marker) || !statSync(marker).isFile()) throw new Error(`Missing exploration marker: ${path}`);
  let identity;
  try { identity = JSON.parse(readFileSync(marker, 'utf8')); }
  catch { throw new Error(`Invalid exploration marker: ${path}`); }
  if (identity?.version !== 1 || identity?.path !== path || Object.keys(identity).length !== 2) throw new Error(`Exploration marker does not match: ${path}`);
  rmSync(path, { recursive: true, force: true });
}
