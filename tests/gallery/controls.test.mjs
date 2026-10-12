import test from 'node:test';
import assert from 'node:assert/strict';
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
    data: undefined, saves: undefined, device: 'phone', theme, panelOpen: true,
    currentRound: () => undefined, feedbackEntry: () => ({ choice: null, note: '', requests: [] }),
    viewed: () => undefined, effectiveTheme: () => ({ color: '#6750a4', variant: 'neutral', scheme: 'light' }),
    locked: () => false, error: () => undefined,
  }));
}
test('device and palette controls are connected single-select Material groups', () => {
  const source = render();
  assert.match(source, /<m3e-button-group class="device-switch" variant="connected"/);
  assert.match(source, /<m3e-button-group id="theme-variant" variant="connected"/);
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
    GalleryApp.prototype.chooseToggle.call({}, { currentTarget: button }, () => choices.push('phone'));
    assert.equal(button.selected, true);
    assert.deepEqual(choices, ['phone']);
  }
});
test('canvas scale always automatically fits the unchanged device viewport', () => {
  const context = { slotWidth: 500, slotHeight: 652, device: { width: 1000, height: 1000 }, zoom: 2 };
  assert.equal(ProposalCanvas.prototype.scale.call(context), 0.5);
});

test('connected radio buttons bind checked state to the active device and palette', () => {
  const source = render({ variant: 'expressive' });
  const devices = source.match(/<m3e-icon-button[^>]+>/g);
  assert.equal(devices.length, 4);
  devices.forEach((button, index) => assert.match(button, new RegExp(`aria-checked=${index === 0}`)));
  const palettes = source.slice(source.indexOf('<m3e-button-group id="theme-variant"')).match(/<m3e-button[^>]*toggle[^>]+>/g);
  assert.equal(palettes.length, 5);
  palettes.forEach((button, index) => assert.match(button, new RegExp(`aria-checked=${index === 4}`)));
});
