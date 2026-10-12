import { LitElement, html } from 'lit';
import { frameScale, frameTitle, frameURL, optionName, themeMessage, authoredTheme, proposalKey } from '../state.mjs';

// A live device viewport, automatically fit without changing its layout width.
export class ProposalCanvas extends LitElement {
  static properties = {
    round: { attribute: false },
    proposal: { attribute: false },
    device: { attribute: false },
    theme: { attribute: false },
    slotWidth: { state: true },
    slotHeight: { state: true },
    ready: { state: true },
  };
  constructor() {
    super();
    this.slotWidth = 0;
    this.slotHeight = 0;
    this.ready = false;
    this.resizeFrame = undefined;
    this.observer = new ResizeObserver(entries => { this.slotWidth = entries[0].contentRect.width; this.slotHeight = entries[0].contentRect.height; });
  }
  createRenderRoot() { return this; }
  disconnectedCallback() {
    super.disconnectedCallback();
    this.observer.disconnect();
    cancelAnimationFrame(this.resizeFrame);
  }
  firstUpdated() {
    this.observer.observe(this.closest('.canvas-scroll'));
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
    return frameScale({ availableWidth: this.slotWidth, maxHeight: Math.max(1, this.slotHeight - 52), device: this.device });
  }
  updated(changed) {
    this.layout();
    if (changed.has('theme')) this.postTheme();
  }
  frameWindow() { return this.querySelector('iframe')?.contentWindow; }
  postTheme() {
    const message = themeMessage(this.theme);
    if (message && this.loadedDocument === this.querySelector('iframe').contentDocument
      && this.loadedKey === proposalKey(this.round, this.proposal)) this.frameWindow()?.postMessage(message, location.origin);
  }
  frameLoaded() {
    const frame = this.querySelector('iframe');
    // A previous navigation may finish after the user switches directions.
    if (frame.contentWindow.location.pathname !== frameURL(this.round, this.proposal)) return;
    const element = frame.contentDocument.querySelector('m3e-theme');
    const theme = authoredTheme(Object.fromEntries(['color', 'variant', 'scheme'].map(key => [key, element?.getAttribute(key)])));
    this.loadedDocument = frame.contentDocument;
    this.loadedKey = proposalKey(this.round, this.proposal);
    this.dispatchEvent(new CustomEvent('proposal-theme', { detail: { key: this.loadedKey, theme }, bubbles: true }));
    // The parent resolves the original theme with its overrides and supplies a new theme.
  }
  render() {
    const { round, proposal, device } = this;
    const name = optionName(round, proposal.id);
    const scale = this.scale();
    return html`<article class="canvas-proposal" aria-label=${proposal.name}>
      <div class="frame-caption"><span>${name}</span><h2>${proposal.name}</h2></div>
      <div class="frame-slot">
        <div class=${`frame-stage ${this.ready ? 'ready' : ''}`} ?hidden=${!scale}>
          <iframe src=${frameURL(round, proposal)} title=${frameTitle(name, device)} @load=${() => this.frameLoaded()}></iframe>
        </div>
      </div>
    </article>`;
  }
}
customElements.define('proposal-canvas', ProposalCanvas);
