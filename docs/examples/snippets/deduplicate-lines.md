# Snippet: deduplicate-lines

**Name**  
Remove duplicate lines (keep first occurrence)

**Use case**  
Use this when you want to remove exact duplicate lines from a file, similar to `uniq` but without requiring sorted input.

**When not to use**  
Avoid this if the file is huge and may contain many distinct lines; the `seen` array could consume significant memory. For massive data, consider external tools.

**Sample script**

```awk
# Print each unique line once, preserving first occurrence order.
!seen[$0]++ {
    print
}
```

**Version**  
2025-12-07

---
