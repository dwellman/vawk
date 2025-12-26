# Journal & Proof-of-Work Standard – BuJo for Agents

**BLUF:** `docs/journal/` is the proof-of-work trail for stories and significant changes. Every meaningful change should be logged with who/what/when/evidence/outcome in a tabular, prompt-friendly format. Audience: engineers and AI agents recording work.

## 1) Purpose
Define how BuJo-style journaling documents work so that past decisions and changes are easy to reconstruct and use as context for future work.

## 2) Location and naming
- Location: `docs/journal/`.
- Daily logs: `docs/journal/YYYY-MM-DD.md`.
- Epic/story logs: `docs/journal/epic-<name>.md` (e.g., `docs/journal/epic-vawk-chat-plan-first.md`).
- Daily = chronological work; epic = story-level summaries cross-referencing daily entries.

## 3) Document structure
- Title: `# YYYY-MM-DD – Work Log` or `# EPIC-<ID> – Journal`.
- BLUF: 2–4 sentences on what the file tracks and who it is for.
- Log table columns:
  ```
  When | Who | What | Evidence | Outcome
  -----|-----|------|----------|--------
  ```
  Each row is one entry.

## 4) Entry fields
- **When:** ISO timestamp, ideally UTC (e.g., `2025-12-08T07:24:31Z`).
- **Who:** stable actor name (`JavaAgent`, `ChatAgent`, `PM`, `DW`).
- **What:** verb-first summary (1–2 clauses), specific.
- **Evidence:** concrete references (tests, code paths, NDJSON entries, commands).
- **Outcome:** plain-language result; mention story/epic IDs where relevant.

## 5) Granularity and frequency
- Log every meaningful change (stories, commands, tests, golden-path verifications).
- Rule of thumb: if it merits a standup mention or commit message, log it.

## 6) Tags and Perfect Notes
- Optional tags in What/Outcome: `#Todo`, `#Note`, `#Personal`.
- “Perfect Notes” = short entries with essential facts/decisions/references only.

## 7) Relationship to planning rails
- Keep Today/Tomorrow/Someday lists separate (e.g., `docs/journal/planning.md`).
- When work moves between rails, note it in the journal entry (e.g., “Moved VAWK-PROJECT-AGENTS-01 from Someday to Today”).

## 8) Usage by agents
- At end of a meaningful run/story, append 1–3 rows to the appropriate journal file.
- Entries are outputs (facts/evidence), not commentary.

## 9) Docs, code, and journal together
- Docs: expectations/design (`docs/`).
- Code: implementation (`src/`).
- Journal: proof-of-work/history (`docs/journal/`).
- If any are missing or disagree, treat it as a gap to close via doc update, journal entry, or code change.
