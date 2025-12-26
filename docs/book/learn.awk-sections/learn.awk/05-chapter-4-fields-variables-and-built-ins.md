## Chapter 4 – Fields, Variables, and Built‑ins

### Field separator (FS)

By default, AWK splits fields on **runs of whitespace**. You can change this via:

- `-F` flag on the command line.
- Setting `FS` in a `BEGIN` block.

Example: colon‑separated `/etc/passwd`:

```sh
awk -F':' '{ print $1 }' /etc/passwd
```

Equivalent:

```sh
awk 'BEGIN { FS=":" } { print $1 }' //etc/passwd
```

### Output field separator (OFS)

When you write:

```awk
print $1, $2, $3
```

AWK inserts `OFS` between fields. Default is a single space.

To produce CSV, you can set:

```awk
BEGIN {
    FS  = " *\| *"
    OFS = ","
}
```

Then:

```awk
{ print $1, $2, $3, $4, $5 }
```

will produce comma‑separated output.

### Record separator (RS and ORS)

- `RS` controls how input is split into records (default: newline).
- `ORS` is appended after each `print` (default: newline).

Most scripts use the defaults.

### Built‑in variables (partial list)

- `$0` – whole record (line).
- `$1`, `$2`, … – fields.
- `NF` – number of fields in the current record.
- `NR` – number of records read so far (global).
- `FNR` – record number in the current file.
- `FILENAME` – name of the current input file.
- `FS` – input field separator.
- `OFS` – output field separator.
- `RS` – input record separator.
- `ORS` – output record separator.

---
