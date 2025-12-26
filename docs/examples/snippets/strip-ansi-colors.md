# Snippet: strip-ansi-colors

**Name**  
Strip ANSI color codes from logs

**Use case**  
Use this when your logs contain ANSI escape sequences for colors and you want plain text, such as when capturing output from colored CLI tools.

**When not to use**  
Do not use this when ANSI escapes are used for more than color and you need them preserved. Also avoid on binary data.

**Sample script**

```awk
# Remove common ANSI escape sequences.
{
    gsub(/\033\[[0-9;]*m/, "", $0)
    print
}
```

**Version**  
2025-12-07
