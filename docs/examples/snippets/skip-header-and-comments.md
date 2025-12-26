# Snippet: skip-header-and-comments

**Name**  
Skip header row and comment lines

**Use case**  
Use this when your input file has a single header row followed by data lines, and you also need to ignore any comment lines beginning with `#`. Common in CSV or delimited reports that add a header and optional comments.

**When not to use**  
Do not use this pattern if the file has multiple header rows or section headers scattered throughout, or if comment lines use a different marker without adjusting the regex.

**Sample script**

```awk
# Skip the first line (header) and any comment or empty lines.
NR == 1 { next }           # skip header row
/^#/ || NF == 0 { next }   # skip comments and empty lines

# Process remaining data lines
{
    # example: print first and second fields
    print $1, $2
}
```

**Version**  
2025-12-07

---
