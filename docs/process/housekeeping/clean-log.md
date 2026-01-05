# Deep Clean Log

Last verified: 2026-01-05

Use this file to log deep-clean passes. Keep entries chronological and concise.

## Entry Format
```
## YYYY-MM-DD — <Scope> — <Owner>
- Scope: <what was reviewed/changed>
- Commands: <tests/commands; note blockers>
- Coverage: <tool/command or not available>
- Docs: <README/docs updates, Last verified stamps>
- Status: Pending / In Progress / Clean (date, owner)
- Risks/Follow-ups: <open items>
```

## 2026-01-05 — Docs test API key note — Codex
- Scope: Documented test API key requirement in README and guides; added Last verified stamps; logged sweep.
- Commands: `SPRING_AI_OPENAI_API_KEY=dummy mvn -q test`; vawk test not run (no AWK changes).
- Coverage: JaCoCo Instruction 94.8%, Branch 89.6%, Line 94.7% (`target/site/jacoco/index.html`).
- Docs: Updated `README.md`, `docs/howto/guides/getting-started.md`, `docs/howto/guides/quickstart.md`.
- Status: Clean (2026-01-05, Codex)
- Risks/Follow-ups: Branch coverage below 90% target.
