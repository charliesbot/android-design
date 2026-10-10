# Visual design exploration

For multiple proposals or an exploration request, use real Android Compose code and a local comparison website. The gallery displays the same PNGs you inspect, not an HTML recreation or generated UI image. Approval finishes exploration without implementing anything.

## Understand the brief

Read the requesting app's relevant screens, theme, assets, content, and dependency versions. Match the prompt's scope: a feature exploration preserves the surrounding visual language unless structural changes are needed; a redesign can go further. Answer section 0 of the skill for the directions. Unless the user specifies otherwise, start with three distinct compositions of the same task and content, not three recolorings.

Keep app code and dependencies unchanged. Proposals must transfer to the app's existing libraries: do not upgrade or downgrade the app to match the starter. Surface known incompatibilities before approval. Use representative data and non-sensitive assets, never credentials, databases, or personal data.

## Create or resume

The gallery and the workspace helper both require Node.js 22+. Rendering belongs to the installed `compose-preview` skill and CLI. If a required tool is missing, report it rather than installing it silently or substituting a renderer.

Resolve the installed skill directory and run:

```bash
node <skill-dir>/gallery/cli.mjs create --project <requesting-repo> --title "Today screen redesign"
```

The helper copies the starter to `design-explorations/<unique-id>/project/` in that Git repository. It records a local Git exclusion without editing the tracked `.gitignore`. Tell the user the returned absolute exploration path. Keep the installed template and production app untouched. A new app idea still needs a user-selected repository; do not silently write to the skill repository.

Resume the same exploration directory for revisions. `project/` is the working Compose sandbox; published `rounds/` are preserved source and image snapshots and must not be edited. Read saved selections and notes with:

```bash
node <skill-dir>/gallery/cli.mjs status <exploration>
```

The gallery includes phone, tablet, desktop, and wide-foldable layouts. Wear remains experimental and outside this gallery workflow; use the separate Wear shell and preview skill only when explicitly requested, without claiming validated Wear quality.

## Compose, render, inspect

The starter has one adaptive sample screen and four named main-source previews. For each proposal, create four named preview wrappers around the same composition, sample content, and theme. Preserve the starter's device settings and modeled-density labels; their rationale is in its README. Match viewport, content, state, and theme across alternatives. Add light/dark, large-font, or alternate-state previews when needed for the screen check; those supplement rather than replace the four gallery images.

Follow `compose-preview` in `project/`, render the requested previews, and open their returned PNG paths. Inspect all four device layouts for each new or revised proposal against the skill's screen check. Do not infer visual quality from compilation. Use returned filenames, not guesses. On failure, stop publication and report visual quality unverified; never publish old output as a new revision. The starter's maintenance `render.mjs` is not an alternative agent renderer.

## Publish and review

After inspecting the current renders, write a round draft outside `project/`. Image paths are relative to `project/`, using the actual PNG locations returned by Compose Preview CLI:

```json
{
  "title": "First directions",
  "proposals": [
    {
      "id": "a",
      "name": "Content first",
      "rationale": "Prioritizes today's content over navigation.",
      "images": {
        "phone": "app/build/compose-previews/renders/<phone-file>.png",
        "tablet": "app/build/compose-previews/renders/<tablet-file>.png",
        "desktop": "app/build/compose-previews/renders/<desktop-file>.png",
        "foldable": "app/build/compose-previews/renders/<foldable-file>.png"
      }
    }
  ],
  "answers": [{ "roundId": "001", "at": "<request timestamp>" }]
}
```

Include every option in that round's `proposals` array. Optional `answers` names the saved requests (round id and request timestamp) this round responds to; requests it does not name stay pending. Then:

```bash
node <skill-dir>/gallery/cli.mjs publish <exploration> <draft.json>
node <skill-dir>/gallery/cli.mjs serve <exploration>
```

Open the printed localhost URL for the user. Publication preserves source and four PNGs per proposal. Missing images fail publication; completed rounds are never overwritten. The website supports side-by-side comparison, device switching, one selected preview and a shared notes textbox per round, and previous rounds. New rounds appear automatically when the user is not editing notes or inspecting a dialog. Browsing an earlier round stays on that round. It does not launch an agent or execute builds.

Selecting a preview is not finalization. The shared **Refine [name]** action records a request in `feedback.rounds[roundId].requests` with its proposal key, notes, and timestamp. **None of these** clears the proposal choice and requires notes before requesting new directions. The website tells the user to return to chat; it does not invoke an agent. Read the latest submitted request, not merely the currently edited selection, apply clear instructions, and ask a focused question only when a meaningful decision remains. Show alternatives when the decision is visual. Produce one refinement by default. Render, inspect, and publish it as a new round. Never rewrite an earlier round to conceal changes. The user can explicitly ask to explore several alternatives again.

Wait for saved feedback before relying on it. Failed saves remain pending in the tab with a retry control. A stale-tab conflict requires reloading saved state; preserve or copy unsaved notes before discarding them. Approval waits for pending saves, verifies the published artifacts, and makes the exploration read-only.

## Watch session

A session starts only when the user asks for it, such as "keep refining while I review". A plain "adjust option B" does not start one. Run one session per exploration.

After publishing and serving, loop:

```bash
node <skill-dir>/gallery/cli.mjs wait <exploration>
```

It prints one JSON line and exits 0. Branch on `status`:

- `request`: read `request` (and mention `superseded` if present), apply the refine steps above, render, inspect, and publish a draft whose `answers` lists the handled request plus any superseded ones it consolidated. Then wait again.
- `approved`: report the approved revision and stop.
- `timeout`: rerun `wait`. Stop when 30 minutes pass with no request, say so in chat, and say how to resume ("continue refining").

When a meaningful design decision remains, ask in chat and stop waiting; the gallery's status line falls back to "return to chat" when its budget expires. A failed render or publish ends the session with a chat report. Never publish stale output; the request stays pending for a later session or a plain chat request.

The default `--timeout` is 25 seconds, so every supported harness returns it as a normal foreground result. In Claude Code you may pass `--timeout 540` with a 10-minute tool timeout to cut the number of calls.

Nothing runs after your turn ends. To resume, the user asks in chat; read `status`, whose `pending` field lists unanswered requests.

## Approve, implement, or discard

The user finalizes the single selected proposal with the page-level **Finish with [name]** action and its confirmation. A combination of several ideas must first be rendered as one consolidated proposal. Read the exact approved round and proposal via `status`; use that round's `source/` for a later implementation, not the potentially newer working sandbox.

Report the approved revision and stop. Approval does not authorize implementation or library version changes. A later explicit implementation request follows the requesting project's planning, branch, verification, and review rules. If the design cannot be preserved with its dependencies, ask rather than silently change the design or versions.

Keep source, history, notes, and images until the user explicitly requests deletion. Stop the gallery server first, then run from the requesting repository:

```bash
node <skill-dir>/gallery/cli.mjs cleanup <exploration> --project <requesting-repo> --yes
```

Cleanup removes only a directly contained, identity-marked exploration and refuses symlinks or mismatched markers. It deletes every round and note, not just build outputs. It never deletes the installed starter or the requesting app.
