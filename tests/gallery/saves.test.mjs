import test from 'node:test';
import assert from 'node:assert/strict';
import { FeedbackSaves } from '../../skills/android-design/gallery/public/saves.mjs';

const response = (revision, status = 200) => ({ ok: status === 200, status, json: async () => status === 200 ? { revision, proposals: {}, approved: null } : { error: 'Save failed' } });
test('serializes writes, keeps newest edits, and updates revisions', async () => {
  let release;
  const sent = [];
  const saves = new FeedbackSaves(0, () => {}, async body => {
    sent.push(body);
    if (sent.length === 1) await new Promise(resolve => { release = resolve; });
    return response(sent.length);
  });
  saves.enqueue('001', '001/a', 'first');
  await Promise.resolve();
  saves.enqueue('001', '001/a', 'latest');
  saves.enqueue('002', 'none', 'other');
  release();
  await saves.flush();
  assert.deepEqual(sent.map(x => [x.roundId, x.revision, x.note]), [['001', 0, 'first'], ['001', 1, 'latest'], ['002', 2, 'other']]);
  assert.equal(saves.pending.size, 0);
});
test('retains HTTP failures for explicit retry and does not silently overwrite conflicts', async () => {
  let status = 500;
  const saves = new FeedbackSaves(0, () => {}, async () => response(1, status));
  saves.enqueue('001', '001/a', 'do not lose');
  assert.equal(await saves.flush(), false);
  assert.equal(saves.pending.size, 1);
  status = 200;
  await saves.retry();
  assert.equal(saves.pending.size, 0);
  status = 409;
  saves.enqueue('001', null, 'conflict');
  assert.equal(await saves.flush(), false);
  status = 200;
  assert.equal(await saves.retry(), false);
  assert.equal(saves.pending.size, 1);
});
