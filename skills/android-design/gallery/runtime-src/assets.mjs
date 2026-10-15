import { readdirSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import subsetFont from 'subset-font';

const here = new URL('./', import.meta.url);
const repo = new URL('../../../../', here);
const read = (url, encoding) => readFileSync(fileURLToPath(url), encoding);

const STARTER = new URL('skills/android-design/assets/exploration-starter/', repo);
// The bundled typefaces. The sources are the Compose starter's TTFs and licenses, so the web runtime and the Compose port ship the same fonts.
// Google Sans Flex keeps the unqualified OFL.txt name; every other family has <Name>-OFL.txt.
const FONTS = [
  { name: 'GoogleSansFlex', file: 'google_sans_flex', license: 'google-sans-flex', licenseOut: 'OFL.txt' },
  { name: 'Fraunces', file: 'fraunces', license: 'fraunces' },
  { name: 'BricolageGrotesque', file: 'bricolage_grotesque', license: 'bricolage-grotesque' },
  { name: 'BigShouldersDisplay', file: 'big_shoulders_display', license: 'big-shoulders-display' },
  { name: 'Doto', file: 'doto', license: 'doto' },
  { name: 'GoogleSansCode', file: 'google_sans_code', license: 'google-sans-code' },
];
const SYMBOLS = new URL('node_modules/@material-symbols/svg-400/', repo);

// Basic Latin, Latin-1 Supplement, and the punctuation and symbols that appear in UI copy.
const RANGES = [[0x20, 0x7e], [0xa0, 0xff], [0x2010, 0x2027], [0x2030, 0x203a], [0x2190, 0x2193], [0x2212, 0x2212], [0x20ac, 0x20ac], [0x2122, 0x2122]];
const LATIN = RANGES.flatMap(([from, to]) => Array.from({ length: to - from + 1 }, (_, i) => String.fromCodePoint(from + i))).join('');

// Folders copied unchanged to public/runtime/<name>/.
const PAGES = ['sample', 'verify', 'checks-fixture', 'review'];

async function sprite() {
  const names = JSON.parse(read(new URL('icons.json', here), 'utf8'));
  const symbols = names.flatMap(name => [name, `${name}-fill`].map(id => {
    const svg = read(new URL(`rounded/${id}.svg`, SYMBOLS), 'utf8');
    const path = /<path d="([^"]+)"\/>/.exec(svg);
    if (!path) throw new Error(`Unexpected Material Symbols file for ${id}`);
    return `<symbol id="${id}" viewBox="0 -960 960 960"><path d="${path[1]}"/></symbol>`;
  }));
  return `<svg xmlns="http://www.w3.org/2000/svg">\n${symbols.join('\n')}\n</svg>\n`;
}

// Emits everything in the runtime that Rollup does not produce from m3e.js.
export function runtimeAssets() {
  return {
    name: 'proposal-runtime-assets',
    async generateBundle() {
      const emit = (fileName, source) => this.emitFile({ type: 'asset', fileName, source });
      for (const { name, file, license, licenseOut } of FONTS) {
        const ttf = read(new URL(`core/designsystem/common/src/main/res/font/${file}.ttf`, STARTER));
        emit(`fonts/${name}.woff2`, await subsetFont(ttf, LATIN, { targetFormat: 'woff2' }));
        emit(`fonts/${licenseOut ?? `${name}-OFL.txt`}`, read(new URL(`licenses/${license}-OFL.txt`, STARTER)));
      }
      emit('icons.svg', await sprite());
      emit('icons-LICENSE.txt', read(new URL('LICENSE', SYMBOLS)));
      emit('runtime.css', read(new URL('runtime.css', here)));
      for (const dir of PAGES) {
        for (const name of readdirSync(fileURLToPath(new URL(`${dir}/`, here))).sort()) emit(`${dir}/${name}`, read(new URL(`${dir}/${name}`, here)));
      }
    },
  };
}
