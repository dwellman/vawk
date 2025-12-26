# Snippet: filter-matching-regex

**Name**  
Filter lines that match a regular expression

**Use case**  
Use this when you need to keep only lines that contain a string or pattern, such as all log lines that mention ERROR or a specific user id.

**When not to use**  
Do not use this snippet if the pattern is extremely complex or requires full regular expression features not supported by your AWK implementation. For multi-line or structured logs, consider more specialized tools.

**Sample script**

```awk
# Print only lines that contain the word ERROR.
/ERROR/ {
    print
}
```

**Version**  
2025-12-07

---
