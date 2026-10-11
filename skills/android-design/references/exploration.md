# Visual design exploration

For multiple proposals or an exploration request, build HTML proposals on the bundled Material 3 Expressive runtime, review them in your harness's browser, and publish them to a local comparison gallery. The gallery shows each proposal live. Approval finishes exploration without implementing anything.

## Understand the brief

Read the requesting app's relevant screens, theme, assets, content, and dependency versions. Match the prompt's scope: a feature exploration preserves the surrounding visual language unless structural changes are needed; a redesign can go further. Answer section 0 of the skill for the directions. Unless the user specifies otherwise, start with three distinct compositions of the same task and content, not three recolorings.

Give each direction its own seed: the app's brand color or a content color from the subject (a dish, an album, a trip), never the runtime's default palette. Make at least one direction a hero moment that layers three or more tactics (type voice, shape, color role, container form; see section 3 of the skill) and name them in its rationale.

Keep app code and dependencies unchanged. Proposals must transfer to the app's existing libraries. Use representative data and non-sensitive assets, never credentials, databases, or personal data.

## Create or resume

The gallery and the workspace helper require Node.js 22+. If it is missing, report it rather than installing it silently.

Resolve the installed skill directory and run:

```bash
node <skill-dir>/gallery/cli.mjs create --project <requesting-repo> --title "Today screen redesign"
```

The helper copies the HTML proposal starter to `design-explorations/<unique-id>/project/` in that Git repository. It records a local Git exclusion without editing the tracked `.gitignore`. Tell the user the returned absolute exploration path. Keep the installed template and production app untouched. A new app idea still needs a user-selected repository; do not silently write to the skill repository.

Resume the same exploration directory for revisions. Working drafts live in `project/proposals/<id>/`; published `rounds/` are preserved snapshots and must not be edited. Read saved selections and notes with:

```bash
node <skill-dir>/gallery/cli.mjs status <exploration>
```

The gallery includes phone, foldable, tablet, and desktop frames. Wear stays outside this workflow; use the Android starter's experimental Wear shell (`create --starter android`) only when explicitly requested, without claiming validated Wear quality.

## Author proposals

A proposal is `proposals/<id>/index.html` and `style.css` on the runtime. Copy `proposals/a` for each extra direction and change the design, not the rules. Details are in `project/README.md` and the runtime contract in `gallery/runtime-src/README.md`. Publishing rejects violations.

- HTML on the runtime only, wrapped in `<m3e-theme color="#RRGGBB" variant="tonal-spot" scheme="light">`. Use only HTML that has a Compose counterpart.
- Text uses the `.t-*` role classes, or one `.voice-<name>` class declared once. No ad hoc sizes or weights.
- Color uses `--md-sys-color-*` roles. A non-role color (a dish or category color) goes in one `/* static colors */` block of `--static-*` properties.
- Icons come from the runtime list through `<p-icon>`.
- Corners come from the corner scale, spacing from the 8dp scale.
- Components come only from `references/compose-mapping.md` rows marked `verified`. Anything else is built from plain HTML layout with role tokens when it has a plain Compose layout counterpart (`Box`, `Row`, `Column`, `Text`, `Surface`); otherwise leave it out.
- A large voice sets `line-height` to at least about 1.2 times its `font-size`.
- No inline styles or scripts. One `index.html` and one `style.css` per proposal.
- Mark the primary action `data-primary`.
- Compact below 840px, expanded from 840px up.

## Review before publishing

Do not judge from code alone. Browser tools cannot emulate exact viewports, so review through the gallery's review page, which frames the draft at all four devices (411x923, 704x933, 1280x800, 1440x900). Start the gallery server and open it:

```bash
node <skill-dir>/gallery/cli.mjs serve <exploration>
```

1. Open `/runtime/review/?src=/draft/<id>/` in your harness's browser.
2. Call `window.reviewChecks()` on that page and fix every failure. Treat `unresolved` entries as unchecked, not passed. A result with `complete: false` means some frame was not checked: treat it as not verified and report it.
3. Take one screenshot per device from that page (or open a frame full size) and judge hierarchy, character, and color balance, using the skill's screen check.

If the harness has no browser, report visual quality as unverified and do not claim the screen check passed. Publish only with the user's knowledge of that.

## Publish and review

Write a round draft outside `project/`. The proposal folder is implied by the id:

```json
{
  "title": "First directions",
  "proposals": [
    {
      "id": "a",
      "name": "Content first",
      "rationale": "Prioritizes today's content over navigation."
    }
  ],
  "answers": [{ "roundId": "001", "at": "<request timestamp>" }]
}
```

