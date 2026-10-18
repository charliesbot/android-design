#!/usr/bin/env node
import { spawn as spawnProcess } from 'node:child_process';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { LintError } from './lint.mjs';
import { renderProposal, report } from './render.mjs';

export const help = `Design exploration helper (Node.js 22+, Google Chrome for render).

node <skill>/gallery/cli.mjs render <proposal-folder> [--open]
  Lint the proposal folder, then render it headlessly in Chrome. One folder is one proposal: an index.html plus its files.
  Lint errors print as file:line and stop the render (exit 2). Writes, under <proposal-folder>/renders/ (recreated each render):
    light.png, dark.png, wallpaper.png, foldable.png, tablet.png, desktop.png   the checked views
    light-full.png   the phone light view at the full document height, for your own review (not checked)
    review.png       every checked view on one image, for your own review; show this to the user in chat
    checks.json      window.runtimeChecks() result of every view
    view.html        one self-contained file: double-click to try theme colors and device sizes
  --open: after the render finishes, open renders/view.html in the default browser.
  Set CHROME_PATH when Chrome is not at its usual location.`;

export function parse(argv) {
  const { values, positionals } = parseArgs({ args: argv, options: { help: { type: 'boolean', short: 'h' }, open: { type: 'boolean' } }, allowPositionals: true });
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

// Opens the rendered view.html in the default browser, detached so the CLI can exit. Returns the path.
export function openView(result, { platform = process.platform, spawn = spawnProcess } = {}) {
  const file = join(result.dir, 'view.html');
  const [command, args] = openCommand(file, platform);
  const child = spawn(command, args, { detached: true, stdio: 'ignore' });
  child.on('error', error => console.error(`Could not open ${file}: ${error.message}`));
  child.unref();
  return file;
}

async function main(argv) {
  const { values, command, rest } = parse(argv);
  if (values.help || !command) console.log(help);
  else if (command === 'render' && rest.length === 1) {
    const result = await renderProposal(rest[0]);
    console.log(report(result));
    if (values.open) console.log(`Opening ${openView(result)}`);
  } else throw new Error(help);
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  main(process.argv.slice(2)).catch(error => {
    console.error(error.message);
    process.exitCode = error instanceof LintError ? 2 : 1;
  });
}
