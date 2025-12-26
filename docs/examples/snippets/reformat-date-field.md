# Snippet: reformat-date-field

**Name**  
Reformat a date field

**Use case**  
Use this when you want to change date format from `YYYY-MM-DD` to `DD/MM/YYYY` or similar simple rearrangements where the date is in its own field.

**When not to use**  
Do not use this for complex date/time parsing (time zones, localized month names). Use more specialized tools or libraries for that.

**Sample script**

```awk
# Reformat date in field 1 from YYYY-MM-DD to DD/MM/YYYY.
{
    split($1, d, "-")
    newDate = d[3] "/" d[2] "/" d[1]
    $1 = newDate
    print
}
```

**Version**  
2025-12-07

---
