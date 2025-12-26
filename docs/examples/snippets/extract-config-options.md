# Snippet: extract-config-options

**Name**  
Extract key=value options from config files

**Use case**  
Use this when you have simple config files where each line is `key=value` and you want to list keys and values or filter certain keys.

**When not to use**  
Do not use this for complex formats (INI sections, nested configs, JSON, YAML). It assumes flat key=value lines.

**Sample script**

```awk
# Print key and value from key=value lines, skipping comments and blanks.
/^[ 	]*#/ || NF == 0 { next }
{
    split($0, kv, "=")
    key = kv[1]
    val = kv[2]
    gsub(/^[ 	]+/, "", key)
    gsub(/[ 	]+$/, "", key)
    print key, val
}
```

**Version**  
2025-12-07

---
