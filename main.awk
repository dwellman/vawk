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
    printf "total_lines=%d\n", lineCount;
    printf "INFO=%d\n", levelCounts["INFO"] + 0;
    printf "WARN=%d\n", levelCounts["WARN"] + 0;
    printf "ERROR=%d\n", levelCounts["ERROR"] + 0;

    if (by_user) {
        for (i = 1; i <= userIndex; i++) {
            uid = userOrder[i];
            printf "user=%s total=%d INFO=%d WARN=%d ERROR=%d\n", uid, userTotals[uid] + 0, userLevelCounts[uid, "INFO"] + 0, userLevelCounts[uid, "WARN"] + 0, userLevelCounts[uid, "ERROR"] + 0;
        }
    }
}