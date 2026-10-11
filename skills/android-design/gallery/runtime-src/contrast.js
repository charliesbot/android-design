// Pure WCAG 2.x contrast math, kept free of DOM access so Node tests can import it.
// Colors are { r, g, b, a } with r, g, b in 0..255 and a in 0..1.

export const luminance = ({ r, g, b }) => {
  const f = v => { v /= 255; return v <= 0.03928 ? v / 12.92 : ((v + 0.055) / 1.055) ** 2.4; };
  return 0.2126 * f(r) + 0.7152 * f(g) + 0.0722 * f(b);
};

export const ratio = (a, b) => {
  const [hi, lo] = [luminance(a), luminance(b)].sort((x, y) => y - x);
  return (hi + 0.05) / (lo + 0.05);
};

// The opaque color of `fg` painted over the opaque `back`.
export const over = (fg, back) => (fg.a < 1
  ? { r: fg.r * fg.a + back.r * (1 - fg.a), g: fg.g * fg.a + back.g * (1 - fg.a), b: fg.b * fg.a + back.b * (1 - fg.a), a: 1 }
  : fg);

// WCAG large text: 24px and up, or bold at 18.66px and up. Large text needs 3:1, other text 4.5:1.
export const neededRatio = (fontSize, fontWeight) => {
  const large = fontSize >= 24 || (fontSize >= 18.66 && fontWeight >= 700);
  return large ? 3 : 4.5;
};
