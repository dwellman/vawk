# Snippet: tsv-group-summary

**Name**  
Summarize TSV data by group

**Use case**  
Use this when you have tab-separated output from a database or command and you want a sum per group, such as total sales per region.

**When not to use**  
Do not use this if tabs appear inside fields or if the file is not strictly tab-separated. Adjust FS accordingly.

**Sample script**

```awk
# Sum numeric field 3 per value in field 2 in a TSV file.
BEGIN {
    FS = "	"
}
{
    key = $2
    totals[key] += $3 + 0
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
