import test from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, mkdirSync, writeFileSync, readFileSync, rmSync, symlinkSync } from 'node:fs';
import { join } from 'node:path';
import { GalleryStore } from '../../skills/android-design/gallery/store.mjs';

import { createHash } from 'node:crypto';
import { fixture, writeProposal, PAGE } from './fixture.mjs';

test('publication snapshots proposal folders with hashes across rounds and restart', t => {
  const { root, draft } = fixture(t);
  const round = new GalleryStore(root).publish(draft);
  const sha = createHash('sha256').update(PAGE).digest('hex');
  assert.deepEqual(round.proposals[0].files[0], { path: 'index.html', sha256: sha });
  assert.deepEqual(round.proposals[0].files.map(f => f.path), ['index.html', 'style.css']);
  assert.equal(round.proposals[0].entry, 'index.html');
  assert.equal('sourceFiles' in round, false);
  assert.match(round.sourceHash, /^[0-9a-f]{64}$/);
  writeFileSync(join(root, 'project/proposals/a/index.html'), PAGE.replace('Hello', 'Revised'));
  new GalleryStore(root).publish(draft);
  const data = new GalleryStore(root).read();
  assert.equal(data.rounds.length, 2);
  assert.notEqual(data.rounds[0].sourceHash, data.rounds[1].sourceHash);
  assert.equal(readFileSync(join(root, 'rounds/001/proposals/a/index.html'), 'utf8'), PAGE);
  assert.equal(readFileSync(join(root, 'rounds/002/proposals/a/index.html'), 'utf8').includes('Revised'), true);
});

test('feedback uses optimistic revision and approval is terminal', t => {
  const { root, draft } = fixture(t);
  new GalleryStore(root).publish(draft);
  const store = new GalleryStore(root);
  let state = store.read().feedback;
  state = store.save({ revision: state.revision, roundId: '001', choice: '001/a', note: 'Keep hierarchy' });
  assert.equal(state.rounds['001'].note, 'Keep hierarchy');
  assert.throws(() => store.save({ revision: state.revision - 1, roundId: '001', choice: null, note: '' }), /changed/);
  state = store.approve({ revision: state.revision, key: '001/a' });
  assert.equal(new GalleryStore(root).read().feedback.approved.key, '001/a');
  assert.throws(() => store.save({ revision: state.revision, roundId: '001', choice: '001/a', note: 'late' }), /approved/);
  assert.throws(() => new GalleryStore(root).publish(draft), /approved/);
});

test('a rejected publish cannot corrupt an existing exploration', t => {
  const { root, draft } = fixture(t);
  const original = readFileSync(join(root, 'exploration.json'), 'utf8');
  draft.proposals.push({ id: 'b', name: 'Missing', rationale: 'No folder.' });
  assert.throws(() => new GalleryStore(root).publish(draft), /Missing proposal folder/);
  assert.equal(readFileSync(join(root, 'exploration.json'), 'utf8'), original);
  assert.equal(existsSync(join(root, 'rounds')), false);
  assert.throws(() => new GalleryStore(root).save({ revision: 0, roundId: 'unknown', choice: null, note: '' }), /Unknown/);
});

test('the old images draft field is rejected', t => {
  const { root, draft } = fixture(t);
  draft.proposals[0].images = { phone: 'renders/a-phone.png' };
  assert.throws(() => new GalleryStore(root).publish(draft), /no longer take images/);
  delete draft.proposals[0].images;
  draft.images = {};
  assert.throws(() => new GalleryStore(root).publish(draft), /no longer take images/);
});

test('reject missing index, bad ids, symlinks, and duplicate ids', t => {
  const { root, draft } = fixture(t);
  rmSync(join(root, 'project/proposals/a/index.html'));
  assert.throws(() => new GalleryStore(root).publish(draft), /index\.html/);
  writeProposal(root, 'a');
  for (const id of ['../x', 'A', 'a/b', '1a']) {
    assert.throws(() => new GalleryStore(root).publish({ ...draft, proposals: [{ ...draft.proposals[0], id }] }), /Invalid proposal id/);
  }
  symlinkSync(join(root, 'feedback.json'), join(root, 'project/proposals/a/link.json'));
  assert.throws(() => new GalleryStore(root).publish(draft), /symlink/);
  rmSync(join(root, 'project/proposals/a/link.json'));
  symlinkSync(join(root, 'project/proposals/a'), join(root, 'project/proposals/b'));
  assert.throws(() => new GalleryStore(root).publish({ ...draft, proposals: [{ ...draft.proposals[0], id: 'b' }] }), /symlink/);
  draft.proposals.push(draft.proposals[0]);
  assert.throws(() => new GalleryStore(root).publish(draft), /Duplicate/);
});

