import { LitElement, html } from 'lit';
import { IMAGE_UNAVAILABLE_TEXT, imageAlt, imageURL, optionName, proposalKey, selectionLabel } from '../state.mjs';

// One proposal. Renders in the light DOM so global styles and the radio group apply.
export class ProposalCard extends LitElement {
  static properties = {
    round: { attribute: false },
    proposal: { attribute: false },
    device: {},
    checked: { type: Boolean },
    locked: { type: Boolean },
    failedSrc: { state: true },
  };
  createRenderRoot() { return this; }
  render() {
    const { round, proposal, device } = this;
    const name = optionName(round, proposal.id);
    const src = imageURL(round, proposal, device);
    const { width, height } = proposal.images[device];
    return html`<article class="proposal">
      <div class="card-heading"><h2>${name}</h2></div>
      <p class="rationale">${proposal.rationale}</p>
      <label class="thumbnail">
        <input type="radio" name="design" .value=${proposalKey(round, proposal)} aria-label=${`Select ${name}`}
          .checked=${this.checked} ?disabled=${this.locked}
          @change=${() => this.dispatchEvent(new CustomEvent('choose', { detail: proposalKey(round, proposal), bubbles: true }))}>
        <img src=${src} alt=${imageAlt(name, device)} width=${width} height=${height}
          ?hidden=${this.failedSrc === src} @error=${() => { this.failedSrc = src; }}>
        <span class="selection-label">${selectionLabel(this.checked)}</span>
        ${this.failedSrc === src ? html`<span>${IMAGE_UNAVAILABLE_TEXT}</span>` : ''}
      </label>
    </article>`;
  }
}
customElements.define('proposal-card', ProposalCard);
