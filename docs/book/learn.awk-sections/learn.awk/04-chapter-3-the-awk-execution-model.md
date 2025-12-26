## Chapter 3 – The AWK Execution Model

Understanding the execution model will keep AWK scripts predictable and easier to debug.

### Records, fields, and the main loop

Conceptually, AWK does:

1. Initialize variables and run all `BEGIN` blocks.
2. For each input record (line):
   - Set `$0` to the entire line.
   - Split `$0` into fields `$1`, `$2`, …, `$NF` using the field separator `FS`.
   - For each rule `pattern { action }`, check the pattern. If it is true, run the action.
3. After the last line, run all `END` blocks.

You do not see this loop; it is built into AWK.

### Patterns

A pattern can be:

- A regular expression literal: `/ERROR/`
- A boolean expression: `$3 > 100`, `NR == 1`, `NF == 0`
- A range pattern: `pattern1, pattern2`
- Special tokens: `BEGIN`, `END`

If the pattern is omitted, it defaults to “always true”.

Examples:

```awk
# Lines containing ERROR
/ERROR/ { print }

# First line only (header)
NR == 1 { print }

# Empty lines
NF == 0 { print "EMPTY LINE" }

# Lines between two markers (inclusive)
/^START/, /^END/ { print }
```

### Actions

An action is a block in braces `{ ... }` containing AWK statements:

- Assignments (`x = 5`)
- `if`, `while`, `for`
- `print`, `printf`
- Function calls

If the action is omitted, AWK prints the entire line (`$0`).

---
