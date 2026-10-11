// The gallery's preview-only theme controls post {type: "theme", color, variant, scheme}
// to the proposal frame. Apply it to the page's <m3e-theme>; ignore other origins and bad values.
const VARIANTS = ['monochrome', 'neutral', 'tonal-spot', 'vibrant', 'expressive', 'fidelity', 'content', 'rainbow', 'fruit-salad'];
const SCHEMES = ['light', 'dark', 'auto'];

window.addEventListener('message', event => {
  if (event.origin !== window.location.origin) return;
  const { type, color, variant, scheme } = event.data ?? {};
  if (type !== 'theme') return;
  const theme = document.querySelector('m3e-theme');
  if (!theme) return;
  if (typeof color === 'string' && /^#[0-9a-f]{6}$/i.test(color)) theme.setAttribute('color', color);
  if (VARIANTS.includes(variant)) theme.setAttribute('variant', variant);
  if (SCHEMES.includes(scheme)) theme.setAttribute('scheme', scheme);
});
