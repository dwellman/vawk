## Appendix B – Common Idioms

### Skip header line

```awk
NR == 1 { next }    # skip first line
{ ... }             # process remaining lines
```

### Skip comments and empty lines

```awk
/^#/ || NF == 0 { next }
{ ... }
```

### Sum a numeric field

```awk
{ sum += $3 }
END { print sum }
```

### Average of a field

```awk
{
    sum += $3
    count++
}
END {
    if (count > 0) {
        print sum / count
    }
}
```

### Replace text in a field

```awk
{
    gsub(/foo/, "bar", $3)
    print
}
```

---
