## Chapter 8 – Text Transformation and Reformatting

One of the most common AWK tasks is reformatting text from one layout to another. This is the classic “clean up a raw file into a clean CSV” job.

### Example: pipe‑separated to CSV

Input (`employees_raw.txt`):

```text
# id | name                 | email                     | dept | hours
001  | Smith, John          | john.smith@example.com    | ENG  | 40
002  | Doe, Jane            | jane.doe@example.com      | HR   | 32
003  | Brown, Alice         | alice.brown@example.com   | FIN  | 37.5
```

Desired output (`employees_clean.csv`):

```text
id,name,email,dept,hours
001,John Smith,john.smith@example.com,ENG,40
002,Jane Doe,jane.doe@example.com,HR,32
003,Alice Brown,alice.brown@example.com,FIN,37.5
```

Script (`employees_format.awk`):

```awk
# employees_format.awk
# Input: pipe-separated fields with optional spaces:
#   id | Last, First | email | dept | hours
# Output: CSV:
#   id,First Last,email,dept,hours

BEGIN {
    FS  = " *\| *"  # pipe with optional spaces
    OFS = ","
    print "id,name,email,dept,hours"
}

# Skip comments and empty lines
/^#/ || NF == 0 { next }

{
    id    = $1
    name  = $2
    email = $3
    dept  = $4
    hours = $5

    # name is "Last, First"
    split(name, parts, ",")
    last  = parts[1]
    first = parts[2]

    # Trim spaces around first and last
    sub(/^ +/, "", first)
    sub(/ +$/, "", first)
    sub(/^ +/, "", last)
    sub(/ +$/, "", last)

    fullname = first " " last

    print id, fullname, email, dept, hours
}
```

Run:

```sh
awk -f employees_format.awk employees_raw.txt > employees_clean.csv
```

This example shows:

- Using a regex field separator to handle spaces.
- Skipping comments and blank lines.
- Splitting and trimming strings.
- Reassembling fields into a different format.

---
