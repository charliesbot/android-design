// A modal m3e-bottom-sheet moves focus to its drag handle when it opens. The handle is a tabindex=0 div, so the browser draws its
// default :focus-visible outline (blue) around it. Every other focusable @m3e/web part sets outline: none and relies on
// m3e-focus-ring, which m3e-theme hides unless strong-focus is set; the sheet's .handle is the one part that omits the rule
// (an upstream omission in 2.9.2). This adds it inside each sheet's shadow root. Drop this file once @m3e/web sets it itself.
const sheet = new CSSStyleSheet();
sheet.replaceSync('.handle { outline: none; }');
const done = new WeakSet();

function adopt(element) {
  const root = element.shadowRoot;
  if (!root || done.has(root)) return;
  done.add(root);
  root.adoptedStyleSheets = [...root.adoptedStyleSheets, sheet];
}

const Sheet = customElements.get('m3e-bottom-sheet');
if (Sheet) {
  const connected = Sheet.prototype.connectedCallback;
  Sheet.prototype.connectedCallback = function connectedCallback() {
    connected.call(this);
    adopt(this);
  };
  // Sheets already parsed were upgraded when the element was defined, before the patch above.
  document.querySelectorAll('m3e-bottom-sheet').forEach(adopt);
}
