# Snippet: convert-crlf-to-lf

**Name**  
Convert Windows CRLF line endings to Unix LF

**Use case**  
Use this when you have files with `
` line endings (from Windows) and you want to strip the carriage return.

**When not to use**  
Do not use this when the `
` character is meaningful content (rare). For binary files, do not use this snippet.

**Sample script**

```awk
# Strip trailing carriage return characters.
{
    sub(/
$/, "", $0)
    print
}
```

**Version**  
2025-12-07

---
