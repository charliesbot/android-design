// Seeded-defect regression test for the HTML eval checks. Each folder in tests/evals/defects/ is the starter
// proposal with one minimal mutation; the control is the starter itself, read from the skill so it cannot drift. Every fixture goes through the real
// pipeline (cli.mjs publish, evals/render.mjs, evals/checks.py) and must fail exactly the check named here.

import assert from 'node:assert/strict';
import { spawn, spawnSync } from 'node:child_process';
import { cpSync, existsSync, mkdirSync, mkdtempSync, readFileSync, realpathSync, rmSync, writeFileSync } from 'node:fs';
import { createServer } from 'node:net';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { after, before, describe, test } from 'node:test';
import { fileURLToPath } from 'node:url';
import puppeteer from 'puppeteer-core';
import { chromeExecutable, renderAll, summarize } from '../../evals/render.mjs';

const ROOT = join(dirname(fileURLToPath(import.meta.url)), '../..');
const CLI = join(ROOT, 'skills/android-design/gallery/cli.mjs');
const CHECKS = join(ROOT, 'evals/checks.py');
const DEFECTS = join(ROOT, 'tests/evals/defects');
const STARTER = join(ROOT, 'skills/android-design/assets/proposal-starter/proposals/a');
const chrome = chromeExecutable();

// name: the checks that must fail (empty: all pass; the first is the one the mutation targets), and a pattern its detail must match.
const CASES = {
  'control': { fails: [] },
  'control-offscreen': { fails: [] }, // fixed elements fully off screen are hidden on purpose, not a spill
  'raw-hex': { fails: ['C-publish', 'C-review'], detail: /Raw color/ },
  'inline-style': { fails: ['C-publish', 'C-review'], detail: /style="" attributes/ },
  'font-size-outside-voice': { fails: ['C-publish', 'C-review'], detail: /font-size is only allowed in a single-class \.voice-\*/ },
  'icon-unknown': { fails: ['C-publish', 'C-review'], detail: /<p-icon name="homey"> is not a Material Symbols name/ }, // a made-up symbol would render as plain text
  'palette-expressive': { fails: ['C-palette'], detail: /variant expressive/ },
  'tiny-target': { fails: ['C-review'], detail: /light: touchTargets/ },
  'non-flex-font': { fails: ['C-publish', 'C-review'], detail: /font-family is not allowed: "Georgia" is not bundled/ }, // publish lint rejects unbundled families, so the runtime fonts check has no seeded case
  'brand-font-small': { fails: ['C-publish', 'C-review'], detail: /Fraunces is only for headline small/ }, // a brand face below 24px
  'control-brand-font': { fails: [] }, // a brand face at display size is allowed and renders
  'low-contrast': { fails: ['C-review'], detail: /light: contrast/ },
  'desktop-only': { fails: ['C-review'], detail: /desktop: pageOverflow/, notIn: /tablet: |foldable: / },
  'foldable-only': { fails: ['C-review'], detail: /foldable: pageOverflow/, notIn: /tablet: |desktop: / },
  'fixed-right': { fails: ['C-review'], detail: /light: pageOverflow/ },
  'midword-break': { fails: ['C-review'], detail: /midWordBreaks/ },
  'toolbar-overlap': { fails: ['C-review'], detail: /overlap/ },
  'control-scroll-list': { fails: [] }, // content scrolls clear of the floating toolbar
  'control-inner-scroller': { fails: [] },
  'control-scroller-toolbar-inside': { fails: [] }, // a pinned toolbar inside the scroller still lets content scroll clear
  'control-scroller-sticky-bar': { fails: [] },
  'toolbar-overlap-static': { fails: ['C-review'], detail: /overlaps/ },
  'fixed-left': { fails: ['C-review'], detail: /light: pageOverflow/ },
  'control-slider': { fails: [] }, // Compose's whole 48dp Slider is the touch target; the thumb has none
  'slider-short': { fails: ['C-review'], detail: /light: touchTargets/ },
};

