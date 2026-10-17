#!/usr/bin/env node
import { spawn as spawnProcess } from 'node:child_process';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { create, cleanup } from './workspace.mjs';
import { LintError } from './lint.mjs';
import { renderExploration, report } from './render.mjs';

export const help = `Design exploration helper (Node.js 22+, Google Chrome for render).

node <skill>/gallery/cli.mjs create --project <repo> [--title "..."] [--starter html|android]
  Copy a starter into <repo>/design-explorations/ and print the exploration path.
  html (default): the HTML proposal starter. Proposals are folders:
  <exploration>/project/proposals/<id>/index.html plus its files.
  android: the Compose starter, for implementation sandboxes and the experimental Wear shell.
node <skill>/gallery/cli.mjs render <exploration> [id ...] [--open]
  Lint, then render each proposal (default: every folder in project/proposals/) headlessly in Chrome.
  Lint errors print as file:line and stop the render (exit 2). Writes, under <exploration>/renders/:
    contact-sheet.png      one row per proposal; show this to the user in chat
    <id>/<view>.png        light, dark, wallpaper, foldable, tablet, desktop
    <id>/<state>-light.png, <state>-tablet.png   each state in the proposal's p-states, opened by window.proposalState
    <id>/checks.json       window.runtimeChecks() result of every view
    <id>/view.html         one self-contained file: double-click to try theme colors and device sizes
  --open: after the render finishes, open each rendered view.html in the default browser.
  Set CHROME_PATH when Chrome is not at its usual location.
node <skill>/gallery/cli.mjs cleanup <exploration> --project <repo> --yes
  Delete a marked exploration after the user confirms.`;

export function parse(argv) {
  const { values, positionals } = parseArgs({ args: argv, options: { help: { type: 'boolean', short: 'h' }, project: { type: 'string' }, title: { type: 'string' }, starter: { type: 'string' }, yes: { type: 'boolean' }, open: { type: 'boolean' } }, allowPositionals: true });
  const [command, ...rest] = positionals;
  return { values, command, rest };
}

// The platform's "open with the default app" command for a file.
export function openCommand(file, platform = process.platform) {
  if (platform === 'darwin') return ['open', [file]];
  // explorer.exe opens the file with its default app without cmd parsing the path (& ^ % stay literal).
  if (platform === 'win32') return ['explorer.exe', [file]];
  return ['xdg-open', [file]];
}

// Opens every rendered proposal's view.html in the default browser, detached so the CLI can exit. Returns the paths.
export function openViews(result, { platform = process.platform, spawn = spawnProcess } = {}) {
  return result.proposals.map(({ dir }) => {
    const file = join(dir, 'view.html');
    const [command, args] = openCommand(file, platform);
    const child = spawn(command, args, { detached: true, stdio: 'ignore' });
    child.on('error', error => console.error(`Could not open ${file}: ${error.message}`));
    child.unref();
    return file;
  });
}

async function main(argv) {
  const { values, command, rest } = parse(argv);
  if (values.help || !command) console.log(help);
  else if (command === 'create' && rest.length === 0) console.log(create({ root: values.project, title: values.title, starter: values.starter }));
  else if (command === 'render' && rest.length >= 1) {
    const result = await renderExploration(rest[0], rest.slice(1));
    console.log(report(result));
    if (values.open) for (const file of openViews(result)) console.log(`Opening ${file}`);
  }
  else if (command === 'cleanup' && rest.length === 1) {
    if (!values.yes) throw new Error('Cleanup deletes proposals and renders; confirm with the user, then pass --yes');
    cleanup(rest[0], values.project);
    console.log(`Removed ${rest[0]}`);
  } else throw new Error(help);
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  main(process.argv.slice(2)).catch(error => {
    console.error(error.message);
    process.exitCode = error instanceof LintError ? 2 : 1;
  });
}
