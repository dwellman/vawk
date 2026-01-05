# Docs Style Standard

**BLUF:** The `docs/` folder is a first-class language for humans and AI. Every important behavior, invariant, and proof-of-work must be captured here in clear, numbered Markdown that is easy to chunk for RAG. Also follow the workspace standards in `PROCESS.md` (if present at the workspace root). Audience: all contributors.

## 1) Purpose
Define how we write, structure, and maintain documents under `docs/` so they stay authoritative and AI-friendly.

## 2) Scope
- Covers: required top-level docs, naming, structure, writing style, golden paths, and how docs relate to code.
- Excludes: Java code style (see `docs/reference/standards/java-style.md`), prompt text (see `prompts/`).

## 3) Expectations / Rules
- Required: Markdown only; filenames in lowercase kebab-case (e.g., `overview.md`, `architecture.md`, `java-style.md`, `docs-style.md`).
- Required top-level docs:
  0. `docs/index.md` – docs map and navigation.
  1. `docs/readme.md` – pointer to the canonical docs index.
  2. `docs/reference/overview.md` – BLUF of system purpose and primary flows.
  3. `docs/reference/architecture/overview.md` – components, data flows, invariants.
  4. `docs/reference/standards/java-style.md` – Java style baseline + local addendum.
  5. `docs/reference/standards/docs-style.md` – this document.
  6. `docs/process/logs/index.md` – process log hub.
  7. `docs/process/journal.md` – scope + tests log.
  8. `docs/process/bujo/readme.md` – BUJO process notes.
- Recommended: `docs/howto/runbooks/index.md`, `docs/howto/guides/`, `docs/adr/index.md`, `docs/glossary.md`.
- Required: number sections and include a BLUF plus a short audience note.
- Required: tag rules as “Required/Recommended/Optional.” No “TBD.”
- Required: For AI-facing docs (chat, RAG, jobs), include inputs, outputs, invariants, and at least one example.
- Recommended: Domain subfolders (`docs/chat/`, `docs/examples/`, `docs/book/`, `docs/rag/`, `docs/process/`, `docs/howto/`, `docs/reference/`).

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
- Existing docs following this structure: `docs/reference/overview.md`, `docs/reference/architecture/overview.md`, `docs/reference/standards/java-style.md`, `docs/chat/vawk-chat-overview.md`, `docs/chat/golden-path-pipe-logs.md`.
- Tests: `mvn -q test` for functional proof; `.vawk/chat` and `.vawk/logs` for run-time receipts.
