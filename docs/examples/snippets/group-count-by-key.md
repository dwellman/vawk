# Snippet: group-count-by-key

**Name**  
Count records per key

**Use case**  
Use this to compute a simple histogram of occurrences per key, such as number of log lines per user or per IP.

**When not to use**  
Avoid this when you only need global counts or when the number of distinct keys is too large to fit in memory.

**Sample script**

```awk
# Count lines per value in the first field.
{
    counts[$1]++
}
END {
    for (k in counts) {
        print k, counts[k]
    }
}
```

**Version**  
2025-12-07

---
