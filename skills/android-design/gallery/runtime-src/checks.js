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
// Text the user cannot see: opacity 0 on itself or any flat-tree ancestor (a collapsed rail's labels, a
// faded-out panel). Such text is skipped, not measured. Fractional opacity is a real, measurable state.
const fullyTransparent = el => {
  for (let node = el; node; node = flatParent(node)) if (parseFloat(getComputedStyle(node).opacity) === 0) return true;
  return false;
};
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


// Mid-word breaks: a word split across two lines with no hyphen, which `overflow-wrap: anywhere`, `word-break`
// or a too-narrow box produces ("Golde / n Hour"). Words are measured in segments split at spaces, hyphens,
// soft hyphens, zero-width spaces, and slashes, where a break is legitimate. Text with `hyphens: auto` shows a hyphen at the
// break, and CJK runs have no spaces to break at, so both are skipped.
const BREAKABLE = /[^\s\-\u00ad\u200b\u2010-\u2015/]+/g;
function midWordBreaks(node, style) {
  if (style.hyphens === 'auto' || /[　-鿿가-힯฀-๿]/.test(node.textContent)) return [];
  const found = [];
  const range = document.createRange();
  for (const match of node.textContent.matchAll(BREAKABLE)) {
    if (match[0].length < 2) continue;
    range.setStart(node, match.index);
    range.setEnd(node, match.index + match[0].length);
    const rects = [...range.getClientRects()].filter(r => r.width > 0 && r.height > 0);
    if (rects.length < 2) continue;
    const tops = rects.map(r => r.top);
    if (Math.max(...tops) - Math.min(...tops) > Math.min(...rects.map(r => r.height)) / 2) found.push(match[0]);
  }
  return found;
}

