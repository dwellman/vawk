# VAWK Design v0.2 – Plan-First, Proof-of-Work, and AGENTS.md

## 1. Purpose

VAWK is a small, CLI-first "vibe coder" for AWK, built with Spring Boot, Spring AI, and Picocli.

It is designed as:

- A focused demo of AI-assisted coding patterns (plan-first, iterative refinement, RAG, PoW).
- A deterministic harness around AWK, where the LLM operates only at the edges.
- A teaching tool for how to design agent workflows similar to Codex, but in a constrained domain.

VAWK converts natural language specifications into AWK scripts, explains them, refines them, and runs tests, while keeping all orchestration logic transparent and auditable.

---

## 2. Core principles

1. **Plan-first**

   - The first task for the AI on any code-changing operation is always to write a **PLAN**.
   - The plan must describe, in concrete terms:
     - What the script should do.
     - Which fields and inputs it will use.
     - What outputs it will produce.
     - How edge cases are handled.
   - Code generation or modification is not allowed until the plan exists.
   - The plan is persisted as part of the session’s proof-of-work.

2. **Explicit proof-of-work (PoW)**

   - Every AI interaction that changes code must produce human-readable receipts.
   - VAWK standardizes AI responses into four sections:
     - `PLAN:` – detailed, natural-language plan.
     - `CODE:` – AWK code, usually `main.awk`.
     - `TESTS:` – which fixtures should pass and what they assert.
     - `NOTES:` – limitations, assumptions, future ideas.
   - These sections are parsed and stored in a `SessionLog` for each run.

3. **AGENTS.md as project guidance**

   - Each project may define an `AGENTS.md` at its root.
   - `AGENTS.md` describes:
     - Data format.
     - Setup (AWK flavor, constraints).
     - How to run the script.
     - Tests and proof expectations.
     - Constraints and code style.
     - Examples.
     - Agent workflow and PoW requirements.
   - VAWK always reads `AGENTS.md` (if present) and injects it as high-priority guidance into the AI prompts.

4. **Deterministic runtime, AI at the edges**

   - All execution uses the local `awk` binary via `AwkRunner`.
   - Given the same `main.awk` and test inputs, `vawk test` must produce the same outputs.
   - The AI never runs commands directly. It only proposes code and test ideas, which VAWK executes deterministically.

5. **Small, explicit services over magic**

   - Orchestration is handled by small, readable Spring services.
   - No hidden heuristics or implicit gates.
   - Each step (plan, generate, test, log) is coded explicitly.

---

## 3. CLI surface

VAWK is a Spring Boot CLI application using Picocli for command parsing.

### 3.1 Commands

1. `vawk gen`

   - Purpose: Generate a new AWK script and spec from a natural-language description.
   - Input:
     - Free-text spec (required).
     - Flags:
       - `--mode=single|cot|iter` (default: `cot` with explicit plan).
       - `--output=main.awk` (default).
   - Behavior:
     1. Load `AGENTS.md` if present.
     2. Build an initial `VawkSpec` from CLI arguments and text.
     3. Call AI with enforced `PLAN/CODE/TESTS/NOTES` format.
     4. Parse sections and persist:
        - `spec.yaml`
        - `main.awk`
        - `SessionLog` entry.
   - Output:
     - Summary of generated script.
     - Pointer to files and suggested `vawk test` / `vawk run` calls.

2. `vawk refine`

   - Purpose: Refine an existing AWK script based on a change request.
   - Input:
     - Change description (text).
     - Optional flags to select spec/script if multiple exist.
   - Behavior:
     1. Load `AGENTS.md`, existing `spec.yaml`, and `main.awk`.
     2. Build prompt including current behavior and requested change.
     3. AI returns updated `PLAN`, `CODE`, `TESTS`, `NOTES`.
     4. Persist updated spec, script, and session log.
   - Output:
     - Summary of changes and where they were applied.

3. `vawk explain`

   - Purpose: Explain what an AWK script does and how it maps to the data format.
   - Input:
     - Optional path to an AWK file (default `main.awk`).
   - Behavior:
     1. Load `AGENTS.md` and AWK file.
     2. Ask AI to produce:
        - PLAN: description of behavior and data flow.
        - CODE: optional commented version or just echo existing code.
        - TESTS: suggested test cases that would confirm behavior.
        - NOTES: caveats and potential issues.
   - Output:
     - Explanation printed to stdout.
     - Optionally stored explanation in `.vawk/logs/` as part of the session log.

