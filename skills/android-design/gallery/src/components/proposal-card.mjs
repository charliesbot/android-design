import { LitElement, html } from 'lit';
import { MAX_FRAME_HEIGHT_RATIO, frameScale, frameTitle, frameURL, optionName, proposalKey, selectionLabel, themeMessage } from '../state.mjs';

// One proposal: its live page at the device's exact viewport, scaled to the card.
// Renders in the light DOM so global styles and the radio group apply.
export class ProposalCard extends LitElement {
  static properties = {
    round: { attribute: false },
    proposal: { attribute: false },
    device: { attribute: false },
    theme: { attribute: false },
    resets: { type: Number },
    checked: { type: Boolean },
    locked: { type: Boolean },
    slotWidth: { state: true },
    viewportHeight: { state: true },
    ready: { state: true },
  };
  constructor() {
    super();
    this.slotWidth = 0;
    this.viewportHeight = window.innerHeight;
    this.ready = false;
    this.resizeFrame = undefined;
    this.onResize = () => { this.viewportHeight = window.innerHeight; };
    this.observer = new ResizeObserver(entries => { this.slotWidth = entries[0].contentRect.width; });
  }
  createRenderRoot() { return this; }
  connectedCallback() {
    super.connectedCallback();
    window.addEventListener('resize', this.onResize);
  }
  disconnectedCallback() {
    super.disconnectedCallback();
    window.removeEventListener('resize', this.onResize);
    this.observer.disconnect();
    cancelAnimationFrame(this.resizeFrame);
  }
  firstUpdated() {
    this.observer.observe(this.querySelector('.frame-slot'));
    // Enable transitions only after the first layout so frames do not animate in from default sizes.
    this.resizeFrame = requestAnimationFrame(() => { this.resizeFrame = requestAnimationFrame(() => { this.ready = true; }); });
  }
  // Sizes are set through the CSSOM: the gallery CSP forbids style attributes.
  layout() {
    const { device } = this;
    const scale = this.scale();
    const stage = this.querySelector('.frame-stage'), frame = this.querySelector('iframe');
    stage.style.width = `${device.width * scale}px`;
    stage.style.height = `${device.height * scale}px`;
    stage.style.borderRadius = `${device.radius * scale}px`;
    frame.style.width = `${device.width}px`;
    frame.style.height = `${device.height}px`;
    frame.style.transform = `scale(${scale})`;
  }
  scale() {
    return frameScale({ availableWidth: this.slotWidth, maxHeight: this.viewportHeight * MAX_FRAME_HEIGHT_RATIO, device: this.device });
  }
  updated(changed) {
    this.layout();
    if (changed.has('theme')) this.postTheme();
    if (changed.has('resets') && changed.get('resets') !== undefined) this.reloadFrame();
  }
  frameWindow() { return this.querySelector('iframe')?.contentWindow; }
  postTheme() {
    const message = themeMessage(this.theme);
    if (message) this.frameWindow()?.postMessage(message, location.origin);
  }
  reloadFrame() {
    try { this.frameWindow()?.location.reload(); } catch { /* The frame has no document yet; it loads fresh anyway. */ }
  }
  render() {
    const { round, proposal, device } = this;
    const name = optionName(round, proposal.id);
    const scale = this.scale();
    return html`<article class="proposal">
      <div class="card-heading"><h2>${name}</h2></div>
      <p class="rationale">${proposal.rationale}</p>
      <div class="frame-slot">
        <div class=${`frame-stage ${this.ready ? 'ready' : ''}`} ?hidden=${!scale}>
          <iframe src=${frameURL(round, proposal)} title=${frameTitle(name, device)} @load=${() => this.postTheme()}></iframe>
        </div>
      </div>
      <label class="thumbnail">
        <input type="radio" name="design" .value=${proposalKey(round, proposal)} aria-label=${`Select ${name}`}
          .checked=${this.checked} ?disabled=${this.locked}
          @change=${() => this.dispatchEvent(new CustomEvent('choose', { detail: proposalKey(round, proposal), bubbles: true }))}>
        <span class="selection-label">${selectionLabel(this.checked)}</span>
      </label>
    </article>`;
  }
}
customElements.define('proposal-card', ProposalCard);
