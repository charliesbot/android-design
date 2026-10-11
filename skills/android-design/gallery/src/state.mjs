// Pure decisions for the gallery page: the dynamic status and label text and every
// enable/disable rule live here so components only render the answers.

// Device canvases in CSS px (equal to dp). Order is the switcher order; the key is its shortcut.
export const DEVICES = [
  { id: 'phone', kind: 'Phone', name: 'Pixel 11', width: 411, height: 923, radius: 28, key: '1' },
  { id: 'foldable', kind: 'Foldable', name: 'Z Fold8', width: 704, height: 933, radius: 20, key: '2' },
  { id: 'tablet', kind: 'Tablet', name: 'Pixel Tablet', width: 1280, height: 800, radius: 14, key: '3' },
  { id: 'desktop', kind: 'Desktop', name: 'Googlebook', width: 1440, height: 900, radius: 10, key: '4' },
];
export const DEFAULT_DEVICE = DEVICES[0].id;
export const deviceById = id => DEVICES.find(device => device.id === id) ?? DEVICES[0];
export const deviceForKey = key => DEVICES.find(device => device.key === key);
export const deviceLabel = device => `${device.kind}, ${device.name}`;
export const deviceSize = device => `${device.width} by ${device.height}`;

// Largest scale at which the whole device canvas fits the box, never enlarging past 1.
// Zero means the box is not measured yet.
export function frameScale({ availableWidth, maxHeight, device }) {
  if (!(availableWidth > 0) || !(maxHeight > 0)) return 0;
  return Math.min(1, availableWidth / device.width, maxHeight / device.height);
}
export const MAX_FRAME_HEIGHT_RATIO = 0.7;

// Preview-only theme: only the fields the user touched are sent, so untouched ones keep
// the proposal's own theme. Nothing here is saved to feedback.
export const THEME_VARIANTS = ['tonal-spot', 'neutral', 'vibrant', 'expressive'];
export const THEME_SCHEMES = ['light', 'dark'];
export const DEFAULT_THEME_COLOR = '#6750a4';
const THEME_VALID = {
  color: value => typeof value === 'string' && /^#[0-9a-f]{6}$/i.test(value),
  variant: value => THEME_VARIANTS.includes(value),
  scheme: value => THEME_SCHEMES.includes(value),
};
export function withThemeOverride(overrides, key, value) {
  const next = { ...overrides };
  if (THEME_VALID[key]?.(value)) next[key] = value; else delete next[key];
  return next;
}
// Null when there is nothing to send.
export function themeMessage(overrides) {
  const fields = Object.keys(THEME_VALID).filter(key => THEME_VALID[key](overrides?.[key]));
  return fields.length ? { type: 'theme', ...Object.fromEntries(fields.map(key => [key, overrides[key]])) } : null;
}
export const EMPTY_ENTRY = { choice: null, note: '', requests: [] };
export const WAITING_TEXT = 'Waiting for proposals.';

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
export const REQUEST_SAVED_TEXT = 'Request saved. Return to chat to continue.';
export const REQUEST_WAITING_TEXT = 'Request saved. The next round will appear here.';
export const REQUEST_WORKING_TEXT = 'The agent is working on your request.';
export function requestStatusText(entry, session) {
  if (!requested(entry)) return '';
  if (session?.state === 'working') return REQUEST_WORKING_TEXT;
  if (session?.state === 'waiting') return REQUEST_WAITING_TEXT;
  return REQUEST_SAVED_TEXT;
}
export const selectionLabel = checked => checked ? 'Selected' : 'Select';

export function approvalText(data) {
  const approved = data?.feedback.approved;
  const round = approved && data.rounds.find(candidate => candidate.id === approved.roundId);
  if (!round) return '';
  return `Final: ${optionName(round, approved.key.split('/')[1])}, round ${approved.roundId}. Read-only.`;
}
export const confirmName = (round, proposal) => `${optionName(round, proposal.id)} / Round ${round.id}`;
export const frameTitle = (name, device) => `${name} on ${device.name}`;
export const frameURL = (round, proposal) => `/rounds/${round.id}/${proposal.id}/`;

// Never replace an editor while loading, typing, saving, confirming, or recovering an error.
export const mayReplace = ({ loading, busy, dialogOpen, noteFocused, pendingSaves, error }) =>
  !(loading || busy || dialogOpen || noteFocused || pendingSaves || error);

// A fetched result is discarded when the page changed while it was in flight.
export const staleResult = ({ busy, editsChanged, savesChanged, revisionChanged, noteFocused, dialogOpen }) =>
  Boolean(busy || editsChanged || savesChanged || revisionChanged || noteFocused || dialogOpen);

export const sameRounds = (previous, incoming) => JSON.stringify(previous) === JSON.stringify(incoming);
export const shouldReload = ({ hadData, newRounds, sameRevision, sameTitle, sameSession = true }) => !hadData || newRounds || !sameRevision || !sameTitle || !sameSession;

// The agent session changes without a feedback revision, so compare it on its own.
export const sameSession = (a, b) => (a === null || a === undefined) === (b === null || b === undefined) && a?.state === b?.state && a?.until === b?.until;

// Follow the newest round only when the user was already viewing the latest one.
export function nextRoundSelection({ previousRound, previousLatest, hadData, incomingRounds }) {
  const latest = incomingRounds.at(-1) ?? '';
  if (!hadData || previousRound === previousLatest) return latest;
  return incomingRounds.includes(previousRound) ? previousRound : latest;
}

export const canCancelDialog = ({ busy }) => !busy;
export const guardUnload = ({ pendingSaves, busy }) => Boolean(pendingSaves || busy);
