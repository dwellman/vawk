# VAWK Project Overview (Example: Log Summarizer)

## Project

- name: Log Summarizer
- goal: Summarize application logs by level and user, and produce simple text or CSV summaries.

## Data Format

Each log line has the form:

- Field 1: ISO-8601 timestamp, e.g. `2025-12-07T21:15:03Z`
- Field 2: log level, e.g. `INFO`, `WARN`, `ERROR`
- Field 3: user id, no spaces
- Field 4+: free-text message

Assumptions:

- UTF-8 input.
- One log entry per line.
- No embedded newlines inside a single logical record.

## Output Conventions

For summaries:

- Use `key=value` pairs for metrics, one per line.
- Example:

  ```text
  total_lines=1000
  INFO=700
  WARN=200
  ERROR=100
  ```

For CSV-style outputs:

- Use `,` as the separator.
- Include a header row when producing tables.

## Project Constraints

- Use POSIX AWK only unless the user explicitly allows gawk.
- Logs may be large; avoid loading the entire file into memory.
- Do not write new files from scripts; use stdout for all output.
- Avoid `system()` and external commands; keep scripts self-contained.

## Expectations

- Make it easy for the user to:
  - Extract subsets of logs (by level, user, date).
  - Compute counts and simple aggregates.
  - Reformat log lines into CSV for further analysis.
