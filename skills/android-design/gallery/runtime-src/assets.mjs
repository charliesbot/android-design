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
// Material Symbols Rounded: the variable font (FILL, GRAD, opsz, wght) from the pinned `material-symbols` npm package, unsubsetted.
// Icon names are ligatures, so the package's name list becomes runtime/symbols.json for the publish lint.
const SYMBOLS = new URL('node_modules/material-symbols/', repo);

// Basic Latin, Latin-1 Supplement, and the punctuation and symbols that appear in UI copy.
const RANGES = [[0x20, 0x7e], [0xa0, 0xff], [0x2010, 0x2027], [0x2030, 0x203a], [0x2190, 0x2193], [0x2212, 0x2212], [0x20ac, 0x20ac], [0x2122, 0x2122]];
const LATIN = RANGES.flatMap(([from, to]) => Array.from({ length: to - from + 1 }, (_, i) => String.fromCodePoint(from + i))).join('');

// Folders copied unchanged to public/runtime/<name>/.
const PAGES = ['sample', 'verify', 'checks-fixture', 'review'];

function symbolNames() {
  const names = [...read(new URL('index.d.ts', SYMBOLS), 'utf8').matchAll(/^\s*"([a-z0-9_]+)",?$/gm)].map(match => match[1]);
  if (names.length < 3000) throw new Error(`Expected thousands of Material Symbols names, found ${names.length}`);
  return names;
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
      const { version } = JSON.parse(read(new URL('package.json', SYMBOLS), 'utf8'));
      emit('fonts/MaterialSymbolsRounded.woff2', read(new URL('material-symbols-rounded.woff2', SYMBOLS)));
      emit('fonts/MaterialSymbolsRounded-LICENSE.txt', read(new URL('LICENSE', SYMBOLS)));
      emit('fonts/MaterialSymbolsRounded-SOURCE.txt', `Material Symbols Rounded variable font (axes FILL, GRAD, opsz, wght), unmodified.\nSource: npm package material-symbols@${version} (github.com/marella/material-symbols, built from github.com/google/material-design-icons), file material-symbols-rounded.woff2.\nLicense: Apache 2.0, see MaterialSymbolsRounded-LICENSE.txt.\n`);
      emit('symbols.json', `${JSON.stringify(symbolNames())}\n`);
      emit('runtime.css', read(new URL('runtime.css', here)));
      for (const dir of PAGES) {
        for (const name of readdirSync(fileURLToPath(new URL(`${dir}/`, here))).sort()) emit(`${dir}/${name}`, read(new URL(`${dir}/${name}`, here)));
      }
    },
  };
}
