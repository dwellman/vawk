# Snippet: top-n-by-column

**Name**  
Track top N rows by numeric column (single pass)

**Use case**  
Use this when you want to keep the top N records by a numeric field without sorting externally, for small N.

**When not to use**  
Do not use this for large N or huge files; full sorting tools may be simpler and more efficient.

**Sample script**

```awk
# Track top 5 lines by value in field 2.
# Simple O(N * 5) insertion; fine for small N.
{
    val = $2 + 0
    line = $0

    # Insert into arrays value[1..5], text[1..5]
    for (i = 1; i <= 5; i++) {
        if (val > value[i]) {
            for (j = 5; j > i; j--) {
                value[j] = value[j-1]
                text[j]  = text[j-1]
            }
            value[i] = val
            text[i]  = line
            break
        }
    }
}
END {
    for (i = 1; i <= 5; i++) {
        if (text[i] != "") {
            print text[i]
        }
    }
}
```

**Version**  
2025-12-07

---
