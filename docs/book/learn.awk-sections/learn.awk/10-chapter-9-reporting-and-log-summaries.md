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
