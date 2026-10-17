// Seeded-defect regression test for the HTML eval checks. Each folder in tests/evals/defects/ is the starter
// proposal with one minimal mutation; the control is the starter itself, read from the skill so it cannot drift. Every fixture goes through the real
// pipeline (the skill's lint and render modules, evals/checks.py) and must fail exactly the check named here.

import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { cpSync, mkdirSync, mkdtempSync, realpathSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { after, before, describe, test } from 'node:test';
import { fileURLToPath } from 'node:url';
import { Browser, chromeExecutable } from '../../skills/android-design-explore/gallery/cdp.mjs';
import { ORIGIN, lintAll, makeRoute, renderViews, summarize } from '../../skills/android-design-explore/gallery/render.mjs';

const ROOT = join(dirname(fileURLToPath(import.meta.url)), '../..');
const CLI = join(ROOT, 'skills/android-design-explore/gallery/cli.mjs');
const CHECKS = join(ROOT, 'evals/checks.py');
const DEFECTS = join(ROOT, 'tests/evals/defects');
const STARTER = join(ROOT, 'skills/android-design-explore/assets/proposal-starter/proposals/a');
let chrome;
try { chrome = chromeExecutable(); } catch { chrome = null; }

// name: the checks that must fail (empty: all pass; the first is the one the mutation targets), and a pattern its detail must match.
const CASES = {
  'control': { fails: [] },
  'control-offscreen': { fails: [] }, // fixed elements fully off screen are hidden on purpose, not a spill
  'raw-hex': { fails: ['C-lint', 'C-review'], detail: /Raw color/ },
  'inline-style': { fails: ['C-lint', 'C-review'], detail: /style="" attributes/ },
  'font-size-outside-voice': { fails: ['C-lint', 'C-review'], detail: /font-size is only allowed in a single-class \.voice-\*/ },
  'icon-unknown': { fails: ['C-lint', 'C-review'], detail: /<p-icon name="homey"> is not a Material Symbols name/ }, // a made-up symbol would render as plain text
  'palette-expressive': { fails: ['C-palette'], detail: /variant expressive/ },
  'tiny-target': { fails: ['C-review'], detail: /light: touchTargets/ },
  'non-flex-font': { fails: ['C-lint', 'C-review'], detail: /font-family is not allowed: "Georgia" is not bundled/ }, // publish lint rejects unbundled families, so the runtime fonts check has no seeded case
  'brand-font-small': { fails: ['C-lint', 'C-review'], detail: /Fraunces is only for headline small/ }, // a brand face below 24px
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

if (!chrome) {
  test('seeded defects', { skip: 'Chrome not found; install Google Chrome or set CHROME_PATH' }, () => {});
} else {
  describe('seeded defects', () => {
    let repo, exploration, browser, page, proposalsDir;

    before(async () => {
      repo = realpathSync(mkdtempSync(join(tmpdir(), 'defects-')));
      assert.equal(spawnSync('git', ['init', '-q', repo]).status, 0);
      const created = spawnSync('node', [CLI, 'create', '--project', repo, '--title', 'Defects'], { encoding: 'utf8' });
      assert.equal(created.status, 0, created.stderr);
      exploration = created.stdout.trim();
      proposalsDir = join(exploration, 'project/proposals');
      browser = await Browser.launch(chrome);
      page = await browser.newPage();
      await page.serve(makeRoute({ proposalsDir }));
    });

    after(async () => {
      await browser?.close();
      if (repo) rmSync(repo, { recursive: true, force: true });
    });

    // Lints and renders a fixture as proposal `a`, writes the files checks.py reads, and returns its verdict.
    async function run(name) {
      const caseDir = join(repo, 'cases', name);
      const proposal = join(proposalsDir, 'a');
      rmSync(proposal, { recursive: true, force: true });
      cpSync(name === 'control' ? STARTER : join(DEFECTS, name), proposal, { recursive: true });
      mkdirSync(join(caseDir, 'renders'), { recursive: true });
      writeFileSync(join(caseDir, 'case.json'), JSON.stringify({ exploration, proposal: 'a' }));
      const errors = lintAll(proposalsDir, ['a']);
      writeFileSync(join(caseDir, 'lint.json'), JSON.stringify({ ok: errors.length === 0, output: errors.join('\n') }));
      if (!errors.length) {
        const url = `${ORIGIN}/draft/a/`;
        writeFileSync(join(caseDir, 'renders/checks.json'), JSON.stringify(summarize(url, await renderViews(page, url))));
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
