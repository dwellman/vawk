# Snippet: compare-two-files-tag

**Name**  
Mark lines based on which file they come from

**Use case**  
Use this when you want to compare two files and mark lines as belonging to file A or file B, for quick manual inspection.

**When not to use**  
Do not use this for semantic diffs; tools like diff are better. This snippet is for simple tagging, not full comparison.

**Sample script**

```awk
# Tag lines with file name; usage: awk -f compare-two-files-tag.awk fileA fileB
FNR == 1 {
    file = FILENAME
}
{
    print file ":", $0
}
```

**Version**  
2025-12-07

---
