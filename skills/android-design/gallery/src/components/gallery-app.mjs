import { LitElement, html } from 'lit';
import { FeedbackSaves } from '../saves.mjs';
import './proposal-canvas.mjs';
import './feedback-panel.mjs';
import { deviceIcon } from '../icons.mjs';
import {
  viewedProposal, canvasEntry, sharedTheme, SEED_COLORS, DEFAULT_DEVICE, DEVICES, EMPTY_ENTRY, THEME_VARIANTS, WAITING_TEXT, approvalText, deviceById, deviceForKey, deviceLabel,
  withThemeOverride, rejectionChange, canChooseNone, canFinish, canRefine,
  confirmName, guardUnload, isLocked, mayReplace, nextRoundSelection, proposalKey, sameRounds, sameSession, selectedProposal,
  shouldReload, showReload, showRetry, staleResult, statusText,
} from '../state.mjs';

const DEVICE_STORAGE_KEY = 'android-design.device';
function storedDevice() {
  try { return deviceById(localStorage.getItem(DEVICE_STORAGE_KEY)).id; } catch { return DEFAULT_DEVICE; }
}

// Owns the exploration data, feedback saves, polling, and the two dialogs.
export class GalleryApp extends LitElement {
  static properties = {
    activeKey: { state: true },
    panelOpen: { state: true },
    data: { state: true },
    saves: { state: true },
    busy: { state: true },
    connectionError: { state: true },
    actionError: { state: true },
    confirmError: { state: true },
    dialogBusy: { state: true },
    roundId: { state: true },
    device: { state: true },
    theme: { state: true },
    originalTheme: { state: true },
    systemDark: { state: true },
    confirmLabel: { state: true },
  };
  constructor() {
    super();
    this.activeKey = '';
    this.panelOpen = true;
    this.data = undefined;
    this.saves = undefined;
    this.busy = false;
    this.connectionError = '';
    this.actionError = '';
    this.confirmError = '';
    this.dialogBusy = false;
    this.roundId = '';
    this.device = storedDevice();
    this.theme = {};
    this.originalTheme = null;
    this.systemScheme = matchMedia('(prefers-color-scheme: dark)');
    this.systemDark = this.systemScheme.matches;
    this.onSchemeChange = event => { this.systemDark = event.matches; };
    this.confirmLabel = '';
    this.loading = false;
    this.edits = 0;
    this.approving = null;
    this.timer = undefined;
    this.onKeydown = event => {
      if (event.ctrlKey || event.metaKey || event.altKey || event.composedPath().some(node => node.matches?.('input, textarea, m3e-select, m3e-switch, [contenteditable]'))) return;
      const device = deviceForKey(event.key);
      if (device) this.chooseDevice(device.id);
    };
    this.beforeUnload = event => {
      if (guardUnload({ pendingSaves: this.saves?.pending.size, busy: this.busy })) { event.preventDefault(); event.returnValue = ''; }
    };
  }
  createRenderRoot() { return this; }
  connectedCallback() {
    super.connectedCallback();
    window.addEventListener('beforeunload', this.beforeUnload);
    window.addEventListener('keydown', this.onKeydown);
    this.systemScheme.addEventListener('change', this.onSchemeChange);
    this.poll();
  }
  disconnectedCallback() {
    super.disconnectedCallback();
    window.removeEventListener('beforeunload', this.beforeUnload);
    window.removeEventListener('keydown', this.onKeydown);
    this.systemScheme.removeEventListener('change', this.onSchemeChange);
    clearTimeout(this.timer);
    this.stopped = true;
  }
  updated() {
    // Option children render after the select, so apply the selected values here.
    const round = this.querySelector('#round');
    if (round && round.value !== this.roundId) round.value = this.roundId;
    const theme = this.effectiveTheme();
    const shell = this.closest('m3e-theme');
    for (const [key, value] of Object.entries(theme)) shell.setAttribute(key, value);
    document.documentElement.style.colorScheme = theme.scheme;
  }

  currentRound() { return this.data?.rounds.find(round => round.id === this.roundId); }
  viewed() { return viewedProposal(this.currentRound(), this.activeKey, this.draft()); }
  feedbackEntry() { return canvasEntry(this.currentRound(), this.viewed(), this.draft()); }
  chooseProposal(key) {
    this.activeKey = key;
    if (!this.locked()) this.save(key, this.draft().note);
  }
  prepareFeedback() {
    const entry = this.feedbackEntry();
    if (entry.choice !== this.draft().choice) this.save(entry.choice, entry.note);
  }
  draft() { return this.data?.feedback.rounds[this.roundId] || EMPTY_ENTRY; }
  noteFocused() { return document.activeElement === this.querySelector('#note'); }
  dialogOpen() { return [...this.querySelectorAll('m3e-dialog')].some(dialog => dialog.open); }
  error() { return this.saves?.error; }
  locked() { return isLocked({ busy: this.busy, approved: this.data?.feedback.approved }); }

