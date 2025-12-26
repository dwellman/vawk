# Docs Style Standard

**BLUF:** The `docs/` folder is a first-class language for humans and AI. Every important behavior, invariant, and proof-of-work must be captured here in clear, numbered Markdown that is easy to chunk for RAG. Also follow the workspace standards in `/Users/dwellman/workspace/demo/PROCESS.md`. Audience: all contributors.

## 1) Purpose
Define how we write, structure, and maintain documents under `docs/` so they stay authoritative and AI-friendly.

## 2) Scope
- Covers: required top-level docs, naming, structure, writing style, golden paths, and how docs relate to code.
- Excludes: Java code style (see `java-style.md`), prompt text (see `prompts/`).

## 3) Expectations / Rules
- Required: Markdown only; filenames in lowercase kebab-case (e.g., `overview.md`, `architecture.md`, `java-style.md`, `docs-style.md`).
- Required top-level docs:
  0. `index.md` – docs map and navigation.
  1. `overview.md` – BLUF of system purpose and primary flows.
  2. `architecture.md` – components, data flows, invariants.
  3. `java-style.md` – Google Java Style baseline + local addendum.
  4. `docs-style.md` – this document.
  5. `housekeeping/checklist.md` (recommended) – formatting/tools/tests expectations.
- Recommended: `runbooks/index.md`, `adr/index.md`, `glossary.md`, and `guides/` for setup flows.
- Required: number sections and include a BLUF plus a short audience note.
- Required: tag rules as “Required/Recommended/Optional.” No “TBD.”
- Required: For AI-facing docs (chat, RAG, jobs), include inputs, outputs, invariants, and at least one example.
- Recommended: Domain subfolders (`docs/chat/`, `docs/jobs/`, `docs/rag/`, `docs/agents/`, `docs/journal/`).

## 4) Structure of each document
1. Header: `# Title`, BLUF (2–4 sentences), optional metadata (Audience, Last updated).
2. Sections (suggested):
   - `1) Purpose`
   - `2) Scope`
   - `3) Expectations / Rules` (use Required/Recommended/Optional)
   - `4) Examples`
   - `5) Proof-of-work / References`

## 5) Writing style
- Plain, direct English; intent first, then how.
- Concrete commands and paths over vague descriptions.
- Avoid ambiguous terms (“usually”, “maybe”)—state exact rules and exceptions.
- Stable terminology: PLAN/CODE/TESTS/NOTES, command names, class names.

## 6) Golden paths
- Maintain at least one real “golden path” doc for complex flows (e.g., `docs/chat/golden-path-pipe-logs.md`).
- Each golden path must include exact commands, transcript snippets, generated files (`script.awk`, `spec.yaml`, sample input/output), and a note on how output matches expectations.

## 7) Docs and code
- Code is “done” only if behavior is documented and backed by tests/logs.
- If docs and code disagree, treat it as a bug: align code to docs or update docs to match reality.

## 8) Proof-of-work / References
- Existing docs following this structure: `overview.md`, `architecture.md`, `java-style.md`, `vawk-chat-overview.md`, `chat/golden-path-pipe-logs.md`.
- Tests: `mvn -q test` for functional proof; `.vawk/chat` and `.vawk/logs` for run-time receipts.
