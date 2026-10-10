#!/usr/bin/env node
import { readFileSync } from 'node:fs';
import { parseArgs } from 'node:util';
import { create, cleanup } from './workspace.mjs';
import { GalleryStore } from './store.mjs';
import { startServer } from './server.mjs';

const help = `Local design exploration gallery (Node.js 22+).

node <skill>/gallery/cli.mjs create --project <repo> [--title "..."]
  Copy the starter into <repo>/design-explorations/ and print the exploration path.
node <skill>/gallery/cli.mjs cleanup <exploration> --project <repo> --yes
  Delete a marked exploration after the user confirms.
node <skill>/gallery/cli.mjs publish <exploration> <draft.json>
  Preserve the current project source and four PNGs per proposal as a new round.
  Image paths in the draft are relative to <exploration>/project.
node <skill>/gallery/cli.mjs serve <exploration> [--port 8765]
  Print a loopback-only URL. Open it in your browser; Ctrl-C stops the server.
node <skill>/gallery/cli.mjs status <exploration>
  Read rounds, notes, selections, and approval as JSON for the agent.

Draft: {"title":"First directions","proposals":[{"id":"a","name":"Editorial",
"rationale":"Prioritizes reading.","images":{"phone":"renders/a-phone.png",
"tablet":"renders/a-tablet.png","desktop":"renders/a-desktop.png",
"foldable":"renders/a-foldable.png"}}],
"answers":[{"roundId":"001","at":"<request timestamp>"}]}
Optional "answers" names the saved requests this round responds to; requests it
does not name stay pending.

Rendering stays with Compose Preview CLI. Publish only after inspecting fresh renders.
Approval never implements a design. Previous rounds are preserved.`;
try {
  const { values, positionals } = parseArgs({ options: { help: { type: 'boolean', short: 'h' }, port: { type: 'string' }, project: { type: 'string' }, title: { type: 'string' }, yes: { type: 'boolean' } }, allowPositionals: true });
  const [command, root, draft] = positionals;
  if (values.help || !command) console.log(help);
  else if (command === 'create' && root === undefined) {
    console.log(create({ root: values.project, title: values.title }));
  } else if (command === 'cleanup' && root && positionals.length === 2) {
    if (!values.yes) throw new Error('Cleanup deletes source, feedback, history, and renders; confirm with the user, then pass --yes');
    cleanup(root, values.project);
    console.log(`Removed ${root}`);
  } else if (command === 'publish' && root && draft && positionals.length === 3) {
    const round = new GalleryStore(root).publish(JSON.parse(readFileSync(draft, 'utf8')));
    console.log(`Published round ${round.id}: ${round.proposals.length} proposals. Source: ${root}/rounds/${round.id}/source`);
  } else if (command === 'status' && root && positionals.length === 2) {
    console.log(JSON.stringify(new GalleryStore(root).read(), null, 2));
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
