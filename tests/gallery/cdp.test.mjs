import assert from 'node:assert/strict';
import { test } from 'node:test';
import { Page } from '../../skills/android-design/gallery/cdp.mjs';

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
