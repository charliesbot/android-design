import { LitElement, html } from 'lit';
import { FeedbackSaves } from '../saves.mjs';
import './proposal-card.mjs';
import './action-bar.mjs';
import { deviceIcon } from '../icons.mjs';
import {
  DEFAULT_DEVICE, DEFAULT_THEME_COLOR, DEVICES, EMPTY_ENTRY, THEME_VARIANTS, WAITING_TEXT, approvalText, deviceById, deviceForKey, deviceLabel, deviceSize,
  withThemeOverride, canCancelDialog, canChooseNone, canFinish, canRefine,
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
    resets: { state: true },
    confirmLabel: { state: true },
  };
  constructor() {
    super();
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
    this.resets = 0;
    this.confirmLabel = '';
    this.loading = false;
    this.edits = 0;
    this.approving = null;
    this.timer = undefined;
    this.onKeydown = event => {
      if (event.ctrlKey || event.metaKey || event.altKey || event.target.closest?.('input, select, textarea, [contenteditable]')) return;
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
    this.poll();
  }
  disconnectedCallback() {
    super.disconnectedCallback();
    window.removeEventListener('beforeunload', this.beforeUnload);
    window.removeEventListener('keydown', this.onKeydown);
    clearTimeout(this.timer);
    this.stopped = true;
  }
  updated() {
    // Option children render after the select, so apply the selected values here.
    const round = this.querySelector('#round'), variant = this.querySelector('#theme-variant');
    if (round && round.value !== this.roundId) round.value = this.roundId;
    const wanted = this.theme.variant ?? '';
    if (variant && variant.value !== wanted) variant.value = wanted;
  }

  currentRound() { return this.data?.rounds.find(round => round.id === this.roundId); }
  draft() { return this.data?.feedback.rounds[this.roundId] || EMPTY_ENTRY; }
  noteFocused() { return document.activeElement === this.querySelector('#note'); }
  dialogOpen() { return Boolean(this.querySelector('dialog[open]')); }
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
  setTheme(key, value) { this.theme = withThemeOverride(this.theme, key, value); }
  resetTheme() {
    this.theme = {};
    this.resets += 1;
  }
  async none() {
    this.save('none', this.draft().note);
    await this.updateComplete;
    await this.querySelector('action-bar').focusNote();
  }
  async retry() { if (this.saves?.error) await this.saves.retry(); else await this.load(); }
  async discardLocal() {
    this.querySelector('#reload-confirm').close();
    // Keep local notes and the conflict state until the replacement actually arrives.
    const oldSaves = this.saves;
    this.saves = undefined;
    await this.load();
    if (!this.saves) this.saves = oldSaves;
    this.requestUpdate();
  }
  async refine() {
    if (this.busy) return;
    this.actionError = '';
    try { await this.action('/api/refine', { roundId: this.currentRound().id }); }
    catch (error) { this.actionError = error.message; }
  }
  finish() {
    const round = this.currentRound();
    this.approving = this.draft().choice;
    this.confirmLabel = confirmName(round, selectedProposal(round, this.draft()));
    this.confirmError = '';
    this.querySelector('#confirm').showModal();
  }
  async approve() {
    if (this.busy) return;
    this.dialogBusy = true;
    try { await this.action('/api/approve', { key: this.approving }); this.querySelector('#confirm').close(); }
    catch (error) { this.confirmError = error.message; }
    finally { this.dialogBusy = false; }
  }

  render() {
    const { data, saves } = this;
    const device = deviceById(this.device), round = this.currentRound(), entry = this.draft(), proposal = selectedProposal(round, entry);
    const approved = data?.feedback.approved, locked = this.locked(), error = this.error();
    const approval = approvalText(data);
    return html`
      <header><h1 id="title">${data?.title ?? 'Design exploration'}</h1></header>
      <main>
        <section class="toolbar" aria-label="View controls">
          <label>Round <select id="round" ?disabled=${this.busy} @change=${event => this.changeView('roundId', event)}><button type="button"><selectedcontent></selectedcontent></button>${(data?.rounds || []).map(item => html`<option value=${item.id}>${item.id} / ${item.title}</option>`)}</select></label>
          <div class="device-switch" role="group" aria-label="Device">
            ${DEVICES.map(item => html`<button type="button" class="device-button" aria-pressed=${item.id === device.id ? 'true' : 'false'}
              aria-label=${deviceLabel(item)} title=${`${item.kind} (${item.key})`} @click=${() => this.chooseDevice(item.id)}>${deviceIcon(item.id)}</button>`)}
            <span class="device-meta" aria-live="polite">${device.name}, ${deviceSize(device)}</span>
          </div>
          <div class="preview-theme" role="group" aria-label="Preview theme, not saved">
            <span class="preview-note">Preview only, not saved</span>
            <label>Seed color <input id="theme-color" type="color" .value=${this.theme.color ?? DEFAULT_THEME_COLOR} @input=${event => this.setTheme('color', event.target.value)}></label>
            <label>Variant <select id="theme-variant" @change=${event => this.setTheme('variant', event.target.value)}><button type="button"><selectedcontent></selectedcontent></button><option value="">Proposal's own</option>${THEME_VARIANTS.map(value => html`<option value=${value}>${value}</option>`)}</select></label>
            <label class="check"><input id="theme-dark" type="checkbox" .checked=${this.theme.scheme === 'dark'} @change=${event => this.setTheme('scheme', event.target.checked ? 'dark' : 'light')}> Dark</label>
            <button id="theme-reset" type="button" ?disabled=${!Object.keys(this.theme).length} @click=${() => this.resetTheme()}>Reset</button>
          </div>
        </section>
        <div class="save-bar">
          <p id="status" role="status" aria-live="polite">${statusText({ error, connectionError: this.connectionError, pendingSaves: saves?.pending.size, loaded: Boolean(data) })}</p>
          <button id="retry" type="button" ?hidden=${!showRetry({ error, connectionError: this.connectionError })} @click=${() => this.retry()}>Retry saving</button>
          <button id="reload" type="button" ?hidden=${!showReload({ error })} @click=${() => this.querySelector('#reload-confirm').showModal()}>Reload saved state</button>
        </div>
        <p id="approval" class="approval" ?hidden=${!approved}>${approval}</p>
        <section id="proposals" class="proposals" aria-label="Design proposals">
          ${data && !round ? html`<p>${WAITING_TEXT}</p>` : ''}
          ${(round?.proposals || []).map(item => html`<proposal-card .round=${round} .proposal=${item} .device=${device} .theme=${this.theme} .resets=${this.resets}
            .checked=${proposalKey(round, item) === entry.choice} .locked=${locked}
            @choose=${event => this.save(event.detail, this.draft().note)}></proposal-card>`)}
        </section>
        ${data ? html`<action-bar .round=${round} .proposal=${proposal} .entry=${entry} .session=${data.session} .approved=${Boolean(approved)} .locked=${locked}
          .refineEnabled=${canRefine({ locked, error: Boolean(error), entry })} .finishEnabled=${canFinish({ locked, error: Boolean(error), proposal })}
          .noneEnabled=${canChooseNone({ locked, round })} .actionError=${this.actionError}
          @note-input=${event => this.save(this.draft().choice, event.detail)} @none=${() => this.none()}
          @refine=${() => this.refine()} @finish=${() => this.finish()}></action-bar>` : ''}
      </main>
      <dialog id="confirm" aria-labelledby="confirm-title" @cancel=${event => { if (!canCancelDialog({ busy: this.busy })) event.preventDefault(); }}>
        <h2 id="confirm-title">Finish with this design?</h2><p id="confirm-name">${this.confirmLabel}</p>
        <p>Finishing makes this exploration read-only. It does not modify your app.</p>
        <div class="actions"><button id="cancel-approval" type="button" ?disabled=${this.dialogBusy} @click=${() => this.querySelector('#confirm').close()}>Keep exploring</button><button id="confirm-approval" type="button" ?disabled=${this.dialogBusy} @click=${() => this.approve()}>Finish design</button></div>
        <p id="confirm-error" role="alert">${this.confirmError}</p>
      </dialog>
      <dialog id="reload-confirm" aria-labelledby="reload-title">
        <h2 id="reload-title">Reload saved feedback?</h2>
        <p>Unsaved notes in this tab will be discarded. Copy them first if needed.</p>
        <div class="actions"><form method="dialog"><button>Cancel</button></form><button id="discard-local" type="button" @click=${() => this.discardLocal()}>Reload saved feedback</button></div>
      </dialog>`;
  }
}
customElements.define('gallery-app', GalleryApp);
