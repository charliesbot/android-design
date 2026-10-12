#!/usr/bin/env node
import { readFileSync } from 'node:fs';
import { parseArgs } from 'node:util';
import { create, cleanup } from './workspace.mjs';
import { GalleryStore } from './store.mjs';
import { startServer } from './server.mjs';
import { wait } from './watch.mjs';

const help = `Local design exploration gallery (Node.js 22+).

node <skill>/gallery/cli.mjs create --project <repo> [--title "..."] [--starter html|android]
  Copy a starter into <repo>/design-explorations/ and print the exploration path.
  html (default): the HTML proposal starter. Proposals are folders:
  <exploration>/project/proposals/<id>/index.html plus its files.
  android: the Compose starter, for implementation sandboxes and the experimental Wear shell.
node <skill>/gallery/cli.mjs cleanup <exploration> --project <repo> --yes
  Delete a marked exploration after the user confirms.
node <skill>/gallery/cli.mjs publish <exploration> <draft.json>
  Lint each listed proposal folder, then preserve it as a new round with file hashes.
  Rejects a raw color, type declaration, style attribute, or inline style/script, naming file and line.
node <skill>/gallery/cli.mjs serve <exploration> [--port 8765]
  Print a loopback-only URL. Open it in your browser; Ctrl-C stops the server.
  Review a working draft at /draft/<id>/ before publishing; published rounds are at /rounds/<round>/<id>/.
node <skill>/gallery/cli.mjs status <exploration>
  Read rounds, notes, selections, and approval as JSON for the agent.
node <skill>/gallery/cli.mjs wait <exploration> [--after <revision>] [--timeout <seconds>]
  Block until something happens, then print one JSON line and exit 0 (default timeout 25, max 3600):
    {"status":"request",...}   newest pending refinement request, older ones in "superseded"
    {"status":"approved",...}  the user pressed Finish; stop
    {"status":"timeout",...}   nothing happened; rerun while the idle budget lasts
  --after ignores requests already seen at or before that revision. SIGINT/SIGTERM exit silently.

Draft: {"title":"First directions","proposals":[{"id":"a","name":"Editorial",
"rationale":"Prioritizes reading."}],
"answers":[{"roundId":"001","at":"<request timestamp>"}]}
The proposal folder is implied by the id. A draft with "images" is rejected.
Optional "answers" names the saved requests this round responds to; requests it
does not name stay pending.

Review each draft in the browser and run window.runtimeChecks() before publishing.
Approval never implements a design. Previous rounds are preserved.`;
try {
  const { values, positionals } = parseArgs({ options: { help: { type: 'boolean', short: 'h' }, after: { type: 'string' }, timeout: { type: 'string' }, port: { type: 'string' }, project: { type: 'string' }, title: { type: 'string' }, starter: { type: 'string' }, yes: { type: 'boolean' } }, allowPositionals: true });
  const [command, root, draft] = positionals;
  if (values.help || !command) console.log(help);
  else if (command === 'create' && root === undefined) {
    console.log(create({ root: values.project, title: values.title, starter: values.starter }));
  } else if (command === 'cleanup' && root && positionals.length === 2) {
    if (!values.yes) throw new Error('Cleanup deletes proposal drafts, feedback, and history; confirm with the user, then pass --yes');
    cleanup(root, values.project);
    console.log(`Removed ${root}`);
  } else if (command === 'publish' && root && draft && positionals.length === 3) {
    const round = new GalleryStore(root).publish(JSON.parse(readFileSync(draft, 'utf8')));
    console.log(`Published round ${round.id}: ${round.proposals.length} proposals. Files: ${root}/rounds/${round.id}/proposals`);
  } else if (command === 'status' && root && positionals.length === 2) {
    console.log(JSON.stringify(new GalleryStore(root).read(), null, 2));
  } else if (command === 'wait' && root && positionals.length === 2) {
    const after = values.after === undefined ? -1 : Number(values.after);
    const timeout = values.timeout === undefined ? 25 : Number(values.timeout);
    if (values.after !== undefined && !(/^\d+$/.test(values.after) && Number.isSafeInteger(after))) throw new Error('Invalid --after: expected a non-negative integer revision');
    if (values.timeout !== undefined && !(/^\d+$/.test(values.timeout) && timeout >= 1 && timeout <= 3600)) throw new Error('Invalid --timeout: expected 1 to 3600 seconds');
    for (const signal of ['SIGINT', 'SIGTERM']) process.once(signal, () => process.exit(0));
    console.log(JSON.stringify(await wait(root, { after, timeout })));
  } else if (command === 'serve' && root && positionals.length === 2) {
    const port = values.port === undefined ? 0 : Number(values.port);
    if (!Number.isInteger(port) || port < 0 || port > 65535) throw new Error('Invalid port');
    const server = await startServer(root, port);
    console.log(`Gallery: http://127.0.0.1:${server.address().port}/`);
    console.log(`Exploration: ${new GalleryStore(root).root}`);
    for (const signal of ['SIGINT', 'SIGTERM']) process.once(signal, () => server.close());
  } else throw new Error(help);
} catch (error) {
  console.error(error.message);
  process.exitCode = 1;
}
