// A proposal written with scheme="auto" follows the OS: its light-dark() static colors must switch with the
// Material roles. Publishes the starter with an auto scheme and one static color, then reads it in light and dark.

import assert from 'node:assert/strict';
import { spawn, spawnSync } from 'node:child_process';
import { existsSync, mkdtempSync, readFileSync, realpathSync, rmSync, writeFileSync } from 'node:fs';
import { createServer } from 'node:net';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { after, before, test } from 'node:test';
import { fileURLToPath } from 'node:url';
import puppeteer from 'puppeteer-core';
import { chromeExecutable } from '../../evals/render.mjs';

const ROOT = join(dirname(fileURLToPath(import.meta.url)), '../..');
const CLI = join(ROOT, 'skills/android-design/gallery/cli.mjs');
const chrome = chromeExecutable();

function freePort() {
  return new Promise((resolve, reject) => {
    const server = createServer();
    server.once('error', reject);
    server.listen(0, '127.0.0.1', () => {
      const { port } = server.address();
      server.close(() => resolve(port));
    });
  });
}

async function waitFor(url) {
  const deadline = Date.now() + 20000;
  for (;;) {
    try { if ((await fetch(url)).ok) return; } catch { /* not up yet */ }
    if (Date.now() > deadline) throw new Error('gallery server did not start within 20 seconds');
    await new Promise(resolve => setTimeout(resolve, 200));
  }
}

if (!existsSync(chrome)) {
  test('auto scheme static colors', { skip: `Chrome not found at ${chrome}; install Google Chrome or set CHROME_PATH` }, () => {});
} else {
  let repo, server, browser, url;

  before(async () => {
    repo = realpathSync(mkdtempSync(join(tmpdir(), 'scheme-')));
    assert.equal(spawnSync('git', ['init', '-q', repo]).status, 0);
    const created = spawnSync('node', [CLI, 'create', '--project', repo, '--title', 'Scheme'], { encoding: 'utf8' });
    assert.equal(created.status, 0, created.stderr);
    const exploration = created.stdout.trim();
    const proposal = join(exploration, 'project/proposals/a');
    const html = join(proposal, 'index.html');
    writeFileSync(html, readFileSync(html, 'utf8').replace('scheme="light"', 'scheme="auto"').replace('</m3e-theme>', '<p class="tomato">Tomato</p></m3e-theme>'));
    const css = join(proposal, 'style.css');
    writeFileSync(css, `${readFileSync(css, 'utf8')}\n/* static colors */\n:root { --static-tomato: light-dark(#D84315, #FFAB91); }\n.tomato { color: var(--static-tomato); }\n`);
    const draft = join(repo, 'draft.json');
    writeFileSync(draft, JSON.stringify({ title: 'Scheme', proposals: [{ id: 'a', name: 'Scheme', rationale: 'Auto scheme.' }] }));
    const done = spawnSync('node', [CLI, 'publish', exploration, draft], { encoding: 'utf8' });
    assert.equal(done.status, 0, done.stdout + done.stderr);
    const port = await freePort();
    server = spawn('node', [CLI, 'serve', exploration, '--port', String(port)], { stdio: 'ignore' });
    await waitFor(`http://127.0.0.1:${port}/`);
    url = `http://127.0.0.1:${port}/rounds/001/a/`;
    browser = await puppeteer.launch({ executablePath: chrome, headless: true });
  });

  after(async () => {
    await browser?.close();
    server?.kill();
    if (repo) rmSync(repo, { recursive: true, force: true });
  });

  async function tomatoIn(scheme) {
    const page = await browser.newPage();
    try {
      await page.emulateMediaFeatures([{ name: 'prefers-color-scheme', value: scheme }]);
      await page.goto(url, { waitUntil: 'load' });
      await page.evaluate(() => document.fonts.ready);
      return await page.$eval('.tomato', el => getComputedStyle(el).color);
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
