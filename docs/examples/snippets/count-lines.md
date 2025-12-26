# Snippet: count-lines

**Name**  
Count total lines in a file

**Use case**  
Use this to quickly count how many lines a file has without using wc, especially when you are already inside an AWK pipeline or want to later extend the logic.

**When not to use**  
There is no strong reason not to use this, but `wc -l` is simpler for pure line counting. Prefer this snippet when you plan to add more AWK logic.

**Sample script**

```awk
# Count total input lines and print the count at the end.
END {
    print NR
}
```

**Version**  
2025-12-07

---
