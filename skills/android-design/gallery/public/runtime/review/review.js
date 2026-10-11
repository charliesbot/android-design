// Proposal review page: /runtime/review/?src=/draft/<id>/
// Shows one proposal in four iframes at exact device viewports and exposes window.reviewChecks(), which runs the
// in-page rule checker (runtimeChecks) in each. Add &device=<id> to show one device at full size for a screenshot.
// Everything here is same-origin and script-only, so it works under the gallery CSP (no inline script or style).

import { parseSource } from './source.js';

const DEVICES = [
  { id: 'phone', name: 'Phone', width: 411, height: 923 },
  { id: 'tablet', name: 'Tablet', width: 704, height: 933 },
  { id: 'laptop', name: 'Laptop', width: 1280, height: 800 },
  { id: 'desktop', name: 'Desktop', width: 1440, height: 900 },
];
// A scaled card fits in this box, so four devices read at a glance. Full size ignores it.
const FIT = { width: 560, height: 480 };

const settle = win => new Promise(resolve => win.requestAnimationFrame(() => win.requestAnimationFrame(resolve)));

function card(device, src, full) {
  const scale = full ? 1 : Math.min(1, FIT.width / device.width, FIT.height / device.height);
  const figure = document.createElement('figure');
  figure.className = 'device';
  figure.dataset.device = device.id;

  const label = document.createElement('figcaption');
  label.className = 'label';
  const name = document.createElement('strong');
  name.textContent = device.name;
  const size = document.createElement('span');
  size.textContent = `${device.width} x ${device.height}`;
  const link = document.createElement('a');
  const target = new URLSearchParams({ src, ...(full ? {} : { device: device.id }) });
  link.href = `?${target}`;
  link.textContent = full ? 'Show all' : 'Full size';
  label.append(name, size, link);

  const stage = document.createElement('div');
  stage.className = 'stage';
  stage.style.setProperty('--w', device.width);
  stage.style.setProperty('--h', device.height);
  stage.style.setProperty('--scale', scale);
  const frame = document.createElement('iframe');
  frame.title = `${device.name} ${device.width} x ${device.height}`;
  frame.width = device.width;
  frame.height = device.height;
  const loaded = new Promise(resolve => frame.addEventListener('load', resolve, { once: true }));
  frame.src = src;
  stage.append(frame);
  figure.append(label, stage);
  return { device, frame, loaded, figure };
}

const params = new URLSearchParams(location.search);
const src = parseSource(params.get('src'), location.origin);
const only = DEVICES.find(d => d.id === params.get('device'));
const cards = [];

if (src) {
  document.getElementById('source').textContent = src;
  for (const device of only ? [only] : DEVICES) cards.push(card(device, src, Boolean(only)));
  document.getElementById('devices').append(...cards.map(c => c.figure));
} else {
  const error = document.getElementById('error');
  error.hidden = false;
  error.textContent = 'Pass ?src=/draft/<id>/ or ?src=/rounds/<round>/<id>/. Only proposals in this gallery can be shown.';
}

// Resolves once every frame has loaded, fonts are ready, and the components have rendered; then each frame's
// runtimeChecks() result. complete is false when any frame left a check unresolved: that is not verified.
window.reviewChecks = async () => {
  if (!src) throw new Error('reviewChecks: no valid src');
  const devices = [];
  for (const { device, frame, loaded } of cards) {
    await loaded;
    const win = frame.contentWindow;
    let result;
    if (typeof win.runtimeChecks !== 'function') {
      result = { pass: false, complete: false, error: 'runtimeChecks is missing: the page does not load /runtime/m3e.js' };
    } else {
      await win.document.fonts.ready;
      await settle(win);
      result = win.runtimeChecks();
    }
    devices.push({ id: device.id, viewport: [win.innerWidth, win.innerHeight], result });
  }
  return {
    src,
    devices,
    pass: devices.every(d => d.result.pass),
    complete: devices.every(d => d.result.complete),
  };
};
