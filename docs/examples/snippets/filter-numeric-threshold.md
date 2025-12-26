# Snippet: filter-numeric-threshold

**Name**  
Filter rows by numeric threshold in a field

**Use case**  
Use this when you have numeric data in a known column and you want to keep only rows above or below a threshold, such as response times greater than 500 ms or scores above 80.

**When not to use**  
Do not use this if the field may contain non-numeric values or units (like "100ms") without adjusting the parsing. Strip units or validate the field first.

**Sample script**

```awk
# Keep lines where the third field is greater than 100.
$3 > 100 {
    print
}
```

**Version**  
2025-12-07

---
