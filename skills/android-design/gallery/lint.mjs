import { readFileSync, readdirSync } from 'node:fs';
import { join } from 'node:path';

// Build caches, secrets, and OS files never belong to a proposal.
const SKIP = new Set(['build', '.gradle', '.kotlin', '.git', 'renders', 'local.properties', '__pycache__', 'screenshotTestDebug', 'node_modules', '.DS_Store']);

export class LintError extends Error {}


// Strict styling lint. Each rule keeps a proposal a lookup into Compose tokens (see assets/proposal-starter/README.md).
const NAMED_COLORS = ('aliceblue antiquewhite aqua aquamarine azure beige bisque black blanchedalmond blue blueviolet brown burlywood cadetblue chartreuse chocolate coral cornflowerblue cornsilk crimson cyan darkblue darkcyan darkgoldenrod darkgray darkgreen darkgrey darkkhaki darkmagenta darkolivegreen darkorange darkorchid darkred darksalmon darkseagreen darkslateblue darkslategray darkslategrey darkturquoise darkviolet deeppink deepskyblue dimgray dimgrey dodgerblue firebrick floralwhite forestgreen fuchsia gainsboro ghostwhite gold goldenrod gray green greenyellow grey honeydew hotpink indianred indigo ivory khaki lavender lavenderblush lawngreen lemonchiffon lightblue lightcoral lightcyan lightgoldenrodyellow lightgray lightgreen lightgrey lightpink lightsalmon lightseagreen lightskyblue lightslategray lightslategrey lightsteelblue lightyellow lime limegreen linen magenta maroon mediumaquamarine mediumblue mediumorchid mediumpurple mediumseagreen mediumslateblue mediumspringgreen mediumturquoise mediumvioletred midnightblue mintcream mistyrose moccasin navajowhite navy oldlace olive olivedrab orange orangered orchid palegoldenrod palegreen paleturquoise palevioletred papayawhip peachpuff peru pink plum powderblue purple rebeccapurple red rosybrown royalblue saddlebrown salmon sandybrown seagreen seashell sienna silver skyblue slateblue slategray slategrey snow springgreen steelblue tan teal thistle tomato turquoise violet wheat white whitesmoke yellow yellowgreen').split(' ');
const COLOR_WORD = new RegExp(`(?<![\\w-])(?:${NAMED_COLORS.join('|')})(?![\\w-])`, 'i');
const COLOR_FUNCTION = /(?<![\w-])(?:rgba?|hsla?|hwb|lab|lch|oklab|oklch|color)\(/i;
const HEX_COLOR = /#[0-9a-f]{3,8}(?![\w-])/i;
// The bundled typefaces (runtime-src/fonts.json, the same list the runtime fonts check reads). A brand face is for headline small (24px) and up,
// Doto for display small (36px) and up, Google Sans Code for numbers and data at any size, Google Sans Flex everywhere.
const FONTS = JSON.parse(readFileSync(new URL('./runtime-src/fonts.json', import.meta.url), 'utf8'));
const BUNDLED = [FONTS.plain, ...FONTS.faces.map(face => face.family)];
// Every Material Symbols name (public/runtime/symbols.json, emitted by the runtime build from the pinned `material-symbols` package).
// <p-icon name> draws the name as a font ligature, so an unknown name would render as plain text; publishing rejects it instead.
const SYMBOLS = new Set(JSON.parse(readFileSync(new URL('./public/runtime/symbols.json', import.meta.url), 'utf8')));
const TYPE_PROPERTIES = new Set(['font', 'font-size', 'font-weight', 'line-height', 'letter-spacing', 'font-variation-settings']);
const blank = text => text.replace(/[^\n]/g, ' ');
const lineAt = (text, index) => text.slice(0, index).split('\n').length;

function lintCss(file, source) {
  const fail = (index, message) => { throw new LintError(`${file}:${lineAt(source, index)}: ${message}`); };
  const text = source.replace(/\/\*[\s\S]*?\*\//g, blank);
  let staticStart = -1, staticEnd = -1;
  const marker = /\/\*\s*static colors\s*\*\//.exec(source);
  if (marker) {
    const open = text.indexOf('{', marker.index);
    let depth = 0, close = -1;
    for (let i = open; open >= 0 && i < text.length; i++) {
      if (text[i] === '{') depth++;
      else if (text[i] === '}' && --depth === 0) { close = i; break; }
    }
    if (open >= 0 && close >= 0) { staticStart = open; staticEnd = close; }
  }
  const stack = [];
  const voices = new Map(); // selector -> { family, min, at, sizes } across every rule that styles that voice
  const frames = []; // one per open block: the family and font-size declared directly in it
  let start = 0;
  const voice = selector => {
    if (!voices.has(selector)) voices.set(selector, { min: 0, sizes: [] });
    return voices.get(selector);
  };
  const declaration = (end) => {
    const raw = text.slice(start, end);
    const colon = raw.indexOf(':');
    if (!stack.length || colon < 0) return;
    const property = raw.slice(0, colon).trim().toLowerCase();
    const value = raw.slice(colon + 1).replace(/url\([^)]*\)/gi, '').replace(/"[^"]*"|'[^']*'/g, '');
    const at = start + raw.search(/\S/);
    const inStatic = start > staticStart && end <= staticEnd;
    if (inStatic) {
      if (!/^--static-[\w-]+$/.test(property)) fail(at, 'The /* static colors */ block may only declare --static-* custom properties');
      return;
    }
    if (HEX_COLOR.test(value) || COLOR_FUNCTION.test(value) || COLOR_WORD.test(value)) {
      fail(at, `Raw color in ${property}. Use var(--md-sys-color-*), or declare a named color once in a /* static colors */ block as --static-*`);
    }
    // The theme emits primary, secondary, tertiary, and error *-dim roles that Compose's ColorScheme lacks; a proposal using one could not be ported (see compose-mapping.md).
    const dim = /--md-sys-color-((?:primary|secondary|tertiary|error)-dim)\b/.exec(value);
    if (dim) fail(at, `${dim[1]} has no Compose counterpart. Use another --md-sys-color-* role`);
    const rule = [...stack].reverse().find(prelude => !prelude.startsWith('@'));
    const isVoice = rule && /^\.voice-[\w-]+$/.test(rule);
    if (property === 'font-family') {
      if (!isVoice) fail(at, 'font-family is not allowed outside a single-class .voice-* rule. Plain text uses Google Sans Flex from the runtime');
      const names = raw.slice(colon + 1).split(',').map(name => name.trim().replace(/^(["'])(.*)\1$/, '$2'));
      const named = names.filter(name => !FONTS.generics.includes(name.toLowerCase()));
      const unknown = named.find(name => !BUNDLED.includes(name));
      if (unknown !== undefined) fail(at, `font-family is not allowed: "${unknown}" is not bundled. Use ${BUNDLED.join(', ')}, optionally followed by ${FONTS.generics.join(', ')} as a fallback`);
      if (named.length !== 1) fail(at, `font-family is not allowed: a voice names exactly one bundled family (got ${named.length}). Use ${BUNDLED.join(', ')}`);
      const face = FONTS.faces.find(item => item.family === named[0]);
      if (face?.minPx) {
        const info = voice(rule);
        info.min = face.minPx; info.family = face.family; info.use = face.use; info.at = at;
        frames.at(-1).needsSize = info;
      }
    }
    if (property === 'font-size' && isVoice) {
      const px = /^\s*(\d+(?:\.\d+)?)px\s*$/.exec(value);
      voice(rule).sizes.push({ px: px ? Number(px[1]) : null, at });
      frames.at(-1).sized = true;
    }
    if (TYPE_PROPERTIES.has(property) && !isVoice) fail(at, `${property} is only allowed in a single-class .voice-* rule. Use a .t-* type role class instead`);
    if (property === 'font' && isVoice) fail(at, 'The font shorthand is not allowed. Set font-size, font-weight, line-height, and font-variation-settings separately');
  };
  for (let i = 0; i < text.length; i++) {
    const char = text[i];
    if (char === '{') { stack.push(text.slice(start, i).trim().replace(/\s+/g, ' ')); frames.push({}); start = i + 1; }
    else if (char === ';') { declaration(i); start = i + 1; }
    else if (char === '}') {
      declaration(i);
      const frame = frames.pop();
      if (frame?.needsSize && !frame.sized) fail(frame.needsSize.at, `${frame.needsSize.family} needs a font-size of at least ${frame.needsSize.min}px in the same rule (${frame.needsSize.use === 'display' ? 'display small and up' : 'headline small and up'})`);
      stack.pop(); start = i + 1;
    }
  }
  for (const info of voices.values()) {
    if (!info.min) continue;
    for (const { px, at } of info.sizes) {
      if (px === null) fail(at, `${info.family} voices need a font-size in px so the minimum can be checked`);
      if (px < info.min) fail(at, `${info.family} is only for ${info.use === 'display' ? 'display small (36px)' : 'headline small (24px)'} and up. This voice sets font-size ${px}px; the minimum is ${info.min}px. Use Google Sans Flex for smaller text`);
    }
  }
}

function lintHtml(file, source) {
  const text = source.replace(/<!--[\s\S]*?-->/g, blank);
  const fail = (index, message) => { throw new LintError(`${file}:${lineAt(text, index)}: ${message}`); };
  for (const tag of text.matchAll(/<([a-zA-Z][\w-]*)\b[^>]*>/g)) {
    const name = tag[1].toLowerCase();
    if (name === 'style') fail(tag.index, 'Inline <style> is not allowed. Put styles in a .css file');
    if (name === 'script' && !/\ssrc\s*=/i.test(tag[0])) fail(tag.index, 'Inline <script> is not allowed. Use <script src="...">');
    if (name === 'p-icon') {
      const icon = /\sname\s*=\s*(?:"([^"]*)"|'([^']*)')/i.exec(tag[0]);
      const symbol = icon ? icon[1] ?? icon[2] : undefined;
      if (symbol === undefined) fail(tag.index, '<p-icon> needs a quoted name="..." attribute that is a Material Symbols name from fonts.google.com/icons (name=home without quotes is not accepted)');
      else if (!SYMBOLS.has(symbol)) fail(tag.index, `<p-icon name="${symbol}"> is not a Material Symbols name. Pick one from fonts.google.com/icons, written in snake_case`);
    }
    const attribute = /\sstyle\s*=/i.exec(tag[0]);
    if (attribute) fail(tag.index + attribute.index, 'style="" attributes are not allowed. Put styles in a .css file');
  }
}

export function lintProposal(id, files) {
  for (const file of files) {
    const label = `proposals/${id}/${file.path}`;
    if (/\.css$/i.test(file.path)) lintCss(label, file.bytes.toString('utf8'));
    else if (/\.html?$/i.test(file.path)) lintHtml(label, file.bytes.toString('utf8'));
  }
}

const skipped = name => SKIP.has(name) || name.startsWith('.env') || /\.(pem|key|keystore|jks)$/.test(name);

// Regular files of a proposal folder with their bytes. Build caches and secrets are skipped; symlinks and special files are refused.
export function proposalFiles(root, prefix = '') {
  return readdirSync(join(root, prefix), { withFileTypes: true }).sort((a, b) => a.name.localeCompare(b.name)).flatMap(entry => {
    if (skipped(entry.name)) return [];
    const path = prefix ? `${prefix}/${entry.name}` : entry.name;
    if (entry.isSymbolicLink()) throw new LintError('Refusing symlink in proposal source');
    if (entry.isDirectory()) return proposalFiles(root, path);
    if (!entry.isFile()) throw new LintError('Proposal source must contain regular files');
    return [{ path, bytes: readFileSync(join(root, path)) }];
  });
}