4. `vawk test`

   - Purpose: Run existing tests to validate the current script.
   - Input:
     - Uses `tests/` directory convention by default.
   - Behavior:
     1. Discover fixtures (`input_*.log` and `expect_*.txt`) via `TestRepository`.
     2. For each pair:
        - Run `AwkRunner` on the input.
        - Compare actual vs expected output.
     3. Build a test report.
   - Output:
     - Pass/fail per test, with unified diffs on failure.
   - AI is not required for `vawk test`, but results are recorded in `SessionLog`.

5. `vawk run`

   - Purpose: Execute the AWK script against real data.
   - Input:
     - Data file path or STDIN.
   - Behavior:
     1. Use `AwkRunner` to execute `main.awk`.
     2. Stream output to STDOUT.
   - No AI involvement.

6. `vawk lint` (optional for v1, recommended for completeness)

   - Purpose: Lint and review an AWK script with AI assistance.
   - Input:
     - Optional path to AWK file (default `main.awk`).
   - Behavior:
     1. Load `AGENTS.md` and AWK file.
     2. Ask AI to:
        - PLAN: describe intended behavior inferred from the code.
        - CODE: suggest small refactors or improved comments.
        - TESTS: suggest missing edge-case tests.
        - NOTES: list potential problems.
   - Output:
     - Lint report printed to stdout and stored in session log.

### 3.2 Example flow

**Happy path (log summarizer project):**

1. User writes `AGENTS.md` describing the log format and constraints.
2. User runs:

   ```bash
   vawk gen "Summarize log files by level and optionally per user"
   ```

3. VAWK:
   - Reads `AGENTS.md`.
   - Calls AI, which returns PLAN/CODE/TESTS/NOTES.
   - Saves `spec.yaml`, `main.awk`, and a `SessionLog`.
4. User creates `tests/input_basic.log` and `tests/expect_basic.txt`.
5. User runs:

   ```bash
   vawk test
   ```

6. If tests pass, user runs:

   ```bash
   vawk run input.log > summary.txt
   ```

---

## 4. Architecture

VAWK is implemented as a Spring Boot application with Picocli-based commands.

### 4.1 Packages

- `com.vawk.cli`
  - `GenCommand`
  - `RefineCommand`
  - `ExplainCommand`
  - `TestCommand`
  - `RunCommand`
  - `LintCommand`

- `com.vawk.domain`
  - `VawkSpec`
  - `AwkProgram`
  - `AwkTestCase`
  - `GenerationMode` (SINGLE, COT, ITERATIVE, RAG)
  - `SessionLog`

- `com.vawk.ai`
  - `VawkChatConfig` – Spring AI config and `ChatClient` bean.
  - `SystemPrompts` – constants for system messages per agent role.
  - `VawkAgents` – simple methods encapsulating AI calls.

- `com.vawk.orchestration`
  - `VawkGeneratorService`
  - `VawkRefinementService`
  - `VawkExplainService`
  - `VawkLintService`

- `com.vawk.runtime`
  - `AwkRunner` – executes AWK scripts.
  - `AwkTestRunner` – orchestrates running tests via `AwkRunner`.

- `com.vawk.store`
  - `SpecRepository` – read/write `spec.yaml`.
  - `ProgramRepository` – read/write `main.awk`.
  - `TestRepository` – manage `tests/` fixtures.
  - `SessionLogRepository` – read/write `.vawk/logs/*.json`.
  - `AgentsFileRepository` – read `AGENTS.md`.

### 4.2 Data models

- `VawkSpec`
  - `description` – natural-language summary.
  - `inputs` – description of input format and fields.
  - `outputs` – description of desired outputs.
  - `constraints` – performance/safety rules.
  - `examples` – optional example input/output pairs.

- `AwkProgram`
  - `path` – path to AWK file (default `main.awk`).
  - `content` – script text.
  - `hash` – hash of content for reproducibility.

- `AwkTestCase`
  - `name`
  - `inputPath`
  - `expectedPath`

