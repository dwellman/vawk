package com.vawk.chat;

import java.util.Locale;

public final class  IntentDetector {
    private IntentDetector() {
    }

    /**
     * Intent labels for chat turns to decide between explanation and structured code paths.
     */
    public enum ChatIntent {
        EXPLAIN,
        CODE,
        MIXED
    }

    /**
     * Classifies a user message into EXPLAIN, CODE, or MIXED using simple keyword heuristics.
     *
     * @param message raw user text
     * @return intent classification guiding structured vs plain behavior
     */
    public static ChatIntent detectIntent(String message) {
        if (message == null || message.isBlank()) {
            return ChatIntent.MIXED;
        }
        String m = message.toLowerCase(Locale.ROOT);

        // CODE markers
        if (m.contains("write awk")) return ChatIntent.CODE;
        if (m.contains("awk script")) return ChatIntent.CODE;
        if (m.contains("generate awk")) return ChatIntent.CODE;
        if (m.contains("show awk code")) return ChatIntent.CODE;
        if (m.contains("plan, code, tests, notes")) return ChatIntent.CODE;
        if (m.contains("plan/code/tests/notes")) return ChatIntent.CODE;
        if (m.startsWith("plan:") || m.startsWith("code:")) return ChatIntent.CODE;

        // EXPLAIN markers (only if no code markers hit)
        boolean explain = m.contains("explain") ||
                m.contains("what does") ||
                m.contains("what is") ||
                m.contains("how does this work") ||
                m.contains("how does") ||
                m.contains("help me understand");
        if (explain) {
            return ChatIntent.EXPLAIN;
        }

        return ChatIntent.MIXED;
    }
}
