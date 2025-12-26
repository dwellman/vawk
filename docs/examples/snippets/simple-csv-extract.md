# Snippet: simple-csv-extract

**Name**  
Extract a column from a simple CSV

**Use case**  
Use this when you have simple comma-separated data without embedded commas or quotes and you want a specific column.

**When not to use**  
Do not use this on complex CSV with quoted fields; AWK’s basic splitting will mis-handle those cases.

**Sample script**

```awk
# Print the second column from a simple CSV file.
BEGIN {
    FS = ","
}
{
    print $2
}
```

**Version**  
2025-12-07

---
