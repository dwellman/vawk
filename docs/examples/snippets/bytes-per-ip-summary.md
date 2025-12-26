# Snippet: bytes-per-ip-summary

**Name**  
Summarize total bytes transferred per IP

**Use case**  
Use this when log format has IP in one field and bytes sent in another (for example, common web server logs) and you want totals per IP.

**When not to use**  
Do not use this if bytes field can contain "-" or non-numeric placeholders without handling them; filter or normalize first.

**Sample script**

```awk
# Sum bytes (field 10) per IP (field 1), adjust indices for your log format.
{
    ip    = $1
    bytes = $10
    if (bytes == "-" || bytes == "") next
    totals[ip] += bytes + 0
}
END {
    for (ip in totals) {
        print ip, totals[ip]
    }
}
```

**Version**  
2025-12-07

---
