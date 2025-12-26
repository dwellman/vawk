# employees.awk
# Input: pipe-separated fields with optional spaces:
#   id | Last, First | email | dept | hours
# Output: CSV:
#   id,First Last,email,dept,hours

BEGIN {
    FS  = "[[:space:]]*[|][[:space:]]*"  # pipe with optional spaces
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
