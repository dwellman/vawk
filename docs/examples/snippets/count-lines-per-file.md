# Snippet: count-lines-per-file

**Name**  
Count lines per input file

**Use case**  
Use this when processing multiple input files at once and you want to know how many lines each file contributed.

**When not to use**  
Avoid this if you are only interested in global counts; a simpler END block with NR may be enough.

**Sample script**

```awk
# Count lines per file when multiple files are provided.
{
    counts[FILENAME]++
}
END {
    for (f in counts) {
        print f, counts[f]
    }
}
```

**Version**  
2025-12-07

---
