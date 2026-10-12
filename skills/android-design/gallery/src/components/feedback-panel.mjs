import { LitElement, html } from 'lit';
import { live } from 'lit/directives/live.js';
import { noteLabel, refineLabel, requestStatusText } from '../state.mjs';

// Feedback and explicit workflow actions for the proposal on the canvas.
export class FeedbackPanel extends LitElement {
  static properties = {
    round: { attribute: false },
    proposal: { attribute: false },
    entry: { attribute: false },
    session: { attribute: false },
    approved: { type: Boolean },
    locked: { type: Boolean },
    refineEnabled: { type: Boolean },
    finishEnabled: { type: Boolean },
    actionError: {},
  };
  createRenderRoot() { return this; }
  updated() { this.querySelector('m3e-textarea-autosize')?.resizeToFitContent(true); }
  emit(name, detail) { this.dispatchEvent(new CustomEvent(name, { detail, bubbles: true })); }
  render() {
    const { entry } = this;
    return html`<section id="feedback-panel" class="feedback-panel" aria-label="Next step" ?hidden=${!this.round}>
      <h2 id="selection">Feedback</h2>
      <m3e-form-field id="note-field" variant="outlined"><label slot="label" for="note">${noteLabel(entry)}</label>
        <textarea id="note" rows="3" maxlength="10000" .value=${live(entry.note)} ?disabled=${this.locked}
          @input=${event => this.emit('note-input', event.target.value)}></textarea>
        <m3e-textarea-autosize for="note" min-rows="3" max-rows="8"></m3e-textarea-autosize>
      </m3e-form-field>
      <div id="workflow-actions" class="actions" ?hidden=${this.approved}>
        <m3e-button variant="tonal" id="refine" type="button" ?disabled=${!this.refineEnabled} @click=${() => this.emit('refine')}>${refineLabel(entry)}</m3e-button>
        <m3e-button variant="filled" id="finish" type="button" ?disabled=${!this.finishEnabled} @click=${() => this.emit('finish')}>Finish</m3e-button>
      </div>
      <p id="request-status" role="status">${requestStatusText(entry, this.session)}</p>
      <p id="action-error" role="alert">${this.actionError}</p>
    </section>`;
  }
}
customElements.define('feedback-panel', FeedbackPanel);
