# Snippet: trim-fields

**Name**  
Trim leading and trailing spaces from each field

**Use case**  
Use this when your fields may have extra spaces around them and you want clean values, especially before comparisons or output to CSV.

**When not to use**  
Do not use this if leading or trailing spaces are significant (for example, in fixed-width formats) unless you are sure trimming is safe.

**Sample script**

```awk
# Trim spaces from all fields on each line.
{
    for (i = 1; i <= NF; i++) {
        gsub(/^ +/, "", $i)
        gsub(/ +$/, "", $i)
    }
    print
}
```

**Version**  
2025-12-07

---
