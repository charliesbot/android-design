import test from 'node:test';
import assert from 'node:assert/strict';
import { luminance, neededRatio, over, ratio } from '../../skills/android-design-explore/gallery/runtime-src/contrast.js';

const rgb = (r, g, b, a = 1) => ({ r, g, b, a });
const round = v => Math.round(v * 100) / 100;

test('contrast ratio matches the WCAG reference values', () => {
  assert.equal(round(ratio(rgb(0, 0, 0), rgb(255, 255, 255))), 21);
  assert.equal(round(ratio(rgb(255, 255, 255), rgb(255, 255, 255))), 1);
  assert.equal(round(ratio(rgb(119, 119, 119), rgb(255, 255, 255))), 4.48); // #777 on white just fails 4.5
  assert.equal(round(ratio(rgb(118, 118, 118), rgb(255, 255, 255))), 4.54); // #767676 on white just passes
  assert.ok(luminance(rgb(255, 255, 255)) > luminance(rgb(0, 0, 255)));
});

test('contrast ratio is symmetric', () => {
  const [a, b] = [rgb(253, 247, 255), rgb(101, 87, 137)];
  assert.equal(ratio(a, b), ratio(b, a));
});

test('a translucent foreground is composited over the backdrop before measuring', () => {
  assert.deepEqual(over(rgb(0, 0, 0, 0.5), rgb(255, 255, 255)), rgb(127.5, 127.5, 127.5));
  const opaque = rgb(10, 20, 30);
  assert.equal(over(opaque, rgb(255, 255, 255)), opaque);
});

test('large text needs 3:1, other text 4.5:1', () => {
  assert.equal(neededRatio(14, 400), 4.5);
  assert.equal(neededRatio(23.9, 400), 4.5);
  assert.equal(neededRatio(24, 400), 3);
  assert.equal(neededRatio(18.66, 700), 3);
  assert.equal(neededRatio(18.66, 500), 4.5);
});
