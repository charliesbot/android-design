// The gallery's preview-only controls post {type: "theme", color, variant, scheme} and {type: "state", name}
// to the proposal frame. Apply a theme to the page's <m3e-theme> and open a state through the proposal's own
// window.proposalState; ignore other origins and bad values.
const VARIANTS = ['monochrome', 'neutral', 'tonal-spot', 'vibrant', 'expressive', 'fidelity', 'content', 'rainbow', 'fruit-salad'];
const SCHEMES = ['light', 'dark', 'auto'];

window.addEventListener('message', event => {
  if (event.origin !== window.location.origin) return;
  const { type, color, variant, scheme, name } = event.data ?? {};
  if (type === 'state') {
    // Wait for the page to settle first: a bottom sheet runs a close animation when it first renders and would hide itself again.
    if (typeof name === 'string' && typeof window.proposalState === 'function') window.runtimeSettle().then(() => window.proposalState(name)).catch(console.error);
    return;
  }
  if (type !== 'theme') return;
  const theme = document.querySelector('m3e-theme');
  if (!theme) return;
  if (typeof color === 'string' && /^#[0-9a-f]{6}$/i.test(color)) theme.setAttribute('color', color);
  if (VARIANTS.includes(variant)) theme.setAttribute('variant', variant);
  if (SCHEMES.includes(scheme)) theme.setAttribute('scheme', scheme);
});