The round's `proposals` array lists only the proposals in that round. A refinement round reuses the working folder id of the proposal being refined (for example `b`). Optional `answers` names the saved requests this round responds to, each as `{roundId, at}` exactly as returned in `wait`'s `request` (and `superseded`) or in `status`'s `pending`; requests it does not name stay pending. Then:

```bash
node <skill-dir>/gallery/cli.mjs publish <exploration> <draft.json>
node <skill-dir>/gallery/cli.mjs serve <exploration>
```

`publish` lints each folder and snapshots it with file hashes. On a rejected raw color, type declaration, style attribute, or inline script, it names the file and line: fix and retry. A draft with `images` is rejected. Completed rounds are never overwritten. Give the user the printed localhost URL.

The gallery shows each proposal live in one device frame, with an icon switcher that resizes the frames in place across the four devices. Seed color, palette variant, and light or dark controls are preview-only: they re-theme the frames and save nothing, so a change the user wants to keep goes in the notes. It also has one selected proposal and a shared notes box per round, and previous rounds. New rounds appear automatically when the user is not editing notes or inspecting a dialog. Browsing an earlier round stays on that round. It does not launch an agent or execute builds.

Selecting a proposal is not finalization. The shared **Refine [name]** action records a request in `feedback.rounds[roundId].requests` with its proposal key, notes, and timestamp. **None of these** clears the proposal choice and requires notes before requesting new directions. The website tells the user to return to chat; it does not invoke an agent. Read the latest submitted request, not merely the currently edited selection, apply clear instructions, and ask a focused question only when a meaningful decision remains. Show alternatives when the decision is visual. Produce one refinement by default. Review it in the browser and publish it as a new round with `answers`. Never rewrite an earlier round to conceal changes. The user can explicitly ask to explore several alternatives again.

Wait for saved feedback before relying on it. Failed saves remain pending in the tab with a retry control. A stale-tab conflict requires reloading saved state; preserve or copy unsaved notes before discarding them. Approval waits for pending saves, verifies the published artifacts, and makes the exploration read-only.

## Watch session

A session starts only when the user asks for it, such as "keep refining while I review". A plain "adjust option B" does not start one. Run one session per exploration.

After publishing and serving, loop:

```bash
node <skill-dir>/gallery/cli.mjs wait <exploration>
```

It prints one JSON line and exits 0. Branch on `status`:

- `request`: read `request` (and mention `superseded` if present), apply the refine steps above, build, review in the browser, and publish a draft whose `answers` lists the handled request plus any superseded ones it consolidated. Then wait again.
- `approved`: report the approved revision and stop.
- `timeout`: rerun `wait`. Stop when 30 minutes pass with no request, say so in chat, and say how to resume ("continue refining").

When a meaningful design decision remains, ask in chat and stop waiting; the gallery's status line falls back to "return to chat" when its budget expires. A failed review or publish ends the session with a chat report. Never publish stale output; the request stays pending for a later session or a plain chat request.

The default `--timeout` is 25 seconds, so every supported harness returns it as a normal foreground result. In Claude Code you may pass `--timeout 540` with a 10-minute tool timeout to cut the number of calls.

Nothing runs after your turn ends. To resume, the user asks in chat; read `status`, whose `pending` field lists unanswered requests.

## Approve, hand off, or discard

The user finalizes the single selected proposal with the page-level **Finish with [name]** action and its confirmation. A combination of several ideas must first be built as one consolidated proposal. Read the exact approved round and proposal via `status`; use that round's published files, not the potentially newer working `project/`.

The skill's job ends here. Do not implement. Report the approved round and proposal, the path of its published files, and that `references/compose-mapping.md` is the porting table. Approval does not authorize implementation or library version changes.

A later explicit implementation request follows the requesting project's planning, branch, verification, and review rules. Port the approved proposal into the app with the mapping table, keeping the app's library versions. Then render the Compose result and compare it with screenshots of the approved proposal at the same viewports. Report unmapped or unverified elements instead of inventing a Compose equivalent. If the design cannot be preserved with the app's dependencies, ask rather than silently change the design or versions.

Keep published files, history, and notes until the user explicitly requests deletion. Stop the gallery server first, then run from the requesting repository:

```bash
node <skill-dir>/gallery/cli.mjs cleanup <exploration> --project <requesting-repo> --yes
```

Cleanup removes only a directly contained, identity-marked exploration and refuses symlinks or mismatched markers. It deletes every round and note. It never deletes the installed starter or the requesting app.
