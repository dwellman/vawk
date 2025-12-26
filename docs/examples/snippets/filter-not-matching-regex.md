# Snippet: filter-not-matching-regex

**Name**  
Filter out lines that match a regular expression

**Use case**  
Use this snippet when you want to exclude lines matching a pattern, for example to remove DEBUG noise from a log while keeping INFO and ERROR.

**When not to use**  
Do not use this if the pattern is too generic and would remove important lines. In that case, refine the regex or add additional conditions (like checking specific fields).

**Sample script**

```awk
# Drop lines that contain DEBUG; print everything else.
/DEBUG/ {
    next
}
{
    print
}
```

**Version**  
2025-12-07

---
