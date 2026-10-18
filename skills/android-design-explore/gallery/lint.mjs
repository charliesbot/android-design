import { readFileSync, readdirSync } from 'node:fs';
import { join } from 'node:path';

// Build caches, secrets, and OS files never belong to a proposal.
const SKIP = new Set(['build', '.gradle', '.kotlin', '.git', 'renders', 'local.properties', '__pycache__', 'screenshotTestDebug', 'node_modules', '.DS_Store']);

export class LintError extends Error {}


// Strict styling lint. Each rule keeps a proposal a lookup into Compose tokens (documented in runtime-src/README.md, Authoring rules).
const NAMED_COLORS = ('aliceblue antiquewhite aqua aquamarine azure beige bisque black blanchedalmond blue blueviolet brown burlywood cadetblue chartreuse chocolate coral cornflowerblue cornsilk crimson cyan darkblue darkcyan darkgoldenrod darkgray darkgreen darkgrey darkkhaki darkmagenta darkolivegreen darkorange darkorchid darkred darksalmon darkseagreen darkslateblue darkslategray darkslategrey darkturquoise darkviolet deeppink deepskyblue dimgray dimgrey dodgerblue firebrick floralwhite forestgreen fuchsia gainsboro ghostwhite gold goldenrod gray green greenyellow grey honeydew hotpink indianred indigo ivory khaki lavender lavenderblush lawngreen lemonchiffon lightblue lightcoral lightcyan lightgoldenrodyellow lightgray lightgreen lightgrey lightpink lightsalmon lightseagreen lightskyblue lightslategray lightslategrey lightsteelblue lightyellow lime limegreen linen magenta maroon mediumaquamarine mediumblue mediumorchid mediumpurple mediumseagreen mediumslateblue mediumspringgreen mediumturquoise mediumvioletred midnightblue mintcream mistyrose moccasin navajowhite navy oldlace olive olivedrab orange orangered orchid palegoldenrod palegreen paleturquoise palevioletred papayawhip peachpuff peru pink plum powderblue purple rebeccapurple red rosybrown royalblue saddlebrown salmon sandybrown seagreen seashell sienna silver skyblue slateblue slategray slategrey snow springgreen steelblue tan teal thistle tomato turquoise violet wheat white whitesmoke yellow yellowgreen').split(' ');
const COLOR_WORD = new RegExp(`(?<![\\w-])(?:${NAMED_COLORS.join('|')})(?![\\w-])`, 'i');
const COLOR_FUNCTION = /(?<![\w-])(?:rgba?|hsla?|hwb|lab|lch|oklab|oklch|color)\(/i;
const HEX_COLOR = /#[0-9a-f]{3,8}(?![\w-])/i;
// The bundled typefaces (runtime-src/fonts.json, the same list the runtime fonts check reads). A brand face is for headline small (24px) and up,
// Doto for display small (36px) and up, Google Sans Code for numbers and data at any size, Google Sans Flex everywhere.
const FONTS = JSON.parse(readFileSync(new URL('./runtime-src/fonts.json', import.meta.url), 'utf8'));
const BUNDLED = [FONTS.plain, ...FONTS.faces.map(face => face.family)];
// Every Material Symbols name (public/runtime/symbols.json, emitted by the runtime build from the pinned `material-symbols` package).
// <p-icon name> draws the name as a font ligature, so an unknown name would render as plain text; the lint rejects it instead.
const SYMBOLS = new Set(JSON.parse(readFileSync(new URL('./public/runtime/symbols.json', import.meta.url), 'utf8')));
// The Compose icon sizes a proposal may use (runtime-src/icon-sizes.json). <p-icon size="N"> sets the glyph size; font-size is not allowed outside a voice.
const ICON_SIZES = JSON.parse(readFileSync(new URL('./runtime-src/icon-sizes.json', import.meta.url), 'utf8')).sizes;
// @m3e/web 2.9.2 elements whose every slotted icon is sized by the component with `!important` (`::slotted(...) { font-size: ... !important }` or an
// `--m3e-*-icon-size` token), found by grepping dist/*.js, so a p-icon size inside them has no effect. Components that force only a named slot
// (the close icon of a dialog or snackbar, an expansion toggle, paginator or slide-group arrows, a search clear icon) are not linted because none
// is verified for proposals; m3e-list-item, m3e-theme, m3e-card and m3e-app-bar do not force it.
const FORCES_ICON_SIZE = new Set(['m3e-button', 'm3e-button-segment', 'm3e-icon-button', 'm3e-fab', 'm3e-fab-menu-item', 'm3e-chip', 'm3e-assist-chip', 'm3e-filter-chip',
  'm3e-input-chip', 'm3e-suggestion-chip', 'm3e-nav-item', 'm3e-tab', 'm3e-step', 'm3e-menu-item', 'm3e-menu-item-checkbox', 'm3e-menu-item-radio',
  'm3e-breadcrumb-item', 'm3e-breadcrumb-item-button']);
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
  const imported = /@import\b/.exec(text);
  if (imported) fail(imported.index, '@import is not allowed: view.html inlines each file, so a CSS file cannot load another. Put the rules in one .css file or link each file from index.html');
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

// view.html inlines each script into one document, so a script must be self-contained: no module imports, no URLs built from its own location.
function lintJs(file, source) {
  const text = source.replace(/\/\*[\s\S]*?\*\/|\/\/[^\n]*/g, blank);
  const fail = (index, message) => { throw new LintError(`${file}:${lineAt(text, index)}: ${message}`); };
  const dynamic = /(?<![\w$.])import\s*\(/.exec(text);
  if (dynamic) fail(dynamic.index, 'Dynamic import() is not allowed: a proposal script must be self-contained (view.html inlines it). Put the code in this file or load it with another <script src> in index.html');
  const meta = /import\.meta\b/.exec(text);
  if (meta) fail(meta.index, 'import.meta is not allowed: the script is inlined, so it has no file location. Use a data: URI or put assets in the HTML or CSS');
  for (const match of text.matchAll(/(?<![\w$.])(?:import|export)\b[^;'"`]*?(?:\bfrom\s*)?(["'])(\.{0,2}\/[^"']*|\.[^"']*)\1/g)) {
    fail(match.index, `Relative import "${match[2]}" is not allowed: a proposal script must be self-contained (view.html inlines it). Put the code in this file or load it with another <script src> in index.html`);
  }
}

function lintHtml(file, source) {
  const text = source.replace(/<!--[\s\S]*?-->/g, blank);
  const fail = (index, message) => { throw new LintError(`${file}:${lineAt(text, index)}: ${message}`); };
  const open = []; // open element names, to find the component a p-icon sits in
  const VOID = new Set(['area', 'base', 'br', 'col', 'embed', 'hr', 'img', 'input', 'link', 'meta', 'source', 'track', 'wbr']);
  for (const tag of text.matchAll(/<(\/?)([a-zA-Z][\w-]*)\b[^>]*>/g)) {
    const name = tag[2].toLowerCase();
    if (tag[1]) { const at = open.lastIndexOf(name); if (at >= 0) open.length = at; continue; }
    const pushed = !VOID.has(name) && !tag[0].endsWith('/>');
    if (pushed) open.push(name);
    if (name === 'style') fail(tag.index, 'Inline <style> is not allowed. Put styles in a .css file');
    if (name === 'script' && !/\ssrc\s*=/i.test(tag[0])) fail(tag.index, 'Inline <script> is not allowed. Use <script src="...">');
    if (name === 'p-icon') {
      const icon = /\sname\s*=\s*(?:"([^"]*)"|'([^']*)')/i.exec(tag[0]);
      const symbol = icon ? icon[1] ?? icon[2] : undefined;
      if (symbol === undefined) fail(tag.index, '<p-icon> needs a quoted name="..." attribute that is a Material Symbols name from fonts.google.com/icons (name=home without quotes is not accepted)');
      else if (!SYMBOLS.has(symbol)) fail(tag.index, `<p-icon name="${symbol}"> is not a Material Symbols name. Pick one from fonts.google.com/icons, written in snake_case`);
      if (/\ssize\b/i.test(tag[0])) {
        const size = /\ssize\s*=\s*(?:"([^"]*)"|'([^']*)')/i.exec(tag[0]);
        if (!size) fail(tag.index, '<p-icon size> needs a quoted value such as size="24"');
        const value = size[1] ?? size[2];
        const host = (pushed ? open.slice(0, -1) : open).slice().reverse().find(item => FORCES_ICON_SIZE.has(item) || item === 'm3e-list-item');
        if (host && host !== 'm3e-list-item') fail(tag.index, `<p-icon size> is ignored inside <${host}>: the component sets this icon's size; port with its default icon size (an icon button or FAB follows its container size). Remove size`);
        if (!ICON_SIZES.includes(Number(value)) || !/^\d+$/.test(value)) fail(tag.index, `<p-icon size="${value}"> is not a Compose icon size. Use one of ${ICON_SIZES.join(', ')} (the size of the Compose icon it ports to)`);
      }
    }
    const attribute = /\sstyle\s*=/i.exec(tag[0]);
    if (attribute) fail(tag.index + attribute.index, 'style="" attributes are not allowed. Put styles in a .css file');
  }
}

// A proposal names its open states (a sheet, a dialog, a menu, or any other hidden state worth rendering) in the head of
// index.html: <meta name="p-states" content="queue lyrics">. Its own script defines window.proposalState(name), which opens that
// state; `render` calls it and checks and shoots each state.
const STATE_NAME = /^[a-z0-9-]+$/;
function stateMetas(html) {
  const text = html.replace(/<!--[\s\S]*?-->/g, blank);
  const metas = [...text.matchAll(/<meta\b[^>]*>/gi)].filter(tag => /\sname\s*=\s*["']p-states["']/i.test(tag[0])).map(tag => ({
    index: tag.index,
    names: (/\scontent\s*=\s*(?:"([^"]*)"|'([^']*)')/i.exec(tag[0])?.slice(1).find(value => value !== undefined) ?? '').split(/\s+/).filter(Boolean),
  }));
  return { text, metas };
}

// The state names of an index.html, [] when it declares none. The lint has already vetted the names of a proposal that renders.
export function proposalStates(html) {
  return stateMetas(html).metas[0]?.names ?? [];
}

function lintStates(label, html, files) {
  const { text, metas } = stateMetas(html);
  if (!metas.length) return;
  const fail = (meta, message) => { throw new LintError(`${label}:${lineAt(text, meta.index)}: ${message}`); };
  if (metas.length > 1) fail(metas[1], 'p-states is declared twice: list every state name in one <meta name="p-states">');
  const [meta] = metas;
  if (!meta.names.length) fail(meta, 'p-states needs at least one name, such as content="queue lyrics"');
  const seen = new Set();
  for (const name of meta.names) {
    if (!STATE_NAME.test(name)) fail(meta, `p-states name "${name}" is not valid: use lowercase letters, digits, and hyphens`);
    if (seen.has(name)) fail(meta, `p-states name "${name}" is declared twice`);
    seen.add(name);
  }
  if (!files.some(file => /\.m?js$/i.test(file.path))) {
    fail(meta, 'p-states declares states but the proposal has no script file: add a .js file that defines window.proposalState = name => { ... } to open each state');
  }
}

export function lintProposal(id, files) {
  for (const file of files) {
    const label = `proposals/${id}/${file.path}`;
    if (/\.css$/i.test(file.path)) lintCss(label, file.bytes.toString('utf8'));
    else if (/\.html?$/i.test(file.path)) lintHtml(label, file.bytes.toString('utf8'));
    else if (/\.m?js$/i.test(file.path)) lintJs(label, file.bytes.toString('utf8'));
  }
  const index = files.find(file => file.path === 'index.html');
  if (index) lintStates(`proposals/${id}/index.html`, index.bytes.toString('utf8'), files);
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
