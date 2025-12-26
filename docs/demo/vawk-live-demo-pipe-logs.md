# VAWK Live Demo Script – Pipe Logs to CSV

## 0. Demo goal

You say:

> I’m going to show VAWK as an AWK “vibe coder” that:
> - Knows its own rules via layered prompts.
> - Pulls in reference docs with RAG.
> - Produces AWK with a clear header and tests.
> - And logs the whole conversation in `.vawk` as proof-of-work.

You will:

- Run one `vawk chat --one-shot` command.
- Show how prompts + RAG shape the answer.
- Save and run the AWK script on a sample file.

---

## 1. Show the project and prompts

In terminal:

```sh
cd /path/to/vawk-demo
ls
```

You say:

- “This is just a regular project folder. There’s no `.vawk` yet; VAWK will create it.”
- “All AI behavior is driven by Markdown prompts.”

Show prompts:

```sh
ls prompts
cat prompts/vawk.system.md | sed -n '1,40p'
```

You say:

- “This is the hidden system prompt. It:
  - Locks us to POSIX AWK.
  - Enforces plan-first behavior.
  - Requires PLAN / CODE / TESTS / NOTES.
  - Requires a header at the top of every AWK script.”

Optionally show developer + project:

```sh
sed -n '1,40p' prompts/vawk.developer.md
sed -n '1,40p' prompts/vawk.project.md
```

You say:

- “Developer settings add style preferences.”
- “Project overview tells VAWK what the logs look like and what outputs we want.”

---

## 2. (Optional) Show RAG sources

You say:

- “VAKW has a small AWK library behind it: snippets and a short book. RAG pulls from those.”

In terminal:

```sh
ls docs/examples/snippets | head
ls learn.awk
ls learn.awk/*.md
```

You say:

- “These are the snippet patterns and the learn.awk mini-book that RAG can use as context.”

---

## 3. Run the chat: “How do I reformat pipe logs into CSV?”

You say:

- “Now I’m going to ask VAWK a very natural question about logs and let it do the rest.”

In terminal:

```sh
vawk chat --one-shot "How do I reformat pipe logs into CSV?"
```

Let it run.

You say:

- “Under the hood, VAWK just built a ChatClient call with:
  - System prompt,
  - Developer settings,
  - Project overview,
  - A small RAG context message with relevant snippets (like `pipes-to-csv`),
  - Then my question.”

If you have DEBUG logging wired, you can show a snippet of the built messages (optional for live; good for a recorded demo).

---

## 4. Show the answer structure (PLAN / CODE / TESTS / NOTES)

When VAWK prints the reply, scroll and highlight the structure.

You say:

- “Notice the answer is structured:
  - PLAN: explains the approach.
  - CODE: pure AWK with a header.
  - TESTS: how to prove it works.
  - NOTES: assumptions and limitations.”

You can use this as the “golden” target answer:

```text
PLAN:
- Restate the task:
  - The logs are pipe-separated with spaces around '|'.
  - We want clean CSV output.
- Input assumptions:
  - TIMESTAMP | LEVEL | USER | MESSAGE...
  - Optional header row and comment lines starting with '#'.
  - No '|' characters inside actual fields.
- Output requirements:
  - CSV with ',' as the separator.
  - A header row.
  - Columns: timestamp, level, user, message.
- Parsing approach:
  - Use FS = " *\| *" to handle spaces around '|'.
  - Use OFS = "," for CSV.
- Header/comments:
  - Print a CSV header in BEGIN.
  - Skip header (NR == 1) if it looks like a header.
  - Skip # comments and empty lines.
- Per-line logic:
  - timestamp = $1, level = $2, userId = $3.
  - Join $4..$NF as message.
  - print timestamp, level, userId, message.
- Constraints:
  - POSIX AWK only.
  - Single-pass streaming.

CODE:
```awk
# VAWK: pipe_logs_to_csv
# Purpose: Reformat pipe-separated log records into a simple CSV file.
# Intent: Make log files easy to import into spreadsheets or downstream tools without changing meaning.
# Input: Lines like '2025-12-07T21:15:03Z | INFO | user123 | Logged in', one per line; header and comment lines may be present.
# Output: CSV with a header row and rows containing timestamp, level, user, and message fields.

