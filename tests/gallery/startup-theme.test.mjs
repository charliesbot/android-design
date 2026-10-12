import test from 'node:test';
import assert from 'node:assert/strict';
import { GalleryStore } from '../../skills/android-design/gallery/store.mjs';
import { startServer } from '../../skills/android-design/gallery/server.mjs';
import { fixture, writeProposal } from './fixture.mjs';
import { GalleryApp } from '../../skills/android-design/gallery/src/components/gallery-app.mjs';

test('root HTML bootstraps the same saved/latest authored theme before initial paint', async t => {
  const { root, draft } = fixture(t);
  writeProposal(root, 'b', { page: '<!doctype html><m3e-theme color="#984061" variant="expressive" scheme="light"></m3e-theme>' });
  draft.proposals.push({ id: 'b', name: 'Alternative', rationale: 'Alternative' });
  const store = new GalleryStore(root); store.publish(draft);
  store.save({ revision: 0, roundId: '001', choice: '001/b', note: '' });
  const server = await startServer(root); t.after(() => new Promise(resolve => server.close(resolve)));
  const response = await fetch(`http://127.0.0.1:${server.address().port}/`), source = await response.text();
  assert.match(source, /<meta name="color-scheme" content="light">/);
  assert.match(source, /<m3e-theme color="#984061" variant="expressive" scheme="light" data-proposal-key="001\/b">/);
  assert.doesNotMatch(response.headers.get('content-security-policy'), /unsafe-inline/);
});
test('loading and first frame use the server bootstrap instead of briefly following a conflicting OS scheme', () => {
  const context = { viewed: () => undefined, originalTheme: null, bootstrapTheme: { key: '001/b', theme: { color: '#984061', variant: 'expressive', scheme: 'light' } }, theme: {}, systemDark: true };
  assert.deepEqual(GalleryApp.prototype.effectiveTheme.call(context), context.bootstrapTheme.theme);
  context.viewed = () => ({ id: 'b' }); context.currentRound = () => ({ id: '001' });
  assert.deepEqual(GalleryApp.prototype.effectiveTheme.call(context), context.bootstrapTheme.theme);
});

test('bootstrap preserves authored auto/default schemes and uses first direction for legacy rejection', async () => {
  const { startupTheme, applyStartupTheme, themeFromHTML } = await import('../../skills/android-design/gallery/startup-theme.mjs');
  const data = { rounds: [{ id: '002', proposals: [{ id: 'a' }, { id: 'b' }] }], feedback: { rounds: { '002': { choice: 'none', note: '', requests: [] } } } };
  const calls = [];
  const result = startupTheme(data, (...args) => { calls.push(args); return '<!-- <m3e-theme scheme="light"> --><m3e-theme color=\'#0061a4\' variant="vibrant" scheme=auto>'; });
  assert.deepEqual(result, { key: '002/a', theme: { color: '#0061a4', variant: 'vibrant', scheme: 'auto' } });
  assert.deepEqual(calls, [['002', 'a']]);
  const empty = startupTheme({ rounds: [], feedback: { rounds: {} } }, () => assert.fail('No proposal to read'));
  assert.deepEqual(empty, { key: null, theme: { color: '#6750a4', variant: 'neutral', scheme: 'auto' } });
  const html = '<meta name="color-scheme" content="light dark"><m3e-theme scheme="auto">';
  assert.match(applyStartupTheme(html, result), /content="light dark"/);
  const invalid = themeFromHTML('<m3e-theme color="javascript:evil" variant="evil" scheme="evil" data-proposal-key="&quot;bad">');
  assert.deepEqual(invalid, empty);
  assert.doesNotMatch(applyStartupTheme(html, invalid), /evil|data-proposal-key/);
});
test('bootstrap is ignored after switching to an unrelated direction', () => {
  const context = { viewed: () => ({ id: 'a' }), currentRound: () => ({ id: '001' }), originalTheme: null, bootstrapTheme: { key: '001/b', theme: { color: '#984061', variant: 'expressive', scheme: 'light' } }, theme: {}, systemDark: true };
  assert.deepEqual(GalleryApp.prototype.effectiveTheme.call(context), { color: '#6750a4', variant: 'neutral', scheme: 'dark' });
});
