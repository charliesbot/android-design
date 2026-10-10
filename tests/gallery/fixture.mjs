import { mkdtempSync, mkdirSync, writeFileSync, rmSync, realpathSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

export function fixture(t) {
  const root = realpathSync(mkdtempSync(join(tmpdir(), 'gallery-test-')));
  t.after(() => rmSync(root, { recursive: true, force: true }));
  mkdirSync(join(root, 'project'));
  writeFileSync(join(root, '.android-design-exploration.json'), JSON.stringify({ version: 1, path: root }));
  writeFileSync(join(root, 'project/settings.gradle.kts'), 'rootProject.name = "Test"');
  writeFileSync(join(root, 'exploration.json'), JSON.stringify({ version: 1, title: 'Reading', rounds: [] }));
  writeFileSync(join(root, 'feedback.json'), JSON.stringify({ revision: 0, rounds: {}, approved: null }));
  // Valid 1x1 PNG used only for storage/HTTP tests, never visual evidence.
  const png = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII=', 'base64');
  writeFileSync(join(root, 'project/test.png'), png);
  const proposal = { id: 'a', name: 'Editorial', rationale: 'Prioritizes reading.', images: Object.fromEntries(['phone', 'tablet', 'desktop', 'foldable'].map(d => [d, 'test.png'])) };
  const draft = { title: 'First directions', proposals: [proposal] };
  return { root, draft };
}
