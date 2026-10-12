import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { deviceIcon } from '../../skills/android-design/gallery/src/icons.mjs';
import { FeedbackPanel } from '../../skills/android-design/gallery/src/components/feedback-panel.mjs';
import { GalleryApp } from '../../skills/android-design/gallery/src/components/gallery-app.mjs';
import { ProposalCanvas } from '../../skills/android-design/gallery/src/components/proposal-canvas.mjs';

// Inspect Lit's template result without a browser or a second DOM implementation.
function markup(value) {
  if (Array.isArray(value)) return value.map(markup).join('');
  if (value?.strings) return value.strings.reduce((text, part, i) => text + part + markup(value.values[i] ?? ''), '');
  return String(value ?? '');
}
function render(theme = {}) {
  return markup(GalleryApp.prototype.render.call({
    data: undefined, saves: undefined, device: 'phone', theme, panelOpen: true, inspectorTab: 'directions',
    currentRound: () => undefined, feedbackEntry: () => ({ choice: null, note: '', requests: [] }),
    viewed: () => undefined, effectiveTheme: () => ({ color: '#6750a4', variant: 'neutral', scheme: 'light', ...theme }),
    locked: () => false, error: () => undefined,
  }));
}
test('device buttons and palette filter chips preserve every single-select option', () => {
  const source = render();
  assert.match(source, /<m3e-button-group class="device-switch" variant="connected"/);
  assert.match(source, /<m3e-filter-chip-set id="theme-variant"/);
  assert.equal((source.match(/<m3e-icon-button[^>]*toggle/g) || []).length, 4);
  for (const label of ["Proposal's own", 'Tonal spot', 'Neutral', 'Vibrant', 'Expressive']) assert.ok(source.includes(label), label);
  assert.doesNotMatch(source, /Canvas zoom|Zoom out|Zoom in|Fit device|\.zoom=/);
  assert.equal('zoom' in GalleryApp.properties, false);
  assert.equal('zoom' in ProposalCanvas.properties, false);
});
test('seed rows retain all five choices and use generated theme roles for samples', () => {
  const source = render();
  assert.equal((source.match(/class="palette-preview"/g) || []).length, 5);
  for (const label of ['Purple', 'Blue', 'Green', 'Amber', 'Rose']) assert.ok(source.includes(label), label);
  assert.match(source, /class="swatch"/);
  assert.match(source, /class="palette-samples"/);
  assert.doesNotMatch(source, /Custom/);
});
test('selecting an active toggle preserves a single selection', () => {
  for (const selected of [false, true]) {
    const button = { selected }, choices = [];
    GalleryApp.prototype.chooseToggle.call({}, { target: button, currentTarget: button }, () => choices.push('phone'));
    assert.equal(button.selected, true);
    assert.deepEqual(choices, ['phone']);
  }
});
test('canvas scale always automatically fits the unchanged device viewport', () => {
  const context = { slotWidth: 512, slotHeight: 652, chromeHeight: 152, device: { width: 1000, height: 1000 }, zoom: 2 };
  assert.equal(ProposalCanvas.prototype.scale.call(context), 0.5);
});

test('device radios and palette chips bind checked state to the active device and palette', () => {
  const source = render({ variant: 'expressive' });
  const devices = source.match(/<m3e-icon-button[^>]+>/g);
  assert.equal(devices.length, 4);
  devices.forEach((button, index) => assert.match(button, new RegExp(`aria-checked=${index === 0}`)));
  const palettes = source.slice(source.indexOf('<m3e-filter-chip-set id="theme-variant"')).match(/<m3e-filter-chip [^>]+>/g);
  assert.equal(palettes.length, 5);
  palettes.forEach((button, index) => assert.match(button, new RegExp(`aria-checked=${index === 4}`)));
});


