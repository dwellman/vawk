# VAWK

VAWK is a plan-first AWK assistant and CLI. It runs AWK programs, chats with layered prompts + RAG, and can promote chat answers into runnable jobs.

Learn AWK: `docs/book/learn.awk.md`

## Features
- **Chat**: `vawk chat [--one-shot "..."]` with layered system/developer/project prompts, RAG snippets/book context, and intent-aware responses (EXPLAIN vs CODE/MIXED).
- **Structured replies**: CODE turns must return PLAN/CODE/TESTS/NOTES with a required AWK header; automatic fix-up and clear errors if validation fails.
- **Promotion**: `vawk promote --session <id> --turn <idx> [--name <job>]` turns a chat answer into `vawk/jobs/<job>/script.awk` and `spec.yaml` linked back to the chat turn.
- **RAG**: Uses snippet and learn.awk indexes under `docs/` for reference context.
- **AWK execution**: `vawk test`, `vawk run`, and AWK runners use POSIX AWK (no gawk extensions).

## Quickstart
1) Build: `mvn -q package spring-boot:repackage`
2) Run chat (one-shot): `SPRING_MAIN_WEB_APPLICATION_TYPE=none java -jar target/vawk-0.2.0-SNAPSHOT.jar chat --one-shot "Explain what FS does in awk"`
3) Run chat (structured code): `... chat --one-shot "Write awk script to sum column 3"`
4) Promote: `... promote --session <sessionId> --turn <idx> --name sum_col3` → see `vawk/jobs/sum_col3/`.
5) Execute promoted script: `awk -f vawk/jobs/sum_col3/script.awk input.txt`
6) Run via wrapper (optional):
   - Add the wrapper to your PATH: `export PATH="/path/to/vawk/bin:$PATH"`
   - Then: `vawk chat --one-shot "Explain what FS does in awk"`

See `docs/howto/guides/quickstart.md` for more setup tips (stub vs real model, shell alias, promotion).

---

## Attribution

If you use this project in your own work, demos, or products, a brief attribution is appreciated:

> Built with tools and ideas from this project.

This is a request for credit, not a legal requirement. Please see the LICENSE file for the actual terms.

## Prompts and AGENTS
- System/developer prompts live in `prompts/` alongside the default `AGENTS.md` and `vawk.project.example.md` fallbacks.
- Project prompt: `AGENTS.vawk.md` or `AGENTS.md` at project root (primary), with optional `vawk.project.md` appended. Falls back to `vawk.project.example.md` if none present.

## Patterns (aligns with Adventure)
- **Grounding:** RAG snippets/learn.awk constrain chat; CODE replies must include PLAN/CODE/TESTS/NOTES with AWK headers; promotion links jobs back to chat turn/spec (`spec.yaml`).
- **Orchestration:** Deterministic turn handling; CODE intent produces structured blocks; promotion flow writes jobs/specs; runners use POSIX AWK only.
- **Verification:** Validation of structured replies (PLAN/CODE/TESTS/NOTES), promotion checks, AWK runner tests; `mvn -q test` runs chat/promo/intent suites.
- **Trust UX:** Clear errors on validation fail, no hidden gawk features, logs in `.vawk/chat/`, jobs/specs under `vawk/jobs/`.
- **Learning (process):** Receipts in chat logs/specs, promotes link back to source turn; follow BUJO practices if co-developed with Adventure.

### Pattern → Code quick map
| Pattern       | Key Files / Spots |
| ---           | --- |
| Grounding     | `src/main/java/com/vawk/chat/VawkChatService.java` (RAG), structured replies enforced by `SectionValidator`; chat logs `.vawk/chat/` |
| Orchestration | `GenCommand`/`VawkGeneratorService` (PLAN → CODE → auto-test), promotion outputs in `vawk/jobs/` |
| Verification  | `test` CLI + `AwkTestRunner`, auto-test receipts in `.vawk/logs/*.json` |
| Trust UX      | Validation errors surfaced in CLI; POSIX AWK only; visible logs/specs/jobs |
| Learning      | Session logs in `.vawk/logs/`, chat history `.vawk/chat/`; BUJO alignment via `AGENTS.md` |

## Logs and receipts
- Chat logs: `.vawk/chat/<session>.vawk` (meta + turn NDJSON; includes auto-fix steps for structured turns).
- Jobs: `vawk/jobs/<name>/script.awk`, `spec.yaml` (contains sessionId/turnIdx/plan/tests/promotedAt).

## Tests
- Run all: `mvn -q test`
- Includes intent detection, structured chat (one-shot/REPL), AGENTS prompt loading, promotion flow, and AWK runners.

## Quick Start Examples
- **Explain (one-shot)**:  
  `vawk chat --one-shot "Explain what FS does in awk"` → plain explanation; `.vawk/chat/<session>.vawk` has meta + user + assistant.

- **Structured code + promote**:  
  1) `vawk chat --one-shot "Write awk script to sum column 3"` → PLAN/CODE/TESTS/NOTES with header.  
  2) Identify the assistant turn (e.g., idx=2) in `.vawk/chat/<session>.vawk` that contains PLAN/CODE/TESTS/NOTES.  
  3) Promote: `vawk promote --session <sessionId> --turn <idx> --name sum_col3`  
     Creates `vawk/jobs/sum_col3/script.awk` and `vawk/jobs/sum_col3/spec.yaml` (links back to session/turn).  
  4) Run: prepare `input.txt` matching the script’s expectations, then `awk -f vawk/jobs/sum_col3/script.awk input.txt > output.txt`.

- **AGENTS-driven project prompt**:  
  Place `AGENTS.md` (or `AGENTS.vawk.md`) in project root; optional `vawk.project.md` is appended under `--- Additional project notes`.  
  `vawk chat --one-shot "What does this project do?"` → third system message starts with AGENTS content.***
