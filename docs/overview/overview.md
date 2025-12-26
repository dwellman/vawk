# VAWK Overview

**BLUF:** VAWK is a plan-first, POSIX AWK-focused assistant and CLI. It chats with layered prompts + RAG, enforces structured PLAN/CODE/TESTS/NOTES answers with AWK headers, runs/tests AWK scripts, and can promote chat outputs into runnable jobs. Audience: engineers and AI agents new to the project who need the mental model of what VAWK delivers.

## 1) Purpose
Describe what VAWK is for and the primary user-facing flows. This is the entry doc for anyone evaluating or extending the system.

## 2) Scope
- Covers: high-level goals, main commands/flows, proof-of-work expectations.
- Excludes: deep implementation details (see `architecture.md`), prompt text (see `prompts/`), and RAG source details (see `docs/rag/`).

## 3) Expectations / Rules
- Required: POSIX AWK only; no gawk-only features unless explicitly flagged.
- Required: PLAN/CODE/TESTS/NOTES structure for code-oriented chat responses; AWK header block (# VAWK/# Purpose/# Intent/# Input/# Output) in CODE.
- Required: `.vawk/chat/*.vawk` NDJSON logs for all chat sessions; `.vawk/logs` for generation/refinement logs.
- Required: Jobs promoted via `vawk promote` must capture chat linkage (sessionId/turnIdx) and produce `script.awk` + `spec.yaml`.
- Recommended: Run `mvn -q test` before handoff; keep prompts and docs in sync with behavior.

## 4) Examples
- Chat (code): `vawk chat --one-shot "Write awk script to sum column 3"` → structured reply with header.
- Promote: `vawk promote --session <id> --turn <idx> --name sum_col3` → `vawk/jobs/sum_col3/script.awk` + `spec.yaml`.
- Run: `awk -f vawk/jobs/sum_col3/script.awk input.txt > output.txt`.

## 5) Proof-of-work / References
- Tests: `mvn -q test` (covers chat intent, structured enforcement, promotion, AWK runners).
- Logs: `.vawk/chat/<session>.vawk` shows meta + turns (including auto-fix steps).
- Prompts: `prompts/vawk.system.md`, `prompts/vawk.developer.md`, `prompts/vawk.project.md` (or AGENTS-derived).
- Golden path: see `docs/chat/golden-path-pipe-logs.md`.
