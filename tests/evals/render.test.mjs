import assert from 'node:assert/strict';
import test from 'node:test';
import { RENDERS, summarize } from '../../evals/render.mjs';

test('render table has the fixed names and sizes', () => {
  const table = Object.fromEntries(RENDERS.map(r => [r.name, r.viewport]));
  assert.deepEqual(table, {
    light: [411, 923], dark: [411, 923], wallpaper: [411, 923],
    foldable: [933, 704], tablet: [1280, 800], desktop: [1920, 1080],
  });
});

test('summarize passes only when every render passes and completes', () => {
  const ok = { pass: true, complete: true };
  const render = (name, result) => ({ name, viewport: [1, 1], result });
  assert.deepEqual(summarize('u', [render('a', ok), render('b', ok)]), { url: 'u', renders: [render('a', ok), render('b', ok)], pass: true, complete: true });
  const failed = summarize('u', [render('a', ok), render('b', { pass: false, complete: true })]);
  assert.equal(failed.pass, false);
  assert.equal(failed.complete, true);
  const partial = summarize('u', [render('a', ok), render('b', { pass: true, complete: false })]);
  assert.equal(partial.pass, true);
  assert.equal(partial.complete, false);
});
