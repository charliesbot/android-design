import test from 'node:test';
import { get } from 'node:http';
import assert from 'node:assert/strict';
import { symlinkSync, rmSync, writeFileSync, readFileSync, mkdirSync } from 'node:fs';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { fixture } from './fixture.mjs';
import { GalleryStore } from '../../skills/android-design/gallery/store.mjs';
import { startServer } from '../../skills/android-design/gallery/server.mjs';

async function setup(t) {
  const { root, draft } = fixture(t);
  new GalleryStore(root).publish(draft);
  const server = await startServer(root, 0);
  t.after(() => new Promise(resolve => server.close(resolve)));
  const url = `http://127.0.0.1:${server.address().port}`;
  return { server, url, root };
}

test('serves rounds and saved feedback but not source or arbitrary files', async t => {
  const { url } = await setup(t);
  assert.equal((await fetch(url)).status, 200);
  const arrows = await fetch(`${url}/select-arrows.svg`);
  assert.equal(arrows.status, 200);
  assert.equal(arrows.headers.get('content-type'), 'image/svg+xml');
  const data = await (await fetch(`${url}/api/exploration`)).json();
  assert.equal(data.rounds.length, 1);
  assert.equal((await fetch(`${url}/rounds/001/a/`)).headers.get('content-type'), 'text/html; charset=utf-8');
  for (const path of ['/feedback.json', '/project/proposals/a/index.html', '/images/001/a/phone', '/rounds/001/a/unknown.png', '/rounds/001/a/../../feedback.json']) {
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

const rawStatus = (url, path) => new Promise((resolve, reject) => {
  // fetch normalizes dot segments, so send the path untouched.
  get({ host: '127.0.0.1', port: new URL(url).port, path, headers: { host: new URL(url).host } }, response => { response.resume(); resolve(response.statusCode); }).on('error', reject);
});

test('static serving refuses traversal, symlinks, and directories', async t => {
  const { url } = await setup(t);
  const publicDir = fileURLToPath(new URL('../../skills/android-design/gallery/public/', import.meta.url));
  const link = `${publicDir}zz-test-link.mjs`, dirLink = `${publicDir}zz-test-dir`;
  t.after(() => { rmSync(link, { force: true }); rmSync(dirLink, { force: true }); });
  symlinkSync(fileURLToPath(new URL('../../skills/android-design/gallery/store.mjs', import.meta.url)), link);
  symlinkSync(fileURLToPath(new URL('../../skills/android-design/gallery/', import.meta.url)), dirLink);
  for (const path of ['/assets/../../store.mjs', '/../store.mjs', '/%2e%2e/store.mjs', '/assets/%2e%2e/%2e%2e/store.mjs', '/%2e%2e%2fstore.mjs', '/zz-test-link.mjs', '/zz-test-dir/store.mjs', '/fonts', '/fonts/', '/assets']) {
    assert.equal(await rawStatus(url, path), 404, path);
  }
  assert.equal(await rawStatus(url, '/'), 200);
});

test('exposes a live agent session and hides an expired one', async t => {
  const { root, draft } = fixture(t);
  const store = new GalleryStore(root);
  store.publish(draft);
  const server = await startServer(root, 0);
  t.after(() => new Promise(resolve => server.close(resolve)));
  const url = `http://127.0.0.1:${server.address().port}`;
  const read = async () => (await (await fetch(`${url}/api/exploration`)).json()).session;
  store.writeSession('waiting', new Date(Date.now() + 60_000));
  assert.equal((await read()).state, 'waiting');
  store.writeSession('waiting', new Date(Date.now() - 1000));
  assert.equal(await read(), null);
});

test('serves published and draft proposal files with frameable CSP', async t => {
  const { url, root } = await setup(t);
  for (const path of ['/rounds/001/a/', '/rounds/001/a/index.html', '/draft/a/', '/draft/a/style.css']) {
    const response = await fetch(url + path);
    assert.equal(response.status, 200, path);
    const csp = response.headers.get('content-security-policy');
    assert.match(csp, /frame-ancestors 'self'/, path);
    assert.doesNotMatch(csp, /frame-ancestors 'none'/, path);
  }
  assert.equal((await fetch(`${url}/rounds/001/a/style.css`)).headers.get('content-type'), 'text/css; charset=utf-8');
  assert.equal(await (await fetch(`${url}/rounds/001/a/index.html`)).text(), readFileSync(join(root, 'project/proposals/a/index.html'), 'utf8'));
  const gallery = await fetch(url);
  assert.match(gallery.headers.get('content-security-policy'), /frame-ancestors 'none'/);
  assert.match(gallery.headers.get('content-security-policy'), /frame-src 'self'/);
  const redirect = await fetch(`${url}/draft/a`, { redirect: 'manual' });
  assert.equal(redirect.status, 301);
  assert.equal(redirect.headers.get('location'), '/draft/a/');
  assert.equal((await fetch(`${url}/runtime/m3e.js`)).status, 200);
});

test('draft edits show immediately but published files are frozen and verified', async t => {
  const { url, root } = await setup(t);
  writeFileSync(join(root, 'project/proposals/a/style.css'), 'body { color: var(--md-sys-color-primary); }');
  assert.match(await (await fetch(`${url}/draft/a/style.css`)).text(), /primary/);
  assert.match(await (await fetch(`${url}/rounds/001/a/style.css`)).text(), /static-tomato/);
  writeFileSync(join(root, 'rounds/001/proposals/a/style.css'), 'tampered');
  assert.equal((await fetch(`${url}/rounds/001/a/style.css`)).status, 409);
  assert.equal((await fetch(`${url}/rounds/001/a/index.html`)).status, 200);
});

test('proposal routes refuse traversal, symlinks, directories, secrets, and unknown ids', async t => {
  const { url, root } = await setup(t);
  const proposal = join(root, 'project/proposals/a');
  symlinkSync(join(root, 'feedback.json'), join(proposal, 'link.json'));
  symlinkSync(join(root, 'project'), join(proposal, 'dir-link'));
  mkdirSync(join(proposal, 'sub'));
  writeFileSync(join(proposal, '.env'), 'SECRET');
  symlinkSync(proposal, join(root, 'project/proposals/linked'));
  const paths = [
    '/draft/a/../../feedback.json', '/draft/a/%2e%2e/%2e%2e/feedback.json', '/draft/a/..%2f..%2ffeedback.json', '/draft/a/%5c..%5cfeedback.json',
    '/draft/a/link.json', '/draft/a/dir-link/proposals/a/index.html', '/draft/a/sub/', '/draft/a/.env', '/draft/linked/index.html', '/draft/missing/',
    '/draft/a/%00', '/draft/a/%zz', '/rounds/001/a/..%2f..%2f..%2ffeedback.json', '/rounds/001/a/.env', '/rounds/001/missing/', '/rounds/009/a/', '/rounds/001/a/%00',
  ];
  for (const path of paths) assert.equal(await rawStatus(url, path), 404, path);
});
