# Agent-driven Refine loop

> **Archived:** Superseded by chat iteration on 2026-10-15. The gallery, server, `wait` loop, and feedback store this document describes were removed.

**Date:** 2026-10-10

## TL;DR

Close the gap between pressing Refine in the gallery and the next round appearing, without turning the gallery into a chat client or a background service. The user starts a watch session in chat. The agent then runs a blocking `wait` command that returns when a refinement request is saved, answers it by revising, reviewing in the browser, and publishing, and waits again until the user presses Finish, the session idles out, or the turn ends. Nothing runs after the agent's turn ends; the existing "return to chat" path remains the fallback. This document records the investigation for [issue #9](https://github.com/charliesbot/android-design/issues/9); it authorizes planning, not implementation.

## Background

[Visual design exploration](visual-design-exploration-v2.md) settled that the gallery never launches an agent and that Refine saves an explicit request the agent reads later. Today that read happens only when the user returns to chat and asks. The gallery shows "Request saved. Return to chat to continue." after a request, polls for new rounds every few seconds, and keeps unsaved edits when a round arrives. The store already provides everything a watcher needs: `feedback.json` carries a monotonically increasing `revision`, every write is an atomic rename under a write lock, requests carry a timestamp, and rounds carry `createdAt`.

The issue asked three questions: whether an explicitly started watch session can detect saved requests and publish the next round; how duplicates, failures, and Finish are handled; and whether any automation is needed after the agent's turn ends. A throwaway prototype of the `wait` command was run against the real store and HTTP server to answer the first two. It was not committed.

## Design

### An explicit watch session

The session is a loop the agent runs inside one turn, started by the user in chat ("keep refining while I review", "watch the gallery"). It is not started by the gallery, by a hook, or by a scheduler. A plain request such as "adjust option B" still works without a session.

```
publish round N ─▶ serve (background) ─▶ wait ──▶ request ──▶ revise, review, publish N+1 ─┐
                                          ▲  └──▶ approved ─▶ report the approved revision, stop     │
                                          │  └──▶ timeout ──▶ idle budget left? rerun wait : stop    │
                                          └───────────────────────────────────────────────────────────┘
```

Every step the agent already performs between rounds (reading the request, applying clear feedback, asking a focused question when a real design decision remains, building, reviewing the proposal, publishing) stays as written in the skill's exploration reference. The session only replaces "the user returns to chat" with "the agent is already listening". When a meaningful decision remains, the agent asks in chat as before; the user is told to check chat only when the gallery's status line says the session stopped.

One refinement per request remains the default. A request whose notes ask for several alternatives still produces one round with several proposals.

### The `wait` command

A new gallery CLI command, alongside `publish`, `serve`, and `status`:

```bash
node <skill-dir>/gallery/cli.mjs wait <exploration> [--after <revision>] [--timeout <seconds>]
```

It blocks until one of three outcomes and prints one JSON object on stdout:

| Outcome | When | Output |
| --- | --- | --- |
| `request` | A pending refinement request exists, immediately or after a save | the newest pending request (`roundId`, `choice`, `note`, `at`), any older pending requests as `superseded`, and the current `revision` |
| `approved` | The user pressed Finish and confirmed | the `approved` record and `revision` |
| `timeout` | `--timeout` elapsed with nothing pending | the current `revision` |

It exits 0 for all three so the agent branches on `status`, and non-zero only when the exploration cannot be read. `--after <revision>` suppresses requests the agent already saw at that revision, for the case where it chose to ask a question instead of publishing. `--timeout` defaults to 25 seconds, which every supported harness returns as an ordinary foreground result (see Lifecycle limitations), and the agent reruns the command until something happens. A signal (the user interrupting the turn) ends it with no output, which the agent treats as the session ending.

The command polls `feedback.json` and the round list about once a second. Polling matches the gallery's own three-second poll, needs no file watcher, and reads are safe because every write is an atomic rename. In the prototype a request submitted through the HTTP server was returned within one poll interval while the server's write lock was in use.

An MCP server was considered instead of a CLI subcommand. The protocol is standard and Chai can register the server for every harness, so configuration is not the objection. The objections are cost and fit: the gallery is dependency-free Node with tests in `node --test`, and a subcommand keeps it that way, while MCP adds an SDK dependency, a long-lived process in every session, and an MCP client in the tests. MCP tool timeouts are also tighter and less uniform than shell timeouts (Codex fails a tool call after 60 seconds by default; Claude Code moves one to the background after 2 minutes; Antigravity documents none), which makes a blocking `wait` harder to tune. The one thing MCP would genuinely improve is `serve`, whose lifetime could follow the server process instead of a background shell command. Revisit MCP as a whole-gallery decision if that becomes a problem; it is out of scope here.

