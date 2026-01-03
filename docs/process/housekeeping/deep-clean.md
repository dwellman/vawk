# Deep Clean Playbook (VAWK)

Last verified: 2026-01-02

Purpose: repo-wide housekeeping pass to keep code, docs, and prompts aligned. Review classes, refresh comments where needed, validate docs, and keep logs current.

## Rules of the Road
- Class comments act as indexes: summarize intent, invariants, and entry points when needed.
- Add method comments only when a method is non-obvious; keep inline comments rare and factual.
- Prefer descriptive names and top-to-bottom readability (fields -> constructors -> public API -> helpers).
- Keep POSIX AWK constraints intact; do not introduce gawk-only features.
- Avoid behavior changes unless required for cleanup or correctness.
- Keep prompts and narrative docs in `prompts/` and `docs/` (no ad-hoc prompt text in code).

## Deep Clean Protocol
1. Scope & plan
   - Log the sweep in `docs/process/housekeeping/cleaning.log` and `docs/process/bujo/daily/<date>.md`.
   - Record intended tests in `docs/process/journal.md`.
2. Inventory
   - Verify root `README.md` and `docs/` are current; note any drift.
   - List packages/classes to review and recent hot spots.
3. Code pass
   - Refresh class summaries where missing or out of date.
   - Trim dead code, unused methods, and stale TODOs.
   - Confirm POSIX AWK rules and structured PLAN/CODE/TESTS/NOTES validation still match prompts.
4. Docs pass
   - Update `docs/readme.md` and any touched references.
   - Add or refresh `Last verified` stamps for touched docs.
5. Testing
   - Run `mvn -q test` (or targeted suites when appropriate).
   - Run `vawk test` for any updated AWK scripts/fixtures.
   - Record commands/outcomes in logs.
6. Log & handoff
   - Update `docs/process/housekeeping/cleaning.log` and `docs/process/housekeeping/clean-log.md`.
   - Capture risks/follow-ups in BUJO and `docs/process/journal.md`.

## Module Deep-Clean Backlog
Update Status to `Pending` / `In Progress (date, call sign)` / `Clean (date, call sign)`.

| Module | Status | Notes |
| --- | --- | --- |
| com.vawk.cli | Pending |  |
| com.vawk.chat | Pending |  |
| com.vawk.ai | Pending |  |
| com.vawk.runtime | Pending |  |
| com.vawk.store | Pending |  |
| com.vawk.domain | Pending |  |
| docs | Pending |  |
| prompts | Pending |  |
| tests | Pending |  |

## Deliverables
- Updated docs and housekeeping logs.
- Unused code removed or documented.
- Test runs recorded with outcomes.
- Coverage status noted when reports are available.