test('inspector removes introductory copy, reset, direction details, footer, and seed checkmarks', () => {
  const source = render();
  assert.doesNotMatch(source, /Make it yours|Shape the next round|WORKSPACE|theme-reset|seed-check|canvas-footer/);
  assert.equal('resetTheme' in GalleryApp.prototype, false);
  const code = readFileSync(new URL('../../skills/android-design/gallery/src/components/gallery-app.mjs', import.meta.url), 'utf8');
  assert.doesNotMatch(code, /CURRENT DIRECTION|direction-detail/);
});
test('selected device symbols use the installed genuine filled Rounded paths', () => {
  for (const [device, name] of Object.entries({ phone: 'mobile_3', foldable: 'devices_fold', tablet: 'tablet', desktop: 'computer' })) {
    for (const selected of [false, true]) {
      const file = readFileSync(new URL(`../../node_modules/@material-symbols/svg-400/rounded/${name}${selected ? '-fill' : ''}.svg`, import.meta.url), 'utf8');
      const path = file.match(/<path d="([^"]+)"/)[1];
      assert.ok(markup(deviceIcon(device, selected)).includes(path), `${device}, selected ${selected}`);
    }
    assert.notEqual(markup(deviceIcon(device, false)), markup(deviceIcon(device, true)));
  }
});

