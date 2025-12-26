# Snippet: word-frequency

**Name**  
Compute word frequency across text

**Use case**  
Use this to build a simple frequency table of words in a text file, for exploratory analysis or quick checks.

**When not to use**  
Do not use this when text is huge or you need advanced tokenization (stemming, Unicode handling). This is a simple ASCII-oriented snippet.

**Sample script**

```awk
# Count how often each word appears (naive split on whitespace).
{
    for (i = 1; i <= NF; i++) {
        word = $i
        gsub(/[^A-Za-z0-9_]/, "", word)
        if (word != "") {
            freq[word]++
        }
    }
}
END {
    for (w in freq) {
        print w, freq[w]
    }
}
```

**Version**  
2025-12-07

---