test('excludes build caches and secrets from proposal snapshots', t => {
  const { root, draft } = fixture(t);
  mkdirSync(join(root, 'project/proposals/a/node_modules'));
  writeFileSync(join(root, 'project/proposals/a/node_modules/generated'), 'cache');
  writeFileSync(join(root, 'project/proposals/a/.env'), 'PRIVATE');
  writeFileSync(join(root, 'project/proposals/a/signing.key'), 'PRIVATE');
  new GalleryStore(root).publish(draft);
  const files = new GalleryStore(root).read().rounds[0].proposals[0].files;
  assert.deepEqual(files.map(f => f.path), ['index.html', 'style.css']);
});

test('mutated published file is rejected rather than approved', t => {
  const { root, draft } = fixture(t);
  new GalleryStore(root).publish(draft);
  const store = new GalleryStore(root);
  store.save({ revision: 0, roundId: '001', choice: '001/a', note: '' });
  writeFileSync(join(root, 'rounds/001/proposals/a/style.css'), 'body { color: red; }');
  assert.throws(() => store.approve({ revision: 1, key: '001/a' }), /changed/);
  assert.equal(store.read().feedback.approved, null);
});

test('an added published file prevents approval and another store cannot overwrite a newer note', t => {
  const { root, draft } = fixture(t);
  new GalleryStore(root).publish(draft);
  const first = new GalleryStore(root), second = new GalleryStore(root);
  first.save({ revision: 0, roundId: '001', choice: '001/a', note: 'Newest note' });
  assert.throws(() => second.save({ revision: 0, roundId: '001', choice: null, note: 'Stale' }), /changed/);
  writeFileSync(join(root, 'rounds/001/proposals/a/extra.js'), 'added after publishing');
  assert.throws(() => second.approve({ revision: 1, key: '001/a' }), /proposal changed/);
  assert.equal(first.read().feedback.rounds['001'].note, 'Newest note');
  assert.equal(first.read().feedback.approved, null);
});

test('one choice per round, explicit refinement snapshots, and none-of-these requests', t => {
  const { root, draft } = fixture(t);
  writeProposal(root, 'b');
  draft.proposals.push({ ...draft.proposals[0], id: 'b', name: 'Alternative' });
  new GalleryStore(root).publish(draft);
  const store = new GalleryStore(root);
  store.save({ revision: 0, roundId: '001', choice: '001/a', note: 'First thought' });
  store.save({ revision: 1, roundId: '001', choice: '001/b', note: 'Use A colors' });
  assert.throws(() => store.approve({ revision: 2, key: '001/a' }), /selected/);
  let state = store.refine({ revision: 2, roundId: '001' });
  assert.equal(state.approved, null);
  assert.equal(state.rounds['001'].choice, '001/b');
  assert.equal(state.rounds['001'].requests[0].note, 'Use A colors');
  state = store.save({ revision: state.revision, roundId: '001', choice: 'none', note: '' });
  assert.throws(() => store.refine({ revision: state.revision, roundId: '001' }), /Describe/);
  state = store.save({ revision: state.revision, roundId: '001', choice: 'none', note: 'Try a simpler layout' });
  state = store.refine({ revision: state.revision, roundId: '001' });
  assert.equal(state.rounds['001'].requests.length, 2);
  assert.equal(state.rounds['001'].requests[0].choice, '001/b');
  assert.equal(state.rounds['001'].requests[1].choice, 'none');
  assert.equal(new GalleryStore(root).read().feedback.rounds['001'].requests.length, 2);
  new GalleryStore(root).publish(draft);
  assert.equal(store.read().rounds.length, 2);
  assert.throws(() => store.save({ revision: state.revision, roundId: '002', choice: '001/b', note: '' }), /round/);
});
