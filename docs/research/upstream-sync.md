# Upstream Source Sync

Every rule in `android-design` must trace back to an official Google source: `m3.material.io`, `developer.android.com/design/ui`, the official Material and Android videos, and `design.google` articles. Shipped apps, including Google's own, are examples rather than rules.

Distill from committed files in `sources/`, not from memory or fresh web fetches, so every claim stays verifiable against a saved copy. Never edit `sources/` by hand.

## Update order

When a source changes or a gap is found:

1. Update the distilled research docs in `docs/research/` first.
2. Update the skill files (`plugin/skills/android-design/` and `plugin/skills/android-design-explore/`).
3. Update the matching ledger rows in [source-coverage.md](source-coverage.md).

## Sync steps

Fetch monthly and after major Material or Android announcements. Quarterly, look for new `design.google` articles and official talks (`scripts/fetch.py` fetches those from fixed lists and cannot discover new ones).

1. Run `python3 scripts/fetch.py` (about 90 seconds; `--only m3,blog,android,articles,videos` for a subset). Output is deterministic, so `git diff --stat sources/` shows real upstream changes. If a fetch stops because it got far fewer pages than saved, check whether the site's format changed before rerunning with `--allow-shrink`.
2. For each changed page, read its diff and update `docs/research/`, then the skills, where guidance changed. Deleted pages usually moved; find the new route before dropping anything.
3. For Material 3 component pages (`sources/m3.material.io/pages/components/`), keep `scripts/test_m3_page_coverage.py` (`M3_PAGE_CLAIMS` / `M3_OMISSIONS`) and `CallNameMapper.kt` aligned: a new component page fails `python3 -m unittest discover scripts` until its Compose API is mapped or added to `M3_OMISSIONS` with a reason.
4. Run `python3 scripts/check_coverage.py`. It fails when a distilled ledger row has no source file or a source file has no ledger row. Give each new page a row with a verdict.
5. Commit `sources/` together with the research, skill, and ledger changes it caused so git history records what changed upstream and what it changed here.
