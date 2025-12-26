# Golden Path – Pipe Logs to CSV

**BLUF:** A complete, real run that shows VAWK chat producing a structured PLAN/CODE/TESTS/NOTES answer, promotion into a job, and execution of the generated AWK on sample input. Audience: engineers/demo speakers; usable as RAG context.

## 1) Purpose
Provide a verified end-to-end example that can be replayed to prove the chat → promotion → AWK run path works.

## 2) Scope
- Covers: commands used, transcript snippets, generated files, sample input/output.
- Excludes: full prompt text; see `prompts/` for details.

## 3) Expectations / Rules
- Required: POSIX AWK only; CODE includes the header block.
- Required: Chat turn being promoted passes SectionValidator (PLAN/CODE/TESTS/NOTES + header).
- Required: Promotion writes `vawk/jobs/<name>/script.awk` and `spec.yaml` with session linkage.

## 4) Example (real run)
- Chat (one-shot, CODE):
  - Command: `vawk chat --one-shot "How do I reformat pipe logs into CSV?"`
  - User message (recorded in `.vawk/chat/<session>.vawk`, idx=1): `How do I reformat pipe logs into CSV?`
  - Assistant reply (idx=2) included:
    - PLAN: set `FS=" *\\| *"`, `OFS=","`, print CSV header, skip comments/blank/header, join message fields.
    - CODE:
      ```awk
      # VAWK: pipe_logs_to_csv
      # Purpose: Reformat pipe-separated log records into a simple CSV file.
      # Intent: Make log files easy to import into spreadsheets or downstream tools without changing meaning.
      # Input: Lines like '2025-12-07T21:15:03Z | INFO | user123 | Logged in', one per line; header and comment lines may be present.
      # Output: CSV with a header row and rows containing timestamp, level, user, and message fields.

      BEGIN {
          FS  = " *\\| *"
          OFS = ","
          print "timestamp","level","user","message"
      }

      /^#/ || NF == 0 { next }
      NR == 1 && ($1 ~ /[Tt]ime/ || $2 ~ /[Ll]evel/ || $3 ~ /[Uu]ser/) { next }

      {
          timestamp = $1
          level     = $2
          userId    = $3
          message = ""
          if (NF >= 4) {
              message = $4
              for (i = 5; i <= NF; i++) { message = message " " $i }
          }
          print timestamp, level, userId, message
      }
      ```
    - TESTS: sample command `awk -f pipe_logs_to_csv.awk pipe.log > pipe.csv`.
    - NOTES: assumptions about pipes and commas.

- Promotion:
  - Command: `vawk promote --session <sessionId> --turn 2 --name pipe_logs_to_csv`
  - Outputs:
    - `vawk/jobs/pipe_logs_to_csv/script.awk` (same CODE as above).
    - `vawk/jobs/pipe_logs_to_csv/spec.yaml`:
      ```yaml
      id: pipe_logs_to_csv
      source:
        sessionId: <sessionId>
        turnIdx: 2
      plan: |
        PLAN content from the chat turn...
      tests: |
        TESTS content from the chat turn...
      promotedAt: <timestamp>
      ```

- Sample input (`pipe.log`):
  ```
  # time | level | user | message
  2025-12-07T21:15:03Z | INFO  | user123 | Logged in
  2025-12-07T21:15:04Z | ERROR | user123 | Payment failed: card declined
  2025-12-07T21:15:05Z | WARN  | user456 | Slow response
  ```

- Run:
  - Command: `awk -f vawk/jobs/pipe_logs_to_csv/script.awk pipe.log > pipe.csv`
  - Output (`pipe.csv`):
    ```
    timestamp,level,user,message
    2025-12-07T21:15:03Z,INFO,user123,Logged in
    2025-12-07T21:15:04Z,ERROR,user123,Payment failed: card declined
    2025-12-07T21:15:05Z,WARN,user456,Slow response
    ```

## 5) Proof-of-work / References
- NDJSON transcript: `.vawk/chat/<sessionId>.vawk` (meta + turns 1/2).
- Promoted artifacts: `vawk/jobs/pipe_logs_to_csv/script.awk`, `spec.yaml`.
- Test suite: `mvn -q test` (covers structured chat and promotion).