// Overlap: text another element paints over. The text is sampled on a grid across each line; at every
// sample, document.elementsFromPoint lists what is hit top first, and anything above the text's own box
// (ancestors and descendants of the text's element excluded) that really paints there counts as covering it.
// elementsFromPoint already leaves out pointer-events: none elements. A coverer paints when it is nearly
// opaque (a background of alpha 0.9 or more after its opacity, a url() background image, a replaced element
// or svg, or its own text at that point, or a component whose shadow root paints there). Translucent
// scrims and gradients leave the text legible, so they are not coverers. A fixed or sticky coverer over
// content that can be scrolled clear of it is normal stacking, not a defect, and is skipped.
const COVER_MIN_SHARE = 0.2;
function opacityChain(el) {
  let value = 1;
  for (let node = el; node; node = flatParent(node)) value *= parseFloat(getComputedStyle(node).opacity);
  return value;
}
function flatContains(ancestor, el) {
  for (let node = el; node; node = flatParent(node)) if (node === ancestor) return true;
  return false;
}
function ownsTextAt(el, x, y) {
  const range = document.createRange();
  for (const node of el.childNodes) {
    if (node.nodeType !== Node.TEXT_NODE || !node.textContent.trim()) continue;
    range.selectNodeContents(node);
    for (const r of range.getClientRects()) if (x >= r.left && x <= r.right && y >= r.top && y <= r.bottom) return true;
  }
  return false;
}
function paintsAt(el, x, y) {
  const style = getComputedStyle(el);
  if (style.visibility !== 'visible' || opacityChain(el) < 0.9) return false;
  if (/^(img|svg|canvas|video|picture)$/i.test(el.localName) || style.backgroundImage.includes('url(')) return true;
  if (rgba(style.backgroundColor).a >= 0.9 || ownsTextAt(el, x, y)) return true;
  return Boolean(el.shadowRoot?.elementsFromPoint(x, y).some(inner => inner !== el && el.shadowRoot.contains(inner) && paintsAt(inner, x, y)));
}
// Can scrolling alone move the text out from under a viewport-pinned coverer? The text scrolls in its nearest
// scrollable flat-tree ancestor, or in the document when there is none. A pinned coverer stays put while that
// content scrolls under it, even when the coverer sits inside the scroller; text sharing the coverer's pinned
// ancestor does not scroll under it, and the pinned(el) test rules that out.
function scrollerOf(el) {
  for (let node = el; node; node = flatParent(node)) {
    if (node === document.documentElement || node === document.body) break;
    const style = getComputedStyle(node);
    if (/auto|scroll/.test(style.overflowY) && node.scrollHeight > node.clientHeight + 1) return node;
  }
  return document.documentElement;
}
function scrollsClear(textRect, el, coverer) {
  const pinned = node => { for (let n = node; n; n = flatParent(n)) if (/fixed|sticky/.test(getComputedStyle(n).position)) return true; return false; };
  if (pinned(el) || !pinned(coverer)) return false;
  const scroller = scrollerOf(el);
  const remaining = scroller.scrollHeight - scroller.clientHeight - scroller.scrollTop;
  if (remaining <= 0) return false;
  const box = coverer.getBoundingClientRect();
  const top = textRect.top - remaining, bottom = textRect.bottom - remaining;
  return bottom <= box.top || top >= box.bottom || textRect.right <= box.left || textRect.left >= box.right;
}
function coveredText(node, el) {
  const range = document.createRange();
  range.selectNodeContents(node);
  const lines = [...range.getClientRects()].filter(r => r.width > 0 && r.height > 0);
  const step = Math.max(1, Math.ceil(lines.length / 8));
  let samples = 0, covered = 0, top = null;
  for (const line of lines.filter((_, i) => i % step === 0)) {
    for (const f of [0.1, 0.3, 0.5, 0.7, 0.9]) {
      const x = line.left + line.width * f, y = line.top + line.height / 2;
      if (x < 0 || y < 0 || x >= innerWidth || y >= innerHeight) continue;
      const hits = document.elementsFromPoint(x, y);
      const own = hits.findIndex(hit => flatContains(hit, el));
      if (own < 0) continue; // the text itself is not hit-testable here (pointer-events: none), so it is unknown
      samples++;
      const cover = hits.slice(0, own).find(hit => !flatContains(el, hit) && !fullyTransparent(hit) && paintsAt(hit, x, y)
        && !scrollsClear(line, el, hit));
      if (cover) { covered++; top ??= cover; }
    }
  }
  return samples > 0 && covered / samples >= COVER_MIN_SHARE ? { by: top, share: Math.round((covered / samples) * 100) } : null;
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

// A page wider than its viewport is a layout bug: Android has no horizontal page scroll. Returns the widest
// offenders (widest first, then shallowest, so the cause is named before its children), skipping anything inside a horizontal scroller the author made on purpose.
// A fixed or sticky element never widens the page, so it is checked on its own: it fails when it is partly
// visible but crosses the left or right viewport edge. One fully off screen (a closed drawer, a skip link) is hidden on purpose.
function pageOverflowCheck() {
  const width = innerWidth;
  const scrollWidth = Math.max(document.documentElement.scrollWidth, document.body.scrollWidth);
  const depth = el => { let n = 0; for (let node = flatParent(el); node; node = flatParent(node)) n++; return n; };
  const inDeliberateScroller = el => {
    for (let node = flatParent(el); node; node = flatParent(node)) {
      if (node === document.documentElement || node === document.body) return false;
      const style = getComputedStyle(node);
      if (/auto|scroll/.test(style.overflowX) && node.getBoundingClientRect().width <= width + 1) return true;
    }
    return false;
  };
  const wide = scrollWidth > width + 1;
  const offenders = [];
  walk(document, el => {
    const style = getComputedStyle(el);
    if (style.display === 'none' || style.display === 'contents') return;
    const pinned = style.position === 'fixed' || style.position === 'sticky';
    if (!pinned && !wide) return;
    const { left, right } = el.getBoundingClientRect();
    if (pinned) {
      const spills = (left < -1 && right > 1) || (right > width + 1 && left < width - 1);
      if (spills && visible(el) && !fullyTransparent(el) && !inDeliberateScroller(el)) offenders.push({ el: describeFlat(el), right: Math.round(right), left: Math.round(left), depth: depth(el) });
    } else if (right > width + 1 && !inDeliberateScroller(el)) {
      offenders.push({ el: describeFlat(el), right: Math.round(right), depth: depth(el) });
    }
  });
  offenders.sort((a, b) => b.right - a.right || a.depth - b.depth);
  return { fails: offenders.length > 0, scrollWidth, offenders: offenders.slice(0, 5).map(({ depth: _depth, ...rest }) => rest) };
}

// Resolves once the page holds still: two frames, then no finite animation or transition left running
// (a nav item's label fades in after load, and measuring it mid-fade gives a wrong contrast). Looping
// animations never finish, so they are not waited for. Gives up after about 3 seconds.
export async function runtimeSettle() {
  const frames = () => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)));
  // document.getAnimations() does not reach into shadow roots, where the components run their transitions.
  const running = (root = document) => {
    const live = a => a.playState === 'running' && Number.isFinite(a.effect?.getComputedTiming().endTime);
    if (root.getAnimations().some(live)) return true;
    for (const el of root.querySelectorAll('*')) if (el.shadowRoot && running(el.shadowRoot)) return true;
    return false;
  };
  await frames();
  for (let i = 0; i < 90 && running(); i++) await frames();
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
  const fonts = [], overflow = [], midWord = [], overlaps = [], contrast = { fails: [], passed: 0 };
  const seen = new Set();
  const once = (...keys) => { const key = keys.join('|'); if (seen.has(key)) return false; seen.add(key); return true; };
  walkText(document, node => {
    const el = flatParent(node);
    if (!el || el.closest?.('p-icon, m3e-icon')) return;
    const style = getComputedStyle(el);
    const range = document.createRange();
    range.selectNodeContents(node);
    const rect = range.getBoundingClientRect();
    if (rect.width <= 0 || rect.height <= 0 || style.visibility !== 'visible' || fullyTransparent(el)) return;
    const name = describeFlat(boxOf(el) || el);
    if (!isFlex(style.fontFamily) && once('font', name)) fonts.push({ el: name, font: style.fontFamily });
    if (hiddenFromAssistiveTech(el)) return; // decorative text is exempt from overflow and contrast

    // Mid-word breaks and overlap. Both read the live layout, so they are measured where the text renders.
    const broken = midWordBreaks(node, style);
    if (broken.length && once('midword', name)) midWord.push({ el: name, words: broken.slice(0, 3) });
    let covered = null;
    try { covered = coveredText(node, el); } catch (error) { if (once('overlap-error', name)) unresolved.push({ check: 'overlaps', el: name, reason: String(error) }); }
    if (covered && once('overlap', name)) overlaps.push({ el: name, coveredBy: describeFlat(covered.by), share: covered.share });

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

  const pageOverflow = pageOverflowCheck();

  const complete = unresolved.length === 0;
  const pass = !pageOverflow.fails && under48.length === 0 && fonts.length === 0 && overflow.length === 0 && midWord.length === 0 && overlaps.length === 0 && primary.status !== 'fail' && fabs <= 1 && contrast.fails.length === 0;
  return JSON.parse(JSON.stringify({
    pass,
    complete,
    viewport: [innerWidth, innerHeight],
    touchTargets: { checked: interactive.length, under48 },
    fonts: { offenders: fonts },
    overflow,
    midWordBreaks: midWord,
    overlaps,
    pageOverflow,
    primary,
    fabs: { count: fabs, ok: fabs <= 1 },
    contrast,
    unresolved,
  }));
}