  async poll() {
    if (this.stopped) return;
    if (!document.hidden) await this.load();
    this.timer = setTimeout(() => this.poll(), 3000);
  }
  async load() {
    if (!mayReplace({ loading: this.loading, busy: this.busy, dialogOpen: this.dialogOpen(), noteFocused: this.noteFocused(), pendingSaves: this.saves?.pending.size, error: this.saves?.error })) return;
    this.loading = true;
    const startedEdits = this.edits, startedSaves = this.saves, revision = this.saves?.revision;
    try {
      const response = await fetch('/api/exploration');
      if (!response.ok) throw new Error((await response.json()).error);
      const incoming = await response.json();
      if (staleResult({ busy: this.busy, editsChanged: this.edits !== startedEdits, savesChanged: this.saves !== startedSaves, revisionChanged: this.saves?.revision !== revision, noteFocused: this.noteFocused(), dialogOpen: this.dialogOpen() })) return;
      this.connectionError = '';
      const incomingRounds = incoming.rounds.map(round => round.id);
      const newRounds = !sameRounds(incomingRounds, this.data?.rounds.map(round => round.id));
      if (!shouldReload({ hadData: Boolean(this.data), newRounds, sameRevision: incoming.feedback.revision === revision, sameTitle: incoming.title === this.data?.title, sameSession: sameSession(incoming.session, this.data?.session) })) return;
      this.roundId = nextRoundSelection({ previousRound: this.roundId, previousLatest: this.data?.rounds.at(-1)?.id, hadData: Boolean(this.data), incomingRounds });
      this.data = incoming;
      this.saves = new FeedbackSaves(incoming.feedback.revision, () => this.requestUpdate());
      document.title = `${incoming.title} / Design exploration`;
      this.actionError = '';
    } catch (error) { this.connectionError = `Cannot load updates: ${error.message}`; }
    finally { this.loading = false; this.requestUpdate(); }
  }
  save(choice, note) {
    const round = this.currentRound();
    this.data.feedback.rounds[round.id] = { choice, note, requests: this.draft().requests };
    this.edits += 1;
    this.saves.enqueue(round.id, choice, note);
    this.actionError = '';
    this.requestUpdate();
  }
  async action(path, body) {
    this.busy = true;
    try {
      if (!(await this.saves.flush())) throw new Error('Save your feedback before continuing.');
      const response = await fetch(path, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...body, revision: this.saves.revision }) });
      const result = await response.json();
      if (!response.ok) {
        if (response.status === 409) this.saves.error = { message: result.error, conflict: true };
        throw new Error(result.error);
      }
      this.data.feedback = result;
      this.saves.revision = result.revision;
    } finally { this.busy = false; this.requestUpdate(); }
  }

  changeView(key, event) {
    this[key] = event.target.value;
    this.actionError = '';
  }
  chooseDevice(id) {
    this.device = id;
    try { localStorage.setItem(DEVICE_STORAGE_KEY, id); } catch { /* Remembering the device is optional. */ }
  }
  // Material toggles may deselect themselves; these controls always keep one choice.
  chooseToggle(event, choose) {
    // Nested palette themes also emit bubbling change events during initialization.
    if (event.target !== event.currentTarget) return;
    event.currentTarget.selected = true;
    choose();
  }
  setTheme(key, value) { this.theme = withThemeOverride(this.theme, key, value); }
  effectiveTheme() {
    const proposal = this.viewed();
    const key = proposal && proposalKey(this.currentRound(), proposal);
    const original = this.originalTheme && this.originalTheme.key === key ? this.originalTheme.theme : {};
    return sharedTheme(original, this.theme, this.systemDark);
  }
  receiveTheme(event) {
    const proposal = this.viewed();
    if (proposal && event.detail.key === proposalKey(this.currentRound(), proposal)) this.originalTheme = event.detail;
  }
  async none() {
    const change = rejectionChange(this.currentRound(), this.viewed(), this.draft());
    this.activeKey = change.activeKey;
    this.save(change.choice, this.draft().note);
    await this.updateComplete;
    await this.querySelector('feedback-panel').focusNote();
  }
  async retry() { if (this.saves?.error) await this.saves.retry(); else await this.load(); }
  async discardLocal() {
    this.querySelector('#reload-confirm').hide();
    // Keep local notes and the conflict state until the replacement actually arrives.
    const oldSaves = this.saves;
    this.saves = undefined;
    await this.load();
    if (!this.saves) this.saves = oldSaves;
    this.requestUpdate();
  }
  async refine() {
    if (this.busy) return;
    this.prepareFeedback();
    this.actionError = '';
    try { await this.action('/api/refine', { roundId: this.currentRound().id }); }
    catch (error) { this.actionError = error.message; }
  }
  finish() {
    this.prepareFeedback();
    const round = this.currentRound();
    this.approving = this.draft().choice;
    this.confirmLabel = confirmName(round, selectedProposal(round, this.draft()));
    this.confirmError = '';
    this.querySelector('#confirm').show();
  }
  async approve() {
    if (this.busy) return;
    this.dialogBusy = true;
    try { await this.action('/api/approve', { key: this.approving }); this.querySelector('#confirm').hide(); }
    catch (error) { this.confirmError = error.message; }
    finally { this.dialogBusy = false; }
  }

  render() {
    const { data, saves } = this;
    const device = deviceById(this.device), round = this.currentRound(), entry = this.feedbackEntry(), proposal = this.viewed();
    const theme = this.effectiveTheme();
    const approved = data?.feedback.approved, locked = this.locked(), error = this.error();
    return html`
      <header class="app-header">
        <div class="brand-mark" aria-hidden="true">✳</div>
        <div class="heading"><span class="eyebrow">DESIGN EXPLORATION</span><h1 id="title">${data?.title ?? 'Your next direction'}</h1></div>
        <div class="save-bar">
          <span id="status" role="status" aria-live="polite">${statusText({ error, connectionError: this.connectionError, pendingSaves: saves?.pending.size, loaded: Boolean(data) })}</span>
          <m3e-button id="retry" ?hidden=${!showRetry({ error, connectionError: this.connectionError })} @click=${() => this.retry()}>Retry</m3e-button>
          <m3e-button id="reload" ?hidden=${!showReload({ error })} @click=${() => this.querySelector('#reload-confirm').show()}>Reload saved state</m3e-button>
        </div>
        <m3e-button variant="tonal" aria-expanded=${String(this.panelOpen)} aria-controls="inspector" @click=${() => { this.panelOpen = !this.panelOpen; }}> ${this.panelOpen ? 'Hide panel' : 'Show panel'}</m3e-button>
      </header>
      <main class=${`workspace ${this.panelOpen ? '' : 'panel-closed'}`}>
        <section class="canvas" aria-label="Design canvas">
          <div class="canvas-toolbar">
            <m3e-button-group class="device-switch" variant="connected" aria-label="Device">
              ${DEVICES.map(item => html`<m3e-icon-button variant="tonal" toggle .selected=${item.id === device.id}
                aria-checked=${String(item.id === device.id)}
                aria-label=${deviceLabel(item)} title=${`${item.kind} (${item.key})`}
                @change=${event => this.chooseToggle(event, () => this.chooseDevice(item.id))}>${deviceIcon(item.id, item.id === device.id)}</m3e-icon-button>`)}
            </m3e-button-group>
          </div>
          <div class="canvas-navigation">
            <m3e-form-field variant="outlined"><label slot="label">Round</label>
              <m3e-select id="round" aria-label="Round" ?disabled=${this.busy || !round} @change=${event => this.changeView('roundId', event)}>
                ${(data?.rounds || []).map(item => html`<m3e-option value=${item.id}>${item.id} / ${item.title}</m3e-option>`)}
              </m3e-select>
            </m3e-form-field>
            <m3e-form-field variant="outlined"><label slot="label">Direction</label>
              <m3e-select id="direction" aria-label="Direction" .value=${proposal ? proposalKey(round, proposal) : ''} ?disabled=${this.busy || !proposal}
                @change=${event => this.chooseProposal(event.target.value)}>
                ${(round?.proposals || []).map(item => html`<m3e-option value=${proposalKey(round, item)}>${item.name}</m3e-option>`)}
              </m3e-select>
            </m3e-form-field>
          </div>
          <p id="approval" class="approval" ?hidden=${!approved}>${approvalText(data)}</p>
          <div class="canvas-scroll">
            ${proposal ? html`<proposal-canvas .round=${round} .proposal=${proposal} .device=${device} .theme=${theme} @proposal-theme=${event => this.receiveTheme(event)}></proposal-canvas>`
              : html`<div class="empty-canvas"><span class="empty-mark" aria-hidden="true">✳</span><h2>Room for a new idea.</h2><p>${data ? WAITING_TEXT : 'Connecting to your exploration.'}</p><p>Ask your agent to explore a screen.<br>Your directions will appear here.</p></div>`}
          </div>
        </section>
        <aside id="inspector" class="inspector" ?hidden=${!this.panelOpen} aria-label="Preview and feedback">
          <section class="theme-panel" aria-labelledby="theme-title">
            <h3 id="theme-title">Theme</h3><p class="supporting">One theme for the workspace and app. Saved designs stay unchanged.</p>
            <div class="seed-colors" role="group" aria-label="Seed color">
              ${SEED_COLORS.map(seed => html`<m3e-button class="seed" variant="tonal" toggle .selected=${theme.color.toLowerCase() === seed.color}
                aria-label=${seed.name} @change=${event => this.chooseToggle(event, () => this.setTheme('color', seed.color))}>
                <span class="seed-row">
                  <m3e-theme class="palette-preview" color=${seed.color} variant=${theme.variant} scheme=${theme.scheme} aria-hidden="true">
                    <span class="swatch"></span>
                    <span class="palette-samples"><span></span><span></span><span></span><span></span><span></span></span>
                  </m3e-theme>
                  <span class="seed-name">${seed.name}</span>
                </span>
              </m3e-button>`)}
            </div>
            <div class="palette-control"><span id="palette-label">Palette</span>
              <m3e-filter-chip-set id="theme-variant" aria-labelledby="palette-label">
                ${['own', ...THEME_VARIANTS].map(value => html`<m3e-filter-chip value=${value} .selected=${(this.theme.variant ?? 'own') === value}
                  aria-checked=${String((this.theme.variant ?? 'own') === value)}
                  @change=${event => this.chooseToggle(event, () => this.setTheme('variant', value))}>${value === 'own' ? "Proposal's own" : value === 'tonal-spot' ? 'Tonal spot' : value[0].toUpperCase() + value.slice(1)}</m3e-filter-chip>`)}
              </m3e-filter-chip-set>
            </div>
            <div class="switch-row"><span id="dark-label">Dark theme</span><m3e-switch id="theme-dark" aria-labelledby="dark-label" .checked=${theme.scheme === 'dark'} @change=${event => this.setTheme('scheme', event.target.checked ? 'dark' : 'light')}></m3e-switch></div>
          </section>
          ${data ? html`<feedback-panel .round=${round} .proposal=${proposal} .entry=${entry} .session=${data.session} .approved=${Boolean(approved)} .locked=${locked}
            .refineEnabled=${canRefine({ locked, error: Boolean(error), entry })} .finishEnabled=${canFinish({ locked, error: Boolean(error), proposal: entry.choice === 'none' ? undefined : proposal })}
            .noneEnabled=${canChooseNone({ locked, round })} .actionError=${this.actionError}
            @note-input=${event => this.save(entry.choice, event.detail)} @none=${() => this.none()}
            @refine=${() => this.refine()} @finish=${() => this.finish()}></feedback-panel>` : ''}
        </aside>
      </main>
      <m3e-dialog id="confirm" aria-labelledby="confirm-title" .disableClose=${this.busy}>
        <h2 slot="header" id="confirm-title">Finish this direction?</h2><p id="confirm-name">${this.confirmLabel}</p>
        <p>Finishing makes this exploration read-only. It does not modify your app.</p>
        <div slot="actions" class="actions"><m3e-button id="cancel-approval" ?disabled=${this.dialogBusy} @click=${() => this.querySelector('#confirm').hide()}>Keep exploring</m3e-button><m3e-button variant="filled" id="confirm-approval" ?disabled=${this.dialogBusy} @click=${() => this.approve()}>Finish design</m3e-button></div>
        <p id="confirm-error" role="alert">${this.confirmError}</p>
      </m3e-dialog>
      <m3e-dialog id="reload-confirm" aria-labelledby="reload-title">
        <h2 slot="header" id="reload-title">Reload saved feedback?</h2>
        <p>Unsaved notes in this tab will be discarded. Copy them first if needed.</p>
        <div slot="actions" class="actions"><m3e-button @click=${() => this.querySelector('#reload-confirm').hide()}>Cancel</m3e-button><m3e-button id="discard-local" @click=${() => this.discardLocal()}>Reload saved feedback</m3e-button></div>
      </m3e-dialog>`;
  }
}
customElements.define('gallery-app', GalleryApp);
