# Snippet: add-line-numbers

**Name**  
Add line numbers to each line

**Use case**  
Use this when you want to annotate each line with its line number, similar to `nl`, for debugging or quick indexing.

**When not to use**  
Do not use this if the file already has a leading numeric field that must remain unchanged, unless you print the line number separately.

**Sample script**

```awk
# Prefix each line with its line number.
{
    print NR, $0
}
```

**Version**  
2025-12-07

---
