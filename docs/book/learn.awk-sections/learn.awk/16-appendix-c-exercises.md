## Appendix C – Exercises

These exercises are designed to build comfort with AWK. You can solve them by hand or with the help of a tool like VAWK.

1. **Column extractor**

   Input: a space‑separated file with three columns: name, city, and country.

   Task: print only `city,country` as CSV.

2. **Filter by threshold**

   Input: a file where the second field is a number.

   Task: print only lines where the second field is greater than 100.

3. **Normalize names**

   Input: names in the form `LAST,FIRST`.

   Task: produce `First Last` with initial capital letters.

4. **Summarize log levels**

   Input: logs with fields: timestamp, level, message.

   Task: print counts of each level and a total line.

5. **Group by key**

   Input: records with `user amount`.

   Task: sum `amount` per `user` and print `user total`.

As you work through these, try writing the PLAN in plain language first, then the AWK code. This mirrors the plan‑first style used by VAWK and helps you develop clear mental models of what your script is doing.
