## Chapter 10 – Portability, POSIX, and gawk

There are several AWK implementations:

- Original `awk`.
- `nawk` (new awk).
- `gawk` (GNU awk).
- Others embedded in toolchains.

To keep scripts portable:

- Stick to the **POSIX AWK** feature set:
  - Avoid gawk‑only extensions unless you control the runtime environment.
- Use simple regexes and built‑ins:
  - Do not rely on `gensub`, `asorti`, or `PROCINFO` in portable scripts.
- Test on a system with plain `/usr/bin/awk` if portability matters.

If your environment is guaranteed to have `gawk`, you can use additional features, but it is good practice to mark scripts that depend on them.

---
