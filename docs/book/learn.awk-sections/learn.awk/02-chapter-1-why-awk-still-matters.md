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
