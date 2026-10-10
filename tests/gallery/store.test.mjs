import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdirSync, writeFileSync, readFileSync, rmSync, symlinkSync } from 'node:fs';
import { join } from 'node:path';
import { GalleryStore } from '../../skills/android-design/gallery/store.mjs';

import { fixture } from './fixture.mjs';

test('publication preserves source and images across rounds and restart', t => {
  const { root, draft } = fixture(t);
  new GalleryStore(root).publish(draft);
  writeFileSync(join(root, 'project/settings.gradle.kts'), 'revised');
  new GalleryStore(root).publish(draft);
  const data = new GalleryStore(root).read();
  assert.equal(data.rounds.length, 2);
  assert.equal(readFileSync(join(root, 'rounds/001/source/settings.gradle.kts'), 'utf8'), 'rootProject.name = "Test"');
  assert.equal(data.rounds[0].proposals[0].images.phone.width, 1);
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

test('publishing or unknown feedback cannot corrupt an existing exploration', t => {
  const { root, draft } = fixture(t);
  const original = readFileSync(join(root, 'exploration.json'), 'utf8');
  delete draft.proposals[0].images.tablet;
  assert.throws(() => new GalleryStore(root).publish(draft), /four|tablet/);
  assert.equal(readFileSync(join(root, 'exploration.json'), 'utf8'), original);
  assert.throws(() => new GalleryStore(root).save({ revision: 0, roundId: 'unknown', choice: null, note: '' }), /Unknown/);
});

test('reject traversal, symlinks, duplicate ids, and invalid images', t => {
  for (const path of ['../feedback.json', '/etc/passwd']) {
    const { root, draft } = fixture(t);
    draft.proposals[0].images.phone = path;
    assert.throws(() => new GalleryStore(root).publish(draft), /path|inside/);
  }
  const { root, draft } = fixture(t);
  symlinkSync(join(root, 'feedback.json'), join(root, 'project/link.png'));
  draft.proposals[0].images.phone = 'link.png';
  assert.throws(() => new GalleryStore(root).publish(draft), /symlink/);
  rmSync(join(root, 'project/link.png'));
  draft.proposals[0].images.phone = 'settings.gradle.kts';
  assert.throws(() => new GalleryStore(root).publish(draft), /PNG/);
  draft.proposals[0].images.phone = 'test.png';
  draft.proposals.push(draft.proposals[0]);
  assert.throws(() => new GalleryStore(root).publish(draft), /Duplicate/);
});

test('excludes build caches and secrets from source snapshots', t => {
  const { root, draft } = fixture(t);
  mkdirSync(join(root, 'project/build'));
  writeFileSync(join(root, 'project/build/generated'), 'cache');
  writeFileSync(join(root, 'project/.env'), 'PRIVATE');
  writeFileSync(join(root, 'project/local.properties'), 'sdk.dir=private');
  new GalleryStore(root).publish(draft);
  const files = new GalleryStore(root).read().rounds[0].sourceFiles;
  assert.equal(files.some(f => /build|\.env|local.properties/.test(f.path)), false);
});

test('mutated published image is rejected rather than approved', t => {
  const { root, draft } = fixture(t);
  new GalleryStore(root).publish(draft);
  const store = new GalleryStore(root);
  store.save({ revision: 0, roundId: '001', choice: '001/a', note: '' });
  writeFileSync(join(root, 'rounds/001/images/a-phone.png'), 'different');
  assert.throws(() => store.approve({ revision: 1, key: '001/a' }), /changed/);
});

test('source mutation prevents approval and another store cannot overwrite a newer note', t => {
  const { root, draft } = fixture(t);
  new GalleryStore(root).publish(draft);
  const first = new GalleryStore(root), second = new GalleryStore(root);
  first.save({ revision: 0, roundId: '001', choice: '001/a', note: 'Newest note' });
  assert.throws(() => second.save({ revision: 0, roundId: '001', choice: null, note: 'Stale' }), /changed/);
  writeFileSync(join(root, 'rounds/001/source/settings.gradle.kts'), 'changed after rendering');
  assert.throws(() => second.approve({ revision: 1, key: '001/a' }), /source changed/);
  assert.equal(first.read().feedback.rounds['001'].note, 'Newest note');
  assert.equal(first.read().feedback.approved, null);
});

test('one choice per round, explicit refinement snapshots, and none-of-these requests', t => {
  const { root, draft } = fixture(t);
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
