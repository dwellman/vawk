package com.vawk.util;

import com.vawk.ai.AiResponse;

/**
 * Parses AI responses into PLAN/CODE/TESTS/NOTES sections and provides helpers to strip code fences
 * and extract test lists. Used by validators and promotion flows to handle structured replies.
 */
public final class SectionParser {
    private SectionParser() {
    }

    /**
     * Splits a raw AI response into PLAN/CODE/TESTS/NOTES text.
     *
     * @param raw raw AI response text
     * @return AiResponse with parsed sections (empty strings when missing)
     */
    public static AiResponse parse(String raw) {
        if (raw == null) {
            return new AiResponse();
        }
        StringBuilder plan = new StringBuilder();
        StringBuilder code = new StringBuilder();
        StringBuilder tests = new StringBuilder();
        StringBuilder notes = new StringBuilder();

        Section current = Section.NONE;
        String[] lines = raw.split("\\R", -1);
        for (String line : lines) {
            String trimmed = line.trim();
            Section next = sectionForLine(trimmed);
            if (next != Section.NONE) {
                current = next;
                continue;
            }

            switch (current) {
                case PLAN -> appendLine(plan, line);
                case CODE -> appendLine(code, line);
                case TESTS -> appendLine(tests, line);
                case NOTES -> appendLine(notes, line);
                default -> {
                }
            }
        }

        return new AiResponse(
                plan.toString().trim(),
                stripCodeFences(code.toString().trim()),
                tests.toString().trim(),
                notes.toString().trim()
        );
    }

    /**
     * Converts a TESTS section into a simple list of test descriptions.
     */
    public static java.util.List<String> parseTestsList(String testsText) {
        java.util.List<String> list = new java.util.ArrayList<>();
        if (testsText == null) {
            return list;
        }
        String[] lines = testsText.split("\\R");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("-")) {
                trimmed = trimmed.substring(1).trim();
            }
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    private static void appendLine(StringBuilder builder, String line) {
        if (builder.length() > 0) {
            builder.append("\n");
        }
        builder.append(line);
    }

    private static Section sectionForLine(String trimmed) {
        if (trimmed.equalsIgnoreCase("PLAN:")) return Section.PLAN;
        if (trimmed.equalsIgnoreCase("CODE:")) return Section.CODE;
        if (trimmed.equalsIgnoreCase("TESTS:")) return Section.TESTS;
        if (trimmed.equalsIgnoreCase("NOTES:")) return Section.NOTES;
        return Section.NONE;
    }

    /**
     * Removes leading/trailing ``` fences and whitespace from a code block.
     */
    public static String stripCodeFences(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline >= 0 && lastFence > firstNewline) {
                String inner = trimmed.substring(firstNewline + 1, lastFence);
                return inner.trim();
            }
        }
        return trimmed;
    }

    private enum Section {
        PLAN,
        CODE,
        TESTS,
        NOTES,
        NONE
    }
}
