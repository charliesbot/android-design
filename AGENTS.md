# Repository Purpose

This is the home of the `android-design` agent skill: Material 3 Expressive design judgment for Jetpack Compose on phone, tablet, desktop, Wear OS, and widgets. The repository holds the skill, the research it is distilled from, and the evals that check it.

It is charliesbot's personal repository. Optimize for the owner's workflow, not broad applicability.

## Layout

- `skills/android-design/` is the installable skill (`SKILL.md` and `references/`). Chai installs it from here, so keep anything that is not skill content out of this folder.
- `docs/research/` holds the distilled research the skill is built from. `docs/research/source-coverage.md` is the page-by-page ledger of every official source and whether its guidance reached the research docs.
- `evals/` checks that agents following the skill still produce screens that meet its rules. See `evals/README.md`.

## Sources

Every rule in the skill must trace back to an official Google source: m3.material.io, developer.android.com/design/ui, the official Material and Android videos, and design.google articles. Apps built by other developers, including Google's own apps, are examples, not rules.

When a source changes or a gap is found, update the research docs first, then the skill, then the matching ledger rows.

## Verification

After changing the skill, run the evals (`evals/README.md`). After changing the rubric, run `evals/calibrate.py` first.

## Prose

No em-dashes anywhere in this repo's prose (`SKILL.md`, references, docs, `README.md`, and code comments). Rewrite the sentence with a comma, colon, period, parentheses, or conjunction, whichever it actually wants. Never do a blind character substitution.

## Project documentation

- `docs/PRD.md`, when present, defines the project direction and north star.
- `docs/ARCHITECTURE.md`, when present, describes the current system.
- `docs/design/`, when present, contains focused design documents.
- `docs/research/`, when present, contains external API, platform, and technical research.
- `docs/archive/`, when present, contains non-current documents and is historical context only.

Other project documents can live directly under `docs/`. Use lowercase kebab-case
filenames except for the fixed `PRD.md` and `ARCHITECTURE.md` names.

## Task tracking

Track approved delivery work in GitHub issues at https://github.com/charliesbot/android-design.
When asked to resume tracked work or select the next task, use
`planning-and-task-breakdown` to inspect this tracker and identify the next
implementation-ready task. Create tickets only when explicitly requested, using
`to-tickets`.
