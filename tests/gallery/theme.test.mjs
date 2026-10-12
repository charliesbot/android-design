import test from 'node:test';
import assert from 'node:assert/strict';
import { GalleryApp } from '../../skills/android-design/gallery/src/components/gallery-app.mjs';
import { ProposalCanvas } from '../../skills/android-design/gallery/src/components/proposal-canvas.mjs';

const effectiveTheme = context => GalleryApp.prototype.effectiveTheme.call(context);
for (const phase of ['initial loading', 'empty exploration']) {
  test(`${phase} resolves a theme without a proposal or captured original`, () => {
    assert.deepEqual(effectiveTheme({ viewed: () => undefined, originalTheme: null, theme: {}, systemDark: false }),
      { color: '#6750a4', variant: 'neutral', scheme: 'light' });
  });
}
test('the gallery ignores a captured theme belonging to another direction', () => {
  const context = { viewed: () => ({ id: 'b' }), currentRound: () => ({ id: '001' }),
    originalTheme: { key: '001/a', theme: { color: '#984061', variant: 'fidelity', scheme: 'dark' } },
    theme: { color: '#0061a4' }, systemDark: false };
  assert.deepEqual(effectiveTheme(context), { color: '#0061a4', variant: 'neutral', scheme: 'light' });
  context.originalTheme.key = '001/b';
  assert.deepEqual(effectiveTheme(context), { color: '#0061a4', variant: 'fidelity', scheme: 'dark' });
});
test('frame load captures authored attributes before requesting shared overrides', () => {
  const attributes = { color: '#984061', variant: 'fidelity', scheme: 'dark' };
  const document = { querySelector: () => ({ getAttribute: key => attributes[key] }) };
  const events = [];
  const frame = { contentDocument: document, contentWindow: { location: { pathname: '/rounds/001/b/' } } };
  const context = { round: { id: '001' }, proposal: { id: 'b' }, querySelector: () => frame, dispatchEvent: event => events.push(event) };
  ProposalCanvas.prototype.frameLoaded.call(context);
  assert.equal(context.loadedDocument, document);
  assert.equal(context.loadedKey, '001/b');
  assert.deepEqual(events[0].detail, { key: '001/b', theme: attributes });
  assert.equal(events[0].type, 'proposal-theme');
  frame.contentWindow.location.pathname = '/rounds/001/a/';
  ProposalCanvas.prototype.frameLoaded.call(context);
  assert.equal(events.length, 1, 'late loads from a previous direction are ignored');
});
