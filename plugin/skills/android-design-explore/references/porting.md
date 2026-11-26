# Porting an approved exploration into the app

Read this only after the user approves a direction in `design/<slug>.explore.kt` (or `-wear.explore.kt`) and wants it wired into the project. Keep any host repository rules for planning, branches, tests, and review, and keep library versions unchanged unless asked.

## 1. Map the app target and theme

- Inspect the target project's module structure (`:app`, `:feature:*`, `:core:designsystem`), navigation graph, ViewModel/state holders, and Compose Material 3 version.
- Port shared tokens (`DESIGN.md` + `designBoard` `seed`, `variant`, and `brandFont`) into the app's theme (`MaterialExpressiveTheme` + `MotionScheme.expressive()` on phone/tablet/desktop; Wear Material 3 `MaterialTheme` + `AppTheme` on Wear OS). Keep Google Sans Flex for plain text and at most one bundled brand face for Display, Headline, and Title Large.

## 2. Move composables and replace kit helpers

Move the approved `@Composable` screen functions out of `design/<slug>.explore.kt` (removing `designBoard` / `wearBoard` / `direction` scaffolding) into the app's UI files, wiring real ViewModel state and callbacks:

| Exploration helper | Production Android replacement |
| --- | --- |
| `SymbolIcon("...")` | Vector drawable or Material Symbol resource drawn with `Icon(painterResource(...))` |
| `designPainter("assets/...")` | `painterResource(R.drawable.<name>)` or the app's image loader |
| `bundledFontFamily(...)` | Bundled `Font(R.font.<file>, weight, variationSettings)` resource in the theme module |
| `listDetail(...)` / `supportingPane(...)` | Real Navigation 3 `ListDetailSceneStrategy` / `SupportingPaneSceneStrategy` entries inside `NavDisplay` |
| `Modifier.testTag("ad:primary")` | Keep only if app UI tests use it; otherwise remove |

## 3. Verify the wired screens

After wiring into the project:

1. Render live `@Preview` functions of the ported composables across phone, foldable, tablet, and desktop (or both Wear watch sizes) through the `compose-preview` skill and compare structure and emphasis against the approved exploration renders.
2. Re-run `android-design`'s Screen check questions and code checks against the wired composables.
