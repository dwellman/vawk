# Snippet: fixed-width-report

**Name**  
Format a simple fixed-width text report

**Use case**  
Use this when you want nicely aligned columns for human-readable reports in the terminal, using printf for formatting.

**When not to use**  
Do not use this when the output must be machine-parseable; fixed-width formatting can be fragile and whitespace-sensitive.

**Sample script**

```awk
# Print two columns with fixed widths.
BEGIN {
    fmt = "%-20s %10s
"
    printf fmt, "Name", "Amount"
    printf fmt, "--------------------", "----------"
}
{
    printf fmt, $1, $2
}
```

**Version**  
2025-12-07

---
