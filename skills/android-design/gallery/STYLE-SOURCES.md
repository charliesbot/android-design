# Gallery style sources

Measured from https://developer.android.com/ on 2026-10-04, using computed
styles and its Android `app.css`, not visual approximation.

| Component | Observed rule |
| --- | --- |
| Section heading | Google Sans, 600, 40px/48px, -0.5px tracking |
| Feature-card heading | Google Sans, 600, 32px/40px, -0.25px tracking |
| Body | Google Sans Text, 400, 16px/24px |
| Outlined button | Google Sans, 500, 16px, 44px line height, 2px #202124 border, 96px radius, 0 24px padding, 0.5px tracking |
| Outlined-button hover | #202124 background, white text |
| Feature card | White, 1px #dadce0 border, 24px radius, 32px content inset |
| Select radius | 2px, from --devsite-select-border-radius |

Source stylesheet:
https://www.gstatic.com/devrel-devsite/prod/vfdb441d2e08dbd9d3e48d8cd72b242388a87bcf7626bf5fb9df50c2bdd4a70fd/android/css/app.css

## Local adaptations

The gallery uses Google Sans for body text too. The reference uses the separate
Google Sans Text family; this is not an exact copy of its body typography.
Native selects and textarea keep accessible sizing. Selection outlines, screenshot
layout, and focus rings belong to the review tool, not the reference homepage.
Screenshot containers stay transparent in normal, hover, and selected states.

Google Sans is bundled locally, so the gallery makes no third-party font requests.
The Latin WOFF2 is the shared 400/500/600 resource returned by Google Fonts:
https://fonts.googleapis.com/css2?family=Google+Sans:wght@400;500;600&display=swap

Font source:
https://fonts.gstatic.com/s/googlesans/v71/4UasrENHsxJlGDuGo1OIlJfC6l_24rlCK1Yo_Iqcsih3SAyH6cAwhX9RPjIUvbQoi-E.woff2

License: `src/static/fonts/OFL.txt`, from
https://github.com/google/fonts/blob/main/ofl/googlesans/OFL.txt.
Characters outside the Latin subset use the system fallback.

## Android Bench dropdowns

Inspected https://developer.android.com/bench#long-horizon-task-results:
trigger #f8f9fa, 24px radius, 16px/24px weight 500, 24px left and 48px
right padding. Menu: white, 24px radius, 8px vertical padding; options
14px/20px with 8px 16px padding. Shadow: 0 1px 2px rgba(60,64,67,.3),
0 2px 6px 2px rgba(60,64,67,.15).

The reference uses a JavaScript listbox. We retain native selects and progressively
style their picker with `appearance: base-select`. Unsupported browsers retain
native menus. The gallery trigger is 48px rather than the reference's 40px for
a larger hit target. Google Sans substitutes for Google Sans Text, as above.

## Proposal runtime fonts and icons

The proposal runtime (`runtime-src/`, built to `public/runtime/`) ships Google Sans Flex
as a Latin-subset WOFF2 that keeps every variable axis. It is subset from the exploration
starter's `google_sans_flex.ttf`; license: `public/runtime/fonts/OFL.txt`, from
`assets/exploration-starter/licenses/google-sans-flex-OFL.txt`.

Icons are Material Symbols Rounded, weight 400, from the `@material-symbols/svg-400` npm
package, packed into `public/runtime/icons.svg` for the names in `runtime-src/icons.json`.
License: Apache 2.0, `public/runtime/icons-LICENSE.txt`.
