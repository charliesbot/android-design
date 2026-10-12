import { readdirSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import subsetFont from 'subset-font';

const here = new URL('./', import.meta.url);
const repo = new URL('../../../../', here);
const read = (url, encoding) => readFileSync(fileURLToPath(url), encoding);

const FONT = new URL('skills/android-design/assets/exploration-starter/core/designsystem/common/src/main/res/font/google_sans_flex.ttf', repo);
const FONT_LICENSE = new URL('skills/android-design/assets/exploration-starter/licenses/google-sans-flex-OFL.txt', repo);
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
      const font = await subsetFont(read(FONT), LATIN, { targetFormat: 'woff2' });
      emit('fonts/GoogleSansFlex.woff2', font);
      emit('fonts/OFL.txt', read(FONT_LICENSE));
      emit('icons.svg', await sprite());
      emit('icons-LICENSE.txt', read(new URL('LICENSE', SYMBOLS)));
      emit('runtime.css', read(new URL('runtime.css', here)));
      for (const dir of PAGES) {
        for (const name of readdirSync(fileURLToPath(new URL(`${dir}/`, here))).sort()) emit(`${dir}/${name}`, read(new URL(`${dir}/${name}`, here)));
      }
    },
  };
}
