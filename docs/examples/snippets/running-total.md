# Snippet: running-total

**Name**  
Compute a running total of a numeric field

**Use case**  
Use this to track cumulative sums over time, such as running balances or cumulative counts.

**When not to use**  
Do not use this when you only need the final total; in that case a simple sum-column is enough.

**Sample script**

```awk
# Print running total of values in field 2.
{
    total += $2
    print total, $0
}
```

**Version**  
2025-12-07

---
