# Snippet: split-file-per-key

**Name**  
Split input into one file per key

**Use case**  
Use this when you want to split a file into multiple files based on a key, such as one file per user or department.

**When not to use**  
Avoid this when the number of distinct keys is very large; you may end up creating thousands of files. Also avoid on systems with strict file limits.

**Sample script**

```awk
# Write each line into a file named by the first field.
{
    key = $1
    filename = key ".txt"
    print $0 >> filename
}
END {
    # Close all files (portable style)
    for (k in filename) {
        close(k)
    }
}
```

**Version**  
2025-12-07

---
