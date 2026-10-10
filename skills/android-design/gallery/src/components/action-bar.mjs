import { LitElement, html } from 'lit';
import { live } from 'lit/directives/live.js';
import { noteLabel, refineLabel, requestStatusText, selectionText } from '../state.mjs';

// Selection heading, note field, and workflow buttons for the current round.
export class ActionBar extends LitElement {
  static properties = {
    round: { attribute: false },
    proposal: { attribute: false },
    entry: { attribute: false },
    approved: { type: Boolean },
    locked: { type: Boolean },
    refineEnabled: { type: Boolean },
    finishEnabled: { type: Boolean },
    noneEnabled: { type: Boolean },
    actionError: {},
  };
  createRenderRoot() { return this; }
  async focusNote() {
    await this.updateComplete;
    this.querySelector('#note')?.focus();
  }
  emit(name, detail) { this.dispatchEvent(new CustomEvent(name, { detail, bubbles: true })); }
  render() {
    const { entry } = this;
    return html`<section id="action-bar" class="action-bar" aria-label="Next step" ?hidden=${!this.round}>
      <h2 id="selection">${selectionText({ round: this.round, proposal: this.proposal, entry })}</h2>
      <label id="note-field" class="note-label" ?hidden=${!entry.choice}><span id="note-label">${noteLabel(entry)}</span>
        <textarea id="note" rows="3" maxlength="10000" .value=${live(entry.note)} ?disabled=${this.locked}
          @input=${event => this.emit('note-input', event.target.value)}></textarea>
      </label>
      <div id="workflow-actions" class="actions" ?hidden=${this.approved}>
        <button id="refine" type="button" ?disabled=${!this.refineEnabled} @click=${() => this.emit('refine')}>${refineLabel(entry)}</button>
        <button id="finish" type="button" ?disabled=${!this.finishEnabled} @click=${() => this.emit('finish')}>Finish</button>
        <button id="none" type="button" aria-pressed=${String(entry.choice === 'none')} ?disabled=${!this.noneEnabled} @click=${() => this.emit('none')}>None of these</button>
      </div>
      <p id="request-status" role="status">${requestStatusText(entry)}</p>
      <p id="action-error" role="alert">${this.actionError}</p>
    </section>`;
  }
}
customElements.define('action-bar', ActionBar);
