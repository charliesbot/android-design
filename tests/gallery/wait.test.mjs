import test from 'node:test';
import assert from 'node:assert/strict';
import { spawn, spawnSync } from 'node:child_process';
import { existsSync, readFileSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { GalleryStore } from '../../skills/android-design/gallery/store.mjs';
import { wait } from '../../skills/android-design/gallery/watch.mjs';
import { fixture } from './fixture.mjs';

const CLI = new URL('../../skills/android-design/gallery/cli.mjs', import.meta.url).pathname;
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
// Fake clock: each sleep advances time by the requested amount and runs `onSleep`.
function clock(onSleep = () => {}) {
  const state = { time: 1_000_000, sleeps: 0 };
  return {
    state,
    now: () => state.time,
    sleep: async ms => { state.sleeps += 1; state.time += ms; await onSleep(state.sleeps); },
  };
}
const session = root => JSON.parse(readFileSync(join(root, 'session.json'), 'utf8'));

test('a request saved while waiting is returned within one interval with its revision', async t => {
  const { root, store } = ready(t);
  const c = clock(n => { if (n === 2) request(store, '001', 'More contrast'); });
  const result = await wait(root, { timeout: 30, interval: 1000, now: c.now, sleep: c.sleep });
  assert.equal(result.status, 'request');
  assert.equal(result.request.note, 'More contrast');
  assert.deepEqual(result.superseded, []);
  assert.equal(result.revision, store.read().feedback.revision);
  assert.equal(c.state.sleeps, 2);
});

test('a pending request before the start returns without sleeping', async t => {
  const { root, store } = ready(t);
  request(store, '001', 'Already here');
  const c = clock();
  const result = await wait(root, { now: c.now, sleep: c.sleep });
  assert.equal(result.status, 'request');
  assert.equal(c.state.sleeps, 0);
});

test('with two requests the newest is the request and the older is superseded', async t => {
  const { root, store } = ready(t);
  const older = request(store, '001', 'One');
  const newer = request(store, '001', 'Two');
  const result = await wait(root);
  assert.equal(result.request.at, newer);
  assert.deepEqual(result.superseded.map(r => r.at), [older]);
});

test('after equal to the current revision suppresses a seen request and times out', async t => {
  const { root, store } = ready(t);
  request(store, '001', 'Seen');
  const revision = store.read().feedback.revision;
  const c = clock();
  const result = await wait(root, { after: revision, timeout: 3, now: c.now, sleep: c.sleep });
  assert.deepEqual(result, { status: 'timeout', revision });
  const newer = request(store, '001', 'Fresh');
  assert.equal((await wait(root, { after: revision })).request.at, newer);
});

test('a round published with answers clears the request so the next wait times out', async t => {
  const { root, draft, store } = ready(t);
  const at = request(store, '001', 'One');
  store.publish({ ...draft, answers: [{ roundId: '001', at }] });
  const c = clock();
  const result = await wait(root, { timeout: 2, now: c.now, sleep: c.sleep });
  assert.equal(result.status, 'timeout');
});

test('the second request saved during a render is still returned after publishing', async t => {
  const { root, draft, store } = ready(t);
  const r1 = request(store, '001', 'One');
  const r2 = request(store, '001', 'Two');
  store.publish({ ...draft, answers: [{ roundId: '001', at: r1 }] });
  const c = clock();
  const result = await wait(root, { now: c.now, sleep: c.sleep });
  assert.equal(result.status, 'request');
  assert.equal(result.request.at, r2);
  assert.equal(c.state.sleeps, 0);
});

test('a request on an earlier round is returned while a later round exists', async t => {
  const { root, draft, store } = ready(t);
  store.publish(draft);
  tick();
  const at = request(store, '001', 'Back to one');
  const result = await wait(root);
  assert.equal(result.request.roundId, '001');
  assert.equal(result.request.at, at);
});

test('approval during the wait resolves approved', async t => {
  const { root, store } = ready(t);
  const choose = () => {
    const revision = store.read().feedback.revision;
    store.save({ revision, roundId: '001', choice: '001/a', note: '' });
    return revision + 1;
  };
  const c = clock(n => { if (n === 1) store.approve({ revision: choose(), key: '001/a' }); });
  const result = await wait(root, { now: c.now, sleep: c.sleep });
  assert.equal(result.status, 'approved');
  assert.equal(result.approved.key, '001/a');
  assert.equal(result.revision, store.read().feedback.revision);
});

test('approval wins over a pending request', async t => {
  const { root, store } = ready(t);
  let revision = store.read().feedback.revision;
  store.save({ revision, roundId: '001', choice: '001/a', note: 'Go' });
  revision += 1;
  store.refine({ revision, roundId: '001' });
  store.approve({ revision: revision + 1, key: '001/a' });
  assert.equal(store.read().pending.length, 1);
  assert.equal((await wait(root)).status, 'approved');
});

test('timeout resolves with the current revision', async t => {
  const { root, store } = ready(t);
  const c = clock();
  const result = await wait(root, { timeout: 5, interval: 1000, now: c.now, sleep: c.sleep });
  assert.deepEqual(result, { status: 'timeout', revision: store.read().feedback.revision });
  assert.equal(c.state.sleeps, 5);
});

test('store errors reject instead of being swallowed', async t => {
  const { root } = ready(t);
  writeFileSync(join(root, 'feedback.json'), 'not json');
  await assert.rejects(wait(root, { timeout: 1 }), /valid JSON/);
});

test('session file: waiting at start, working on a request, removed on approval, kept on timeout', async t => {
  const { root, store } = ready(t);
  const near = (iso, ms) => Math.abs(Date.parse(iso) - (Date.now() + ms)) < 5000;

  const seen = [];
  const c = clock(n => { seen.push(session(root)); if (n === 1) request(store, '001', 'Go'); });
  await wait(root, { timeout: 30, now: Date.now, sleep: async ms => { await c.sleep(ms); } });
  assert.equal(seen[0].state, 'waiting');
  assert.ok(near(seen[0].until, 90_000), 'waiting expires one minute after the timeout');
  assert.equal(session(root).state, 'working');
  assert.ok(near(session(root).until, 600_000), 'working lasts ten minutes');
  assert.equal(store.read().session.state, 'working');

  const fresh = ready(t);
  const idle = clock();
  await wait(fresh.root, { timeout: 2, now: idle.now, sleep: idle.sleep });
  assert.equal(session(fresh.root).state, 'waiting');

  const done = ready(t);
  let revision = done.store.read().feedback.revision;
  done.store.save({ revision, roundId: '001', choice: '001/a', note: '' });
  done.store.approve({ revision: revision + 1, key: '001/a' });
  writeFileSync(join(done.root, 'session.json'), JSON.stringify({ state: 'waiting', since: new Date().toISOString(), until: new Date(Date.now() + 60_000).toISOString() }));
  await wait(done.root);
  assert.equal(existsSync(join(done.root, 'session.json')), false);
});

test('session writes never touch feedback.json', t => {
  const { root, store } = ready(t);
  const before = readFileSync(join(root, 'feedback.json'), 'utf8');
  store.writeSession('waiting', new Date(Date.now() + 60_000));
  store.clearSession();
  store.clearSession();
  assert.equal(readFileSync(join(root, 'feedback.json'), 'utf8'), before);
});

test('read().session is null for missing, corrupt, invalid, or expired files', t => {
  const { root, store } = ready(t);
  const file = join(root, 'session.json');
  const iso = ms => new Date(Date.now() + ms).toISOString();
  assert.equal(store.read().session, null);
  writeFileSync(file, '{broken');
  assert.equal(store.read().session, null);
  writeFileSync(file, JSON.stringify({ state: 'sleeping', since: iso(0), until: iso(60_000) }));
  assert.equal(store.read().session, null);
  writeFileSync(file, JSON.stringify({ state: 'waiting', since: iso(-120_000), until: iso(-60_000) }));
  assert.equal(store.read().session, null);
  const live = { state: 'working', since: iso(0), until: iso(60_000) };
  writeFileSync(file, JSON.stringify(live));
  assert.deepEqual(store.read().session, live);
});

const run = (...args) => spawnSync(process.execPath, [CLI, ...args], { encoding: 'utf8' });

test('CLI wait: SIGINT after waiting is written exits silently', async t => {
  const { root } = ready(t);
  const child = spawn(process.execPath, [CLI, 'wait', root, '--timeout', '600'], { stdio: ['ignore', 'pipe', 'pipe'] });
  let out = '';
  child.stdout.on('data', d => { out += d; });
  const exited = new Promise(resolve => child.on('exit', (code, signal) => resolve({ code, signal })));
  for (let i = 0; i < 100 && !existsSync(join(root, 'session.json')); i++) await new Promise(r => setTimeout(r, 50));
  assert.equal(session(root).state, 'waiting');
  child.kill('SIGINT');
  const { code } = await exited;
  assert.equal(code, 0);
  assert.equal(out, '');
});

test('CLI wait: timeout prints one JSON line and exits 0', t => {
  const { root, store } = ready(t);
  const result = run('wait', root, '--timeout', '1');
  assert.equal(result.status, 0);
  assert.deepEqual(JSON.parse(result.stdout), { status: 'timeout', revision: store.read().feedback.revision });
  assert.equal(result.stdout.trim().split('\n').length, 1);
});

test('CLI wait: invalid options and unreadable explorations exit 1', t => {
  const { root } = ready(t);
  for (const args of [['--after', 'abc'], ['--after', '1.5'], ['--timeout', '0'], ['--timeout', '3601'], ['--timeout', 'x']]) {
    const result = run('wait', root, ...args);
    assert.equal(result.status, 1, args.join(' '));
    assert.equal(result.stdout, '');
  }
  writeFileSync(join(root, 'feedback.json'), 'nope');
  assert.equal(run('wait', root, '--timeout', '1').status, 1);
});
