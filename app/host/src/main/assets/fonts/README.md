# Bundled fonts

The Android host APK (`:host`) draws every text and icon with these variable fonts from `assets/fonts/`, so a render never fetches a font over the network. The license of each font is in `licenses/`.

| File | Family | Source |
| --- | --- | --- |
| `google_sans_flex.ttf` | Google Sans Flex (default for plain text) | https://github.com/googlefonts/googlesans-flex |
| `google_sans_code.ttf` | Google Sans Code (numbers and data) | https://github.com/googlefonts/googlesans-code |
| `roboto_flex.ttf` | Roboto Flex (Wear Material 3 default text) | https://github.com/googlefonts/roboto-flex |
| `fraunces.ttf` | Fraunces (brand face, display, headline, and large title) | https://github.com/undercasetype/Fraunces |
| `bricolage_grotesque.ttf` | Bricolage Grotesque (brand face, display, headline, and large title) | https://github.com/ateliertriay/bricolage |
| `big_shoulders_display.ttf` | Big Shoulders Display (brand face, display, headline, and large title) | https://github.com/xotypeco/big_shoulders |
| `doto.ttf` | Doto (brand face, display roles only) | https://github.com/oliverlalan/Doto |
| `material_symbols_rounded.ttf` | Material Symbols Rounded (icons, drawn as ligature text) | https://github.com/google/material-design-icons/tree/master/variablefont (`MaterialSymbolsRounded[FILL,GRAD,opsz,wght].ttf`), Apache License 2.0 |

The Material Symbols file is the official variable font, saved without the axis list in its name. Its license, `licenses/material-symbols-rounded-Apache-2.0.txt`, is the repository's `LICENSE` file at commit `10ecc14790612ca8fd164e3f4ffe9c16fc50d5b7`:
https://github.com/google/material-design-icons/blob/10ecc14790612ca8fd164e3f4ffe9c16fc50d5b7/LICENSE

`scripts/test_font_copies.py` verifies all eight bundled font files and their license files here.
