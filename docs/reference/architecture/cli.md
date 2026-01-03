# CLI Architecture (VAWK) — TODAY → TOMORROW → SOMEDAY

## TODAY (working)
- **Commands:** `gen` (PLAN/CODE/TESTS/NOTES, writes spec/awk, optional auto-test), `chat` (structured chat), `promote`, `test`, `lint`, `refine`, `explain`.
- **AI integration:** Spring AI `ChatClient` when `vawk.ai.use-chat=true` and a model/key are set; otherwise deterministic stub.
- **Auto-test:** `gen --auto-test --tests-dir <dir>` runs propose + judge in one pass; receipts in `.vawk/logs/*.json`.
- **Invariants:** Structured CODE replies required; POSIX AWK only.
- **Patterns:** Grounding (RAG + structured), Orchestration (deterministic commands), Verification (validation/tests), Trust UX (clear errors, visible logs/specs), Learning (session logs).

### Text diagram (CLI surface)
```
gen --auto-test "task"
   |
   +--> spec.yaml + main.awk
   +--> auto-test (tests/...) -> PASS/FAIL + .vawk/logs/*.json

chat/promote -> vawk/jobs/<name>/script.awk + spec.yaml

test --awk vawk/jobs/<name>/script.awk -> PASS/FAIL + .vawk/logs/*.json
```

## TOMORROW (in motion)
- More RAG hooks/project prompts; clearer validation errors; small demo playbooks.

## SOMEDAY (planned)
- REST/multi-user surface if needed; judge hooks for mechanic-adding outputs.
- Provider/base-URL knobs already supported via Spring AI props (`SPRING_AI_OPENAI_API_KEY`, `SPRING_AI_OPENAI_BASE_URL`, `vawk.ai.model`, `vawk.ai.use-chat`).
