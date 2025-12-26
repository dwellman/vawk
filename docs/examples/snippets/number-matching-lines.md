# Snippet: number-matching-lines

**Name**  
Highlight and number only matching lines

**Use case**  
Use this when you are searching for lines that match a pattern and want to see both the line number and the matching content.

**When not to use**  
Do not use this when you need the entire file with line numbers; in that case, use add-line-numbers and filter separately.

**Sample script**

```awk
# Print line number and line for those containing ERROR.
/ERROR/ {
    print NR ":" $0
}
```

**Version**  
2025-12-07

---
