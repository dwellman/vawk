package com.vawk.util;

import com.vawk.ai.AiResponse;

/**
 * Validates structured AI responses for required sections and AWK header rules. Ensures PLAN/CODE/
 * TESTS/NOTES are present and CODE contains the mandatory header labels.
 */
public final class SectionValidator {
    private SectionValidator() {
    }

    /**
     * Ensures an AI response contains PLAN/CODE/TESTS/NOTES and that CODE includes the required AWK
     * header block. Throws IllegalStateException when validation fails.
     */
    // Pattern: Verification
    // - Enforces the structured response contract and required AWK header labels.
    public static void requirePlanCodeTestsNotes(AiResponse response) {
        if (response == null) {
            throw new IllegalStateException("AI response missing");
        }
        if (isBlank(response.getPlan())) {
            throw new IllegalStateException("AI response missing PLAN section");
        }
        if (isBlank(response.getCode())) {
            throw new IllegalStateException("AI response missing CODE section");
        }
        requireAwkHeader(response.getCode());
        if (isBlank(response.getTests())) {
            throw new IllegalStateException("AI response missing TESTS section");
        }
        if (isBlank(response.getNotes())) {
            throw new IllegalStateException("AI response missing NOTES section");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // Pattern: Verification
    // - Rejects scripts missing header labels so generated code stays consistent and auditable.
    private static void requireAwkHeader(String codeWithFence) {
        String code = stripFence(codeWithFence);
        String[] lines = code.split("\\R");
        int checked = 0;
        boolean hasVawk = false;
        boolean hasPurpose = false;
        boolean hasIntent = false;
        boolean hasInput = false;
        boolean hasOutput = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("#!")) {
                continue;
            }
            if (!hasVawk && trimmed.startsWith("# VAWK:")) {
                hasVawk = true;
            }
            if (trimmed.startsWith("# Purpose:")) {
                hasPurpose = true;
            }
            if (trimmed.startsWith("# Intent:")) {
                hasIntent = true;
            }
            if (trimmed.startsWith("# Input:")) {
                hasInput = true;
            }
            if (trimmed.startsWith("# Output:")) {
                hasOutput = true;
            }
            checked++;
            if (checked >= 10) {
                break;
            }
        }
        if (!hasVawk || !hasPurpose || !hasIntent || !hasInput || !hasOutput) {
            throw new IllegalStateException("AWK header missing required labels (VAWK/Purpose/Intent/Input/Output)");
        }
    }

    private static String stripFence(String code) {
        String trimmed = code.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline >= 0 && lastFence > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastFence);
            }
        }
        return trimmed;
    }
}
