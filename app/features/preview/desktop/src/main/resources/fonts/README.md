# Bundled fonts

The previewer draws every text and icon with these files, loaded from the classpath, so a render
never fetches a font. All are variable fonts. The license of each is in `licenses/`.

| File | Family | Source |
| --- | --- | --- |
| `google_sans_flex.ttf` | Google Sans Flex (default for plain text) | https://github.com/googlefonts/googlesans-flex |
| `google_sans_code.ttf` | Google Sans Code (numbers and data) | https://github.com/googlefonts/googlesans-code |
| `fraunces.ttf` | Fraunces (brand face, display and headline) | https://github.com/undercasetype/Fraunces |
| `bricolage_grotesque.ttf` | Bricolage Grotesque (brand face, display and headline) | https://github.com/ateliertriay/bricolage |
| `big_shoulders_display.ttf` | Big Shoulders Display (brand face, display and headline) | https://github.com/xotypeco/big_shoulders |
| `doto.ttf` | Doto (brand face, display roles only) | https://github.com/oliverlalan/Doto |
| `material_symbols_rounded.ttf` | Material Symbols Rounded (icons, drawn as ligature text) | https://github.com/google/material-design-icons/tree/master/variablefont (`MaterialSymbolsRounded[FILL,GRAD,opsz,wght].ttf`), Apache License 2.0 |

The first six moved here from `skills/android-design-explore/assets/fonts/`. The Material Symbols
file is the official variable font, saved without the axis list in its name. Its license,
`licenses/material-symbols-rounded-Apache-2.0.txt`, is the repository's `LICENSE` file at commit
`10ecc14790612ca8fd164e3f4ffe9c16fc50d5b7`:
https://github.com/google/material-design-icons/blob/10ecc14790612ca8fd164e3f4ffe9c16fc50d5b7/LICENSE

The six text faces are identical to the copies in `evals/compose-starter/`, and
`scripts/test_font_copies.py` fails when they drift, so update both together.
