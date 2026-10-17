// <p-icon name="search" filled label="Search"> draws one Material Symbols Rounded glyph by ligature from the bundled variable font.
// `name` is any symbol in fonts.google.com/icons (the publish lint checks it against runtime/symbols.json). `filled` sets the FILL axis to 1.
// Without a label the icon is decorative and hidden from assistive tech. The glyph size is the `size` attribute (a Compose icon size, set by runtime.css; 24px by default).
class PIcon extends HTMLElement {
  static observedAttributes = ['name', 'label'];

  connectedCallback() { this.#render(); }
  attributeChangedCallback() { if (this.isConnected) this.#render(); }

  #render() {
    const label = this.getAttribute('label');
    const glyph = document.createElement('span');
    glyph.setAttribute('aria-hidden', 'true');
    glyph.textContent = this.getAttribute('name') ?? '';
    if (label) {
      this.setAttribute('role', 'img');
      this.setAttribute('aria-label', label);
      this.removeAttribute('aria-hidden');
    } else {
      this.removeAttribute('role');
      this.removeAttribute('aria-label');
      this.setAttribute('aria-hidden', 'true');
    }
    this.replaceChildren(glyph);
  }
}
customElements.define('p-icon', PIcon);
