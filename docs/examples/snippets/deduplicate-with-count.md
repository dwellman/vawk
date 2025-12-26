# Snippet: deduplicate-with-count

**Name**  
Count duplicates and print unique lines with frequency

**Use case**  
Use this to produce an unsorted `uniq -c` style output, counting how many times each distinct line appears.

**When not to use**  
Same caveats as plain deduplication: not ideal for extremely large sets of distinct lines.

**Sample script**

```awk
# Count occurrences of each distinct line and print line plus count.
{
    counts[$0]++
}
END {
    for (line in counts) {
        print counts[line], line
    }
}
```

**Version**  
2025-12-07

---
