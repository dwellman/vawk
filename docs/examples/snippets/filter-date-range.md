# Snippet: filter-date-range

**Name**  
Filter records by simple date range

**Use case**  
Use this when your first field is an ISO-like date (`YYYY-MM-DD` or `YYYY-MM-DDThh:mm:ss`) and you want only records between two dates, using lexical comparison.

**When not to use**  
Do not use this when dates are in non-lexical formats or when time zones must be handled precisely.

**Sample script**

```awk
# Keep lines whose date is between start and end (inclusive).
# Usage: awk -v start=2025-01-01 -v end=2025-12-31 -f filter-date-range.awk input
{
    split($1, parts, "T")
    date = parts[1]
    if (date >= start && date <= end) {
        print
    }
}
```

**Version**  
2025-12-07

---
