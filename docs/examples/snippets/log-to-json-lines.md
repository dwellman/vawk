# Snippet: log-to-json-lines

**Name**  
Convert simple logs to JSON lines

**Use case**  
Use this when your log has a fixed number of whitespace-separated fields and you want one JSON object per line for light-weight ingestion.

**When not to use**  
Do not use this for complex nested JSON or when field values may contain quotes that need proper escaping.

**Sample script**

```awk
# Convert three-field logs to JSON lines with keys a,b,c.
{
    printf("{"a":"%s","b":"%s","c":"%s"}
", $1, $2, $3)
}
```

**Version**  
2025-12-07

---
