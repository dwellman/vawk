# Journal

Legacy table-format logs live under `docs/process/journal-archive/` for reference only.

## 2026-01-02 — Docs/process alignment
- Scope: Aligned VAWK docs to the Adventure SOP (process logs, howto/reference layout, updated indexes, updated AGENTS, archived legacy journal format).
- Scope: Repackaged the boot jar to run the CLI wrapper and verified `vawk test`.
- Tests: `mvn -q test`; `mvn -q package spring-boot:repackage`; `SPRING_AI_OPENAI_API_KEY=dummy ./bin/vawk test`

## 2025-12-26 — Docs alignment and coverage
- Scope: Aligned docs structure (index, runbook index, glossary, ADR stubs) and moved guides under `docs/`; updated README references.
- Tests: Not run (docs-only changes).
- Scope: Added `bin/vawk` wrapper, documented PATH usage, and linked Learn AWK in README.
- Tests: Not run (docs-only changes).
- Scope: Added unit tests and JaCoCo reporting to raise coverage.
- Tests: `mvn -q test`
