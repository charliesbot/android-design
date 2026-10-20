// view.html: one proposal as a single file that works when double-clicked (file://, no server).
// The proposal page is inlined (its CSS, scripts, and images become one document) and shown in an iframe next to a
// theme panel. The runtime (runtime.css, m3e.js, fonts) is carried once and shared: the viewer turns each file into a
// Blob URL and both the viewer chrome and the proposal frame load that copy.

const REMOTE = /^(?:[a-z][a-z0-9+.-]*:|\/\/|#)/i;

export const dataUri = (type, body) => `data:${type};base64,${Buffer.from(body).toString('base64')}`;

// `fetchLocal(URL)` returns { type, body } for a file that belongs to the proposal or the runtime, else null.
// A reference that is remote, a data: URI, or not found is left as written.
function inlineRef(ref, base, fetchLocal) {
  if (!ref || REMOTE.test(ref) && !/^https:\/\/proposal\.invalid\//.test(ref)) return null;
  const found = fetchLocal(new URL(ref, base));
  return found ? { ...found, url: new URL(ref, base) } : null;
}

export function inlineCss(css, base, fetchLocal) {
  return css.replace(/url\(\s*(["']?)([^"')]+)\1\s*\)/gi, (match, _quote, ref) => {
    const found = inlineRef(ref, base, fetchLocal);
    return found ? `url("${dataUri(found.type, found.body)}")` : match;
  }).replace(/<\/style/gi, '<\\/style');
}

// Source text can sit inside a <script>: keep it from closing the tag or opening an HTML comment.
const scriptSafe = code => code.replace(/<\/script/gi, '<\\/script').replace(/<!--/g, '<\\!--');

// The runtime files every proposal loads. `inlineHtml` leaves them out of the document, as the bare path below,
// because the viewer shares one copy of them with the proposal frame.
export const RUNTIME_CSS = '/runtime/runtime.css';
export const RUNTIME_JS = '/runtime/m3e.js';
const SHARED = [RUNTIME_CSS, RUNTIME_JS];

// The bare path when `ref` points at a shared runtime file, else null.
function sharedPath(ref, base) {
  if (!ref || REMOTE.test(ref) && !/^https:\/\/proposal\.invalid\//.test(ref)) return null;
  const { pathname } = new URL(ref, base);
  return SHARED.includes(pathname) ? pathname : null;
}

export function inlineHtml(html, pageUrl, fetchLocal) {
  const text = (ref, base) => {
    const found = inlineRef(ref, base, fetchLocal);
    return found && { ...found, text: Buffer.from(found.body).toString('utf8') };
  };
  return html
    .replace(/<link\b[^>]*>/gi, tag => {
      const href = /\shref\s*=\s*(["'])(.*?)\1/i.exec(tag)?.[2];
      if (/\srel\s*=\s*["']?stylesheet/i.test(tag)) {
        const shared = sharedPath(href, pageUrl);
        if (shared) return tag.replace(href, shared);
        const found = text(href, pageUrl);
        return found ? `<style>${inlineCss(found.text, found.url, fetchLocal)}</style>` : tag;
      }
      const found = /\srel\s*=\s*["'][^"']*icon/i.test(tag) ? inlineRef(href, pageUrl, fetchLocal) : null;
      return found ? tag.replace(href, dataUri(found.type, found.body)) : tag;
    })
    .replace(/<script\b([^>]*)\ssrc\s*=\s*(["'])(.*?)\2([^>]*)><\/script>/gi, (tag, before, _quote, src, after) => {
      const shared = sharedPath(src, pageUrl);
      if (shared) return tag.replace(src, shared);
      const found = text(src, pageUrl);
      return found ? `<script${before}${after}>${scriptSafe(found.text)}</script>` : tag;
    })
    .replace(/(<(?:img|source|video|audio|image)\b[^>]*?\s(?:src|poster)\s*=\s*)(["'])(.*?)\2/gi, (match, lead, quote, ref) => {
      const found = inlineRef(ref, pageUrl, fetchLocal);
      return found ? `${lead}${quote}${dataUri(found.type, found.body)}${quote}` : match;
    });
}

// A runtime file for the viewer: runtime.css and m3e.js as text, each font it references as base64.
// `fetchLocal(URL)` is the same lookup `inlineHtml` uses; `origin` is the origin of the page URL.
export function runtimeAssets(fetchLocal, origin) {
  const read = path => {
    const found = fetchLocal(new URL(path, origin));
    if (!found) throw new Error(`The runtime file ${path} is missing; run npm run build`);
    return Buffer.from(found.body);
  };
  const css = read(RUNTIME_CSS).toString('utf8');
  const fonts = {};
  for (const [, path] of css.matchAll(/url\("(fonts\/[^"]+)"\)/g)) fonts[path] = read(`/runtime/${path}`).toString('base64');
  return { css, js: read(RUNTIME_JS).toString('utf8'), fonts };
}

// The proposal's own theme, read from the first <m3e-theme> in its source.
export function proposalTheme(html) {
  const tag = /<m3e-theme\b[^>]*>/i.exec(html)?.[0] ?? '';
  const attr = name => new RegExp(`\\s${name}\\s*=\\s*(["'])(.*?)\\1`, 'i').exec(tag)?.[2];
  const color = attr('color') ?? '';
  return { color: /^#[0-9a-f]{6}$/i.test(color) ? color.toLowerCase() : '#6750a4', variant: attr('variant') ?? 'tonal-spot' };
}

// `bezel`: a phone or foldable is drawn with a device ring; a tablet or desktop is a plain window.
export const DEVICES = [
  { name: 'Phone', size: [411, 923], icon: 'smartphone', bezel: true },
  { name: 'Foldable', size: [933, 704], icon: 'devices_fold', bezel: true },
  { name: 'Tablet', size: [1280, 800], icon: 'tablet', bezel: false },
  { name: 'Desktop', size: [1920, 1080], icon: 'desktop_windows', bezel: false },
];
// Palettes are seed colors; the style (variant) is chosen separately and applies to every row.
export const PALETTES = [
  { id: 'purple', name: 'Purple', color: '#6750a4' },
  { id: 'blue', name: 'Blue', color: '#0061a4' },
  { id: 'green', name: 'Green', color: '#386a20' },
  { id: 'coral', name: 'Coral', color: '#984061' },
  { id: 'amber', name: 'Amber', color: '#8b5000' },
  { id: 'teal', name: 'Teal', color: '#006a6a' },
];
export const STYLES = [['tonal-spot', 'Tonal spot'], ['neutral', 'Neutral'], ['vibrant', 'Vibrant'], ['expressive', 'Expressive'], ['monochrome', 'Monochrome']];

// A Material spring (unit mass, `damping` ratio, `stiffness`) as a CSS transition: the duration until it stays within 0.2% of
// its target, and a linear() easing sampled from the spring's motion over that time.
export function spring(damping, stiffness) {
  const w = Math.sqrt(stiffness);
  const position = damping < 1
    ? t => { const wd = w * Math.sqrt(1 - damping * damping); return 1 - Math.exp(-damping * w * t) * (Math.cos(wd * t) + (damping * w / wd) * Math.sin(wd * t)); }
    : t => 1 - (1 + w * t) * Math.exp(-w * t);
  let end = 0;
  for (let t = 0; t < 3; t += 0.001) if (Math.abs(1 - position(t)) > 0.002) end = t;
  const steps = 40;
  const points = Array.from({ length: steps + 1 }, (_, i) => (i === steps ? 1 : position(end * i / steps)).toFixed(4).replace(/\.?0+$/, '') || '0');
  return { ms: Math.round(end * 1000), easing: `linear(${points.join(', ')})` };
}
// Expressive's default spatial spec moves the frame (partial-screen motion); its default effects spec changes the bezel color.
const SPATIAL = spring(0.8, 380);
const EFFECTS = spring(1, 1600);

// The screen name shown under the frame: a folder name like "learn-home" becomes "Learn home".
export function screenName(folderName) {
  const words = folderName.replace(/[-_]+/g, ' ').trim();
  return words.charAt(0).toUpperCase() + words.slice(1);
}

const esc = value => String(value).replace(/[&<>"']/g, c => `&#${c.charCodeAt(0)};`);

// A color row or a style row. Its own nested <m3e-theme> scopes the row's tokens to the swatch and dots, so their colors come
// from the same theme engine the proposal uses. A color row has a fixed color and follows the selected style; a style row has a
// fixed style and follows the selected color. Both follow the brightness (the page sets the attributes).
const row = (kind, value, name, { color, variant }, selected) => `<m3e-list-option value="${esc(value)}" data-${kind}="${esc(kind === 'color' ? color : variant)}"${selected ? ' selected' : ''}>
        <m3e-theme class="pal ${kind}" color="${esc(color)}" variant="${esc(variant)}" scheme="light"><span class="split"></span><span class="t-body-large name">${esc(name)}</span><span class="dots"><i></i><i></i><i></i><i></i><i></i></span></m3e-theme>
      </m3e-list-option>`;

// The viewer chrome is built from the runtime's own elements (the page loads the shared runtime) but is plain JS and CSS with
// no build step: it is not a proposal, so no lint or runtime check applies to it.
// `runtime` is runtimeAssets(); `theme` is proposalTheme(); `name` is the screen name.
export function viewerHtml({ title, name = title, doc, runtime, theme = proposalTheme('') }) {
  const [width, height] = DEVICES[0].size;
  const data = JSON.stringify({ doc, runtime, theme: { ...theme, scheme: 'light' } }).replace(/</g, '\\u003c');
  const proposal = row('color', 'proposal', 'Proposal', theme, true);
  const presets = PALETTES.map(p => row('color', p.id, p.name, { color: p.color, variant: theme.variant }, false)).join('\n      ');
  const styles = STYLES.map(([value, label]) => row('variant', value, label, { color: theme.color, variant: value }, value === theme.variant)).join('\n      ');
  const schemes = [['light', 'Light', 'light_mode'], ['dark', 'Dark', 'dark_mode']].map(([value, label, icon]) => `<m3e-button toggle variant="tonal"${value === 'light' ? ' selected' : ''} data-scheme="${value}"><p-icon slot="icon" name="${icon}"></p-icon>${label}</m3e-button>`).join('\n        ');
  const devices = DEVICES.map((d, i) => `<m3e-icon-button toggle variant="tonal"${i === 0 ? ' selected' : ''} data-w="${d.size[0]}" data-h="${d.size[1]}" data-bezel="${d.bezel}" title="${d.name} ${d.size.join('x')}" aria-label="${d.name} ${d.size.join('x')}"><p-icon name="${d.icon}"></p-icon></m3e-icon-button>`).join('\n      ');
  return `<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${esc(title)}</title>
<style>
  html:not(.ready) .app { visibility: hidden; }
  body { overflow: hidden; }
  .app { display: flex; height: 100vh; }
  .panel { flex: none; width: 320px; display: flex; flex-direction: column; gap: 24px; padding: 24px 16px; overflow-y: auto; background: var(--md-sys-color-surface-container); }
  .panel section { display: flex; flex-direction: column; gap: 8px; }
  .panel h2 { padding: 0 8px; color: var(--md-sys-color-on-surface-variant); }
  .groups { display: flex; flex-direction: column; gap: 16px; }
  #schemes { display: flex; }
  #schemes m3e-button { flex: 1; }
  :root { --row: 56px; --foot: 36px; }
  .column { flex: 1; min-width: 0; display: flex; flex-direction: column; background: var(--md-sys-color-surface); }
  main { flex: 1; min-height: 0; overflow: hidden; padding: 24px; display: flex; justify-content: center; align-items: center; }
  /* #wrap holds the scaled device between the switcher row and the label; fit() sets its size and the device's geometry. */
  #wrap { position: relative; flex: none; }
  #row { position: absolute; left: 0; top: 0; width: 100%; height: var(--row); display: flex; align-items: center; padding-bottom: 8px; box-sizing: border-box; }
  #devices { flex: none; }
  /* The runtime gives an icon button a 48px touch box; here the buttons sit 2px apart like a connected group. */
  #devices m3e-icon-button { --_icon-button-min-size: 0px; }
  #name { position: absolute; left: 0; bottom: 0; width: 100%; height: var(--foot); padding-top: 12px; box-sizing: border-box; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: var(--md-sys-color-on-surface-variant); }
  .mono { font-family: "Google Sans Code", monospace; }
  /* The device is the bezel (a ring around the frame for a phone, none for a window) and carries the outside hairline. */
  #device { position: absolute; left: 0; top: var(--row); box-sizing: border-box; background-color: transparent; outline: 1px solid var(--md-sys-color-outline-variant); transform-origin: top left; }
  #device[data-bezel="true"] { background-color: var(--md-sys-color-surface-container-highest); }
  iframe { display: block; border: 0; background: var(--md-sys-color-surface); }
  /* Only a device change animates; a window resize follows the window directly. */
  .animating #wrap, .animating #device, .animating iframe { transition-duration: ${SPATIAL.ms}ms; transition-timing-function: ${SPATIAL.easing}; }
  .animating #wrap { transition-property: width, height; }
  .animating #device { transition-property: width, height, transform, padding, border-radius, background-color; transition-duration: ${SPATIAL.ms}ms, ${SPATIAL.ms}ms, ${SPATIAL.ms}ms, ${SPATIAL.ms}ms, ${SPATIAL.ms}ms, ${EFFECTS.ms}ms; transition-timing-function: ${SPATIAL.easing}, ${SPATIAL.easing}, ${SPATIAL.easing}, ${SPATIAL.easing}, ${SPATIAL.easing}, ${EFFECTS.easing}; }
  .animating iframe { transition-property: width, height, border-radius; }
  @media (prefers-reduced-motion: reduce) { .animating #wrap, .animating #device, .animating iframe { transition: none; } }

  m3e-selection-list { flex: none; --m3e-segmented-list-item-container-color: var(--md-sys-color-surface-container-high); --m3e-segmented-list-segment-gap: 4px; }
  /* A ring in the viewer's own outline-variant keeps a dot visible when its color equals the row's. */
  m3e-list-option { --ring: var(--md-sys-color-outline-variant); }
  m3e-theme.pal { display: flex; align-items: center; gap: 16px; width: 100%; min-width: 0; min-height: 0; background: none; color: inherit; }
  .split { flex: none; width: 32px; height: 32px; border-radius: 50%; background: linear-gradient(135deg, var(--md-sys-color-primary) 50%, var(--md-sys-color-primary-container) 50%); }
  .name { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .dots { display: flex; flex: none; }
  .dots i { width: 24px; height: 24px; box-sizing: border-box; border-radius: 50%; border: 1px solid var(--ring); }
  .dots i + i { margin-inline-start: -8px; }
  .dots i:nth-child(1) { background: var(--md-sys-color-primary); }
  .dots i:nth-child(2) { background: var(--md-sys-color-primary-container); }
  .dots i:nth-child(3) { background: var(--md-sys-color-secondary-container); }
  .dots i:nth-child(4) { background: var(--md-sys-color-tertiary-container); }
  .dots i:nth-child(5) { background: var(--md-sys-color-surface-container-highest); }
</style>
</head>
<body>
<m3e-theme id="chrome" color="${esc(theme.color)}" variant="${esc(theme.variant)}" scheme="light">
<div class="app">
  <aside class="panel">
    <section>
      <h2 class="t-title-small">Brightness</h2>
      <m3e-button-group id="schemes" variant="connected">
        ${schemes}
      </m3e-button-group>
    </section>
    <section>
      <h2 class="t-title-small">Color</h2>
      <div class="groups">
        <m3e-selection-list id="proposal-list" variant="segmented" hide-selection-indicator>
      ${proposal}
        </m3e-selection-list>
        <m3e-selection-list id="preset-list" variant="segmented" hide-selection-indicator>
      ${presets}
        </m3e-selection-list>
      </div>
    </section>
    <section>
      <h2 class="t-title-small">Style</h2>
      <m3e-selection-list id="styles" variant="segmented" hide-selection-indicator>
      ${styles}
      </m3e-selection-list>
    </section>
  </aside>
  <div class="column">
    <main id="stage">
      <div id="wrap">
        <div id="row">
          <m3e-button-group id="devices" variant="connected">
      ${devices}
          </m3e-button-group>
        </div>
        <div id="device"></div>
        <h1 class="t-title-small" id="name">${esc(name)} · <span class="mono" id="size">${width}×${height}</span></h1>
      </div>
    </main>
  </div>
</div>
</m3e-theme>
<script type="application/json" id="data">${data}</script>
<script>
const data = JSON.parse(document.getElementById('data').textContent);
const $ = id => document.getElementById(id);
const all = selector => [...document.querySelectorAll(selector)];

// One copy of the runtime: each file becomes a Blob URL, fonts first so runtime.css can point at them. The viewer and the
// proposal frame load the same URLs (a srcdoc frame shares this page's origin).
const bytes = b64 => Uint8Array.from(atob(b64), c => c.charCodeAt(0));
const blobUrl = (part, type) => URL.createObjectURL(new Blob([part], { type }));
const fontUrls = Object.fromEntries(Object.entries(data.runtime.fonts).map(([path, b64]) => [path, blobUrl(bytes(b64), 'font/woff2')]));
const shared = {
  '${RUNTIME_CSS}': blobUrl(data.runtime.css.replace(/url\\("(fonts\\/[^"]+)"\\)/g, (_, path) => 'url("' + fontUrls[path] + '")'), 'text/css'),
  '${RUNTIME_JS}': blobUrl(data.runtime.js, 'text/javascript'),
};
const doc = Object.entries(shared).reduce((html, [path, url]) => html.split(path).join(url), data.doc);

const sheet = Object.assign(document.createElement('link'), { rel: 'stylesheet', href: shared['${RUNTIME_CSS}'] });
const runtime = Object.assign(document.createElement('script'), { type: 'module', src: shared['${RUNTIME_JS}'] });
const loaded = new Promise(resolve => { runtime.onload = runtime.onerror = resolve; });
document.head.append(sheet, runtime);

let size, bezel, theme = { ...data.theme }, frame;
const chrome = $('chrome');

const post = message => frame?.contentWindow?.postMessage(message, '*');

function mount() {
  frame = document.createElement('iframe');
  frame.addEventListener('load', () => post({ type: 'theme', ...theme }));
  frame.srcdoc = doc;
  $('device').append(frame);
  fit();
}
// Scales the device (the frame plus its bezel) to the stage. #wrap takes the scaled device's size plus the switcher row above
// it and the label below it. A phone gets a 10px ring around a 28px-radius frame; a window has no ring and a 12px radius.
function fit() {
  const [w, h] = size, stage = $('stage');
  const ring = bezel ? 10 : 0, inner = bezel ? 28 : 12, dw = w + 2 * ring, dh = h + 2 * ring;
  const css = getComputedStyle(document.documentElement);
  const row = parseFloat(css.getPropertyValue('--row')), foot = parseFloat(css.getPropertyValue('--foot'));
  const scale = Math.min(1, (stage.clientWidth - 48) / dw, (stage.clientHeight - 48 - row - foot) / dh);
  $('wrap').style.cssText = 'width:' + dw * scale + 'px;height:' + (dh * scale + row + foot) + 'px';
  $('device').dataset.bezel = bezel;
  $('device').style.cssText = 'width:' + dw + 'px;height:' + dh + 'px;padding:' + ring + 'px;border-radius:' + (inner + ring) + 'px;transform:scale(' + scale + ')';
  frame.style.cssText = 'width:' + w + 'px;height:' + h + 'px;border-radius:' + inner + 'px';
}
// A device change animates the frame and the row (CSS transitions on .animating); a window resize does not.
let settle;
function changeDevice(button) {
  size = [Number(button.dataset.w), Number(button.dataset.h)];
  bezel = button.dataset.bezel === 'true';
  $('size').textContent = size[0] + '×' + size[1];
  document.documentElement.classList.add('animating');
  fit();
  clearTimeout(settle);
  settle = setTimeout(() => document.documentElement.classList.remove('animating'), ${SPATIAL.ms + 100});
}
// Applies a theme change to the proposal frame, the viewer's own chrome, and every row (the color rows preview the style, the style rows the color).
function apply(change) {
  Object.assign(theme, change);
  post({ type: 'theme', ...theme });
  for (const name of ['color', 'variant', 'scheme']) chrome.setAttribute(name, theme[name]);
  for (const row of all('m3e-theme.pal')) row.setAttribute('scheme', theme.scheme);
  for (const row of all('m3e-theme.color')) row.setAttribute('variant', theme.variant);
  for (const row of all('m3e-theme.variant')) row.setAttribute('color', theme.color);
  for (const option of all('#styles m3e-list-option')) option.selected = option.value === theme.variant;
  for (const button of all('#schemes m3e-button')) button.selected = button.dataset.scheme === theme.scheme;
}
// Single-select: marks the chosen device button and clears the rest, whatever a button did to its own state.
const choose = (buttons, chosen) => buttons.forEach(button => { button.selected = button === chosen; });

async function start() {
  // Reveal once the icon font is in, so no icon flashes as its ligature name.
  await document.fonts.load('24px "Material Symbols Rounded"', 'smartphone').catch(() => {});
  const first = $('devices').firstElementChild;
  size = [Number(first.dataset.w), Number(first.dataset.h)];
  bezel = first.dataset.bezel === 'true';
  mount();
  apply({});
  document.documentElement.classList.add('ready');
  fit();

  for (const button of all('#devices m3e-icon-button')) button.addEventListener('click', () => {
    choose(all('#devices m3e-icon-button'), button);
    changeDevice(button);
  });
  for (const button of all('#schemes m3e-button')) button.addEventListener('click', () => apply({ scheme: button.dataset.scheme }));
  // The two color groups are one single-select: choosing in one clears the other.
  const lists = [$('proposal-list'), $('preset-list')];
  for (const list of lists) list.addEventListener('change', () => {
    const option = list.selected[0];
    if (!option) return;
    for (const other of lists.filter(item => item !== list)) all('#' + other.id + ' m3e-list-option').forEach(item => { item.selected = false; });
    apply({ color: option.dataset.color });
  });
  $('styles').addEventListener('change', () => {
    const option = $('styles').selected[0];
    if (option) apply({ variant: option.dataset.variant });
  });
  addEventListener('resize', fit);
}
loaded.then(start);
</script>
</body>
</html>
`;
}
