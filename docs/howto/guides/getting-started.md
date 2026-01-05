# Getting Started (VAWK)
Last verified: 2026-01-05

## Prereqs
- Java 17, Maven.
- Optional: model API key if using a live LLM (otherwise run stub mode).

## Build & test
- Build: `mvn -q package spring-boot:repackage`
- Tests (requires `SPRING_AI_OPENAI_API_KEY`; dummy is OK to satisfy Spring AI autoconfig): `SPRING_AI_OPENAI_API_KEY=dummy mvn -q test`

## Chat
- One-shot explain: `SPRING_MAIN_WEB_APPLICATION_TYPE=none java -jar target/vawk-0.2.0-SNAPSHOT.jar chat --one-shot "Explain what FS does in awk"`
- Structured code: `... chat --one-shot "Write awk script to sum column 3"` (returns PLAN/CODE/TESTS/NOTES with header)

## Promote
- `... promote --session <sessionId> --turn <idx> --name my_job`
- Outputs `vawk/jobs/<name>/script.awk` + `spec.yaml` (links back to session/turn)

## Run promoted job
- `awk -f vawk/jobs/<name>/script.awk input.txt`

## Prompts & project context
- System/developer prompts in `prompts/`; project prompt from `AGENTS.vawk.md` or `AGENTS.md` (root), optional `vawk.project.md` appended.

## Logs
- Chat: `.vawk/chat/<session>.vawk`
- Jobs/specs: `vawk/jobs/`

## Patterns (matches Adventure)
- Grounding: RAG snippets/learn.awk, structured PLAN/CODE/TESTS/NOTES with AWK headers.
- Orchestration: Deterministic turn handling, promotion flow, POSIX AWK runners.
- Verification: Structured reply validation, promotion checks, runner tests (`mvn -q test`).
- Trust UX: Clear validation errors, no gawk extensions, visible logs/specs.
- Learning: Receipts in chat/specs; use BUJO if co-developing with Adventure.

## Provider/model knobs (current)
- Use Spring AI env/props for models; default is OpenAI-style: `SPRING_AI_OPENAI_API_KEY`, `SPRING_AI_OPENAI_BASE_URL` (optional), `vawk.ai.model` (recorded in logs, defaults to `local-stub` when chat disabled).
- Enable real chat: `vawk.ai.use-chat=true` plus a valid API key. Without a key, VAWK falls back to the deterministic stub.
