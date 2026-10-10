import test from 'node:test';
import assert from 'node:assert/strict';
import * as state from '../../skills/android-design/gallery/src/state.mjs';

const round = { id: '001', proposals: [{ id: 'a' }, { id: 'b' }] };
test('names options and builds user-visible text', () => {
  assert.equal(state.optionName(round, 'b'), 'Option B');
  const entry = { choice: 'none', note: 'x', requests: [{ choice: 'none', note: 'x' }] };
  assert.equal(state.requestStatusText(entry), 'Request saved. Return to chat to continue.');
  assert.equal(state.refineLabel(entry), 'Request new directions');
  assert.equal(state.selectionText({ round, entry }), 'Try another direction');
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
