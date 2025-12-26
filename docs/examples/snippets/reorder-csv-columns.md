# Snippet: reorder-csv-columns

**Name**  
Reorder columns in a CSV-like file

**Use case**  
Use this when you want to change column order in a simple, comma-separated file without embedded commas or quotes.

**When not to use**  
Do not use this on full CSV with quoted fields and embedded commas; AWK’s simple field splitting will break those. Use a CSV parser instead.

**Sample script**

```awk
# Reorder CSV columns: print 3,1,2 and keep the rest.
BEGIN {
    FS  = ","
    OFS = ","
}
{
    print $3, $1, $2
}
```

**Version**  
2025-12-07

---
