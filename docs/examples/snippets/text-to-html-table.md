# Snippet: text-to-html-table

**Name**  
Convert simple whitespace table to basic HTML table

**Use case**  
Use this when you have a whitespace-separated table and want a quick HTML `<table>` representation for documentation or quick viewing.

**When not to use**  
Do not use this if fields contain HTML special characters that must be escaped; this snippet does not escape them.

**Sample script**

```awk
# Convert whitespace-separated rows to a minimal HTML table.
BEGIN {
    print "<table>"
}
{
    printf "  <tr>"
    for (i = 1; i <= NF; i++) {
        printf "<td>%s</td>", $i
    }
    print "</tr>"
}
END {
    print "</table>"
}
```

**Version**  
2025-12-07

---
