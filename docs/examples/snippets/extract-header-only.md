# Snippet: extract-header-only

**Name**  
Extract only the header row

**Use case**  
Use this to quickly copy or inspect the first line of a file, commonly used for CSV headers.

**When not to use**  
Do not use this when the file has multiple header sections or when the header is not on the first line.

**Sample script**

```awk
# Print only the first line (header) and then stop.
NR == 1 {
    print
    exit
}
```

**Version**  
2025-12-07

---
