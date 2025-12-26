package com.vawk.ai;

import org.springframework.stereotype.Service;

/**
 * Deterministic stub AI client used when no real ChatClient/model is configured. Returns fixed
 * PLAN/CODE/TESTS/NOTES for the log summarizer PoW slice.
 */
@Service
public class LocalStubAiClient implements AiClient {

    @Override
    // Pattern: Trust UX
    // - Provides deterministic PLAN/CODE/TESTS/NOTES when real AI is unavailable.
    public AiResponse generate(String promptText, String systemPrompt) {
        // Deterministic stub for the log summarizer PoW slice.
        String plan = """
- Parse space-delimited log lines; ignore blanks and comment lines starting with #.
- Fields: $1 timestamp, $2 level (INFO/WARN/ERROR), $3 user id, $4+ message (unused for counts).
- Track total line count and per-level counts.
- When by_user is set (-v by_user=1), also track totals per user and per-level per user while preserving first-seen order.
- Output plain text lines: total_lines, INFO, WARN, ERROR, then optional user lines with key=value pairs.
""";
        String code = """
```awk
# VAWK: log_summary_by_level
# Purpose: Summarize log lines by level (INFO/WARN/ERROR).
# Intent: Provide a reusable pattern for quick log health checks without external commands.
# Input: TIMESTAMP LEVEL USER MESSAGE... (space-delimited), one log entry per line.
# Output: key=value lines for total_lines and per-level counts; optional per-user breakdown when by_user=1.
BEGIN {
    FS = " ";
    lineCount = 0;
    userIndex = 0;
}

/^[[:space:]]*#/ { next }      # skip comments
NF == 0 { next }               # skip blank lines
{
    lineCount++;
    level = $2;
    userId = $3;
    levelCounts[level]++;

    if (by_user) {
        if (!(userId in userSeen)) {
            userSeen[userId] = 1;
            userOrder[++userIndex] = userId;
        }
        userTotals[userId]++;
        userLevelCounts[userId, level]++;
    }
}

END {
    printf "total_lines=%d\\n", lineCount;
    printf "INFO=%d\\n", levelCounts["INFO"] + 0;
    printf "WARN=%d\\n", levelCounts["WARN"] + 0;
    printf "ERROR=%d\\n", levelCounts["ERROR"] + 0;

    if (by_user) {
        for (i = 1; i <= userIndex; i++) {
            uid = userOrder[i];
            printf "user=%s total=%d INFO=%d WARN=%d ERROR=%d\\n", uid, userTotals[uid] + 0, userLevelCounts[uid, "INFO"] + 0, userLevelCounts[uid, "WARN"] + 0, userLevelCounts[uid, "ERROR"] + 0;
        }
    }
}
```
""";
        String tests = """
- Run against tests/input_basic.log expecting tests/expect_basic.txt.
- Run with -v by_user=1 against tests/input_basic.log expecting tests/expect_basic_by_user.txt.
""";
        String notes = """
- Assumes fields are whitespace-separated with level at $2 and user id at $3.
- Keeps per-user output order as first seen in the log.
- Ignores comment lines; treat blank lines as no-op.
""";
        return new AiResponse(plan, code, tests, notes);
    }
}
