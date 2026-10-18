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
import { ORIGIN, RENDERS, lintFolder, makeRoute, renderViews, summarize } from '../../skills/android-design-explore/gallery/render.mjs';

const ROOT = join(dirname(fileURLToPath(import.meta.url)), '../..');
const CHECKS = join(ROOT, 'evals/checks.py');
const DEFECTS = join(ROOT, 'tests/evals/defects');
const STARTER = join(ROOT, 'skills/android-design-explore/assets/proposal-starter');
let chrome;
try { chrome = chromeExecutable(); } catch { chrome = null; }

// Overlays a fixture opens through a direct DOM call, as a page script would from a control. The open overlay is checked at
// these two views, after the default views (which see it closed).
const OPEN_VIEWS = ['light', 'tablet'];
const OPEN = {
  queue: "document.getElementById('queue').show()",
  credits: "document.getElementById('credits').show()",
  more: "document.getElementById('more').show(document.querySelector('.supporting'))",
};

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
  'control-scroller-under-pinned-bars': { fails: [] }, // padded list scrolls under opaque pinned bars inside a fixed app shell; rows under the bottom bar scroll clear
  'scroller-end-under-bar': { fails: ['C-review'], detail: /tablet: overlaps/ }, // last row under the bottom bar can never scroll clear (no bottom padding)
  'control-scroller-end-padded': { fails: [] }, // bottom padding at least the bar height lets every row scroll clear
  'scroller-row0-under-top-bar': { fails: ['C-review'], detail: /light: overlaps/ }, // row 0 under the top bar at scroll 0 can never scroll clear
  'control-truncate-ellipsis': { fails: [] }, // single-line ellipsis and a 2-line clamp are intentional truncation
  'text-clipped-no-ellipsis': { fails: ['C-review'], detail: /light: overflow/ },
  'control-text-field': { fails: [] }, // an empty and a short-valued text field are a 56dp touch target and do not overflow or truncate
  'control-primary-with-slider': { fails: [] }, // a full-width slider is a track, not a competing action
  'control-primary-with-nav-rail': { fails: [] }, // navigation chrome is not compared with the primary
  'primary-outsized': { fails: ['C-review'], detail: /light: primary/ }, // a regular button larger than the primary action fails
  'primary-outsized-native-button': { fails: ['C-review'], detail: /light: primary/ }, // a native button that is not a row is an action
  'control-primary-with-content': { fails: [] }, // tappable list rows and a card larger than the primary are content, not competing actions
  'control-primary-equal-buttons': { fails: [] }, // peers of one size: the primary ties with them and leads through fill
  'control-primary-default-with-toolbar': { fails: [] }, // the starter's labeled toggle toolbar is navigation, not competing actions, for a default-size primary
  'control-primary-small-peers': { fails: [] }, // same size step, wider label: height decides, not area
  'control-primary-with-icon-buttons': { fails: [] }, // a standard icon button is a 48px box around a 40px visual
  'primary-outsized-fab': { fails: ['C-review'], detail: /light: primary/ }, // a FAB is taller than a small primary button
  'ellipsis-vertical-clip': { fails: ['C-review'], detail: /light: overflow/ }, // ellipsis does not excuse a vertical clip
  'ellipsis-glyph-clip': { fails: ['C-review'], detail: /light: overflow/ }, // nowrap ellipsis with line-height 0.6 clips the glyphs vertically
  'button-label-ellipsis': { fails: ['C-review'], detail: /light: overflow/ }, // button labels never truncate
  'primary-drawer-container': { fails: ['C-review'], detail: /light: primary/ }, // the drawer container's main content is not nav chrome
  'slider-short': { fails: ['C-review'], detail: /light: touchTargets/ },
  // Open overlays (`open`: the overlay opened after load): content behind an open modal is exempt, the modal's own surface is checked.
  'control-sheet-open': { fails: [], open: 'queue' }, // a modal bottom sheet over the starter covers its text and floating toolbar
  'control-dialog-open': { fails: [], open: 'credits' }, // a dialog covers the starter text
  'control-menu-open': { fails: [], open: 'more' }, // a menu opened over the primary button covers it, and covers no text it should not
  'sheet-low-contrast': { fails: ['C-review'], detail: /^queue-light: contrast/, open: 'queue' }, // the sheet's own title matches its container; the closed sheet in the default views is not measured
  'sheet-text-overflow': { fails: ['C-review'], detail: /^queue-light: overflow/, open: 'queue' }, // the sheet's own text is cut off with no ellipsis
  'modal-open-by-default': { fails: ['C-review'], detail: /^light: a modal is open on load/ }, // an open dialog on load would exempt the page and skip the two-primary failure
  'dialog-low-contrast': { fails: ['C-review'], detail: /credits-light: contrast/, open: 'credits' }, // measured against the dialog's container, not the page behind it
};

if (!chrome) {
  test('seeded defects', { skip: 'Chrome not found; install Google Chrome or set CHROME_PATH' }, () => {});
} else {
  describe('seeded defects', () => {
    let repo, browser, page, current;

    before(async () => {
      repo = realpathSync(mkdtempSync(join(tmpdir(), 'defects-')));
      browser = await Browser.launch(chrome);
      page = await browser.newPage();
      await page.serve(url => makeRoute({ proposalDir: current })(url));
    });

    after(async () => {
      await browser?.close();
      if (repo) rmSync(repo, { recursive: true, force: true });
    });

    // The checked result of `open` at one of the open views: the overlay is opened after load, then the checks run.
    async function renderOpen(url, id, name) {
      const spec = RENDERS.find(render => render.name === name);
      await page.setViewport(...spec.viewport);
      await page.goto(url);
      const result = await page.evaluate(async (script, scheme) => {
        document.querySelector('m3e-theme')?.setAttribute('scheme', scheme);
        await document.fonts.ready;
        // A sheet runs a 250ms close animation when it first renders, and would hide itself again if opened before it ends.
        await window.runtimeSettle();
        await eval(script);
        await window.runtimeSettle();
        return window.runtimeChecks();
      }, OPEN[id], spec.scheme);
      return { name: `${id}-${name}`, viewport: spec.viewport, result };
    }

    // Lints and renders a fixture, writes the files checks.py reads, and returns its verdict.
    async function run(name, open) {
      const caseDir = join(repo, 'cases', name);
      const proposal = join(caseDir, 'proposal');
      current = proposal;
      cpSync(name === 'control' ? STARTER : join(DEFECTS, name), proposal, { recursive: true });
      mkdirSync(join(caseDir, 'renders'), { recursive: true });
      writeFileSync(join(caseDir, 'case.json'), JSON.stringify({ proposal_dir: proposal }));
      const errors = lintFolder(proposal);
      writeFileSync(join(caseDir, 'lint.json'), JSON.stringify({ ok: errors.length === 0, output: errors.join('\n') }));
      if (!errors.length) {
        const url = `${ORIGIN}/proposal/`;
        const renders = await renderViews(page, url);
        if (open) for (const view of OPEN_VIEWS) renders.push(await renderOpen(url, open, view));
        writeFileSync(join(caseDir, 'renders/checks.json'), JSON.stringify(summarize(url, renders)));
      }
      const checked = spawnSync('python3', [CHECKS, caseDir], { encoding: 'utf8' });
      assert.equal(checked.status, 0, checked.stderr);
      return JSON.parse(checked.stdout);
    }

    for (const [name, expected] of Object.entries(CASES)) {
      test(name, { timeout: 120000 }, async () => {
        const verdict = await run(name, expected.open);
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
