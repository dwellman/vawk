# Snippet: filter-long-lines

**Name**  
Filter lines longer than a certain length

**Use case**  
Use this when you want to find or isolate unusually long lines, which may indicate corrupted records or unbounded fields.

**When not to use**  
Do not use this when line length is not meaningful (for example, minified JSON); you may get many false positives.

**Sample script**

```awk
# Print only lines where length is greater than 120 characters.
length($0) > 120 {
    print
}
```

**Version**  
2025-12-07

---
