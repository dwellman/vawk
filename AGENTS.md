# Agents Guide (VAWK)

## BUJO + housekeeping
- Log daily work (if collaborating with other projects) in a BUJO-style `docs/process/bujo/daily/<YYYY-MM-DD>.md` and capture scope/tests in `docs/process/journal.md`. Keep entries short; migrate unfinished items forward.
- Record sweeps/refactors in a housekeeping log if you create one.

## Patterns (align with Adventure)
- **Grounding:** Use RAG snippets/learn.awk; CODE intent must return structured PLAN/CODE/TESTS/NOTES with AWK headers; promoted jobs link back to the chat turn/spec.
- **Orchestration:** Deterministic turn handling; promotion writes `vawk/jobs/<name>/script.awk` + `spec.yaml`; runners are POSIX AWK only.
- **Verification:** Structured reply validation (reject missing PLAN/CODE/TESTS/NOTES), promotion checks, `vawk test` runner with PASS/FAIL receipts in `.vawk/logs/`.
- **Trust UX:** Clear validation errors; no gawk extensions; logs are visible (`.vawk/chat/`, `vawk/jobs/`, `.vawk/logs/`).
- **Learning (process):** Receipts in chat logs/specs; use judges/transcripts to prevent regressions; follow BUJO practices when co-developing with Adventure.

## Prompt placement
- System/developer prompts live in `prompts/`.
- Project prompt is loaded from `AGENTS.vawk.md` or `AGENTS.md` (root), plus optional `vawk.project.md` if present. Keep prompts generic and mechanically focused (no lore baked into the prompt).

## Testing expectations
- Default: `mvn -q test` (covers intent, structured chat, promotion, runners).
- Judge a promoted job: `SPRING_MAIN_WEB_APPLICATION_TYPE=none java -jar target/vawk-0.2.0-SNAPSHOT.jar test --awk vawk/jobs/<name>/script.awk` (writes `.vawk/logs/...json`).
