# Docs Index

**BLUF:** Quick map of the `docs/` folder so contributors and AI agents know where to look for system expectations, style guides, and golden paths.

## 1) Purpose
Orient readers to the primary documents and subfolders under `docs/`.

## 2) Map of key files
- `overview/overview.md` – What VAWK is for and the main flows.
- `overview/architecture.md` – Components, data flows, invariants.
- `overview/java-style.md` – Google Java Style baseline + VAWK addendum.
- `overview/docs-style.md` – How we write and structure docs.
- `overview/housekeeping-checklist.md` – Pre-merge quality gate.
- `overview/journal-standard.md` – BuJo proof-of-work standard (logs live under `docs/journal/`).
- `guides/quickstart.md` – Quickstart steps and common usage.
- `guides/getting-started.md` – Getting started checklist and setup notes.
- `design/patterns.md` – Design patterns and usage notes.
- `runbooks/index.md` – Runbook index and conventions.
- `adr/index.md` – Architecture decision records.
- `glossary.md` – Shared terms and definitions.
- `journal/` – Space for daily/epic proof-of-work logs.
- `bujo/` – Daily BUJO logs when co-developed with other projects.
- `housekeeping/` – Sweep/refactor logs (`cleaning.log`).
- `chat/` – Chat-specific docs and golden paths (`vawk-chat-overview.md`, `golden-path-pipe-logs.md`).
- `rag/` – RAG index (`rag-index.md`) and sources.
- `examples/`, `book/` – RAG source content (snippet index, learn.awk).

## 3) Usage
- Start with `overview/overview.md` and `overview/architecture.md` for context.
- Follow style rules in `overview/java-style.md` and `overview/docs-style.md` when changing code or docs.
- Record proof-of-work in `journal/` per `overview/journal-standard.md`.
- Use golden paths under `chat/` for demos or validation.
- See `docs/index.md` for the full docs map.
