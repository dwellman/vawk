# Snippet: average-column

**Name**  
Compute average of a numeric column

**Use case**  
Use this to find the average value of a numeric field, such as average latency or average score.

**When not to use**  
Do not use this when the distribution is skewed and you actually want median or percentiles. Also avoid when there can be many invalid values; validate inputs first.

**Sample script**

```awk
# Compute average of the third field.
NF > 0 {
    sum += $3
    count++
}
END {
    if (count > 0) {
        print "avg=" (sum / count)
    } else {
        print "avg=NaN"
    }
}
```

**Version**  
2025-12-07

---
