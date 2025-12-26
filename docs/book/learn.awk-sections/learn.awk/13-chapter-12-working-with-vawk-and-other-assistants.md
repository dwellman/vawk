## Chapter 12 – Working with VAWK and Other Assistants

Tools like VAWK sit on top of the language you have just learned.

Think of the split like this:

- **AWK** is the engine: it runs the script deterministically.
- **VAWK** (or any assistant) helps you:
  - Draft the script.
  - Explain existing code.
  - Propose tests.
  - Refine behavior from a natural language description.

To use assistants effectively:

1. **Describe your data clearly.**
   - Show sample lines.
   - Explain which fields matter.
2. **Describe the desired output.**
   - Include an example output file when possible.
3. **Ask for a plan before code.**
   - A step‑by‑step plan in plain language is easier to review than raw code.
4. **Keep scripts small and explicit.**
   - Short scripts are easier to debug and safer to run.

The more you understand AWK itself, the easier it is to judge whether a generated script is safe, portable, and correct.

---
