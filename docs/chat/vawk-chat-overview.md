# VAWK Chat + Jobs Overview

**BLUF:** VAWK chat layers system/developer/project prompts and RAG context to produce structured PLAN/CODE/TESTS/NOTES answers with AWK headers. Sessions are logged as NDJSON, and structured turns can be promoted into jobs (`script.awk` + `spec.yaml`). Audience: engineers and AI agents working on chat or promotion.

## 1) Purpose
Explain how chat behaves (prompts, intent detection, validation, logging) and how promotion turns a chat answer into runnable artifacts.

## 2) Scope
- Covers: prompt layering, intent paths (EXPLAIN vs CODE/MIXED), NDJSON format, promotion outputs, AGENTS usage.
- Excludes: full RAG corpus details (`docs/rag/rag-index.md`, snippet/book indexes) and Java style rules (`docs/reference/standards/java-style.md`).

## 3) Expectations / Rules
- Required: POSIX AWK only; CODE must include the header block (# VAWK/# Purpose/# Intent/# Input/# Output).
- Required: PLAN/CODE/TESTS/NOTES for CODE/MIXED intents; SectionParser + SectionValidator enforce structure.
- Required: `.vawk/chat/<session>.vawk` NDJSON (meta then turns); auto-fix flows log user/assistant/correction turns.
- Required: Project prompt from `AGENTS.vawk.md`/`AGENTS.md` (primary) plus optional `vawk.project.md` appended under `--- Additional project notes`; fallback to `vawk.project.example.md`.
- Required: Promotion writes `vawk/jobs/<name>/script.awk` and `spec.yaml` with `sessionId` and `turnIdx`.
- Recommended: Inject RAG context from `RagRepository.searchForChat(query, 3)` as a single `# Reference context...` system message.

## 4) Examples
- One-shot CODE: `vawk chat --one-shot "Write awk script to sum column 3"` → wrapped/validated PLAN/CODE/TESTS/NOTES reply with header.
- REPL code turn: `vawk chat` then `write awk script to sum column 3` → same structured path; on failure, prints reason/suggestions and continues.
- Promotion: `vawk promote --session <sessionId> --turn <idx> --name sum_col3` → `vawk/jobs/sum_col3/script.awk` + `spec.yaml`.
- AGENTS prompt: add `AGENTS.md`; `vawk chat --one-shot "What does this project do?"` → third system message starts with AGENTS content (plus optional project notes).

## 5) Proof-of-work / References
- Logs: `.vawk/chat/<session>.vawk` (meta + turns, including auto-fix sequences).
- Jobs: `vawk/jobs/<name>/script.awk` (header-compliant AWK) and `spec.yaml` (id, sessionId, turnIdx, plan, tests, promotedAt).
- Tests: `mvn -q test` covers intent detection, structured enforcement, RAG prompt layering, promotion.
- Prompts: `prompts/vawk.system.md`, `prompts/vawk.developer.md`, project prompt (AGENTS + optional `vawk.project.md`).
- Golden path: `docs/chat/golden-path-pipe-logs.md`.
