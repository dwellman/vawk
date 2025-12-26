# Content Pipeline (VAWK) — TODAY → TOMORROW → SOMEDAY

## TODAY (working)
- **Sources:** Natural language tasks → structured PLAN/CODE/TESTS/NOTES → `spec.yaml` + `main.awk`.
- **Artifacts:** Promoted jobs under `vawk/jobs/<name>/` with `script.awk` + `spec.yaml`.
- **RAG:** Snippets/learn.awk under `docs/` feed context to chat/gen flows.
- **Validation:** `SectionValidator` enforces PLAN/CODE/TESTS/NOTES; AWK headers required; POSIX AWK only.
- **Tests:** `vawk test`/`gen --auto-test` run fixtures under `tests/` or `--tests-dir`; receipts in `.vawk/logs/*.json`.
- **Patterns:** Grounding (structured replies + RAG), Orchestration (gen/promote flows), Verification (tests/diffs), Trust UX (fail loud on invalid sections), Learning (logs/spec hashes).

## TOMORROW (in motion)
- More curated RAG corpora; richer project prompt hooks.
- Additional validation on promoted jobs/specs.

## SOMEDAY (planned)
- Golden transcript fixtures; content completion gates per job/project.
- REST ingest/export of specs/jobs; multi-user/project surfaces.
- DB-backed logs if scaled to multi-user.
