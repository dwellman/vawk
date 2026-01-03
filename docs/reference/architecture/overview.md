# Architecture

**BLUF:** VAWK is a Spring Boot CLI that layers prompts + RAG over AWK tooling. It separates chat orchestration, AWK runtime/test harnesses, repositories, and CLI commands to keep responsibilities clear. Audience: engineers extending services or wiring new features.

## 1) Purpose
Map the major components, data flows, and invariants so changes stay aligned with the design.

## 2) Scope
- Covers: modules, core services, storage/layout, and key paths (chat, generation/refinement, promotion, runtime).
- Excludes: full prompt text (see `prompts/`), AWK code style (see `docs/reference/standards/java-style.md`), and RAG corpus details (see `docs/rag/`).

## 3) Expectations / Rules
- Required: POSIX AWK only in generated scripts and tests.
- Required: PLAN/CODE/TESTS/NOTES for code-oriented AI replies; AWK header enforced by `SectionValidator`.
- Required: `.vawk/chat/` NDJSON sessions (meta + turns); `.vawk/logs/` JSON session logs for gen/refine/explain/lint.
- Required: Directory creation via `DirectoryService` for `.vawk`, `chat`, `logs`.
- Recommended: Keep services narrow (chat/orchestration/runtime/store/util); avoid god classes and keep methods small.

## 4) Components (high level)
1. **CLI (Picocli)**: `VawkCommand` root; subcommands `chat`, `gen`, `refine`, `explain`, `lint`, `test`, `run`, `promote`.
2. **Chat**: `VawkChatService`, `VawkChatPromptBuilder`, `IntentDetector`, `VawkPromptLoader`; uses layered system/developer/project prompts, optional RAG context (`RagRepository`), NDJSON session logging.
3. **AI Agents**: `VawkAgents` with `AiClient` implementations (`SpringAiClient`, `LocalStubAiClient`) and system prompts in `prompts/`.
4. **Orchestration**: `VawkGeneratorService`, `VawkRefinementService`, `VawkExplainService`, `VawkLintService`, `VawkSpecBuilder`; handle PLAN/CODE/TESTS/NOTES validation, hashing, logging, optional auto-tests.
5. **Runtime**: `AwkRunner`, `AwkTestRunner`, `AwkRunRequest/Result`; execute AWK scripts and compare outputs.
6. **Stores/Repositories**: `ProgramRepository`, `SpecRepository`, `SessionLogRepository`, `TestRepository`, `AgentsFileRepository`, `RagRepository`; persist programs/specs/logs/tests and guarded RAG access.
7. **Domains**: Plain data holders for specs, programs, session logs, tests, etc.
8. **Docs/Prompts**: `prompts/` (system/developer/project), `docs/reference` (overview/architecture/design/standards), `docs/process` (journal/BUJO/housekeeping), `docs/chat` (golden paths), `AGENTS.md` as primary project prompt source with optional `vawk.project.md`.

## 5) Data flows (examples)
- **Chat (one-shot CODE/MIXED)**: CLI → `VawkChatService` → build prompts (system/developer/project/RAG/history + wrapped user) → ChatClient/stub → validate via `SectionValidator` → append NDJSON turns.
- **Gen/Refine**: CLI → orchestrator → `VawkAgents` (spec/plan/code) → `SectionValidator` → write `spec.yaml`/`main.awk` → optional auto-tests (`AwkTestRunner`) → `SessionLogRepository`.
- **Promotion**: CLI → `PromoteCommand` → parse/validate chat turn → write `vawk/jobs/<name>/script.awk` + `spec.yaml` with session linkage.

## 6) Proof-of-work / References
- Tests: `mvn -q test`.
- Logs: `.vawk/chat/*.vawk` and `.vawk/logs/*.json`.
- Prompts: `prompts/` + AGENTS-derived project prompt.
- Golden path: `docs/chat/golden-path-pipe-logs.md`.