function freePort() {
  return new Promise((resolve, reject) => {
    const server = createServer();
    server.once('error', reject);
    server.listen(0, '127.0.0.1', () => {
      const { port } = server.address();
      server.close(() => resolve(port));
    });
  });
}

async function waitFor(url) {
  const deadline = Date.now() + 20000;
  for (;;) {
    try { if ((await fetch(url)).ok) return; } catch { /* not up yet */ }
    if (Date.now() > deadline) throw new Error('gallery server did not start within 20 seconds');
    await new Promise(resolve => setTimeout(resolve, 200));
  }
}

if (!existsSync(chrome)) {
  test('seeded defects', { skip: `Chrome not found at ${chrome}; install Google Chrome or set CHROME_PATH` }, () => {});
} else {
  describe('seeded defects', () => {
    let repo, exploration, server, browser, port;

    before(async () => {
      repo = realpathSync(mkdtempSync(join(tmpdir(), 'defects-')));
      assert.equal(spawnSync('git', ['init', '-q', repo]).status, 0);
      const created = spawnSync('node', [CLI, 'create', '--project', repo, '--title', 'Defects'], { encoding: 'utf8' });
      assert.equal(created.status, 0, created.stderr);
      exploration = created.stdout.trim();
      port = await freePort();
      server = spawn('node', [CLI, 'serve', exploration, '--port', String(port)], { stdio: 'ignore' });
      await waitFor(`http://127.0.0.1:${port}/`);
      browser = await puppeteer.launch({ executablePath: chrome, headless: true });
    });

    after(async () => {
      await browser?.close();
      server?.kill();
      if (repo) rmSync(repo, { recursive: true, force: true });
    });

    // Publishes a fixture as a new round, renders it, and returns checks.py's verdict.
    async function run(name) {
      const caseDir = join(repo, 'cases', name);
      const proposal = join(exploration, 'project/proposals/a');
      rmSync(proposal, { recursive: true, force: true });
      cpSync(name === 'control' ? STARTER : join(DEFECTS, name), proposal, { recursive: true });
      mkdirSync(caseDir, { recursive: true });
      writeFileSync(join(caseDir, 'case.json'), JSON.stringify({ exploration, proposal: 'a' }));
      const draft = join(caseDir, 'draft.json');
      writeFileSync(draft, JSON.stringify({ title: name, proposals: [{ id: 'a', name, rationale: 'Seeded defect.' }] }));
      const done = spawnSync('node', [CLI, 'publish', exploration, draft], { encoding: 'utf8' });
      const round = /Published round (\d+)/.exec(done.stdout)?.[1];
      writeFileSync(join(caseDir, 'publish.json'), JSON.stringify({ ok: done.status === 0 && Boolean(round), round, output: done.stdout + done.stderr }));
      if (done.status === 0 && round) {
        const url = `http://127.0.0.1:${port}/rounds/${round}/a/`;
        const out = join(caseDir, 'renders');
        mkdirSync(out, { recursive: true });
        const review = summarize(url, await renderAll(url, out, chrome, browser));
        writeFileSync(join(out, 'review.json'), JSON.stringify(review));
      }
      const checked = spawnSync('python3', [CHECKS, caseDir], { encoding: 'utf8' });
      assert.equal(checked.status, 0, checked.stderr);
      return JSON.parse(checked.stdout);
    }

    for (const [name, expected] of Object.entries(CASES)) {
      test(name, { timeout: 120000 }, async () => {
        const verdict = await run(name);
        const failed = Object.keys(verdict).filter(id => !verdict[id].pass).sort();
        if (process.env.DEFECTS_VERBOSE) console.log(name, JSON.stringify(verdict));
        const details = failed.map(id => `${id}: ${verdict[id].detail}`).join('\n');
        assert.deepEqual(failed, [...expected.fails].sort(), `unexpected verdict for ${name}:\n${details}`);
        if (expected.detail) {
          const detail = verdict[expected.fails[0]].detail;
          assert.match(detail, expected.detail);
          if (expected.notIn) assert.doesNotMatch(detail, expected.notIn);
        }
      });
    }
  });
}
