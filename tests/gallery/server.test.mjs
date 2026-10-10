import test from 'node:test';
import { get } from 'node:http';
import assert from 'node:assert/strict';
import { fixture } from './fixture.mjs';
import { publishRound } from '../../skills/android-design/gallery/store.mjs';
import { startServer } from '../../skills/android-design/gallery/server.mjs';

async function setup(t) {
  const { root, draft } = fixture(t);
  publishRound(root, draft);
  const server = await startServer(root, 0);
  t.after(() => new Promise(resolve => server.close(resolve)));
  const url = `http://127.0.0.1:${server.address().port}`;
  return { server, url };
}

test('serves images and saved feedback but not source or arbitrary files', async t => {
  const { url } = await setup(t);
  assert.equal((await fetch(url)).status, 200);
  const arrows = await fetch(`${url}/select-arrows.svg`);
  assert.equal(arrows.status, 200);
  assert.equal(arrows.headers.get('content-type'), 'image/svg+xml');
  const data = await (await fetch(`${url}/api/exploration`)).json();
  assert.equal(data.rounds.length, 1);
  assert.equal((await fetch(`${url}/images/001/a/phone`)).headers.get('content-type'), 'image/png');
  for (const path of ['/feedback.json', '/project/settings.gradle.kts', '/images/001/a/unknown']) {
    assert.equal((await fetch(url + path)).status, 404);
  }
  const result = await fetch(`${url}/api/feedback`, { method: 'PUT', headers: { 'content-type': 'application/json', origin: url }, body: JSON.stringify({ revision: 0, roundId: '001', choice: '001/a', note: 'keep' }) });
  assert.equal(result.status, 200);
  assert.equal((await result.json()).revision, 1);
  const approved = await fetch(`${url}/api/approve`, { method: 'POST', headers: { 'content-type': 'application/json', origin: url }, body: JSON.stringify({ revision: 1, key: '001/a' }) });
  assert.equal(approved.status, 200);
  assert.equal((await approved.json()).approved.key, '001/a');
});

test('reject foreign hosts/origins, invalid bodies, and stale writes', async t => {
  const { url } = await setup(t);
  const badHost = await new Promise((resolve, reject) => {
    get(url, { headers: { host: 'evil.example' } }, response => { response.resume(); resolve(response.statusCode); }).on('error', reject);
  });
  assert.equal(badHost, 403);
  for (const headers of [{ origin: 'https://evil.example', 'content-type': 'application/json' }, { 'content-type': 'text/plain' }]) {
    assert.equal((await fetch(`${url}/api/feedback`, { method: 'PUT', headers, body: '{}' })).status, 403);
  }
  assert.equal((await fetch(`${url}/api/feedback`, { method: 'PUT', headers: { 'content-type': 'application/json' }, body: '{' })).status, 400);
  const write = revision => fetch(`${url}/api/feedback`, { method: 'PUT', headers: { 'content-type': 'application/json' }, body: JSON.stringify({ revision, roundId: '001', choice: '001/a', note: '' }) });
  assert.equal((await write(0)).status, 200);
  assert.equal((await write(0)).status, 409);
});

test('oversized feedback returns a useful error without changing saved state', async t => {
  const { url } = await setup(t);
  const response = await fetch(`${url}/api/feedback`, {
    method: 'PUT', headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ revision: 0, roundId: '001', choice: '001/a', note: 'x'.repeat(25000) }),
  });
  assert.equal(response.status, 413);
  assert.match((await response.json()).error, /too large/);
  assert.equal((await (await fetch(`${url}/api/exploration`)).json()).feedback.revision, 0);
});

test('refinement saves an explicit request without finalizing', async t => {
  const { url } = await setup(t);
  const headers = { 'content-type': 'application/json' };
  const save = await fetch(`${url}/api/feedback`, { method: 'PUT', headers, body: JSON.stringify({ revision: 0, roundId: '001', choice: 'none', note: 'Try a quieter layout' }) });
  assert.equal(save.status, 200);
  const response = await fetch(`${url}/api/refine`, { method: 'POST', headers, body: JSON.stringify({ revision: 1, roundId: '001' }) });
  assert.equal(response.status, 200);
  const state = await response.json();
  assert.equal(state.approved, null);
  assert.equal(state.rounds['001'].requests[0].choice, 'none');
  assert.equal(state.rounds['001'].requests[0].note, 'Try a quieter layout');
});

test('serves the local Google Sans font without opening arbitrary asset paths', async t => {
  const { url } = await setup(t);
  const font = await fetch(`${url}/fonts/google-sans-latin.woff2`);
  assert.equal(font.status, 200);
  assert.equal(font.headers.get('content-type'), 'font/woff2');
  assert.equal(Buffer.from(await font.arrayBuffer()).subarray(0, 4).toString(), 'wOF2');
  assert.equal((await fetch(`${url}/fonts/unknown.woff2`)).status, 404);
});
