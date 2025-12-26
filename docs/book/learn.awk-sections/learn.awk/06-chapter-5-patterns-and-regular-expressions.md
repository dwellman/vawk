## Chapter 5 – Patterns and Regular Expressions

AWK has regular expressions built in. They are used in patterns and string operations.

### Regex patterns

Pattern:

```awk
/ERROR/ { print }
```

matches any line containing the substring "ERROR".

With anchors:

```awk
/^ERROR/ { ... }   # line starts with ERROR
/ERROR$/ { ... }   # line ends with ERROR
```

### Matching operator `~` and `!~`

You can match a field against a regex:

```awk
$3 ~ /^user[0-9]+$/ { print $3 }   # third field matches "user123"
$2 !~ /^(INFO|WARN)$/ { print }   # exclude INFO and WARN lines
```

### Escaping

Inside regex literals `/.../`, backslashes must be escaped if you embed them in strings or double‑quoted contexts. When in doubt, test small examples.

---
