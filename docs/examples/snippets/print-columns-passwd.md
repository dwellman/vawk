# Snippet: print-columns-passwd

**Name**  
Extract usernames from /etc/passwd-style files

**Use case**  
Use this snippet to extract the username (first field) or other colon-separated fields from system files like `/etc/passwd` or similar colon-delimited records.

**When not to use**  
Do not use this on files where the colon appears inside fields in a meaningful way, or where the format is not strictly colon-delimited. Also avoid if fields can be missing in the middle; adjust field indices if the schema differs.

**Sample script**

```awk
# Print the first field (username) from a colon-separated file.
/^[^#]/ {
    FS=":"
    print $1
}
```

**Version**  
2025-12-07

---
