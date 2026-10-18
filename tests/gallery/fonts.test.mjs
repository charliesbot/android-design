import test from 'node:test';
import assert from 'node:assert/strict';
import { readdirSync, readFileSync } from 'node:fs';

const SKILL = new URL('../../skills/android-design-explore/assets/fonts/', import.meta.url);
const SANDBOX = new URL('../../evals/compose-starter/core/designsystem/common/src/main/res/font/', import.meta.url);
const ttfs = dir => readdirSync(dir).filter(name => name.endsWith('.ttf')).sort();

test('the skill ships the same TTFs as the Compose sandbox', () => {
  const skill = ttfs(SKILL);
  assert.ok(skill.length > 0);
  assert.deepEqual(skill, ttfs(SANDBOX));
  for (const name of skill) {
    assert.ok(readFileSync(new URL(name, SKILL)).equals(readFileSync(new URL(name, SANDBOX))), `${name} differs`);
  }
});
