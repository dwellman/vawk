# Programming AWK with VAWK – A Guide for Humans

> A practical mini‑book on AWK, written for people who want to understand the language  
> and then use VAWK or a similar assistant without feeling like the AI is doing magic.

![vawk.png](img/vawk_book.png)

---

## Table of Contents

- [Preface](#preface)
- [Chapter 1 – Why AWK Still Matters](#chapter-1--why-awk-still-matters)
- [Chapter 2 – Your First AWK Commands](#chapter-2--your-first-awk-commands)
- [Chapter 3 – The AWK Execution Model](#chapter-3--the-awk-execution-model)
- [Chapter 4 – Fields, Variables, and Built‑ins](#chapter-4--fields-variables-and-built-ins)
- [Chapter 5 – Patterns and Regular Expressions](#chapter-5--patterns-and-regular-expressions)
- [Chapter 6 – Control Flow and User Functions](#chapter-6--control-flow-and-user-functions)
- [Chapter 7 – Arrays and Aggregations](#chapter-7--arrays-and-aggregations)
- [Chapter 8 – Text Transformation and Reformatting](#chapter-8--text-transformation-and-reformatting)
- [Chapter 9 – Reporting and Log Summaries](#chapter-9--reporting-and-log-summaries)
- [Chapter 10 – Portability, POSIX, and gawk](#chapter-10--portability-posix-and-gawk)
- [Chapter 11 – Debugging and Troubleshooting](#chapter-11--debugging-and-troubleshooting)
- [Chapter 12 – Working with VAWK and Other Assistants](#chapter-12--working-with-vawk-and-other-assistants)
- [Appendix A – AWK Cheat Sheet](#appendix-a--awk-cheat-sheet)
- [Appendix B – Common Idioms](#appendix-b--common-idioms)
- [Appendix C – Exercises](#appendix-c--exercises)

---

## Preface

AWK is a small, focused language for scanning, filtering, and transforming text. It is old, simple, and unusually good at what it does. If you live in logs, CSV files, or line‑oriented text, AWK is a sharp tool that fits in your mental pocket.

This mini‑book has two goals:

1. Teach you how to read and write AWK by hand.
2. Prepare you to use tools like **VAWK** (an AWK “vibe coder”) without feeling dependent on them.

You should be able to:

- Look at an AWK script and understand what it is doing.
- Draft simple scripts on your own.
- Use an AI assistant as a collaborator, not as a mysterious black box.

The examples assume a Unix‑like shell, POSIX `awk`, and basic command‑line comfort.

---

## Chapter 1 – Why AWK Still Matters

Many modern tools can slice and transform data: Python, JavaScript, SQL, data‑frame libraries, and more. AWK remains relevant because:

- It is **always there** on Unix‑like systems.
- It starts instantly and works well in pipes.
- It was designed **specifically** for line‑oriented text.

Typical use cases:

- Reformatting logs.
- Calculating totals from ad‑hoc reports.
- Extracting fields from delimited text.
- Generating quick summaries or CSVs.

You can think of AWK as a programmable `grep + cut + paste + sed`, with a small language wrapped around that idea.

### AWK in one sentence

> AWK reads the input line by line, splits each line into fields, and runs **pattern { action }** rules on those lines.

If there is no pattern, the action runs on every line. If there is no action, AWK prints the matching lines.

Examples:

```sh
# Print every line of file.txt (awk defaults to { print $0 })
awk '{ print $0 }' file.txt

# Print only lines that contain the word ERROR
awk '/ERROR/ { print }' app.log

# Print just the second field of each line
awk '{ print $2 }' data.txt
```

---

## Chapter 2 – Your First AWK Commands

This chapter gets your hands on the keyboard quickly.

### Printing lines

`$0` is the whole line. The simplest useful AWK script is:

```sh
awk '{ print $0 }' file.txt
```

Because `{ print }` is the default action, you can write:

```sh
awk '{ print }' file.txt
awk '{ }' file.txt    # prints nothing
awk '1' file.txt      # prints every line (pattern that is always true)
```

The last form, `awk '1'`, works because a bare expression that evaluates to true triggers the default action `{ print }`.

### Printing specific fields

AWK splits each line into whitespace‑separated fields. `$1` is the first field, `$2` the second, and so on. `NF` is the number of fields.

Example input (`people.txt`):

```text
Alice 30 Developer
Bob   25 Designer
Cara  28 Manager
```

Commands:

```sh
# Print the first field (names)
awk '{ print $1 }' people.txt

# Print name and age
awk '{ print $1, $2 }' people.txt

# Print the last field (job title)
awk '{ print $NF }' people.txt
```

### BEGIN and END blocks

`BEGIN` and `END` are special patterns:

- `BEGIN { ... }` runs **before** any input is read.
- `END { ... }` runs **after** all input is processed.

Example: count lines:

```sh
awk 'BEGIN { count = 0 } { count++ } END { print count }' file.txt
```

You can write this more idiomatically as:

```sh
awk 'END { print NR }' file.txt
```

because `NR` is the built‑in line (record) counter.

---

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

## Chapter 4 – Fields, Variables, and Built‑ins

### Field separator (FS)

By default, AWK splits fields on **runs of whitespace**. You can change this via:

- `-F` flag on the command line.
- Setting `FS` in a `BEGIN` block.

Example: colon‑separated `/etc/passwd`:

```sh
awk -F':' '{ print $1 }' /etc/passwd
```

Equivalent:

```sh
awk 'BEGIN { FS=":" } { print $1 }' //etc/passwd
```

### Output field separator (OFS)

When you write:

```awk
print $1, $2, $3
```

AWK inserts `OFS` between fields. Default is a single space.

To produce CSV, you can set:

```awk
BEGIN {
    FS  = " *\| *"
    OFS = ","
}
```

Then:

```awk
{ print $1, $2, $3, $4, $5 }
```

will produce comma‑separated output.

### Record separator (RS and ORS)

- `RS` controls how input is split into records (default: newline).
- `ORS` is appended after each `print` (default: newline).

Most scripts use the defaults.

### Built‑in variables (partial list)

- `$0` – whole record (line).
- `$1`, `$2`, … – fields.
- `NF` – number of fields in the current record.
- `NR` – number of records read so far (global).
- `FNR` – record number in the current file.
- `FILENAME` – name of the current input file.
- `FS` – input field separator.
- `OFS` – output field separator.
- `RS` – input record separator.
- `ORS` – output record separator.

---

## Chapter 5 – Patterns and Regular Expressions

AWK has regular expressions built in. They are used in patterns and string operations.

### Regex patterns

Pattern:

```awk
/ERROR/ { print }
```

matches any line containing the substring "ERROR".

With anchors:

```awk
/^ERROR/ { ... }   # line starts with ERROR
/ERROR$/ { ... }   # line ends with ERROR
```

### Matching operator `~` and `!~`

You can match a field against a regex:

```awk
$3 ~ /^user[0-9]+$/ { print $3 }   # third field matches "user123"
$2 !~ /^(INFO|WARN)$/ { print }   # exclude INFO and WARN lines
```

### Escaping

Inside regex literals `/.../`, backslashes must be escaped if you embed them in strings or double‑quoted contexts. When in doubt, test small examples.

---

## Chapter 6 – Control Flow and User Functions

AWK’s control structures are similar to C and many scripting languages.

### if / else

```awk
{
    if ($3 > 100) {
        print "BIG", $0
    } else {
        print "SMALL", $0
    }
}
```

### while

```awk
i = 1
while (i <= NF) {
    print $i
    i++
}
```

### User‑defined functions

You can define functions at the top of the script:

```awk
function trim(s) {
    sub(/^ +/, "", s)
    sub(/ +$/, "", s)
    return s
}

{
    name = trim($1)
    print name
}
```

Conventions:

- Function parameters are passed by value.
- Arrays are effectively passed by reference.

---

## Chapter 7 – Arrays and Aggregations

AWK arrays are associative by default: keys are strings.

### Counting occurrences

Example: count log lines per level:

```awk
# Count occurrences of each log level
{
    level = $2
    counts[level]++
}

END {
    for (level in counts) {
        print level, counts[level]
    }
}
```

The iteration order over `counts` is unspecified; if order matters, you must manage it with an auxiliary array or post‑processing.

### Two‑dimensional arrays

You can simulate two‑dimensional arrays using concatenated keys:

```awk
key = user "|" level
counts[key]++
```

Later:

```awk
for (k in counts) {
    split(k, parts, "|")
    user  = parts[1]
    level = parts[2]
    print user, level, counts[k]
}
```

---

## Chapter 8 – Text Transformation and Reformatting

One of the most common AWK tasks is reformatting text from one layout to another. This is the classic “clean up a raw file into a clean CSV” job.

### Example: pipe‑separated to CSV

Input (`employees_raw.txt`):

```text
# id | name                 | email                     | dept | hours
001  | Smith, John          | john.smith@example.com    | ENG  | 40
002  | Doe, Jane            | jane.doe@example.com      | HR   | 32
003  | Brown, Alice         | alice.brown@example.com   | FIN  | 37.5
```

Desired output (`employees_clean.csv`):

```text
id,name,email,dept,hours
001,John Smith,john.smith@example.com,ENG,40
002,Jane Doe,jane.doe@example.com,HR,32
003,Alice Brown,alice.brown@example.com,FIN,37.5
```

Script (`employees_format.awk`):

```awk
# employees_format.awk
# Input: pipe-separated fields with optional spaces:
#   id | Last, First | email | dept | hours
# Output: CSV:
#   id,First Last,email,dept,hours

BEGIN {
    FS  = " *\| *"  # pipe with optional spaces
    OFS = ","
    print "id,name,email,dept,hours"
}

# Skip comments and empty lines
/^#/ || NF == 0 { next }

{
    id    = $1
    name  = $2
    email = $3
    dept  = $4
    hours = $5

    # name is "Last, First"
    split(name, parts, ",")
    last  = parts[1]
    first = parts[2]

    # Trim spaces around first and last
    sub(/^ +/, "", first)
    sub(/ +$/, "", first)
    sub(/^ +/, "", last)
    sub(/ +$/, "", last)

    fullname = first " " last

    print id, fullname, email, dept, hours
}
```

Run:

```sh
awk -f employees_format.awk employees_raw.txt > employees_clean.csv
```

This example shows:

- Using a regex field separator to handle spaces.
- Skipping comments and blank lines.
- Splitting and trimming strings.
- Reassembling fields into a different format.

---

## Chapter 9 – Reporting and Log Summaries

AWK is well‑suited to log analysis and summarization.

### Example: count lines per level

Input (`app.log`):

```text
2025-12-07T21:15:03Z INFO  user123 Logged in
2025-12-07T21:15:04Z ERROR user123 Payment failed
2025-12-07T21:15:05Z WARN  user456 Slow response
2025-12-07T21:15:06Z ERROR user123 Payment failed again
```

Task:

- Count total lines.
- Count how many of each level (`INFO`, `WARN`, `ERROR`).

Script:

```awk
# log_summary.awk
# Summarize log lines by level.

{
    total++
    level = $2
    counts[level]++
}

END {
    print "total_lines=" total
    for (level in counts) {
        print level "=" counts[level]
    }
}
```

Output (order of levels may vary):

```text
total_lines=4
INFO=1
WARN=1
ERROR=2
```

You can extend this to group by user and level by using composite keys as shown earlier.

---

## Chapter 10 – Portability, POSIX, and gawk

There are several AWK implementations:

- Original `awk`.
- `nawk` (new awk).
- `gawk` (GNU awk).
- Others embedded in toolchains.

To keep scripts portable:

- Stick to the **POSIX AWK** feature set:
  - Avoid gawk‑only extensions unless you control the runtime environment.
- Use simple regexes and built‑ins:
  - Do not rely on `gensub`, `asorti`, or `PROCINFO` in portable scripts.
- Test on a system with plain `/usr/bin/awk` if portability matters.

If your environment is guaranteed to have `gawk`, you can use additional features, but it is good practice to mark scripts that depend on them.

---

## Chapter 11 – Debugging and Troubleshooting

AWK does not have a built‑in debugger, but there are practical techniques you can use.

### Print debugging

Insert temporary `print` statements to inspect intermediate values:

```awk
{
    print "DEBUG line:", NR, "raw:", $0 > "/dev/stderr"
    # actual logic
}
```

Sending debug messages to standard error (`/dev/stderr`) keeps them separate from normal output.

### Check fields and NF

When parsing fails, inspect the number of fields:

```awk
{
    if (NF < 5) {
        print "WARN: short line at", NR, ":", $0 > "/dev/stderr"
        next
    }
    # normal processing
}
```

### Simplify

If a script misbehaves, reduce it to the simplest version that still shows the problem:

- Comment out non‑essential logic.
- Hard‑code values to verify branches.
- Test with a very small input file.

---

## Chapter 12 – Working with VAWK and Other Assistants

Tools like VAWK sit on top of the language you have just learned.

Think of the split like this:

- **AWK** is the engine: it runs the script deterministically.
- **VAWK** (or any assistant) helps you:
  - Draft the script.
  - Explain existing code.
  - Propose tests.
  - Refine behavior from a natural language description.

To use assistants effectively:

1. **Describe your data clearly.**
   - Show sample lines.
   - Explain which fields matter.
2. **Describe the desired output.**
   - Include an example output file when possible.
3. **Ask for a plan before code.**
   - A step‑by‑step plan in plain language is easier to review than raw code.
4. **Keep scripts small and explicit.**
   - Short scripts are easier to debug and safer to run.

The more you understand AWK itself, the easier it is to judge whether a generated script is safe, portable, and correct.

---

## Appendix A – AWK Cheat Sheet

### Basic command

```sh
awk 'pattern { action }' file
```

### Common patterns

```awk
/regex/        # lines matching regex
! /regex/      # lines not matching regex
NR == 1        # first line
NR > 1         # all but first line
NF == 0        # empty line
BEGIN { ... }  # before input
END   { ... }  # after input
```

### Common actions

```awk
{ print }          # print entire line
{ print $1, $3 }   # print fields 1 and 3
{ count++ }        # increment counter
{ sum += $2 }      # accumulate values
```

### Built‑in variables

- `$0` – whole line
- `$1`, `$2`, … – fields
- `NF` – number of fields
- `NR` – line number
- `FS` – input field separator
- `OFS` – output field separator

---

## Appendix B – Common Idioms

### Skip header line

```awk
NR == 1 { next }    # skip first line
{ ... }             # process remaining lines
```

### Skip comments and empty lines

```awk
/^#/ || NF == 0 { next }
{ ... }
```

### Sum a numeric field

```awk
{ sum += $3 }
END { print sum }
```

### Average of a field

```awk
{
    sum += $3
    count++
}
END {
    if (count > 0) {
        print sum / count
    }
}
```

### Replace text in a field

```awk
{
    gsub(/foo/, "bar", $3)
    print
}
```

---

## Appendix C – Exercises

These exercises are designed to build comfort with AWK. You can solve them by hand or with the help of a tool like VAWK.

1. **Column extractor**

   Input: a space‑separated file with three columns: name, city, and country.

   Task: print only `city,country` as CSV.

2. **Filter by threshold**

   Input: a file where the second field is a number.

   Task: print only lines where the second field is greater than 100.

3. **Normalize names**

   Input: names in the form `LAST,FIRST`.

   Task: produce `First Last` with initial capital letters.

4. **Summarize log levels**

   Input: logs with fields: timestamp, level, message.

   Task: print counts of each level and a total line.

5. **Group by key**

   Input: records with `user amount`.

   Task: sum `amount` per `user` and print `user total`.

As you work through these, try writing the PLAN in plain language first, then the AWK code. This mirrors the plan‑first style used by VAWK and helps you develop clear mental models of what your script is doing.
