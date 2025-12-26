# Snippet: extract-sql-columns

**Name**  
Extract column names from a simple SQL CREATE TABLE

**Use case**  
Use this when you have a simple CREATE TABLE statement with one column per line and you want just the column names.

**When not to use**  
Do not use this on complex SQL with inline constraints, subqueries, or dialect-specific syntax. It is for straightforward schemas only.

**Sample script**

```awk
# Extract column names from lines like:   col_name TYPE,
# Usage: awk -f extract-sql-columns.awk schema.sql
/^[ 	]*[A-Za-z_][A-Za-z0-9_]*[ 	]+/ {
    gsub(/^[ 	]+/, "", $1)
    gsub(/,$/, "", $1)
    print $1
}
```

**Version**  
2025-12-07

---
