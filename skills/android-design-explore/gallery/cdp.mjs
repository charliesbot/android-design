// A minimal Chrome DevTools Protocol driver with no npm dependency and no open port.
// Chrome runs headless with --remote-debugging-pipe: we write NUL-terminated JSON to its fd 3 and read it from fd 4.
import { spawn } from 'node:child_process';
import { existsSync, mkdtempSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { delimiter, join } from 'node:path';

const MAC_CHROME = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const LINUX_CHROME = ['google-chrome', 'google-chrome-stable', 'chromium', 'chromium-browser'];

// CHROME_PATH, else the macOS default, else a common Linux name on PATH.
export function chromeExecutable({ env = process.env, exists = existsSync } = {}) {
  if (env.CHROME_PATH) {
    if (!exists(env.CHROME_PATH)) throw new Error(`CHROME_PATH does not exist: ${env.CHROME_PATH}`);
    return env.CHROME_PATH;
  }
  if (exists(MAC_CHROME)) return MAC_CHROME;
  for (const dir of (env.PATH ?? '').split(delimiter).filter(Boolean)) {
    for (const name of LINUX_CHROME) if (exists(join(dir, name))) return join(dir, name);
  }
  throw new Error('Chrome not found. Install Google Chrome or Chromium, or set CHROME_PATH to its executable.');
}

const TIMEOUT = 30000;

export class Browser {
  #child; #profile; #exited = false; #next = 1; #pending = new Map(); #listeners = []; #buffer = '';

  get pid() { return this.#child.pid; }
  get profile() { return this.#profile; }

  static async launch(executable = chromeExecutable()) {
    const browser = new Browser();
    browser.#profile = mkdtempSync(join(tmpdir(), 'android-design-chrome-'));
    const flags = ['--headless=new', '--remote-debugging-pipe', `--user-data-dir=${browser.#profile}`, '--no-first-run',
      '--no-default-browser-check', '--disable-extensions', '--hide-scrollbars', '--force-color-profile=srgb', '--mute-audio', 'about:blank'];
    // stdio[3] is Chrome's fd 3 (we write commands), stdio[4] its fd 4 (we read replies).
    const child = spawn(executable, flags, { stdio: ['ignore', 'ignore', 'ignore', 'pipe', 'pipe'] });
    browser.#child = child;
    child.on('error', error => browser.#fail(error));
    child.on('exit', () => { browser.#exited = true; browser.#fail(new Error('Chrome exited')); });
    child.stdio[4].on('data', chunk => browser.#read(chunk));
    child.stdio[3].on('error', () => {});
    return browser;
  }

  #fail(error) {
    for (const { reject } of this.#pending.values()) reject(error);
    this.#pending.clear();
  }

  #read(chunk) {
    this.#buffer += chunk.toString('utf8');
    for (let end = this.#buffer.indexOf('\0'); end >= 0; end = this.#buffer.indexOf('\0')) {
      const message = JSON.parse(this.#buffer.slice(0, end));
      this.#buffer = this.#buffer.slice(end + 1);
      if (message.id) {
        const call = this.#pending.get(message.id);
        this.#pending.delete(message.id);
        if (message.error) call?.reject(new Error(`${call.method}: ${message.error.message}`));
        else call?.resolve(message.result);
      } else for (const listener of [...this.#listeners]) listener(message);
    }
  }

  send(method, params = {}, sessionId) {
    const id = this.#next++;
    if (this.#exited) return Promise.reject(new Error('Chrome exited'));
    return new Promise((resolve, reject) => {
      const timer = setTimeout(() => { this.#pending.delete(id); reject(new Error(`${method} timed out`)); }, TIMEOUT);
      this.#pending.set(id, { method, resolve: value => { clearTimeout(timer); resolve(value); }, reject: error => { clearTimeout(timer); reject(error); } });
      this.#child.stdio[3].write(`${JSON.stringify({ id, method, params, ...(sessionId && { sessionId }) })}\0`);
    });
  }

  // Calls `handler(params)` for each event of `method` in a session. Returns a function that stops listening.
  on(sessionId, method, handler) {
    const listener = message => { if (message.method === method && message.sessionId === sessionId) handler(message.params); };
    this.#listeners.push(listener);
    return () => { this.#listeners = this.#listeners.filter(item => item !== listener); };
  }

  async newPage() {
    const { targetId } = await this.send('Target.createTarget', { url: 'about:blank' });
    const { sessionId } = await this.send('Target.attachToTarget', { targetId, flatten: true });
    const page = new Page(this, sessionId, targetId);
    await page.send('Page.enable');
    return page;
  }

  async close() {
    const child = this.#child;
    if (!this.#exited) {
      const exited = new Promise(resolve => child.once('exit', resolve));
      this.send('Browser.close').catch(() => {});
      const timer = setTimeout(() => child.kill('SIGKILL'), 5000);
      await exited;
      clearTimeout(timer);
    }
    rmSync(this.#profile, { recursive: true, force: true });
  }
}

export class Page {
  constructor(browser, sessionId, targetId) { this.browser = browser; this.sessionId = sessionId; this.targetId = targetId; }

  send(method, params) { return this.browser.send(method, params, this.sessionId); }

  setViewport(width, height) {
    return this.send('Emulation.setDeviceMetricsOverride', { width, height, deviceScaleFactor: 1, mobile: false });
  }

  emulateColorScheme(value) {
    return this.send('Emulation.setEmulatedMedia', { features: [{ name: 'prefers-color-scheme', value }] });
  }

  // Answers every request from `route(URL)`, which returns { type, body } or null (a 404). Nothing reaches the network:
  // only data:, blob:, and about: URLs pass through.
  async serve(route, onMissing = () => {}) {
    await this.send('Fetch.enable', { patterns: [{ urlPattern: '*' }] });
    // A request can still be paused when its page closes, so a late reply may find no session; nothing waits on it.
    const reply = (method, params) => this.send(method, params).catch(() => {});
    this.browser.on(this.sessionId, 'Fetch.requestPaused', async ({ requestId, request }) => {
      const url = new URL(request.url);
      if (['data:', 'blob:', 'about:'].includes(url.protocol)) return reply('Fetch.continueRequest', { requestId });
      const found = route(url);
      if (!found) onMissing(request.url);
      const response = found ?? { type: 'text/plain', body: Buffer.from('Not found'), status: 404 };
      return reply('Fetch.fulfillRequest', {
        requestId, responseCode: response.status ?? 200,
        responseHeaders: [{ name: 'Content-Type', value: response.type }, { name: 'Cache-Control', value: 'no-store' }],
        body: Buffer.from(response.body).toString('base64'),
      });
    });
  }

  async goto(url) {
    let stop, timer;
    const loaded = new Promise((resolve, reject) => {
      stop = this.browser.on(this.sessionId, 'Page.loadEventFired', resolve);
      timer = setTimeout(() => reject(new Error(`Loading ${url} timed out`)), TIMEOUT);
    });
    try {
      const { errorText } = await this.send('Page.navigate', { url });
      if (errorText) throw new Error(`Cannot load ${url}: ${errorText}`);
      await loaded;
    } finally {
      stop();
      clearTimeout(timer);
      loaded.catch(() => {});
    }
  }

  // Runs `fn(arg)` in the page (awaiting a returned promise) and returns its JSON value.
  async evaluate(fn, arg) {
    const { result, exceptionDetails } = await this.send('Runtime.evaluate', {
      expression: `(${fn})(${JSON.stringify(arg ?? null)})`, awaitPromise: true, returnByValue: true,
    });
    if (exceptionDetails) throw new Error(exceptionDetails.exception?.description ?? exceptionDetails.text);
    return result.value;
  }

  async screenshot() {
    const { data } = await this.send('Page.captureScreenshot', { format: 'png' });
    return Buffer.from(data, 'base64');
  }

  close() { return this.browser.send('Target.closeTarget', { targetId: this.targetId }).catch(() => {}); }
}
