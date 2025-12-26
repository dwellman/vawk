# Snippet: double-space-file

**Name**  
Double-space a file

**Use case**  
Use this when you want to insert a blank line between each line of text, often to make output more readable.

**When not to use**  
Do not use this if the file already uses blank lines meaningfully or if you care about preserving exact spacing.

**Sample script**

```awk
# Double-space a file by printing an extra blank line after each line.
{
    print
    print ""
}
```

**Version**  
2025-12-07

---
