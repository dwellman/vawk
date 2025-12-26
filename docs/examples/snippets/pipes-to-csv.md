# Snippet: pipes-to-csv

**Name**  
Convert pipe-separated data to CSV

**Use case**  
Use this when you have records separated by `|` with optional spaces around the delimiter, and you want to produce a clean CSV.

**When not to use**  
Do not use this if fields can contain embedded `|` characters or quotes that must be escaped according to full CSV rules. This snippet is for simple, clean data.

**Sample script**

```awk
# Convert pipe-separated fields with optional spaces to CSV.
BEGIN {
    FS  = " *\| *"
    OFS = ","
}
{
    print $1, $2, $3, $4, $5
}
```

**Version**  
2025-12-07

---