### Which request to answer

A request is **pending** when no round answers it. The newest pending request wins; older pending ones are reported as `superseded` so the agent can mention that it consolidated them. This covers a user who refines twice on the same round with different notes (the gallery already blocks resubmitting identical notes) and a user who browses an earlier round and refines from there.

Deciding "answered" by comparing timestamps (request `at` against the latest round's `createdAt`) is not enough. The prototype confirmed the race: the user submits a request, the agent starts rendering, the user submits a second request on the same round, and the agent publishes. The round is newer than both requests, so the second one looks answered and is silently lost. The implementation closes this by making the link explicit:

- `publish` accepts an `answers` list in the draft (`[{ "roundId": "001", "at": "<request timestamp>" }]`) and stores it on `round.json`. The agent passes the request it handled plus any superseded requests it consolidated into the same round.
- `wait` treats a request as answered only when some round's `answers` names it. Rounds published without `answers` (the first round, and rounds from the manual chat flow) fall back to the timestamp rule so existing explorations keep working.

With the link in place, the lost second request is returned by the next `wait` call immediately after publishing, and the agent answers it with another round.

### Failure and stopping

Failures stay in chat. If the build, browser review, or publication fails, the agent reports in chat and stops the session rather than publishing stale output or retrying blindly; the exploration reference already forbids publishing old output as a new revision. The request stays pending, so a later session or a plain chat request picks it up.

The session stops when:

- **Finish.** `wait` returns `approved`. The agent reports the approved round and proposal and stops, exactly as the manual flow does. Approval does not implement anything.
- **Idle budget exhausted.** Each `timeout` consumes part of a session budget (recommended 30 minutes of inactivity). When it runs out, the agent says so in chat and how to restart ("say continue refining"). This keeps a forgotten gallery tab from holding a turn open indefinitely.
- **The user interrupts or the turn ends.** The command is killed; nothing continues.
- **A question is needed.** The agent asks in chat and stops waiting. The gallery status line falls back to "return to chat" once its render budget expires, which is the cue to look at chat.

### Session status in the gallery

Decision: include a minimal status line, with expiry. Without it the user looking at the gallery cannot tell "the agent is building a round, which takes a few minutes" from "nobody is listening", and returning to chat to find out defeats the loop.

`wait` writes `session.json` in the exploration root on each transition: `{ "state": "waiting" | "working", "since": ..., "until": ... }`. `waiting` expires one minute after the command's timeout, so the gap between reruns does not flip the line; `working` is written when it exits with a request and lasts a fixed render budget (recommended 10 minutes). The server exposes the file in `/api/exploration` and the gallery renders one line next to the request status:

- `waiting`: "Request saved. The next round will appear here."
- `working`: "The agent is working on your request."
- missing or past `until`: the existing "Request saved. Return to chat to continue."

Expiry is what makes this honest when an agent dies mid-render, so no `stop` command or cleanup step is required of the agent. The cost is lag: when the agent fails or asks a question in chat instead of publishing, the line keeps saying "working" until the render budget expires. Failure details stay in chat; the status line never carries messages. The alternative, no status at all, keeps the gallery simpler but leaves the user guessing during every render, so it was rejected.

### No automation after the turn ends

A separate automation is not necessary and is not wanted. Once the turn ends, the saved request, `status`, and the gallery's "return to chat" message already let the user resume from chat, and the explicit session covers the active review period. A daemon that invoked an agent on its own would need a harness-specific launcher, credentials, cost control, and a way to surface its questions, and it would contradict the settled rule that the gallery never launches an agent. Harness hooks and scheduled wakeups exist in some harnesses but not all, so the loop must not depend on them.

## Lifecycle limitations

The contract is deliberately harness-agnostic: one foreground command, bounded by `--timeout`, rerun in a loop by the agent. Background execution with a wake-up on exit is an optimization where a harness offers it, never a requirement.

What was found for the three harnesses the skill must support (checked 2026-10-10; "unverified" means no official statement was found):

| Harness | Foreground command limit | What happens past the limit | Wake without a tool call |
| --- | --- | --- | --- |
| Claude Code | Default 2 minutes, the model may request up to 10 minutes (`BASH_DEFAULT_TIMEOUT_MS`, `BASH_MAX_TIMEOUT_MS` raise both) | The command moves to the background and the model is notified when it exits; `run_in_background` does the same from the start. Unattended sessions cap background commands at 30 minutes by default | Hooks, scheduled wakeups, and cron exist but are Claude Code only |
| Codex CLI | Unified exec yields after at most 30 seconds (`yield_time_ms`, 250 ms to 30 s); the one-shot variant defaults to 10 seconds (`timeout_ms`) and kills the process on timeout, with no upper clamp found | Under unified exec the process keeps running across turns and the model re-attaches by polling it, each poll waiting a few seconds up to a 5-minute cap. No notification on exit was found: re-attachment is poll only | `Stop` and `PreToolUse` hooks; a model-called sleep tool. No timer that fires on its own |
| Antigravity 2.0 | Unverified for the IDE: no documented per-command timeout. The CLI's headless mode moved its default from 5 minutes to unlimited | Commands move to the background mid-turn and, since CLI 1.2.16, the agent "sees the command finish with its exit code" and continues. Headless runs wait for background tasks up to a 30-minute cap | Hooks, a `schedule` tool, and `/schedule` timers exist but are Antigravity only |

Sources: Claude Code [tools reference](https://code.claude.com/docs/en/tools-reference) and [environment variables](https://code.claude.com/docs/en/env-vars); Codex [`shell_spec.rs`](https://github.com/openai/codex/blob/main/codex-rs/core/src/tools/handlers/shell_spec.rs), [`unified_exec/mod.rs`](https://github.com/openai/codex/blob/main/codex-rs/core/src/unified_exec/mod.rs), and [config reference](https://learn.chatgpt.com/docs/config-file/config-reference); Antigravity [changelog](https://antigravity.google/docs/changelog/), [headless CLI](https://antigravity.google/docs/cli/headless/), and [hooks](https://antigravity.google/docs/hooks/).

What this means for `wait`:

- **The default timeout is 25 seconds.** That is the longest value every supported harness returns as an ordinary foreground result: under Codex's 30-second yield, far under Claude Code's limit, and short enough not to trigger Antigravity's move to the background. The agent reruns the command until a request arrives or the idle budget runs out.
- **A longer timeout is an opt-in per harness.** In Claude Code the agent may pass `--timeout 540` with a 10-minute tool timeout and cut the number of calls by twenty times. The skill text names this as a Claude Code optimization, not the default, so a harness that backgrounds the command and polls it still works, just with more calls.
- **The idle budget is wall clock, not call count.** Thirty minutes of inactivity is about seventy `wait` calls at the default timeout, each returning one short JSON line. That is acceptable context cost for an active review session and the reason the budget exists.

Consequences:

- A session lives only as long as the agent keeps calling tools. Context use grows by one small JSON result per `wait` call, so a long idle session costs little but is not free; the idle budget bounds it.
- The agent cannot be woken by the gallery. If the harness kills the command or the turn, the user restarts from chat.
- Two sessions on one exploration would race on publication. The write lock makes that safe at the file level, but the skill text should tell the agent to run one session per exploration.

## Verification criteria

For the later implementation, not this document:

- Unit tests for `wait` in `tests/gallery/`: request during wait, pending request before start, newest-wins with `superseded`, `--after` cursor, published round answers a request, the second-request race returns the lost request, Finish during wait, the 25-second default and an explicit timeout, exit without output on signal.
- Store tests: `publish` stores `answers`; rounds without `answers` fall back to the timestamp rule; `session.json` expiry is respected by the server response.
- Server test: `/api/exploration` includes session state and omits an expired one.
- Gallery smoke check in `evals/exploration.md`: start a session, press Refine, observe the next round arrive without touching chat, observe the status line through waiting and working, press Finish, confirm the session reports the approved revision and stops. Separately, let a session idle out and confirm the chat message and the gallery fallback text.
- Routing checks: "keep refining while I review" starts a session after the first publish; "adjust option B" without a session still produces one preserved round; a failed browser review stops the session with visual quality reported unverified.
- Skill text: a "Watch session" subsection in `references/exploration.md` covering start, the loop, one session per exploration, stop conditions, and the idle budget. `SKILL.md` needs no new rule.

## Links

- [Visual design exploration v2](visual-design-exploration-v2.md): the gallery design this loop extends.
- [Exploration reference](../../skills/android-design/references/exploration.md): the agent steps the session repeats.
- [Exploration checks](../../evals/exploration.md): where the smoke and routing checks will be added.
- [Issue #9](https://github.com/charliesbot/android-design/issues/9): the investigation this document answers.
