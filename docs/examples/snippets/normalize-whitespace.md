# Snippet: normalize-whitespace

**Name**  
Collapse repeated spaces into a single space

**Use case**  
Use this when your data has irregular spacing and you want to normalize it, for example before splitting into fields or comparing lines.

**When not to use**  
Avoid this when multiple spaces are meaningful (for example, fixed-width columns). In that case, preserve spacing.

**Sample script**

```awk
# Replace runs of spaces with a single space.
{
    gsub(/ +/, " ", $0)
    print
}
```

**Version**  
2025-12-07

---
