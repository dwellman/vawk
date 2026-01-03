# Housekeeping / Checklist

**BLUF:** Quick quality gate for VAWK changes: format, test, and verify receipts before handoff. Audience: developers and release reviewers.

## 1) Purpose
Provide a repeatable pre-merge checklist so changes stay consistent and verifiable.

## 2) Scope
- Covers: formatting, testing, AWK constraints, and logging expectations.
- Excludes: deep style rules (see `docs/reference/standards/java-style.md`), doc structure (see `docs/reference/standards/docs-style.md`).

## 3) Expectations / Rules
- Required: Format Java with `google-java-format`.
- Required: POSIX AWK only; no gawk-only features in generated scripts.
- Required: Run `mvn -q test` and fix failures.
- Required: Preserve `.vawk/` receipts (NDJSON for chat, JSON logs for gen/refine).
- Recommended: Keep prompts/docs in sync with observed behavior; update golden paths when flows change.

## 4) Checklist
- [ ] Java formatted (`google-java-format`).
- [ ] Tests green: `mvn -q test`.
- [ ] AWK output includes required header (# VAWK, # Purpose, # Intent, # Input, # Output).
- [ ] Chat/gen/refine runs produce .vawk logs when applicable.
- [ ] Docs updated if behavior changed (see `docs/`).
- [ ] Golden path still valid or refreshed.

## 5) Proof-of-work / References
- Test suite: `mvn -q test`.
- Logs: `.vawk/chat/*.vawk`, `.vawk/logs/*.json`.
- Docs: `docs/reference/overview.md`, `docs/reference/architecture/overview.md`, `docs/reference/standards/java-style.md`, `docs/reference/standards/docs-style.md`, `docs/chat/golden-path-pipe-logs.md`.
