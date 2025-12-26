# Snippet: mail-merge-template

**Name**  
Simple mail-merge style text substitution

**Use case**  
Use this when you have a template file with placeholders like {{NAME}} and a data file with names and other fields, and you want personalized outputs.

**When not to use**  
Avoid this for complex templating or HTML emails; dedicated templating engines are safer. This snippet is for illustrative or small-scale tasks.

**Sample script**

```awk
# Simple mail-merge: substitute {{NAME}} in template with field 1 from data.
# Usage: awk -v name="Alice" -f mail-merge-template.awk template.txt
{
    line = $0
    gsub(/{{NAME}}/, name, line)
    print line
}
```

**Version**  
2025-12-07

---
