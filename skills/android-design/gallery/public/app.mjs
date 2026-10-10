import { FeedbackSaves } from './saves.mjs';
const $ = id => document.getElementById(id);
const labels = { phone: 'Pixel 11', tablet: 'Pixel Tablet', desktop: 'Googlebook', foldable: 'Z Fold8' };
let data, saves, approving, busy = false, loading = false, edits = 0, connectionError = '';
const element = (tag, text, className) => {
  const node = document.createElement(tag);
  if (text !== undefined) node.textContent = text;
  if (className) node.className = className;
  return node;
};
const currentRound = () => data?.rounds.find(round => round.id === $('round').value);
const draft = () => data?.feedback.rounds[$('round').value] || { choice: null, note: '', requests: [] };
const selectedProposal = () => currentRound()?.proposals.find(p => `${currentRound().id}/${p.id}` === draft().choice);
function optionName(round, proposalId) {
  let position = round.proposals.findIndex(proposal => proposal.id === proposalId) + 1;
  let label = '';
  while (position > 0) {
    position -= 1;
    label = String.fromCharCode(65 + position % 26) + label;
    position = Math.floor(position / 26);
  }
  return `Option ${label}`;
}
function requested() {
  const entry = draft(), last = entry.requests.at(-1);
  return last && last.choice === entry.choice && last.note === entry.note;
}
function status() {
  const error = saves?.error;
  $('status').textContent = error?.message || connectionError || (saves?.pending.size ? 'Saving...' : data ? 'Saved' : 'Loading...');
  $('retry').hidden = !(connectionError || (error && !error.conflict));
  $('reload').hidden = !error?.conflict;
  const locked = busy || Boolean(data?.feedback.approved);
  document.querySelectorAll('input[name="design"]').forEach(input => { input.disabled = locked; });
  $('note').disabled = locked;
  $('none').disabled = locked || !currentRound();
  $('round').disabled = busy;
  $('device').disabled = busy;
  $('refine').disabled = locked || Boolean(error) || !draft().choice || (draft().choice === 'none' && !draft().note.trim()) || Boolean(requested());
  $('finish').disabled = locked || Boolean(error) || !selectedProposal();
}
function renderActions() {
  const proposal = selectedProposal(), entry = draft();
  $('selection').textContent = proposal ? `${optionName(currentRound(), proposal.id)} selected` : entry.choice === 'none' ? 'Try another direction' : 'Select a design';
  $('note-label').textContent = entry.choice === 'none' ? 'What should we try instead?' : 'What should change?';
  $('note-field').hidden = !entry.choice;
  $('none').setAttribute('aria-pressed', String(entry.choice === 'none'));
  $('refine').textContent = entry.choice === 'none' ? 'Request new directions' : 'Refine';
  $('finish').textContent = 'Finish';
  $('request-status').textContent = requested() ? 'Request saved. Return to chat to continue.' : '';
  $('action-bar').hidden = !currentRound();
  $('workflow-actions').hidden = Boolean(data.feedback.approved);
  document.querySelectorAll('input[name="design"]').forEach(input => {
    input.checked = input.value === entry.choice;
    input.parentElement.querySelector('.selection-label').textContent = input.checked ? 'Selected' : 'Select';
  });
  status();
}
function save(choice, note) {
  const roundId = currentRound().id;
  const requests = draft().requests;
  data.feedback.rounds[roundId] = { choice, note, requests };
  edits += 1;
  saves.enqueue(roundId, choice, note);
  $('action-error').textContent = '';
  renderActions();
}
function imageURL(round, proposal, device) { return `/images/${round.id}/${proposal.id}/${device}`; }
function render() {
  $('proposals').replaceChildren();
  const round = currentRound(), approved = data.feedback.approved;
  $('approval').hidden = !approved;
  $('approval').textContent = approved ? `Final: ${optionName(data.rounds.find(r => r.id === approved.roundId), approved.key.split('/')[1])}, round ${approved.roundId}. Read-only.` : '';
  $('note').value = draft().note;
  $('action-error').textContent = '';
  if (!round) $('proposals').append(element('p', 'Waiting for proposals.'));
  for (const proposal of round?.proposals || []) {
    const name = optionName(round, proposal.id);
    const key = `${round.id}/${proposal.id}`;
    const card = element('article', undefined, 'proposal');
    const heading = element('div', undefined, 'card-heading');
    heading.append(element('h2', name));
    const choice = element('label', undefined, 'thumbnail');
    const radio = element('input');
    radio.type = 'radio'; radio.name = 'design'; radio.value = key;
    radio.setAttribute('aria-label', `Select ${name}`);
    radio.addEventListener('change', () => save(key, $('note').value));
    const image = element('img'), device = $('device').value;
    image.src = imageURL(round, proposal, device);
    image.alt = `${name} on ${labels[device]}`;
    image.width = proposal.images[device].width;
    image.height = proposal.images[device].height;
    image.addEventListener('error', () => {
      image.hidden = true;
      choice.append(element('span', 'Image unavailable.'));
    }, { once: true });
    choice.append(radio, image, element('span', 'Select', 'selection-label'));
    card.append(heading, element('p', proposal.rationale, 'rationale'), choice);
    $('proposals').append(card);
  }
  renderActions();
}
function updateRoundOptions(value) {
  $('round').querySelectorAll('option').forEach(option => option.remove());
  $('round').append(...data.rounds.map(round => {
    const option = element('option', `${round.id} / ${round.title}`); option.value = round.id; return option;
  }));
  $('round').value = value || data.rounds.at(-1)?.id || '';
}
async function load() {
  // Never replace an editor while typing, saving, confirming, or recovering an error.
  if (loading || busy || document.querySelector('dialog[open]') || document.activeElement === $('note') || saves?.pending.size || saves?.error) return;
  loading = true;
  const startedEdits = edits, startedSaves = saves, revision = saves?.revision;
  try {
    const response = await fetch('/api/exploration');
    if (!response.ok) throw new Error((await response.json()).error);
    const incoming = await response.json();
    if (busy || edits !== startedEdits || saves !== startedSaves || saves?.revision !== revision || document.activeElement === $('note') || document.querySelector('dialog[open]')) return;
    connectionError = '';
    const newRounds = JSON.stringify(incoming.rounds.map(r => r.id)) !== JSON.stringify(data?.rounds.map(r => r.id));
    if (data && !newRounds && incoming.feedback.revision === revision && incoming.title === data.title) return;
    const previousRound = $('round').value;
    const wasLatest = !data || previousRound === data.rounds.at(-1)?.id;
    data = incoming;
    saves = new FeedbackSaves(data.feedback.revision, status);
    $('title').textContent = data.title;
    document.title = `${data.title} / Design exploration`;
    updateRoundOptions(newRounds && wasLatest ? undefined : previousRound);
    render();
  } catch (error) { connectionError = `Cannot load updates: ${error.message}`; }
  finally { loading = false; status(); }
}
async function action(path, body) {
  busy = true; status();
  try {
    if (!(await saves.flush())) throw new Error('Save your feedback before continuing.');
    const response = await fetch(path, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...body, revision: saves.revision }) });
    const result = await response.json();
    if (!response.ok) {
      if (response.status === 409) saves.error = { message: result.error, conflict: true };
      throw new Error(result.error);
    }
    data.feedback = result;
    saves.revision = result.revision;
  } finally { busy = false; status(); }
}
$('round').addEventListener('change', render);
$('device').addEventListener('change', render);
$('note').addEventListener('input', () => save(draft().choice, $('note').value));
$('none').addEventListener('click', () => {
  save('none', $('note').value);
  $('note').focus();
});
$('retry').addEventListener('click', async () => { if (saves?.error) await saves.retry(); else await load(); });
$('reload').addEventListener('click', () => $('reload-confirm').showModal());
$('discard-local').addEventListener('click', async () => {
  $('reload-confirm').close();
  // Keep local notes and the conflict state until the replacement actually arrives.
  const oldSaves = saves;
  saves = undefined;
  await load();
  if (!saves) saves = oldSaves;
  status();
});
$('refine').addEventListener('click', async () => {
  if (busy) return;
  $('action-error').textContent = '';
  try { await action('/api/refine', { roundId: currentRound().id }); renderActions(); }
  catch (error) { $('action-error').textContent = error.message; }
});
$('finish').addEventListener('click', () => {
  approving = draft().choice;
  $('confirm-name').textContent = `${optionName(currentRound(), selectedProposal().id)} / Round ${currentRound().id}`;
  $('confirm-error').textContent = ''; $('confirm').showModal();
});
$('cancel-approval').addEventListener('click', () => $('confirm').close());
$('confirm-approval').addEventListener('click', async () => {
  if (busy) return;
  $('confirm-approval').disabled = true; $('cancel-approval').disabled = true;
  try { await action('/api/approve', { key: approving }); $('confirm').close(); render(); }
  catch (error) { $('confirm-error').textContent = error.message; }
  finally { $('confirm-approval').disabled = false; $('cancel-approval').disabled = false; }
});
$('confirm').addEventListener('cancel', event => { if (busy) event.preventDefault(); });
window.addEventListener('beforeunload', event => { if (saves?.pending.size || busy) { event.preventDefault(); event.returnValue = ''; } });
async function poll() {
  if (!document.hidden) await load();
  window.setTimeout(poll, 3000);
}
poll();
