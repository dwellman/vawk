## Appendix A – AWK Cheat Sheet

### Basic command

```sh
awk 'pattern { action }' file
```

### Common patterns

```awk
/regex/        # lines matching regex
! /regex/      # lines not matching regex
NR == 1        # first line
NR > 1         # all but first line
NF == 0        # empty line
BEGIN { ... }  # before input
END   { ... }  # after input
```

### Common actions

```awk
{ print }          # print entire line
{ print $1, $3 }   # print fields 1 and 3
{ count++ }        # increment counter
{ sum += $2 }      # accumulate values
```

### Built‑in variables

- `$0` – whole line
- `$1`, `$2`, … – fields
- `NF` – number of fields
- `NR` – line number
- `FS` – input field separator
- `OFS` – output field separator

---
