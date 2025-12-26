## Chapter 6 – Control Flow and User Functions

AWK’s control structures are similar to C and many scripting languages.

### if / else

```awk
{
    if ($3 > 100) {
        print "BIG", $0
    } else {
        print "SMALL", $0
    }
}
```

### while

```awk
i = 1
while (i <= NF) {
    print $i
    i++
}
```

### User‑defined functions

You can define functions at the top of the script:

```awk
function trim(s) {
    sub(/^ +/, "", s)
    sub(/ +$/, "", s)
    return s
}

{
    name = trim($1)
    print name
}
```

Conventions:

- Function parameters are passed by value.
- Arrays are effectively passed by reference.

---
