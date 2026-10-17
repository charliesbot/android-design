import assert from 'node:assert/strict';
import { test } from 'node:test';
import { existsSync } from 'node:fs';
import { Browser, Page, chromeExecutable } from '../../skills/android-design-explore/gallery/cdp.mjs';

// A request can still be paused when its page closes; the late reply then fails with
// "Session with given id not found". That must not surface as an unhandled rejection.
test('a reply to a request whose page closed is not an unhandled rejection', async () => {
  let paused;
  const browser = {
    on: (_session, event, handler) => { if (event === 'Fetch.requestPaused') paused = handler; return () => {}; },
    send: async method => {
      if (method === 'Fetch.enable') return {};
      throw new Error(`${method}: Session with given id not found.`);
    },
  };
  const page = new Page(browser, 'session', 'target');
  page.send = (method, params) => browser.send(method, params);
  await page.serve(() => ({ type: 'text/plain', body: Buffer.from('ok') }));
  const unhandled = [];
  const onUnhandled = reason => unhandled.push(reason);
  process.on('unhandledRejection', onUnhandled);
  try {
    paused({ requestId: '1', request: { url: 'https://proposal.invalid/draft/a/' } });
    paused({ requestId: '2', request: { url: 'data:text/plain,hi' } });
    await new Promise(resolve => setTimeout(resolve, 20));
  } finally {
    process.off('unhandledRejection', onUnhandled);
  }
  assert.deepEqual(unhandled, []);
});

test('goto rejects promptly when navigation fails and leaves no timer to reject later', async t => {
  t.mock.timers.enable({ apis: ['setTimeout'] });
  const browser = { on: () => () => {}, send: async () => ({ errorText: 'net::ERR_FAILED' }) };
  const page = new Page(browser, 'session', 'target');
  const unhandled = [];
  const onUnhandled = reason => unhandled.push(reason);
  process.on('unhandledRejection', onUnhandled);
  try {
    await assert.rejects(page.goto('https://proposal.invalid/'), /net::ERR_FAILED/);
    t.mock.timers.tick(31000);
    await new Promise(resolve => setImmediate(resolve));
  } finally {
    process.off('unhandledRejection', onUnhandled);
  }
  assert.deepEqual(unhandled, []);
});

let chrome;
try { chrome = chromeExecutable(); } catch { chrome = null; }

test('a Chrome killed by a signal rejects operations, close() resolves, and the profile is removed', { skip: chrome ? false : 'Chrome not found' }, async () => {
  const browser = await Browser.launch(chrome);
  await browser.newPage();
  process.kill(browser.pid, 'SIGKILL');
  await new Promise(resolve => setTimeout(resolve, 500));
  await assert.rejects(browser.newPage(), /Chrome exited/);
  await Promise.race([browser.close(), new Promise((_, reject) => setTimeout(() => reject(new Error('close() hung')), 8000))]);
  assert.equal(existsSync(browser.profile), false);
});
