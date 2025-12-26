# Snippet: validate-field-count

**Name**  
Validate that each record has the expected number of fields

**Use case**  
Use this to catch malformed lines in delimited files, such as CSV or pipe-separated records that lost a field.

**When not to use**  
Do not use this if the format allows variable numbers of fields; instead, adjust the validation to the specific rules.

**Sample script**

```awk
# Expect exactly 5 fields per record; warn on others.
NF != 5 {
    print "WARN: bad field count at line " NR ": " $0 > "/dev/stderr"
    next
}
{
    # process good lines here
    print
}
```

**Version**  
2025-12-07

---
