// <p-floating-toolbar> wraps one <m3e-toolbar>. CSS in runtime.css floats it at the bottom center.
// This script gives the toolbar Material's floating defaults and keeps its toggle buttons
// single-select, with the selected one tonal and the others text.
class PFloatingToolbar extends HTMLElement {
  connectedCallback() {
    const toolbar = this.querySelector('m3e-toolbar');
    if (toolbar) {
      if (!toolbar.hasAttribute('shape')) toolbar.setAttribute('shape', 'rounded');
      toolbar.setAttribute('elevated', '');
    }
    this.#sync();
    this.addEventListener('click', this.#onClick);
  }

  disconnectedCallback() { this.removeEventListener('click', this.#onClick); }

  #toggles() { return [...this.querySelectorAll('m3e-button[toggle]')]; }

  #onClick = event => {
    const target = event.target.closest?.('m3e-button[toggle]');
    if (!target) return;
    for (const button of this.#toggles()) button.selected = button === target;
    this.#sync();
  };

  #sync() {
    for (const button of this.#toggles()) button.setAttribute('variant', (button.selected ?? button.hasAttribute('selected')) ? 'tonal' : 'text');
  }
}
customElements.define('p-floating-toolbar', PFloatingToolbar);
