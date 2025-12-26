# Snippet: min-max-column

**Name**  
Track minimum and maximum of a numeric column

**Use case**  
Use this when you want quick min/max statistics on a numeric field such as minimum and maximum response times.

**When not to use**  
Avoid this if values are not numeric or may overflow typical numeric ranges. Also avoid if you need quantiles or full distributions; this only provides min and max.

**Sample script**

```awk
# Track min and max of the third field.
NF > 0 {
    value = $3 + 0
    if (NR == 1 || value < min) min = value
    if (NR == 1 || value > max) max = value
}
END {
    print "min=" min, "max=" max
}
```

**Version**  
2025-12-07

---
