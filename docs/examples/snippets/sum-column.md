# Snippet: sum-column

**Name**  
Sum numeric values in a column

**Use case**  
Use this to compute the total of a numeric field, such as total bytes transferred, total hours worked, or total sales.

**When not to use**  
Do not use this if the field can contain non-numeric junk or headers without guarding against them. Filter out headers and invalid rows first.

**Sample script**

```awk
# Sum the values in the third field.
# Skips empty lines.
NF > 0 {
    sum += $3
}
END {
    print "total=" sum
}
```

**Version**  
2025-12-07

---
