# Isolated design exploration

Use this workflow for ideas, redesign proposals, or alternatives before implementation. The result is real Compose rendered from a disposable project, not an image-generated mockup or a verified integration into the user's app.

## Understand the brief

For an existing app, read the relevant screen, nearby screens, theme, assets, and representative content. Treat the app as read-only: exploration needs no branch, build edits, or generated files there. Read existing screenshots when available. For a new idea, establish its primary task and sample content from the conversation.

Identify the target surface. Ask when unclear. The starter contains phone/tablet and experimental Wear shells; their presence does not mean every exploration should target both. Wear can be explored on explicit request, but its design quality is not yet validated and its eval cases remain parked.

Answer section 0 of the skill for the proposed directions. Unless the user specifies a count, prepare three distinct compositions with the same content and primary task, not three recolorings.

## Create the workspace

Resolve this installed skill's directory, then run:

```bash
python3 <skill-dir>/scripts/explore.py create
```

Use the returned absolute directory for every write and build. It is a unique `/tmp/android-design-explore-*` directory (`/private/tmp` on macOS). Tell the user its path so a later turn can resume it. Keep the installed template unchanged.

The copy includes Gradle, the five `android-dev` core modules, phone and Wear shells, navigation, Koin, and fonts. `android-dev` and `android create` are not runtime dependencies. Rendering is delegated to the installed `compose-preview` skill; keep its commands and troubleshooting there rather than duplicating them here.

## Compose and compare

Read the starter README for code entry points. Work in the requested platform shell with sample state. Keep alternatives together in that shell; creating one feature module per alternative would confuse proposals with product capabilities. If a proposal becomes a multi-screen journey, follow `android-dev` for feature placement.

Reuse only the necessary non-sensitive assets or representative data from the app. Do not copy credentials, local configuration, databases, or real personal data. Keep sample content fixed across alternatives. Use the actual fonts and platform theme, and preserve existing product constraints unless the brief calls for changing them.

Give each alternative a named, sample-state `@Preview` in the requested shell's main source set, wrapped in its platform theme. Use matched viewport, theme, and state settings across alternatives; add smaller-window, large-font, and meaningful alternate-state previews for the screen check. The starter's `src/screenshotTest` previews and `render.py` belong to its maintenance harness, not this exploration workflow.

Follow `compose-preview` to render those previews from the temporary workspace, targeting the requested shell. Keep all preview additions and tool-generated project files in this copy, not in the target app or installed starter.

Open the resulting PNGs and perform the skill's screen check. Fix the largest mismatches and render again. Present each alternative's image with its defining decision and trade-off. Label Wear proposals experimental. If rendering fails, report "visual quality unverified" with the error; do not present stale screenshots as current.

## Refine, implement, or discard

Follow-up design revisions use the same temporary copy. Choosing a preferred direction is not by itself permission to modify the real app: ask whether to keep refining or implement if the intent is ambiguous. On an implementation request, follow that project's approval and branch rules, adapt the selected code to its architecture, and verify it there.

Temporary storage can be removed by the OS and is not durable. Do not promise automatic cleanup at conversation end. Keep the workspace while revisions are active. After the user confirms discarding it, or confirms cleanup after implementation, run:

```bash
python3 <skill-dir>/scripts/explore.py cleanup <returned-directory> --yes
```

Cleanup removes source and screenshots. The helper accepts only a marked exploration directory directly under `/tmp`, refuses symlinks and mismatched markers, and leaves the installed starter and app untouched. If the user wants to keep a proposal, export it to their requested location before cleanup.
