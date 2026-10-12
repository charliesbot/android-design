import test from 'node:test';
import assert from 'node:assert/strict';
import * as state from '../../skills/android-design/gallery/src/state.mjs';

const round = { id: '001', proposals: [{ id: 'a' }, { id: 'b' }] };
test('names options and builds user-visible text', () => {
  assert.equal(state.optionName(round, 'b'), 'Option B');
  const entry = { choice: 'none', note: 'x', requests: [{ choice: 'none', note: 'x' }] };
  assert.equal(state.requestStatusText(entry), 'Request saved. Return to chat to continue.');
  assert.equal(state.refineLabel(entry), 'Request new directions');
  assert.equal(state.statusText({ pendingSaves: 1 }), 'Saving...');
  assert.equal(state.statusText({ loaded: true }), 'Saved');
});
test('gates refine and finish', () => {
  const entry = { choice: 'none', note: ' ', requests: [] };
  assert.equal(state.canRefine({ locked: false, error: false, entry }), false);
  assert.equal(state.canRefine({ locked: false, error: false, entry: { ...entry, note: 'go' } }), true);
  assert.equal(state.canRefine({ locked: false, error: true, entry: { ...entry, note: 'go' } }), false);
  assert.equal(state.canFinish({ locked: false, error: false, proposal: undefined }), false);
});
test('polling guards and round selection', () => {
  assert.equal(state.mayReplace({ pendingSaves: 1 }), false);
  assert.equal(state.mayReplace({}), true);
  const base = { hadData: true, previousLatest: '001', incomingRounds: ['001', '002'] };
  assert.equal(state.nextRoundSelection({ ...base, previousRound: '001' }), '002');
  assert.equal(state.nextRoundSelection({ ...base, previousLatest: '002', previousRound: '001' }), '001');
  assert.equal(state.shouldReload({ hadData: true, newRounds: false, sameRevision: true, sameTitle: true, sameSession: true }), false);
  assert.equal(state.shouldReload({ hadData: true, newRounds: false, sameRevision: true, sameTitle: true, sameSession: false }), true);
});
test('a changed agent session triggers a reload', () => {
  const waiting = { state: 'waiting', since: 'a', until: 'b' };
  assert.equal(state.sameSession(null, null), true);
  assert.equal(state.sameSession(waiting, { ...waiting }), true);
  assert.equal(state.sameSession(waiting, { ...waiting, state: 'working' }), false);
  assert.equal(state.sameSession(waiting, { ...waiting, until: 'c' }), false);
  assert.equal(state.sameSession(waiting, null), false);
  assert.equal(state.sameSession(null, waiting), false);
});
test('request status text follows the agent session', () => {
  const requestedEntry = { choice: 'none', note: 'x', requests: [{ choice: 'none', note: 'x' }] };
  const idle = { choice: 'none', note: 'y', requests: [{ choice: 'none', note: 'x' }] };
  assert.equal(state.requestStatusText(idle, { state: 'working' }), '');
  assert.equal(state.requestStatusText(requestedEntry, { state: 'working' }), 'The agent is working on your request.');
  assert.equal(state.requestStatusText(requestedEntry, { state: 'waiting' }), 'Request saved. The next round will appear here.');
  assert.equal(state.requestStatusText(requestedEntry, null), 'Request saved. Return to chat to continue.');
  assert.equal(state.requestStatusText(requestedEntry, { state: 'idle' }), 'Request saved. Return to chat to continue.');
});
test('devices: order, lookup, shortcuts, and exact canvases', () => {
  assert.deepEqual(state.DEVICES.map(device => [device.id, device.width, device.height, device.key]), [
    ['phone', 411, 923, '1'], ['foldable', 704, 933, '2'], ['tablet', 1280, 800, '3'], ['desktop', 1440, 900, '4'],
  ]);
  assert.equal(state.deviceById('tablet').name, 'Pixel Tablet');
  assert.equal(state.deviceById('unknown').id, 'phone');
  assert.equal(state.deviceById(null).id, 'phone');
  assert.equal(state.deviceForKey('4').id, 'desktop');
  assert.equal(state.deviceForKey('5'), undefined);
  assert.equal(state.deviceLabel(state.deviceById('foldable')), 'Foldable, Z Fold8');
  assert.equal(state.deviceSize(state.deviceById('phone')), '411 by 923');
});
test('frame scale fits width and height and never enlarges', () => {
  const desktop = state.deviceById('desktop'), phone = state.deviceById('phone');
  assert.equal(state.frameScale({ availableWidth: 720, maxHeight: 10000, device: desktop }), 0.5);
  assert.equal(state.frameScale({ availableWidth: 10000, maxHeight: 450, device: desktop }), 0.5);
  assert.equal(state.frameScale({ availableWidth: 10000, maxHeight: 10000, device: phone }), 1);
  assert.equal(state.frameScale({ availableWidth: 0, maxHeight: 500, device: phone }), 0);
  assert.equal(state.frameScale({ availableWidth: 300, maxHeight: NaN, device: phone }), 0);
});
test('frame title and url', () => {
  assert.equal(state.frameTitle('Option A', state.deviceById('phone')), 'Option A on Pixel 11');
  assert.equal(state.frameURL({ id: '001' }, { id: 'a' }), '/rounds/001/a/');
});
test('theme overrides keep only valid touched fields', () => {
  assert.equal(state.themeMessage({}), null);
  let overrides = state.withThemeOverride({}, 'color', '#B8321F');
  overrides = state.withThemeOverride(overrides, 'scheme', 'dark');
  assert.deepEqual(state.themeMessage(overrides), { type: 'theme', color: '#B8321F', scheme: 'dark' });
  assert.deepEqual(state.withThemeOverride(overrides, 'color', 'red'), { scheme: 'dark' });
  assert.deepEqual(state.withThemeOverride(overrides, 'variant', 'vibrant').variant, 'vibrant');
  assert.deepEqual(state.withThemeOverride({ variant: 'vibrant' }, 'variant', ''), {});
  assert.equal(state.withThemeOverride({}, 'scheme', 'auto').scheme, undefined);
  assert.equal(state.themeMessage({ variant: 'invalid', color: 'nope' }), null);
});

