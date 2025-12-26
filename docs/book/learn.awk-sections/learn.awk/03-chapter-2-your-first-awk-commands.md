## Chapter 2 – Your First AWK Commands

This chapter gets your hands on the keyboard quickly.

### Printing lines

`$0` is the whole line. The simplest useful AWK script is:

```sh
awk '{ print $0 }' file.txt
```

Because `{ print }` is the default action, you can write:

```sh
awk '{ print }' file.txt
awk '{ }' file.txt    # prints nothing
awk '1' file.txt      # prints every line (pattern that is always true)
```

The last form, `awk '1'`, works because a bare expression that evaluates to true triggers the default action `{ print }`.

### Printing specific fields

AWK splits each line into whitespace‑separated fields. `$1` is the first field, `$2` the second, and so on. `NF` is the number of fields.

Example input (`people.txt`):

```text
Alice 30 Developer
Bob   25 Designer
Cara  28 Manager
```

Commands:

```sh
# Print the first field (names)
awk '{ print $1 }' people.txt

# Print name and age
awk '{ print $1, $2 }' people.txt

# Print the last field (job title)
awk '{ print $NF }' people.txt
```

### BEGIN and END blocks

`BEGIN` and `END` are special patterns:

- `BEGIN { ... }` runs **before** any input is read.
- `END { ... }` runs **after** all input is processed.

Example: count lines:

```sh
awk 'BEGIN { count = 0 } { count++ } END { print count }' file.txt
```

You can write this more idiomatically as:

```sh
awk 'END { print NR }' file.txt
```

because `NR` is the built‑in line (record) counter.

---
