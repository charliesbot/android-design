// window.runtimeChecks(): the in-page rule checker. Returns one JSON-safe object whose top-level
// `pass` is false when any check failed. Cases it cannot resolve go in `unresolved`, never in passes.
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
const describe = el => {
  const text = (el.getAttribute?.('aria-label') || el.textContent || '').trim().replace(/\s+/g, ' ').slice(0, 30);
  return el.localName + (el.id ? `#${el.id}` : '') + (text ? ` "${text}"` : '');
};
const visible = el => {
  const rect = el.getBoundingClientRect();
  if (rect.width <= 0 || rect.height <= 0) return false;
  return el.checkVisibility ? el.checkVisibility({ visibilityProperty: true }) : getComputedStyle(el).visibility !== 'hidden';
};
const hiddenFromAssistiveTech = el => {
  for (let node = el; node; node = node.parentElement || node.getRootNode().host) {
    if (node.getAttribute?.('aria-hidden') === 'true') return true;
  }
  return false;
};

// Visit every element in the light DOM and in open shadow roots. `visit` returns false to skip a subtree's shadow root.
function walk(root, visit) {
  for (const el of root.querySelectorAll('*')) {
    if (visit(el) !== false && el.shadowRoot) walk(el.shadowRoot, visit);
  }
}
function walkText(root, visit) {
  walk(root, el => {
    for (const node of el.childNodes) if (node.nodeType === Node.TEXT_NODE && node.textContent.trim()) { visit(el); break; }
  });
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
const luminance = ({ r, g, b }) => {
  const f = v => { v /= 255; return v <= 0.03928 ? v / 12.92 : ((v + 0.055) / 1.055) ** 2.4; };
  return 0.2126 * f(r) + 0.7152 * f(g) + 0.0722 * f(b);
};
const ratio = (a, b) => {
  const [hi, lo] = [luminance(a), luminance(b)].sort((x, y) => y - x);
  return (hi + 0.05) / (lo + 0.05);
};
// The opaque color behind an element, or a reason it cannot be known.
function backdrop(el) {
  for (let node = el; node; node = node.parentElement || node.getRootNode().host) {
    const style = getComputedStyle(node);
    if (parseFloat(style.opacity) < 1) return { reason: 'an ancestor has opacity below 1' };
    if (style.backgroundImage !== 'none') return { reason: 'background is an image or gradient' };
    const color = rgba(style.backgroundColor);
    if (color.a === 1) return { color };
    if (color.a > 0) return { reason: 'background is translucent' };
  }
  return { reason: 'no opaque background found' };
}

export function runtimeChecks() {
  const unresolved = [];

  // Interactive elements: a matched host is measured as a whole, so its own shadow root is not searched.
  const interactive = [];
  walk(document, el => {
    if (!el.matches(INTERACTIVE)) return true;
    if (visible(el)) {
      const { width, height } = el.getBoundingClientRect();
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

  // Text: every element that directly holds a text node, in light and shadow DOM.
  const fonts = [], overflow = [], contrast = { fails: [], passed: 0 };
  walkText(document, el => {
    if (el.closest('p-icon, m3e-icon') || !visible(el)) return;
    const style = getComputedStyle(el);
    if (!isFlex(style.fontFamily)) fonts.push({ el: describe(el), font: style.fontFamily });
    if (hiddenFromAssistiveTech(el)) return; // decorative text is exempt from overflow and contrast
    const scrolls = /auto|scroll/.test(style.overflowX + style.overflowY);
    if (!scrolls && style.display !== 'inline' && (el.scrollWidth > el.clientWidth + 1 || el.scrollHeight > el.clientHeight + 1)) {
      overflow.push({ el: describe(el), scroll: [el.scrollWidth, el.scrollHeight], client: [el.clientWidth, el.clientHeight] });
    }
    const back = backdrop(el);
    if (!back.color) { unresolved.push({ check: 'contrast', el: describe(el), reason: back.reason }); return; }
    const fg = rgba(style.color);
    const text = fg.a < 1 ? { r: fg.r * fg.a + back.color.r * (1 - fg.a), g: fg.g * fg.a + back.color.g * (1 - fg.a), b: fg.b * fg.a + back.color.b * (1 - fg.a) } : fg;
    const size = parseFloat(style.fontSize);
    const large = size >= 24 || (size >= 18.66 && parseInt(style.fontWeight, 10) >= 700);
    const need = large ? 3 : 4.5;
    const value = ratio(text, back.color);
    if (value < need) contrast.fails.push({ el: describe(el), ratio: Math.round(value * 100) / 100, need });
    else contrast.passed++;
  });

  const pass = under48.length === 0 && fonts.length === 0 && overflow.length === 0 && primary.status !== 'fail' && fabs <= 1 && contrast.fails.length === 0;
  return JSON.parse(JSON.stringify({
    pass,
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
