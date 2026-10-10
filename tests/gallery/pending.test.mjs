import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { GalleryStore, pendingRequests } from '../../skills/android-design/gallery/store.mjs';
import { fixture } from './fixture.mjs';

// Timestamps have millisecond resolution; wait so ordering is strict.
const tick = () => Atomics.wait(new Int32Array(new SharedArrayBuffer(4)), 0, 0, 5);

function request(store, roundId, note) {
  const revision = store.read().feedback.revision;
  store.save({ revision, roundId, choice: 'none', note });
  store.refine({ revision: revision + 1, roundId });
  tick();
  return store.read().feedback.rounds[roundId].requests.at(-1).at;
}
const ready = t => {
  const { root, draft } = fixture(t);
  const store = new GalleryStore(root);
  store.publish(draft);
  tick();
  return { root, draft, store };
};

test('a request is pending until a round answers it', t => {
  const { draft, store } = ready(t);
  const at = request(store, '001', 'Try again');
  assert.deepEqual(store.read().pending.map(r => [r.roundId, r.at, r.note, r.choice]), [['001', at, 'Try again', 'none']]);
  store.publish({ ...draft, answers: [{ roundId: '001', at }] });
  assert.deepEqual(store.read().pending, []);
});

test('two requests on one round are pending newest first and cleared individually', t => {
  const { draft, store } = ready(t);
  const older = request(store, '001', 'One');
  const newer = request(store, '001', 'Two');
  assert.deepEqual(store.read().pending.map(r => r.at), [newer, older]);
  store.publish({ ...draft, answers: [{ roundId: '001', at: newer }] });
  assert.deepEqual(store.read().pending.map(r => r.at), [older]);
  store.publish({ ...draft, answers: [{ roundId: '001', at: older }, { roundId: '001', at: newer }] });
  assert.deepEqual(store.read().pending, []);
});

test('a round with answers does not answer other requests by timestamp', t => {
  const { draft, store } = ready(t);
  const r1 = request(store, '001', 'One');
  const r2 = request(store, '001', 'Two');
  store.publish({ ...draft, answers: [{ roundId: '001', at: r1 }] });
  assert.deepEqual(store.read().pending.map(r => r.at), [r2]);
});

test('a round without answers answers older requests only', t => {
  const { draft, store } = ready(t);
  const older = request(store, '001', 'One');
  store.publish(draft);
  tick();
  const newer = request(store, '001', 'Two');
  assert.equal(store.read().rounds[1].answers, undefined);
  assert.deepEqual(store.read().pending.map(r => r.at), [newer]);
  assert.ok(older < newer);
});

test('a request made on an earlier round stays pending until answered', t => {
  const { draft, store } = ready(t);
  store.publish(draft);
  tick();
  const at = request(store, '001', 'Back to round one');
  assert.deepEqual(store.read().pending.map(r => [r.roundId, r.at]), [['001', at]]);
  store.publish({ ...draft, answers: [{ roundId: '001', at }] });
  assert.deepEqual(store.read().pending, []);
});

test('publish rejects invalid answers and creates no round', t => {
  const { root, draft, store } = ready(t);
  const at = request(store, '001', 'One');
  const original = readFileSync(join(root, 'exploration.json'), 'utf8');
  const bad = [
    [[{ roundId: '009', at }], /Unknown answered round/],
    [[{ roundId: '001', at: '2000-01-01T00:00:00.000Z' }], /Unknown answered request/],
    ['nope', /Invalid answers/],
    [[{ roundId: '001', at }, { roundId: '001', at }], /Duplicate/],
    [Array.from({ length: 51 }, () => ({ roundId: '001', at })), /Invalid answers/],
  ];
  for (const [answers, message] of bad) assert.throws(() => store.publish({ ...draft, answers }), message);
  assert.equal(readFileSync(join(root, 'exploration.json'), 'utf8'), original);
  assert.equal(store.read().rounds.length, 1);
});

test('read exposes pending as pendingRequests of the snapshot', t => {
  const { store } = ready(t);
  request(store, '001', 'One');
  const data = store.read();
  assert.equal(data.pending.length, 1);
  assert.deepEqual(data.pending, pendingRequests(data));
});
