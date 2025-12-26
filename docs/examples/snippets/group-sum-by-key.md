# Snippet: group-sum-by-key

**Name**  
Sum numeric values grouped by key

**Use case**  
Use this when you need totals per group, such as total sales per user, total bytes per host, or total hours per project.

**When not to use**  
Do not use this on extremely large cardinality keys (millions of distinct keys) unless you are sure you have enough memory. Consider other tools for very large group-by tasks.

**Sample script**

```awk
# Sum the third field per key in the first field.
{
    key = $1
    value = $3 + 0
    totals[key] += value
}
END {
    for (key in totals) {
        print key, totals[key]
    }
}
```

**Version**  
2025-12-07

---
