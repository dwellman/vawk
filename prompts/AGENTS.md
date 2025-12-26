# AGENTS.md

This repo uses VAWK to generate and maintain an AWK script that summarizes log files.

## Data format

Each input line is a log entry with fields separated by spaces:

- Field 1: ISO-8601 timestamp, e.g. `2025-12-07T21:15:03Z`
- Field 2: log level, one of `INFO`, `WARN`, `ERROR`
- Field 3: user id, alphanumeric string, no spaces
- Field 4+: free-text message

Example:

2025-12-07T21:15:03Z INFO user123 Logged in
2025-12-07T21:15:04Z ERROR user123 Payment failed: card declined

Assumptions:

- UTF-8 input
- One log entry per line
- No embedded newlines inside a single logical record

## Setup

- Use **POSIX awk** only.
  - Avoid GNU awk extensions (`gensub`, `asorti`, `PROCINFO`, etc.).
- Do **not** call external commands via `system()` or backticks.
- Assume the script will be run as:

  awk -f main.awk input.log

  on a typical Linux system.

## How to run

Normal usage:

  awk -f main.awk input.log > summary.txt

The script should:

- Count total lines.
- Count lines per log level.
- Optionally group by user when requested via a flag.

If a `--by-user` behavior is implemented, use an AWK variable:

- `by_user=1` to enable grouping by user.

Example:

  awk -v by_user=1 -f main.awk input.log

Document any flags at the top of `main.awk`.

## Tests and proof

Use the `tests/` directory for fixtures:

- `tests/input_basic.log` - small sample input.
- `tests/expect_basic.txt` - exact expected output for that input.
- Additional cases can be added as `input_*.log` / `expect_*.txt` pairs.

To run tests with VAWK:

  vawk test

Vawk should:

- Run `awk -f main.awk` on each `tests/input_*.log`.
- Compare the output to the matching `tests/expect_*.txt`.
- Report pass/fail for each case, with a unified diff when outputs differ.

To run the basic test manually:

  awk -f main.awk tests/input_basic.log > /tmp/actual_basic.txt
  diff -u tests/expect_basic.txt /tmp/actual_basic.txt

Agents should treat tests as **required proof**, not optional.

## Constraints

- Scripts must run in a **single pass** over input (streaming).
- Memory usage should not depend on total input size (avoid storing the whole file).
- Do not modify input files.
- Do not write any new files; only write to stdout.
- Assume logs may be large; favor simple, linear-time logic.

## Code style

- Use descriptive variable names, even in AWK, e.g.:
  - `lineCount`, `infoCount`, `errorCount`, `warnCount`, `userId`.
- Put a short header comment at the top of `main.awk` describing:
  - Purpose.
  - Expected input format.
  - Supported flags (such as `by_user`).
- Prefer clearly structured blocks, for example:

  BEGIN { ... }
  /ERROR/ { ... }
  /WARN/  { ... }
  /INFO/  { ... }
  END { ... }

- Comment any non-obvious regex or parsing logic.
- Keep line length and nesting simple so humans can review quickly.

## Agent workflow and proof-of-work

Agents working in this repo must follow this workflow for every change:

1. **Always start with a PLAN.**
   - Before writing or changing any AWK code, write a brief plan that explains:
     - What the script should do.
     - Which fields it will use.
     - What outputs it will produce.
     - How it will handle edge cases.
   - The plan should be specific enough that a human could implement it directly.

2. **Then write CODE that follows the plan.**
   - Generate or modify `main.awk` to implement the plan.
   - Keep code aligned with the data format, constraints, and style rules above.
   - If you need to change the plan, update the PLAN section and then adjust the code.

3. **Describe TESTS as proof-of-work.**
   - List which fixtures you expect to pass (for example, `input_basic.log`).
   - Describe the high-level expected output for each fixture.
   - If new edge cases are introduced, propose or create new fixture names.

4. **Add NOTES only for important observations.**
   - Use NOTES to record anything a human should know:
     - Limitations.
     - Assumptions.
     - Ideas for future improvements.

When responding through VAWK, format your output in these sections, in this order:

PLAN:
- ...

CODE:
\`\`\`awk
# contents of main.awk
\`\`\`

TESTS:
- ...

NOTES:
- ...

The PLAN must come first and must be complete before the CODE section.

## Examples

Given this input:

2025-12-07T21:15:03Z INFO  user123 Logged in
2025-12-07T21:15:04Z ERROR user123 Payment failed: card declined
2025-12-07T21:15:05Z WARN  user456 Slow response

An acceptable default output would look like:

total_lines=3
INFO=1
WARN=1
ERROR=1

With `by_user=1`, an acceptable output would look like:

total_lines=3
INFO=1
WARN=1
ERROR=1

user=user123 total=2 INFO=1 ERROR=1
user=user456 total=1 WARN=1

The exact formatting can change, but keep it:

- Plain text.
- One key=value pair or record per line.
- Easy to diff and parse in tests.
