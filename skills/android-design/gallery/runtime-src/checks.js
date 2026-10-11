import { neededRatio, over, ratio } from './contrast.js';

// window.runtimeChecks(): the in-page rule checker. Returns one JSON-safe object. `pass` is false when
// any check failed. Cases it cannot resolve go in `unresolved`, never in passes, and make `complete` false:
// a result with complete: false is not verified, whatever `pass` says.
const INTERACTIVE = [
  'a[href]', 'button', 'input', 'select', 'textarea', 'summary', '[role=button]', '[role=link]', '[role=tab]',
  '[role=switch]', '[role=checkbox]', '[role=menuitem]', '[tabindex]:not([tabindex="-1"])',
  'm3e-button', 'm3e-icon-button', 'm3e-fab', 'm3e-fab-menu-trigger', 'm3e-fab-menu-item', 'm3e-nav-item',
  'm3e-switch', 'm3e-slider', 'm3e-split-button', 'm3e-card[actionable]', 'm3e-list-item-button', 'm3e-list-action',
  'm3e-list-option', 'm3e-chip', 'm3e-assist-chip', 'm3e-filter-chip', 'm3e-input-chip', 'm3e-suggestion-chip',
].join(',');
const FAB = 'm3e-fab, m3e-fab-menu, [data-fab]';
const MIN_TARGET = 48;

const isFlex = family => /google sans flex/i.test(family.split(',')[0]);
const label = el => (el.getAttribute?.('aria-label') || el.textContent || '').trim().replace(/\s+/g, ' ').slice(0, 30);
const describe = el => {
  const text = label(el);
  return el.localName + (el.id ? `#${el.id}` : '') + (text ? ` "${text}"` : '');
};
// An element inside a shadow root is named through its host: m3e-button#cta > div.label "Start".
const path = el => {
  const host = el.getRootNode().host;
  const own = el.localName + (el.id ? `#${el.id}` : '') + (host && el.classList[0] ? `.${el.classList[0]}` : '');
  return host ? `${path(host)} > ${own}` : own;
};
const describeFlat = el => {
  const text = label(el);
  return path(el) + (text ? ` "${text}"` : '');
};
const visible = el => {
  const rect = el.getBoundingClientRect();
  if (rect.width <= 0 || rect.height <= 0) return false;
  return el.checkVisibility ? el.checkVisibility({ visibilityProperty: true }) : getComputedStyle(el).visibility !== 'hidden';
};

// The parent a node is rendered under: a slotted node renders under its slot, a shadow root's child under
// the host. Null when the node is not rendered (a light child of a host with no slot for it).
function flatParent(node) {
  if (node.assignedSlot) return node.assignedSlot;
  const parent = node.parentNode;
  if (!parent) return null;
  if (parent instanceof ShadowRoot) return parent.host;
  if (parent instanceof Element && parent.shadowRoot) return null;
  return parent instanceof Element ? parent : null;
}
// The nearest flat-tree ancestor (self included) that has a layout box; slots and display: contents have none.
function boxOf(el) {
  for (let node = el; node; node = flatParent(node)) if (getComputedStyle(node).display !== 'contents') return node;
  return null;
}
const hiddenFromAssistiveTech = el => {
  for (let node = el; node; node = flatParent(node)) if (node.getAttribute?.('aria-hidden') === 'true') return true;
  return false;
};

// Visit every element in the light DOM and in open shadow roots. `visit` returns false to skip a subtree's shadow root.
function walk(root, visit) {
  for (const el of root.querySelectorAll('*')) {
    if (visit(el) !== false && el.shadowRoot) walk(el.shadowRoot, visit);
  }
}
// Visit every non-blank text node in light and shadow DOM.
function walkText(root, visit) {
  const direct = parent => { for (const node of parent.childNodes) if (node.nodeType === Node.TEXT_NODE && node.textContent.trim()) visit(node); };
  if (root instanceof ShadowRoot) direct(root);
  walk(root, el => { direct(el); });
}

// Any CSS color the browser can compute becomes sRGB through a 1x1 canvas.
const canvas = document.createElement('canvas');
canvas.width = canvas.height = 1;
const context = canvas.getContext('2d', { willReadFrequently: true });
function rgba(css) {
  context.clearRect(0, 0, 1, 1);
  context.fillStyle = '#000';
  context.fillStyle = css;
  context.fillRect(0, 0, 1, 1);
  const [r, g, b, a] = context.getImageData(0, 0, 1, 1).data;
  return { r, g, b, a: a / 255 };
}
// The opaque color behind a rendering element, or a reason it cannot be known. Walks the flat tree, so the
// painted fill inside a component's shadow root (a button's .base) is found before the page behind it.
function backdrop(el) {
  for (let node = el; node; node = flatParent(node)) {
    const style = getComputedStyle(node);
    if (parseFloat(style.opacity) < 1) return { reason: 'an ancestor has opacity below 1' };
    if (style.backgroundImage !== 'none') return { reason: 'background is an image or gradient' };
    const color = rgba(style.backgroundColor);
    if (color.a === 1) return { color };
    if (color.a > 0) return { reason: 'background is translucent' };
  }
  return { reason: 'no opaque background found' };
}

