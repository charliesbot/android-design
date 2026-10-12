// Only a proposal in this gallery may be framed: a same-origin path under /draft/ or /rounds/.
// Returns the path (with any query), or null when the value is anything else.
export function parseSource(value, origin) {
  if (!value || !/^\/(draft|rounds)\//.test(value) || value.startsWith('//') || value.includes('\\')) return null;
  let url;
  try { url = new URL(value, origin); } catch { return null; }
  if (url.origin !== origin || !/^\/(draft|rounds)\//.test(url.pathname)) return null;
  return url.pathname + url.search;
}
