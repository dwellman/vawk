## Chapter 7 – Arrays and Aggregations

AWK arrays are associative by default: keys are strings.

### Counting occurrences

Example: count log lines per level:

```awk
# Count occurrences of each log level
{
    level = $2
    counts[level]++
}

END {
    for (level in counts) {
        print level, counts[level]
    }
}
```

The iteration order over `counts` is unspecified; if order matters, you must manage it with an auxiliary array or post‑processing.

### Two‑dimensional arrays

You can simulate two‑dimensional arrays using concatenated keys:

```awk
key = user "|" level
counts[key]++
```

Later:

```awk
for (k in counts) {
    split(k, parts, "|")
    user  = parts[1]
    level = parts[2]
    print user, level, counts[k]
}
```

---