BEGIN {
    # Field separator: optional spaces, then '|' then optional spaces
    FS  = " *\| *"
    # Output field separator: CSV comma
    OFS = ","

    # Print CSV header
    print "timestamp","level","user","message"
}

# Skip pure comment lines and empty lines
/^#/ || NF == 0 {
    next
}

# Optionally skip an input header row if it looks like a header
NR == 1 && ($1 ~ /[Tt]ime/ || $2 ~ /[Ll]evel/ || $3 ~ /[Uu]ser/) {
    next
}

{
    # Expect at least 4 fields: timestamp | level | user | message...
    timestamp = $1
    level     = $2
    userId    = $3

    # Build message from remaining fields
    message = ""
    if (NF >= 4) {
        message = $4
        for (i = 5; i <= NF; i++) {
            message = message " " $i
        }
    }

    print timestamp, level, userId, message
}
```

TESTS:
- Sample input `pipe.log`:

  ```text
  # time | level | user | message
  2025-12-07T21:15:03Z | INFO  | user123 | Logged in
  2025-12-07T21:15:04Z | ERROR | user123 | Payment failed: card declined
  2025-12-07T21:15:05Z | WARN  | user456 | Slow response
  ```

- Command:

  ```sh
  awk -f pipe_logs_to_csv.awk pipe.log > pipe.csv
  ```

- Expected output:

  ```text
  timestamp,level,user,message
  2025-12-07T21:15:03Z,INFO,user123,Logged in
  2025-12-07T21:15:04Z,ERROR,user123,Payment failed: card declined
  2025-12-07T21:15:05Z,WARN,user456,Slow response
  ```

NOTES:
- Assumes no '|' inside fields.
- Does not implement full CSV quoting.
- Remains single-pass and streaming.
```

You do not need to read the whole answer in the demo. Just point to PLAN and the AWK header.

---

## 5. Save the AWK script and run it

You say:

- “Now I’m going to copy that CODE block into a script and run it on a sample file. This is the proof-of-work part.”

In terminal:

1. Create the input file:

```sh
cat > pipe.log << 'EOF'
# time | level | user | message
2025-12-07T21:15:03Z | INFO  | user123 | Logged in
2025-12-07T21:15:04Z | ERROR | user123 | Payment failed: card declined
2025-12-07T21:15:05Z | WARN  | user456 | Slow response
EOF
```

2. Create the AWK script:

```sh
nano pipe_logs_to_csv.awk    # or your editor of choice
```

Paste the AWK code with the header into this file.

3. Run the script:

```sh
awk -f pipe_logs_to_csv.awk pipe.log > pipe.csv
cat pipe.csv
```

You say:

- “This is the proof-of-work loop:
  - VAWK produces a plan, code, and tests.
  - We save the code.
  - We run the tests.
  - The CSV matches the expectation.”

---

## 6. Show the `.vawk` chat log (optional)

You say:

- “Behind the scenes, VAWK also logged the entire chat as structured NDJSON. This is where auditability lives.”

In terminal:

```sh
ls .vawk/chat
cat .vawk/chat/<sessionId>.vawk | sed -n '1,5p'
```

Explain:

- First line is `kind:"meta"`.
- Next lines are `kind:"turn"` with:
  - `idx`, `ts`, `role`, `msg`, etc.

You say:

- “This file is the transcript and proof-of-work for this conversation. We can feed it back into VAWK later, or just keep it as an audit log.”

---

## 7. Close the loop

You say:

- “We just:
  - Asked a natural question.
  - Let layered prompts and RAG bring in the right AWK knowledge.
  - Got a fully documented AWK script and tests.
  - Proved it by running the script.
  - And captured everything in `.vawk` for later.”
