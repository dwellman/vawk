package com.vawk.chat;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IntentDetectorTest {

    @Test
    void detectsMixedWhenNullOrBlank() {
        assertThat(IntentDetector.detectIntent(null)).isEqualTo(IntentDetector.ChatIntent.MIXED);
        assertThat(IntentDetector.detectIntent(" ")).isEqualTo(IntentDetector.ChatIntent.MIXED);
    }

    @Test
    void detectsCodeMarkers() {
        assertThat(IntentDetector.detectIntent("write awk script")).isEqualTo(IntentDetector.ChatIntent.CODE);
        assertThat(IntentDetector.detectIntent("use awk script to do X")).isEqualTo(IntentDetector.ChatIntent.CODE);
        assertThat(IntentDetector.detectIntent("generate awk for logs")).isEqualTo(IntentDetector.ChatIntent.CODE);
        assertThat(IntentDetector.detectIntent("show awk code for this")).isEqualTo(IntentDetector.ChatIntent.CODE);
        assertThat(IntentDetector.detectIntent("plan, code, tests, notes")).isEqualTo(IntentDetector.ChatIntent.CODE);
        assertThat(IntentDetector.detectIntent("plan/code/tests/notes")).isEqualTo(IntentDetector.ChatIntent.CODE);
        assertThat(IntentDetector.detectIntent("PLAN: do it")).isEqualTo(IntentDetector.ChatIntent.CODE);
        assertThat(IntentDetector.detectIntent("CODE: do it")).isEqualTo(IntentDetector.ChatIntent.CODE);
    }

    @Test
    void detectsExplainMarkers() {
        assertThat(IntentDetector.detectIntent("explain this")).isEqualTo(IntentDetector.ChatIntent.EXPLAIN);
        assertThat(IntentDetector.detectIntent("what does this do")) .isEqualTo(IntentDetector.ChatIntent.EXPLAIN);
        assertThat(IntentDetector.detectIntent("what is awk")) .isEqualTo(IntentDetector.ChatIntent.EXPLAIN);
        assertThat(IntentDetector.detectIntent("how does this work")) .isEqualTo(IntentDetector.ChatIntent.EXPLAIN);
        assertThat(IntentDetector.detectIntent("help me understand")) .isEqualTo(IntentDetector.ChatIntent.EXPLAIN);
    }

    @Test
    void detectsMixedWhenNoMarkers() {
        assertThat(IntentDetector.detectIntent("show me data")) .isEqualTo(IntentDetector.ChatIntent.MIXED);
    }
}
