# Java Style Standard

**BLUF:** Follow Google Java Style for formatting, with a project addendum that requires prompt-friendly class comments, public-method summaries, no abbreviations, and classes that read top-to-bottom “like a book.” Also follow the workspace standards in `/Users/dwellman/workspace/demo/PROCESS.md`. Audience: all Java contributors and AI agents emitting Java.

## 1) Purpose
Define the Java coding standard for VAWK so humans and AI produce consistent, readable code.

## 2) Scope
- Covers: formatting baseline, addendum rules, naming, class/method structure expectations.
- Excludes: build/test commands (see `docs/process/housekeeping/checklist.md`), docs style (see `docs/reference/standards/docs-style.md`).

## 3) Expectations / Rules
- Required: Google Java Style for layout/formatting (`google-java-format` is source of truth).
- Required: Class-level comments explaining why/what/how (prompt-friendly).
- Required: Public methods have short explanations (skip trivial POJO accessors).
- Required: No abbreviations in names (avoid `cfg/svc/mgr/req/resp/tmp/cnt`).
- Required: Classes read in narrative order (intent → fields/constructors → public behavior → helpers); keep methods small.
- Recommended: Split classes before they become multi-concern “god classes.”

## 4) Examples
- Naming: prefer `buildChatPrompt()` over `makePrompt()`, `appendAssistantTurnToLog()` over `writeTurn()`.
- Class comment snippet:
  ```
  /**
   * VawkChatService coordinates chat interactions:
   * - Builds layered prompts (system, developer, project, RAG).
   * - Handles intent detection (EXPLAIN vs CODE vs MIXED).
   * - Validates and logs structured replies (PLAN/CODE/TESTS/NOTES) to NDJSON.
   */
  ```

## 5) Proof-of-work / References
- Source of addendum: `java-style-standard.md` (root) and this doc.
- Codebase conformance: comments and naming updates across services, CLI commands, runtime, and stores; `mvn -q test` passes.
