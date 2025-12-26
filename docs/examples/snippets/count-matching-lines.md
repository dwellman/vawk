# Snippet: count-matching-lines

**Name**  
Count lines matching a condition

**Use case**  
Use this when you need to know how many lines meet a condition, like how many ERROR logs there are or how many rows exceed a threshold.

**When not to use**  
Do not use this if you only need the lines themselves; in that case you can just filter and use `wc -l` externally. This snippet is better when you want the count inside the AWK script.

**Sample script**

```awk
# Count how many lines contain ERROR.
/ERROR/ {
    count++
}
END {
    print "error_lines=" count
}
```

**Version**  
2025-12-07

---
