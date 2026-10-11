// <p-icon name="search" filled label="Search"> draws one glyph from /runtime/icons.svg.
// Names come from icons.json. Without a label the icon is decorative and hidden from assistive tech.
const SPRITE = '/runtime/icons.svg';
const SVG = 'http://www.w3.org/2000/svg';

class PIcon extends HTMLElement {
  static observedAttributes = ['name', 'filled', 'label'];

  connectedCallback() { this.#render(); }
  attributeChangedCallback() { if (this.isConnected) this.#render(); }

  #render() {
    const name = this.getAttribute('name');
    const label = this.getAttribute('label');
    const svg = document.createElementNS(SVG, 'svg');
    svg.setAttribute('viewBox', '0 0 960 960');
    svg.setAttribute('fill', 'currentColor');
    const use = document.createElementNS(SVG, 'use');
    if (name) use.setAttribute('href', `${SPRITE}#${name}${this.hasAttribute('filled') ? '-fill' : ''}`);
    svg.append(use);
    if (label) {
      this.setAttribute('role', 'img');
      this.setAttribute('aria-label', label);
      this.removeAttribute('aria-hidden');
    } else {
      this.removeAttribute('role');
      this.removeAttribute('aria-label');
      this.setAttribute('aria-hidden', 'true');
    }
    this.replaceChildren(svg);
  }
}
customElements.define('p-icon', PIcon);
