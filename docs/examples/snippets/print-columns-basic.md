# Snippet: print-columns-basic

**Name**  
Print specific columns from a whitespace-separated file

**Use case**  
Use this when you have a simple whitespace-separated table (for example, space or tab separated) and you want to select and reorder a few columns. This is the classic “pull out fields 1 and 3” job.

**When not to use**  
Do not use this if the data uses quoted fields with embedded spaces (full CSV) or if the separator is more complex than simple runs of whitespace. In those cases, set `FS` explicitly or use a CSV-aware tool.

**Sample script**

```awk
# Print the first and third fields from a whitespace-separated file.
# Usage: awk -f print-columns-basic.awk input.txt
{
    print $1, $3
}
```

**Version**  
2025-12-07

---
