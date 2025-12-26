# Snippet: kv-to-table

**Name**  
Convert key=value pairs to a simple table

**Use case**  
Use this when each line contains space-separated `key=value` pairs and you want to print selected keys in a consistent order.

**When not to use**  
Avoid this when values can contain spaces or `=` characters; this simple splitter will not handle that well.

**Sample script**

```awk
# Extract selected keys from key=value pairs.
{
    for (i = 1; i <= NF; i++) {
        split($i, kv, "=")
        key = kv[1]
        val = kv[2]
        data[key] = val
    }
    print data["user"], data["status"], data["bytes"]
    delete data
}
```

**Version**  
2025-12-07

---