// Is any part of the text drawn outside an ancestor that clips? Returns that ancestor, or null.
// A scroll container ends the climb: its content is reachable.
function clippedBy(textRect, el) {
  for (let node = boxOf(el); node; node = boxOf(flatParent(node))) {
    const style = getComputedStyle(node);
    if (/auto|scroll/.test(style.overflowX + style.overflowY)) return null;
    if (/hidden|clip/.test(style.overflowX + style.overflowY) && node !== document.documentElement) {
      const box = node.getBoundingClientRect();
      const [left, top] = [box.left + node.clientLeft, box.top + node.clientTop];
      if (textRect.left < left - 1 || textRect.top < top - 1 || textRect.right > left + node.clientWidth + 1 || textRect.bottom > top + node.clientHeight + 1) return node;
    }
  }
  return null;
}

export function runtimeChecks() {
  const unresolved = [];

  // Interactive elements: a matched host is measured as a whole, so its own shadow root is not searched.
  // The target is the host box united with any .touch element in its shadow root (the 48dp minimum
  // interactive size @m3e/web draws around a smaller visual), so a 40px button with a 48px .touch passes.
  const interactive = [];
  walk(document, el => {
    if (!el.matches(INTERACTIVE)) return true;
    if (visible(el)) {
      const host = el.getBoundingClientRect();
      const touch = el.shadowRoot?.querySelector('.touch')?.getBoundingClientRect();
      const width = Math.max(host.width, touch?.width ?? 0);
      const height = Math.max(host.height, touch?.height ?? 0);
      interactive.push({ el: describe(el), node: el, w: Math.round(width * 10) / 10, h: Math.round(height * 10) / 10, area: width * height });
    }
    return false;
  });
  const under48 = interactive.filter(i => i.w < MIN_TARGET - 0.05 || i.h < MIN_TARGET - 0.05).map(({ el, w, h }) => ({ el, w, h }));
  const largest = interactive.reduce((best, i) => (!best || i.area > best.area ? i : best), null);

  // The page marks its primary action with data-primary; it must be the largest interactive element.
  const marked = interactive.filter(i => i.node.hasAttribute('data-primary'));
  const primary = { marked: marked.length, largest: largest && { el: largest.el, w: largest.w, h: largest.h } };
  if (marked.length === 0) {
    primary.status = 'unresolved';
    unresolved.push({ check: 'primary', reason: 'no visible element has data-primary' });
  } else if (marked.length > 1) {
    primary.status = 'fail';
    primary.reason = 'more than one visible element has data-primary';
  } else {
    primary.status = marked[0].area >= largest.area - 0.5 ? 'pass' : 'fail';
    if (primary.status === 'fail') primary.reason = `${marked[0].el} is smaller than ${largest.el}`;
  }

  const fabs = [...document.querySelectorAll(FAB)].filter(el => !el.parentElement?.closest('m3e-fab-menu') && visible(el)).length;

  // Text: every text node, in light and shadow DOM, measured where it renders (through its slot, in the
  // flat tree): color, font, and clipping come from the rendering element, the fill from the first opaque
  // background up the flat tree, including the shadow parts that paint a component's container.
  const fonts = [], overflow = [], contrast = { fails: [], passed: 0 };
  const seen = new Set();
  const once = (...keys) => { const key = keys.join('|'); if (seen.has(key)) return false; seen.add(key); return true; };
  walkText(document, node => {
    const el = flatParent(node);
    if (!el || el.closest?.('p-icon, m3e-icon')) return;
    const style = getComputedStyle(el);
    const range = document.createRange();
    range.selectNodeContents(node);
    const rect = range.getBoundingClientRect();
    if (rect.width <= 0 || rect.height <= 0 || style.visibility !== 'visible') return;
    const name = describeFlat(boxOf(el) || el);
    if (!isFlex(style.fontFamily) && once('font', name)) fonts.push({ el: name, font: style.fontFamily });
    if (hiddenFromAssistiveTech(el)) return; // decorative text is exempt from overflow and contrast

    // Overflow: the box holding the text overflows itself, or an ancestor that clips cuts the text off.
    const box = boxOf(el);
    const scrolls = /auto|scroll/.test(style.overflowX + style.overflowY);
    if (box && !scrolls && getComputedStyle(box).display !== 'inline' && (box.scrollWidth > box.clientWidth + 1 || box.scrollHeight > box.clientHeight + 1)
        && once('overflow', name)) {
      overflow.push({ el: name, scroll: [box.scrollWidth, box.scrollHeight], client: [box.clientWidth, box.clientHeight] });
    }
    const clipper = clippedBy(rect, el);
    if (clipper && clipper !== box && once('overflow', name, 'clip')) overflow.push({ el: name, clippedBy: describeFlat(clipper) });

    if (!once('contrast', name)) return;
    const back = backdrop(el);
    if (!back.color) { unresolved.push({ check: 'contrast', el: name, reason: back.reason }); return; }
    const text = over(rgba(style.color), back.color);
    const need = neededRatio(parseFloat(style.fontSize), parseInt(style.fontWeight, 10));
    const value = ratio(text, back.color);
    if (value < need) contrast.fails.push({ el: name, ratio: Math.round(value * 100) / 100, need });
    else contrast.passed++;
  });

  const complete = unresolved.length === 0;
  const pass = under48.length === 0 && fonts.length === 0 && overflow.length === 0 && primary.status !== 'fail' && fabs <= 1 && contrast.fails.length === 0;
  return JSON.parse(JSON.stringify({
    pass,
    complete,
    viewport: [innerWidth, innerHeight],
    touchTargets: { checked: interactive.length, under48 },
    fonts: { offenders: fonts },
    overflow,
    primary,
    fabs: { count: fabs, ok: fabs <= 1 },
    contrast,
    unresolved,
  }));
}
