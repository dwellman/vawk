# Snippet: log-summary-by-date

**Name**  
Summarize log lines by date

**Use case**  
Use this when your log lines start with a timestamp like `YYYY-MM-DDThh:mm:ss` and you want to count lines per date (day).

**When not to use**  
Avoid this if timestamps are in many different formats or the date is not confined to a single field. In those cases you may need more robust parsing.

**Sample script**

```awk
# Count log lines per date, assuming ISO timestamp in field 1.
{
    split($1, parts, "T")
    date = parts[1]
    counts[date]++
}
END {
    for (d in counts) {
        print d, counts[d]
    }
}
```

**Version**  
2025-12-07

---