test('canvas resumes the saved direction without mutating feedback or approving', () => {
  const entry = { choice: '001/b', note: 'Keep the headline', requests: [] };
  assert.equal(state.viewedProposal(round, '', entry).id, 'b');
  assert.equal(state.viewedProposal(round, '001/a', entry).id, 'a');
  assert.equal(state.viewedProposal(round, 'stale/key', state.EMPTY_ENTRY).id, 'a');
  assert.equal(state.viewedProposal(undefined, '', entry), undefined);
  const effective = state.canvasEntry(round, round.proposals[0], state.EMPTY_ENTRY);
  assert.equal(effective.choice, '001/a');
  assert.equal(state.EMPTY_ENTRY.choice, null);
  assert.deepEqual(entry, { choice: '001/b', note: 'Keep the headline', requests: [] });
});
test('new directions retain their feedback target even while a proposal is visible', () => {
  const entry = { choice: 'none', note: 'Try warmer colors', requests: [] };
  assert.equal(state.canvasEntry(round, round.proposals[0], entry).choice, 'none');
  assert.equal(state.canvasEntry(undefined, undefined, state.EMPTY_ENTRY).choice, null);
});
test('rejecting directions can be undone without switching to a different proposal', () => {
  assert.equal(state.rejectionChange(round, round.proposals[0], { choice: 'none' }).choice, '001/a');
  assert.equal(state.rejectionChange(round, round.proposals[0], { choice: '001/a' }).choice, 'none');
});
test('rejecting preserves the viewed proposal even when its saved choice becomes none', () => {
  const entry = { choice: '001/b', note: '', requests: [] };
  const change = state.rejectionChange(round, round.proposals[1], entry);
  assert.equal(state.viewedProposal(round, change.activeKey, { ...entry, choice: change.choice }).id, 'b');
});

test('shared theme restores all original fields after removing overrides', () => {
  const original = { color: '#123456', variant: 'expressive', scheme: 'dark' };
  const overrides = { color: '#0061a4', variant: 'neutral', scheme: 'light' };
  assert.deepEqual(state.sharedTheme(original, overrides), overrides);
  assert.deepEqual(state.sharedTheme(original, {}), original);
  assert.equal(state.sharedTheme(original, state.withThemeOverride(overrides, 'variant', 'own')).variant, 'expressive');
  assert.deepEqual(original, { color: '#123456', variant: 'expressive', scheme: 'dark' });
});
test('authored themes normalize missing values and preserve supported library variants', () => {
  assert.deepEqual(state.authoredTheme({}), { color: '#6750a4', variant: 'neutral', scheme: 'auto' });
  const original = state.authoredTheme({ color: '#ABCDEF', variant: 'fidelity', scheme: 'auto' });
  assert.equal(original.color, '#abcdef');
  assert.deepEqual(state.sharedTheme(original, {}, true), { color: '#abcdef', variant: 'fidelity', scheme: 'dark' });
  assert.equal(state.sharedTheme(original, {}, false).scheme, 'light');
  assert.equal(state.themeMessage(state.sharedTheme(original, {})).variant, 'fidelity');
});
test('seed choices have stable names and unique valid colors', () => {
  assert.deepEqual(state.SEED_COLORS.map(item => item.name), ['Purple', 'Blue', 'Green', 'Amber', 'Rose']);
  assert.equal(new Set(state.SEED_COLORS.map(item => item.color)).size, 5);
  for (const item of state.SEED_COLORS) assert.match(item.color, /^#[0-9a-f]{6}$/);
});
