import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';
import vm from 'node:vm';

// Execute the picker script with controlled timers, transport, and DOM elements.
async function picker() {
  const html = await readFile(new URL('./picker.html', import.meta.url), 'utf8');
  let script = html.match(/<script(?: type="module")?>([\s\S]*?)<\/script>/)[1];
  const elements = new Map();
  const timers = new Map();
  const events = new Map();
  const requests = [];
  let timerId = 0;
  const element = id => {
    if (!elements.has(id)) elements.set(id, { textContent: '', hidden: false });
    return elements.get(id);
  };
  const context = vm.createContext({
    document: { getElementById: element, addEventListener() {} },
    window: {
      addEventListener: (name, fn) => events.set(name, fn),
      removeEventListener: name => events.delete(name),
    },
    setTimeout: fn => { timers.set(++timerId, fn); return timerId; },
    clearTimeout: id => timers.delete(id),
    fetch: (url, options) => new Promise((resolve, reject) => {
      requests.push({ body: JSON.parse(options.body), resolve, reject });
    }),
  });
  // The production queue is evaluated in this same realm, with the same fake timers.
  if (script.includes('import { PreferenceSaves }')) {
    const queue = await readFile(new URL('./saves.mjs', import.meta.url), 'utf8');
    vm.runInContext(queue.replace('export class', 'class'), context);
    script = script.replace(/^import .*;\n/m, '');
  }
  vm.runInContext(script.replace(/load\(\);\s*$/, ''), context);
  vm.runInContext('round = { cases: [], selections: {} }; renderHeader = () => {};', context);
  const flush = async () => { for (let n = 0; n < 15; n++) await Promise.resolve(); };
  return {
    requests, events, element,
    edit(name, note) {
      vm.runInContext(`pickFor(${JSON.stringify(name)}).note = ${JSON.stringify(note)}; save(${JSON.stringify(name)});`, context);
    },
    async tick() {
      const callbacks = [...timers.values()];
      timers.clear();
      for (const fn of callbacks) fn();
      await flush();
    },
    async respond(index, ok = true) { requests[index].resolve({ ok, status: ok ? 200 : 500 }); await flush(); },
    async reject(index) { requests[index].reject(new Error('offline')); await flush(); },
    async retry() { element('retry-save').onclick(); await flush(); },
  };
}

test('rapid changes to different cases are both saved', async () => {
  const p = await picker();
  p.edit('music', 'first'); p.edit('finance', 'second');
  await p.tick();
  await p.respond(0);
  assert.deepEqual(p.requests.map(r => r.body.case), ['music', 'finance']);
  await p.respond(1);
  assert.equal(p.element('saved').textContent, 'All changes saved');
});

test('edits during a request wait and retain the newest snapshot', async () => {
  const p = await picker();
  p.edit('music', 'old'); await p.tick();
  p.edit('music', 'new'); await p.tick();
  assert.equal(p.requests.length, 1);
  await p.respond(0);
  assert.equal(p.requests[1].body.note, 'new');
  assert.notEqual(p.element('saved').textContent, 'All changes saved');
  await p.respond(1);
  assert.equal(p.element('saved').textContent, 'All changes saved');
});

test('HTTP errors keep edits pending and retry the newest values', async () => {
  const p = await picker();
  p.edit('music', 'old'); await p.tick();
  await p.respond(0, false);
  assert.match(p.element('saved').textContent, /not saved/i);
  assert.equal(p.element('retry-save').hidden, false);
  p.edit('music', 'new'); p.edit('finance', 'also pending'); await p.tick();
  assert.equal(p.requests.length, 1);
  await p.retry();
  assert.equal(p.requests[1].body.note, 'new');
  await p.respond(1); await p.respond(2);
  assert.equal(p.element('saved').textContent, 'All changes saved');
  assert.equal(p.element('retry-save').hidden, true);
});

test('network failures remain retryable', async () => {
  const p = await picker();
  p.edit('music', 'pending'); await p.tick();
  await p.reject(0);
  assert.match(p.element('saved').textContent, /not saved/i);
  await p.retry(); await p.respond(1);
  assert.equal(p.element('saved').textContent, 'All changes saved');
});

test('warn before leaving only while changes are pending', async () => {
  const p = await picker();
  assert.equal(p.events.has('beforeunload'), false);
  p.edit('music', 'pending');
  assert.equal(p.events.has('beforeunload'), true);
  let prevented = false;
  p.events.get('beforeunload')({ preventDefault: () => { prevented = true; } });
  assert.equal(prevented, true);
  await p.tick(); await p.respond(0);
  assert.equal(p.events.has('beforeunload'), false);
});
