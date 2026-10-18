#!/usr/bin/env node
// Render screenshotTest previews for one surface, without launching an emulator.
import { spawnSync } from 'node:child_process';
import { copyFileSync, existsSync, lstatSync, mkdirSync, readdirSync, rmSync } from 'node:fs';
import { homedir } from 'node:os';
import { dirname, join, relative, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const SURFACES = ['app'];

function isSymlink(path) {
  try { return lstatSync(path).isSymbolicLink(); } catch { return false; }
}

function pngs(directory) {
  return readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
    const path = join(directory, entry.name);
    if (entry.isDirectory()) return pngs(path);
    return entry.name.endsWith('.png') ? [path] : [];
  }).sort();
}

export function collect(source, destination) {
  if (isSymlink(destination)) throw new Error(`Refusing symlink output: ${destination}`);
  const images = existsSync(source) ? pngs(source) : [];
  if (!images.length) throw new Error(`No screenshot previews found in ${source}`);
  mkdirSync(destination, { recursive: true });
  for (const image of images) {
    const target = join(destination, relative(source, image));
    mkdirSync(dirname(target), { recursive: true });
    copyFileSync(image, target);
    console.log(target);
  }
}

function hasSymlinkParent(path, root) {
  for (let parent = dirname(path); parent !== dirname(root) && parent !== dirname(parent); parent = dirname(parent)) {
    if (isSymlink(parent)) return true;
  }
  return false;
}

export function main(argv = process.argv.slice(2), { root = dirname(fileURLToPath(import.meta.url)), run = spawnSync } = {}) {
  const [surface, ...extra] = argv;
  if (!SURFACES.includes(surface) || extra.length) {
    console.error(`Usage: node render.mjs ${SURFACES.join('|')}`);
    return 2;
  }
  const output = join(root, 'renders', surface);
  const references = join(root, surface, 'src/screenshotTestDebug/reference');
  try {
    // Clear only generated outputs, before Gradle, so failures cannot show stale proposals.
    for (const path of [output, references]) {
      if (isSymlink(path) || hasSymlinkParent(path, root)) throw new Error(`Refusing symlink output path: ${path}`);
      rmSync(path, { recursive: true, force: true });
    }
    const env = { ...process.env };
    const sdk = join(homedir(), 'Library/Android/sdk');
    if (!env.ANDROID_HOME && !env.ANDROID_SDK_ROOT && existsSync(sdk)) env.ANDROID_HOME = sdk;
    const result = run(join(root, 'gradlew'), ['--no-daemon', '--console=plain', `:${surface}:updateDebugScreenshotTest`], { cwd: root, env, stdio: 'inherit' });
    if (result.error) throw result.error;
    if (result.status !== 0) throw new Error(`Gradle exited with status ${result.status ?? result.signal}`);
    collect(references, output);
  } catch (error) {
    console.error(`Render failed; visual quality unverified: ${error.message}`);
    return 1;
  }
  return 0;
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) process.exitCode = main();
