# Snippet: log-summary-by-level

**Name**  
Summarize log lines by severity level

**Use case**  
Use this for logs where a specific field (for example, second field) is a level such as INFO, WARN, or ERROR, and you want a quick count per level plus a total.

**When not to use**  
Do not use this if logs are unstructured or the level is not in a fixed field; you may need to extract the level with a regex first.

**Sample script**

```awk
# Summarize logs by level assuming level is in field 2.
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

**Version**  
2025-12-07

---
