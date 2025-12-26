# Snippet: unique-ip-from-logs

**Name**  
Extract unique IP addresses from log lines

**Use case**  
Use this when web or access logs have IP addresses in the first field and you want a list of unique IPs that appeared.

**When not to use**  
Do not use this if IPs appear in multiple places or if lines are not consistently formatted; you may need a more robust regex-based extraction.

**Sample script**

```awk
# Extract unique IP addresses from first field.
{
    ips[$1] = 1
}
END {
    for (ip in ips) {
        print ip
    }
}
```

**Version**  
2025-12-07

---
