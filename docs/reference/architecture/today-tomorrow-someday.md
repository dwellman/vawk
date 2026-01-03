# Architecture Overview (VAWK) — TODAY → TOMORROW → SOMEDAY

## TODAY (working)
- Plan-first generation with layered prompts, RAG snippets/learn.awk, and structured PLAN/CODE/TESTS/NOTES for CODE intent.
- Promotion flow writes `vawk/jobs/<name>/script.awk` + `spec.yaml` linked back to the chat turn; `gen --auto-test` runs propose+judge in one pass.
- POSIX AWK runners; no gawk extensions. Validation/fix-up on structured replies; `mvn -q test` covers intent, structured chat, promotion, runners.
- Patterns: Grounding (RAG + structured replies), Orchestration (deterministic turns/promotion/auto-test), Verification (validation/tests), Trust UX (clear errors, visible logs/specs), Learning (receipts in chat/spec/logs).

### Text diagram (VAWK flow)
```
gen (task) --auto-test
   |
   +--> RAG + prompts -> PLAN/CODE/TESTS/NOTES
   |
   +--> write spec.yaml + main.awk
   |
   +--> auto-test (tests/ -> PASS/FAIL) -> .vawk/logs/*.json
```

## TOMORROW (in motion)
- Richer RAG corpora and project prompt hooks; additional validators for edge cases.
- Demo scripts/playbooks for talks.

## SOMEDAY (planned)
- Judge checks for mechanic-adding outputs, golden transcript fixtures, REST/multi-user surface if needed.
- BUJO alignment when co-developed with other projects.
- Provider/base-URL knobs for true vendor agnosticism (Spring AI props: `SPRING_AI_OPENAI_API_KEY`, `SPRING_AI_OPENAI_BASE_URL`, `vawk.ai.model`, `vawk.ai.use-chat`).

## Pointers
- README: `README.md`
- Getting started: `docs/howto/guides/getting-started.md`
- Prompts/agents: `prompts/`, `AGENTS.md`
- Logs: `.vawk/chat/`, jobs/specs in `vawk/jobs/`, test receipts in `.vawk/logs/`
- Engine: `engine.md`
- CLI: `cli.md`
- Content: `content.md`