- `SessionLog`
  - `id`
  - `timestamp`
  - `command` – `GEN`, `REFINE`, `EXPLAIN`, `LINT`, `TEST`, `RUN`.
  - `mode` – `SINGLE`, `COT`, `ITERATIVE`, `RAG` (if applicable).
  - `specHash`
  - `programHash`
  - `planText`
  - `testsRequested` – list of test names the AI claims to cover.
  - `testsRun` – list with pass/fail status.
  - `notes`
  - `model` – model name used by Spring AI.
  - `tokenUsage` – optional, if available.

---

## 5. AI agent design

VAWK uses Spring AI `ChatClient` with a small set of agent roles. Each agent is implemented as a method in `VawkAgents`.

### 5.1 Shared prompt shape

Every AI call that relates to code must:

1. Include `AGENTS.md` content (if present) as a high-priority system or context message.
2. Include task-specific system messages (e.g. “You are an AWK code generator for this project”).
3. Include user content (spec, change request, code, etc.).
4. Require the model to respond in this exact section order:

   ```text
   PLAN:
   <plan text>

   CODE:
   ```awk
   # AWK code or empty if not applicable
   ```
   TESTS:
   - ...

   NOTES:
   - ...
   ```

VAWK parses these sections via simple delimiters, not by complex JSON schemas, to keep prompts small and robust.

### 5.2 Roles

- **SpecNormalizer (`specFromVibe`)**
  - Input: free-text spec, AGENTS.md.
  - Output: refined `VawkSpec` plus a PLAN describing the intended script behavior.

- **Planner (`planFromSpec`)**
  - Input: `VawkSpec`.
  - Output: detailed PLAN with step-by-step logic and data flow.

- **CodeGenerator (`awkFromPlan`)**
  - Input: PLAN, `VawkSpec`, AGENTS.md.
  - Output: AWK code under the CODE section, plus suggested tests and notes.

- **Refiner (`refineSpecAndAwk`)**
  - Input: current `VawkSpec`, current `AwkProgram`, change request, AGENTS.md.
  - Output: updated PLAN, CODE, TESTS, NOTES.

- **Explainer (`explainAwk`)**
  - Input: `AwkProgram`, AGENTS.md.
  - Output: PLAN (describing actual behavior), optional commented CODE, suggested TESTS, NOTES.

- **Linter (`lintAwk`)**
  - Input: `AwkProgram`, AGENTS.md.
  - Output: PLAN (what the script seems to do), suggested CODE improvements, TESTS, NOTES.

These methods are thin wrappers around `ChatClient`, responsible for building messages and parsing the standard PLAN/CODE/TESTS/NOTES format.

---

## 6. Proof-of-work and logging

### 6.1 Session logs

Each CLI command that touches the AI or modifies code writes a `SessionLog` to `.vawk/logs/`:

- File name: `YYYYMMDD-HHmmss-<command>-<shortid>.json`.
- Contains:
  - Command and mode.
  - Model name.
  - Plan text.
  - Tests requested by the AI.
  - Tests actually run and their results.
  - Script hash and spec hash.
  - Any human or agent notes.

### 6.2 Test results

- `vawk test` runs are recorded in `SessionLog` entries.
- If tests are run automatically after `vawk gen --auto-test`, the `GEN` session log includes both the AI receipt and the test results.
- Logs are append-only; corrections are handled by new entries, not modification of old ones.

### 6.3 Relationship to AGENTS.md

- `AGENTS.md` defines:
  - What counts as valid input and output.
  - What tests and constraints matter.
- `SessionLog` shows:
  - How the agent interpreted AGENTS.md in a specific run (through PLAN).
  - Which tests were used as PoW for that run.

Together they give a complete, auditable story: intent → instructions → plan → code → tests → results.

---

## 7. Non-goals and boundaries

- Not a general software engineering agent:
  - VAWK is intentionally limited to AWK.
- No Git or PR automation in v1:
  - Users manage commits themselves.
- No long-lived cloud tasks:
  - Each command invocation is a short-lived local process.
- No direct shell control by the AI:
  - All execution and file operations are handled by VAWK’s deterministic Java code.

This keeps VAWK small, understandable, and safe to run on a developer laptop while still demonstrating modern agent patterns: plan-first, PoW logging, AGENTS.md-based guidance, and deterministic testing.
