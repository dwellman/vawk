# Engine Architecture (VAWK) — TODAY → TOMORROW → SOMEDAY

## TODAY (working)
- **Responsibility:** Execute AWK programs deterministically against fixtures; validate structured replies before writing programs/specs.
- **Core types:** `AwkProgram`, `AwkTestRunner`, `AwkTestCase`, `TestRepository`, `ProgramRepository`, `SpecRepository`, `SessionLogRepository`.
- **Ops:** `vawk test` runner executes AWK against `tests/` fixtures (or `--tests-dir`), diffs outputs, prints PASS/FAIL, and writes receipts (`.vawk/logs/*.json`).
- **Loading:** Structured PLAN/CODE/TESTS/NOTES validated by `SectionValidator`; code written via `ProgramRepository`; specs via `SpecRepository`.
- **Invariants:** POSIX AWK only (no gawk extensions); structured replies required for CODE intent.
- **Tests:** `mvn -q test` covers runner/intent/promotion flows.
- **Patterns:** Grounding (structured replies + fixtures), Orchestration (deterministic run), Verification (diffs + receipts), Trust UX (no hidden magic), Learning (session logs).

### Text diagram (engine/test flow)
```
spec.yaml + main.awk
      |
tests/ (or --tests-dir)
      |
  AwkTestRunner -> PASS/FAIL -> .vawk/logs/*.json
```

## TOMORROW (in motion)
- Expand fixtures and validation coverage; add more precise diff reporting.
- Tighten structured reply checks; optional golden transcript fixtures.

## SOMEDAY (planned)
- Judge checks for mechanic-adding outputs; REST/multi-user surface.
- DB-backed receipts if extended to multi-user.
- Provider/base-URL knobs already exposed via Spring AI props; consider configurable runner sandbox.
