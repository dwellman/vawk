# Snippet: validate-numeric-range

**Name**  
Validate numeric field is within an allowed range

**Use case**  
Use this when you need to ensure that a numeric value (for example, age, percentage, score) stays within given bounds and log violations.

**When not to use**  
Do not use this when the field can be non-numeric text; add extra checks or cleaning first.

**Sample script**

```awk
# Check that field 3 is between 0 and 100 inclusive.
{
    value = $3 + 0
    if (value < 0 || value > 100) {
        print "WARN: out-of-range value at line " NR ": " value > "/dev/stderr"
    }
    print
}
```

**Version**  
2025-12-07

---