test('seed rows use supported toggle selection from the initial effective theme', () => {
  const source = render();
  const seeds = source.match(/<m3e-button class="seed"[^>]+>/g);
  assert.equal(seeds.length, 5);
  seeds.forEach((seed, index) => {
    assert.match(seed, /toggle/);
    assert.match(seed, new RegExp(`\\.selected=${index === 0}`));
  });
  const css = readFileSync(new URL('../../skills/android-design/gallery/src/style.css', import.meta.url), 'utf8');
  assert.match(css, /\.seed\[selected\]/);
  assert.doesNotMatch(css, /\.seed\[aria-pressed=/);
});


test('nested theme change events never select a seed or change its theme', () => {
  const button = { selected: false }, choices = [];
  GalleryApp.prototype.chooseToggle.call({}, { target: { tagName: 'M3E-THEME' }, currentTarget: button }, () => choices.push('rose'));
  assert.equal(button.selected, false);
  assert.deepEqual(choices, []);
});

test('selectors live above theme controls in the sidebar and scheme switch precedes colors', () => {
  const source = render();
  assert.ok(source.indexOf('id="inspector"') < source.indexOf('class="canvas-navigation"'));
  assert.ok(source.indexOf('class="canvas-navigation"') < source.indexOf('class="theme-panel"'));
  assert.ok(source.indexOf('id="theme-dark"') < source.indexOf('class="seed-colors"'));
  assert.match(source, /aria-label="Dark theme"/);
  assert.match(source, /<span id="scheme-label">Light<\/span>/);
});
test('palette row content spans the button container instead of its intrinsic label width', () => {
  const css = readFileSync(new URL('../../skills/android-design/gallery/src/style.css', import.meta.url), 'utf8');
  assert.match(css, /container-type: inline-size/);
  assert.match(css, /width: calc\(100cqw -/);
  assert.match(css, /\.palette-samples[^}]*justify-self: end/);
});


test('the visible scheme label follows dark state while its accessible switch name stays stable', () => {
  const source = render({ scheme: 'dark' });
  assert.match(source, /<span id="scheme-label">Dark<\/span>/);
  assert.match(source, /aria-label="Dark theme" \.checked=true/);
});
test('automatic fit reserves measured device controls and wrapping caption height', () => {
  const context = { slotWidth: 1012, slotHeight: 700, chromeHeight: 200, device: { width: 1000, height: 1000 } };
  assert.equal(ProposalCanvas.prototype.scale.call(context), 0.5);
  context.chromeHeight = 300;
  assert.equal(ProposalCanvas.prototype.scale.call(context), 0.4);
});


test('sidebar exposes a vertical Directions/Theme tab rail and keeps feedback outside both panels', () => {
  const source = render();
  assert.match(source, /role="tablist" aria-orientation="vertical"/);
  assert.match(source, /<m3e-tab id=directions-tab/);
  assert.match(source, /<m3e-tab id=theme-tab/);
  assert.match(source, /id="directions-panel" role="tabpanel"/);
  assert.match(source, /id="theme-panel" role="tabpanel"/);
});
test('feedback has supported bounded autosize and no None action', () => {
  const source = markup(FeedbackPanel.prototype.render.call({ entry: { choice: 'none', note: 'Try warmer colors', requests: [] }, emit() {} }));
  assert.match(source, /<m3e-textarea-autosize for="note" min-rows="3" max-rows="8"/);
  assert.match(source, /maxlength="10000"/);
  assert.doesNotMatch(source, /id="none"|None of these|Use this direction/);
  assert.equal('noneEnabled' in FeedbackPanel.properties, false);
  assert.equal('none' in GalleryApp.prototype, false);
});
test('proposal caption follows the frame rather than preceding it', () => {
  const source = markup(ProposalCanvas.prototype.render.call({ round: { id: '001', proposals: [{ id: 'a' }] }, proposal: { id: 'a', name: 'Alternative' }, device: { name: 'Phone' }, scale: () => 1 }));
  assert.ok(source.indexOf('class="frame-slot"') < source.indexOf('class="frame-caption"'));
});


test('vertical tab navigation supports arrows and Home/End with selected-tab focus', () => {
  const focus = [], context = { inspectorTab: 'directions', selectInspectorTab(tab) { this.inspectorTab = tab; }, querySelector: selector => ({ focus: () => focus.push(selector) }) };
  for (const [key, selected] of [['ArrowDown', 'theme'], ['ArrowUp', 'directions'], ['End', 'theme'], ['Home', 'directions']]) {
    let prevented = false;
    GalleryApp.prototype.navigateInspectorTabs.call(context, { key, preventDefault: () => { prevented = true; } });
    assert.equal(context.inspectorTab, selected);
    assert.equal(prevented, true);
    assert.equal(focus.at(-1), `#${selected}-tab`);
  }
});
test('legacy rejection can be replaced through the direction selector even with a single proposal', () => {
  const proposal = { id: 'a', name: 'Alternative' }, round = { id: '001', proposals: [proposal] };
  const source = markup(GalleryApp.prototype.render.call({
    data: undefined, saves: undefined, device: 'phone', theme: {}, panelOpen: true, inspectorTab: 'directions',
    currentRound: () => round, feedbackEntry: () => ({ choice: 'none', note: 'Try warmer', requests: [] }),
    viewed: () => proposal, effectiveTheme: () => ({ color: '#6750a4', variant: 'neutral', scheme: 'light' }), locked: () => false, error: () => undefined,
  }));
  assert.match(source, /id="direction"[^>]*\.value= \?disabled=false/);
});

test('vertical rail tabs have Material icons and at least a 48px target', () => {
  const source = render();
  assert.equal((source.match(/class="rail-tab-content"/g) || []).length, 2);
  const css = readFileSync(new URL('../../skills/android-design/gallery/src/style.css', import.meta.url), 'utf8');
  assert.match(css, /\.inspector-rail m3e-tab[^}]*min-height: (?:48|56|64|72)px/);
});

test('icon-only tab rail sits in the left sidebar before the canvas with accessible names', () => {
  const source = render();
  assert.ok(source.indexOf('id="inspector"') < source.indexOf('class="canvas"'));
  const tabs = source.match(/<m3e-tab [^>]+>/g);
  assert.equal(tabs.length, 2);
  tabs.forEach((tab, index) => {
    const label = index === 0 ? 'Directions' : 'Theme';
    assert.match(tab, new RegExp(`aria-label=${label}`));
    assert.match(tab, new RegExp(`title=${label}`));
  });
  assert.doesNotMatch(source, /rail-tab-content[^>]*>[^]*?<span>Directions<\/span>/);
});

test('feedback remains mounted but only visible in Directions, with autosize refreshed on tab switches', () => {
  const source = readFileSync(new URL('../../skills/android-design/gallery/src/components/gallery-app.mjs', import.meta.url), 'utf8');
  assert.match(source, /<feedback-panel \?hidden=\$\{this.inspectorTab !== 'directions'\}/);
  assert.match(source, /changed.has\('inspectorTab'\)/);
});
