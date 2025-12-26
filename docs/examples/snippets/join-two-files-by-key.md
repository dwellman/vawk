# Snippet: join-two-files-by-key

**Name**  
Approximate inner join of two files by key

**Use case**  
Use this when you have two files keyed on the same field (for example, user id in column 1) and you want to combine their data.

**When not to use**  
Do not use this for very large datasets where one file cannot fit in memory; AWK holds the key map in RAM.

**Sample script**

```awk
# Join file1 and file2 on key in field 1.
# Usage: awk -f join-two-files-by-key.awk file1 file2
FNR==NR {
    # First file: store rest of fields by key
    key = $1
    data[key] = $0
    next
}
{
    key = $1
    if (key in data) {
        print data[key], $0
    }
}
```

**Version**  
2025-12-07

---
