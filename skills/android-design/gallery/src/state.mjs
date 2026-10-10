// Pure decisions for the gallery page: the dynamic status and label text and every
// enable/disable rule live here so components only render the answers.

export const DEVICE_LABELS = { phone: 'Pixel 11', tablet: 'Pixel Tablet', desktop: 'Googlebook', foldable: 'Z Fold8' };
export const EMPTY_ENTRY = { choice: null, note: '', requests: [] };
export const WAITING_TEXT = 'Waiting for proposals.';
export const IMAGE_UNAVAILABLE_TEXT = 'Image unavailable.';

export function optionName(round, proposalId) {
  let position = round.proposals.findIndex(proposal => proposal.id === proposalId) + 1;
  let label = '';
  while (position > 0) {
    position -= 1;
    label = String.fromCharCode(65 + position % 26) + label;
    position = Math.floor(position / 26);
  }
  return `Option ${label}`;
}

export const proposalKey = (round, proposal) => `${round.id}/${proposal.id}`;
export const selectedProposal = (round, entry) => round?.proposals.find(proposal => proposalKey(round, proposal) === entry.choice);

// True when the latest refine request already carries the current choice and note.
export function requested(entry) {
  const last = entry.requests.at(-1);
  return Boolean(last && last.choice === entry.choice && last.note === entry.note);
}

export const isLocked = ({ busy, approved }) => Boolean(busy || approved);
export const canRefine = ({ locked, error, entry }) =>
  !(locked || error || !entry.choice || (entry.choice === 'none' && !entry.note.trim()) || requested(entry));
export const canFinish = ({ locked, error, proposal }) => !(locked || error || !proposal);
export const canChooseNone = ({ locked, round }) => !(locked || !round);

export function statusText({ error, connectionError, pendingSaves, loaded }) {
  return error?.message || connectionError || (pendingSaves ? 'Saving...' : loaded ? 'Saved' : 'Loading...');
}
export const showRetry = ({ error, connectionError }) => Boolean(connectionError || (error && !error.conflict));
export const showReload = ({ error }) => Boolean(error?.conflict);

export function selectionText({ round, proposal, entry }) {
  if (proposal) return `${optionName(round, proposal.id)} selected`;
  return entry.choice === 'none' ? 'Try another direction' : 'Select a design';
}
export const noteLabel = entry => entry.choice === 'none' ? 'What should we try instead?' : 'What should change?';
export const refineLabel = entry => entry.choice === 'none' ? 'Request new directions' : 'Refine';
export const requestStatusText = entry => requested(entry) ? 'Request saved. Return to chat to continue.' : '';
export const selectionLabel = checked => checked ? 'Selected' : 'Select';

export function approvalText(data) {
  const approved = data?.feedback.approved;
  const round = approved && data.rounds.find(candidate => candidate.id === approved.roundId);
  if (!round) return '';
  return `Final: ${optionName(round, approved.key.split('/')[1])}, round ${approved.roundId}. Read-only.`;
}
export const confirmName = (round, proposal) => `${optionName(round, proposal.id)} / Round ${round.id}`;
export const imageAlt = (name, device) => `${name} on ${DEVICE_LABELS[device]}`;
export const imageURL = (round, proposal, device) => `/images/${round.id}/${proposal.id}/${device}`;

// Never replace an editor while loading, typing, saving, confirming, or recovering an error.
export const mayReplace = ({ loading, busy, dialogOpen, noteFocused, pendingSaves, error }) =>
  !(loading || busy || dialogOpen || noteFocused || pendingSaves || error);

// A fetched result is discarded when the page changed while it was in flight.
export const staleResult = ({ busy, editsChanged, savesChanged, revisionChanged, noteFocused, dialogOpen }) =>
  Boolean(busy || editsChanged || savesChanged || revisionChanged || noteFocused || dialogOpen);

export const sameRounds = (previous, incoming) => JSON.stringify(previous) === JSON.stringify(incoming);
export const shouldReload = ({ hadData, newRounds, sameRevision, sameTitle }) => !hadData || newRounds || !sameRevision || !sameTitle;

// Follow the newest round only when the user was already viewing the latest one.
export function nextRoundSelection({ previousRound, previousLatest, hadData, incomingRounds }) {
  const latest = incomingRounds.at(-1) ?? '';
  if (!hadData || previousRound === previousLatest) return latest;
  return incomingRounds.includes(previousRound) ? previousRound : latest;
}

export const canCancelDialog = ({ busy }) => !busy;
export const guardUnload = ({ pendingSaves, busy }) => Boolean(pendingSaves || busy);
