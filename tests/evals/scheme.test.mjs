// A proposal written with scheme="auto" follows the OS: its light-dark() static colors must switch with the
// Material roles. Loads the starter with an auto scheme and one static color, then reads it in light and dark.

import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { mkdtempSync, readFileSync, realpathSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { after, before, test } from 'node:test';
import { fileURLToPath } from 'node:url';
import { Browser, chromeExecutable } from '../../skills/android-design-explore/gallery/cdp.mjs';
import { ORIGIN, makeRoute } from '../../skills/android-design-explore/gallery/render.mjs';

const ROOT = join(dirname(fileURLToPath(import.meta.url)), '../..');
const CLI = join(ROOT, 'skills/android-design-explore/gallery/cli.mjs');
let chrome;
try { chrome = chromeExecutable(); } catch { chrome = null; }

if (!chrome) {
  test('auto scheme static colors', { skip: 'Chrome not found; install Google Chrome or set CHROME_PATH' }, () => {});
} else {
  let repo, browser, route;

  before(async () => {
    repo = realpathSync(mkdtempSync(join(tmpdir(), 'scheme-')));
    assert.equal(spawnSync('git', ['init', '-q', repo]).status, 0);
    const created = spawnSync('node', [CLI, 'create', '--project', repo, '--title', 'Scheme'], { encoding: 'utf8' });
    assert.equal(created.status, 0, created.stderr);
    const proposals = join(created.stdout.trim(), 'project/proposals');
    const html = join(proposals, 'a/index.html');
    writeFileSync(html, readFileSync(html, 'utf8').replace('scheme="light"', 'scheme="auto"').replace('</m3e-theme>', '<p class="tomato">Tomato</p></m3e-theme>'));
    const css = join(proposals, 'a/style.css');
    writeFileSync(css, `${readFileSync(css, 'utf8')}\n/* static colors */\n:root { --static-tomato: light-dark(#D84315, #FFAB91); }\n.tomato { color: var(--static-tomato); }\n`);
    route = makeRoute({ proposalsDir: proposals });
    browser = await Browser.launch(chrome);
  });

  after(async () => {
    await browser?.close();
    if (repo) rmSync(repo, { recursive: true, force: true });
  });

  async function tomatoIn(scheme) {
    const page = await browser.newPage();
    try {
      await page.serve(route);
      await page.emulateColorScheme(scheme);
      await page.goto(`${ORIGIN}/draft/a/`);
      return await page.evaluate(async () => {
        await document.fonts.ready;
        return getComputedStyle(document.querySelector('.tomato')).color;
      });
    } finally {
      await page.close();
    }
  }

  test('auto scheme static colors follow the OS light setting', async () => {
    assert.equal(await tomatoIn('light'), 'rgb(216, 67, 21)');
  });

  test('auto scheme static colors follow the OS dark setting', async () => {
    assert.equal(await tomatoIn('dark'), 'rgb(255, 171, 145)');
  });
}
