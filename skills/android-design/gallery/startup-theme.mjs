import { authoredTheme, EMPTY_ENTRY, proposalKey, viewedProposal } from './src/state.mjs';

// Only validated theme fields and proposal identifiers reach the shell's HTML.
export function themeFromHTML(source) {
  const tag = source.replace(/<!--[\s\S]*?-->/g, '').match(/<m3e-theme\b[^>]*>/i)?.[0] ?? '';
  const attributes = Object.fromEntries([...tag.matchAll(/\b(color|variant|scheme|data-proposal-key)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+))/gi)]
    .map((match) => [match[1].toLowerCase(), match[2] ?? match[3] ?? match[4]]));
  return { key: validKey(attributes['data-proposal-key']), theme: authoredTheme(attributes) };
}
const validKey = value => /^\d{3,}\/[a-z][a-z0-9-]{0,39}$/.test(value ?? '') ? value : null;
export function startupTheme(data, readProposal) {
  const round = data.rounds.at(-1);
  const proposal = viewedProposal(round, '', data.feedback.rounds[round?.id] ?? EMPTY_ENTRY);
  if (!proposal) return { key: null, theme: authoredTheme({}) };
  return { key: proposalKey(round, proposal), theme: themeFromHTML(readProposal(round.id, proposal.id)).theme };
}
export function applyStartupTheme(source, bootstrap) {
  const theme = authoredTheme(bootstrap.theme), key = validKey(bootstrap.key);
  const attributes = Object.entries(theme).map(([name, value]) => `${name}="${value}"`).join(' ');
  return source.replace(/<meta name="color-scheme" content="[^"]*">/, `<meta name="color-scheme" content="${theme.scheme === 'auto' ? 'light dark' : theme.scheme}">`)
    .replace(/<m3e-theme\b[^>]*>/, `<m3e-theme ${attributes}${key ? ` data-proposal-key="${key}"` : ''}>`);
}
