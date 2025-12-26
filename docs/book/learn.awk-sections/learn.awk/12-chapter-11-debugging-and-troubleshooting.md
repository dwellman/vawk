## Chapter 11 – Debugging and Troubleshooting

AWK does not have a built‑in debugger, but there are practical techniques you can use.

### Print debugging

Insert temporary `print` statements to inspect intermediate values:

```awk
{
    print "DEBUG line:", NR, "raw:", $0 > "/dev/stderr"
    # actual logic
}
```

Sending debug messages to standard error (`/dev/stderr`) keeps them separate from normal output.

### Check fields and NF

When parsing fails, inspect the number of fields:

```awk
{
    if (NF < 5) {
        print "WARN: short line at", NR, ":", $0 > "/dev/stderr"
        next
    }
    # normal processing
}
```

### Simplify

If a script misbehaves, reduce it to the simplest version that still shows the problem:

- Comment out non‑essential logic.
- Hard‑code values to verify branches.
- Test with a very small input file.

---
