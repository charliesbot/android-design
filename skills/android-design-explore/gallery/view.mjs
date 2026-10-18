// view.html: one proposal as a single file that works when double-clicked (file://, no server).
// The proposal page is inlined (runtime script, CSS, fonts, images become one document) and shown in an
// iframe inside a small viewer with a device switcher and theme controls.

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

export function inlineHtml(html, pageUrl, fetchLocal) {
  const text = (ref, base) => {
    const found = inlineRef(ref, base, fetchLocal);
    return found && { ...found, text: Buffer.from(found.body).toString('utf8') };
  };
  return html
    .replace(/<link\b[^>]*>/gi, tag => {
      const href = /\shref\s*=\s*(["'])(.*?)\1/i.exec(tag)?.[2];
      if (/\srel\s*=\s*["']?stylesheet/i.test(tag)) {
        const found = text(href, pageUrl);
        return found ? `<style>${inlineCss(found.text, found.url, fetchLocal)}</style>` : tag;
      }
      const found = /\srel\s*=\s*["'][^"']*icon/i.test(tag) ? inlineRef(href, pageUrl, fetchLocal) : null;
      return found ? tag.replace(href, dataUri(found.type, found.body)) : tag;
    })
    .replace(/<script\b([^>]*)\ssrc\s*=\s*(["'])(.*?)\2([^>]*)><\/script>/gi, (tag, before, _quote, src, after) => {
      const found = text(src, pageUrl);
      return found ? `<script${before}${after}>${scriptSafe(found.text)}</script>` : tag;
    })
    .replace(/(<(?:img|source|video|audio|image)\b[^>]*?\s(?:src|poster)\s*=\s*)(["'])(.*?)\2/gi, (match, lead, quote, ref) => {
      const found = inlineRef(ref, pageUrl, fetchLocal);
      return found ? `${lead}${quote}${dataUri(found.type, found.body)}${quote}` : match;
    });
}

export const DEVICES = [
  { name: 'Phone', size: [411, 923] },
  { name: 'Foldable', size: [933, 704] },
  { name: 'Tablet', size: [1280, 800] },
  { name: 'Desktop', size: [1920, 1080] },
];
export const SEEDS = [['Purple', '#6750a4'], ['Blue', '#0061a4'], ['Green', '#386a20'], ['Amber', '#8b5000'], ['Rose', '#984061']];
export const VARIANTS = ['tonal-spot', 'neutral', 'vibrant', 'expressive'];

// How long a frame may stay hidden waiting for its proposal to report {type: "state-ready"}.
export const REVEAL_MS = 3000;

// The viewer chrome is plain JS and CSS on purpose: it is not a proposal and has no build step.
export function viewerHtml({ title, doc, states = [], revealMs = REVEAL_MS }) {
  const data = JSON.stringify({ doc, devices: DEVICES, seeds: SEEDS, variants: VARIANTS, states, revealMs }).replace(/</g, '\\u003c');
  const esc = value => String(value).replace(/[&<>"]/g, c => `&#${c.charCodeAt(0)};`);
  return `<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${esc(title)}</title>
<style>
  :root { color-scheme: light dark; font: 14px system-ui, sans-serif; }
  body { margin: 0; height: 100vh; display: flex; flex-direction: column; background: Canvas; color: CanvasText; }
  header { display: flex; flex-wrap: wrap; gap: 8px 24px; align-items: center; padding: 8px 16px; border-bottom: 1px solid color-mix(in srgb, CanvasText 20%, transparent); }
  .group { display: flex; gap: 4px; align-items: center; }
  .group > span { opacity: .6; margin-right: 4px; }
  button, select { font: inherit; padding: 4px 10px; border-radius: 16px; border: 1px solid color-mix(in srgb, CanvasText 30%, transparent); background: transparent; color: inherit; cursor: pointer; }
  button[aria-pressed="true"] { background: CanvasText; color: Canvas; }
  .swatch { width: 24px; height: 24px; padding: 0; border-radius: 50%; }
  main { flex: 1; overflow: auto; padding: 16px; display: flex; justify-content: center; align-items: flex-start; }
  #wrap { flex: none; display: flex; flex-wrap: wrap; justify-content: center; align-items: flex-start; gap: 16px; }
  figure { margin: 0; flex: none; }
  figcaption { padding: 0 0 6px; opacity: .7; text-align: center; }
  iframe { display: block; box-sizing: border-box; border: 1px solid color-mix(in srgb, CanvasText 30%, transparent); background: #fff; transform-origin: top left; }
  iframe[data-pending] { visibility: hidden; }
</style>
</head>
<body>
<header>
  <div class="group" id="devices"><span>Device</span></div>
  <div class="group" id="schemes"><span>Mode</span></div>
  <div class="group" id="seeds"><span>Color</span></div>${states.length ? `
  <div class="group" id="states"><span>State</span></div>` : ''}
  <div class="group"><span>Palette</span><select id="variant"></select></div>
  <button id="reset">Reset to proposal's theme</button>
</header>
<main id="stage"><div id="wrap"></div></main>
<script>
const { doc, devices, seeds, variants, states, revealMs } = ${data};
const $ = id => document.getElementById(id);
// A proposal with states opens on All screens: Default and each state, live side by side. Otherwise one frame.
let size = devices[0].size, theme = {}, allMode = states.length > 0, state = '', frames = [];
const MIN_SCALE = 0.5, GAP = 16;

const screens = () => allMode ? ['', ...states] : [state];
const post = (frame, message) => frame.contentWindow.postMessage(message, '*');
// A frame stays hidden until its screen is ready, so the default screen never flashes before a state opens.
const reveal = frame => frame.removeAttribute('data-pending');

function mount() {
  frames = screens().map(name => {
    const frame = document.createElement('iframe');
    frame.dataset.pending = '';
    frame.addEventListener('load', () => {
      post(frame, { type: 'theme', ...theme });
      if (name) {
        post(frame, { type: 'state', name });
        setTimeout(() => reveal(frame), revealMs);
      } else reveal(frame);
    });
    frame.srcdoc = doc;
    const figure = document.createElement('figure');
    if (allMode) figure.append(Object.assign(document.createElement('figcaption'), { textContent: name || 'Default' }));
    figure.append(frame);
    return frame;
  });
  $('wrap').replaceChildren(...frames.map(f => f.parentElement));
  fit();
}
// The proposal reports {type: "state-ready", name} once its state is open (name, error when it could not open).
// The sender must be one of our own frames: from a file:// page the origin is "null", so the source window is the check.
addEventListener('message', event => {
  if (event.data?.type !== 'state-ready') return;
  const frame = frames.find(f => f.contentWindow === event.source);
  if (!frame) return;
  if (event.data.error) console.error('state "' + event.data.name + '": ' + event.data.error);
  reveal(frame);
});
function fit() {
  const [w, h] = size, stage = $('stage'), avail = stage.clientWidth - 32, n = frames.length;
  let scale, columns = 1;
  if (allMode) {
    // The most frames per row that still keep a readable scale; below that, wrap into more rows.
    for (let c = n; c >= 1; c--) {
      scale = Math.min(1, (avail - GAP * (c - 1)) / (c * w));
      columns = c;
      if (scale >= MIN_SCALE) break;
    }
  } else scale = Math.min(1, avail / w, (stage.clientHeight - 32) / h);
  for (const frame of frames) {
    // The transform scales the paint, not the layout box, so negative margins shrink the box to the scaled size.
    frame.style.cssText = 'width:' + w + 'px;height:' + h + 'px;transform:scale(' + scale + ');margin:0 ' + (w * scale - w) + 'px ' + (h * scale - h) + 'px 0';
  }
  $('wrap').style.maxWidth = allMode ? Math.ceil(columns * w * scale + GAP * (columns - 1)) + 1 + 'px' : '';
}
function apply(change) {
  Object.assign(theme, change);
  for (const frame of frames) post(frame, { type: 'theme', ...theme });
  for (const b of document.querySelectorAll('[data-key]')) b.setAttribute('aria-pressed', String(theme[b.dataset.key] === b.dataset.value));
  $('variant').value = theme.variant ?? '';
}
function button(parent, label, props, onclick) {
  const b = document.createElement('button');
  b.textContent = label;
  Object.assign(b, props);
  b.addEventListener('click', onclick);
  $(parent).append(b);
  return b;
}
for (const d of devices) button('devices', d.name, { title: d.size.join('x') }, () => { size = d.size; fit(); });
for (const scheme of ['light', 'dark']) {
  const b = button('schemes', scheme, {}, () => apply({ scheme }));
  Object.assign(b.dataset, { key: 'scheme', value: scheme });
}
for (const [name, color] of seeds) {
  const b = button('seeds', '', { title: name, className: 'swatch' }, () => apply({ color }));
  b.style.background = color;
  Object.assign(b.dataset, { key: 'color', value: color });
}
// A state is opened by the proposal's own window.proposalState; Default reloads the page, since nothing closes a state.
if (states.length) button('states', 'All screens', {}, () => { allMode = true; mount(); mark(); }).dataset.all = '';
for (const name of states.length ? ['', ...states] : []) {
  const b = button('states', name || 'Default', {}, () => { allMode = false; state = name; mount(); mark(); });
  Object.assign(b.dataset, { state: name });
}
function mark() {
  for (const b of document.querySelectorAll('[data-state]')) b.setAttribute('aria-pressed', String(!allMode && b.dataset.state === state));
  document.querySelector('[data-all]')?.setAttribute('aria-pressed', String(allMode));
}
mark();
$('variant').add(new Option("Proposal's palette", ''));
for (const v of variants) $('variant').add(new Option(v, v));
$('variant').addEventListener('change', e => e.target.value && apply({ variant: e.target.value }));
$('reset').addEventListener('click', () => { theme = {}; mount(); apply({}); });
addEventListener('resize', fit);
mount();
</script>
</body>
</html>
`;
}
