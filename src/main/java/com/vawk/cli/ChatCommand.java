package com.vawk.cli;

import com.vawk.chat.IntentDetector;
import com.vawk.chat.VawkChatService;
import com.vawk.chat.VawkChatSession;
import com.vawk.runtime.DirectoryService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Callable;

@Component
@Command(name = "chat", description = "Start or resume a VAWK chat session")
public class ChatCommand implements Callable<Integer> {

    @Option(names = "--session", description = "Existing session id")
    String sessionId;

    @Option(names = "--title", description = "Optional session title")
    String title;

    @Option(names = "--one-shot", description = "Single message, non-interactive")
    String oneShot;

    private final VawkChatService chatService;
    private final DirectoryService directoryService;

    /**
     * Entry point for chat CLI handling both interactive and one-shot flows. Decides intent per
     * turn (EXPLAIN vs CODE/MIXED), applies structured plan-first enforcement for code turns,
     * and logs all conversation turns to the NDJSON session file.
     */
    public ChatCommand(VawkChatService chatService, DirectoryService directoryService) {
        this.chatService = chatService;
        this.directoryService = directoryService;
    }

    @Override
    public Integer call() throws Exception {
        directoryService.ensureBaseDirs();
        VawkChatService.SessionData sessionData;
        if (sessionId == null || sessionId.isBlank()) {
            VawkChatSession session = chatService.createSession(title);
            sessionData = new VawkChatService.SessionData(session, List.of(), 1);
            System.out.println("[vawk] New chat session: " + session.getSessionId());
            sessionId = session.getSessionId();
        } else {
            sessionData = chatService.loadSession(sessionId);
            System.out.println("[vawk] Resuming chat session: " + sessionId);
        }

        if (oneShot != null) {
            return handleOneShot(sessionData, oneShot);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("[vawk] Type your message, or :q to quit.");
        int nextIdx = sessionData.nextIdx();
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null || ":q".equals(line.trim())) {
                break;
            }
            if (line.trim().isEmpty()) {
                continue;
            }
            IntentDetector.ChatIntent intent = IntentDetector.detectIntent(line);
            if (intent == IntentDetector.ChatIntent.CODE || intent == IntentDetector.ChatIntent.MIXED) {
                sessionData = handleCodeTurn(sessionData, line);
            } else {
                sessionData = handleTurn(sessionData.meta(), sessionData.turns(), nextIdx, line);
            }
            nextIdx = sessionData.nextIdx();
        }
        return 0;
    }

    private void handleTurn(VawkChatService.SessionData sessionData, String message) throws Exception {
        VawkChatService.SessionData updated = handleTurn(sessionData.meta(), sessionData.turns(), sessionData.nextIdx(), message);
        System.out.println(updated.turns().get(updated.turns().size() - 1).msg());
    }

    private VawkChatService.SessionData handleTurn(VawkChatSession meta, List<VawkChatService.Turn> history, int nextIdx, String message) throws Exception {
        chatService.appendUserTurn(meta.getSessionId(), nextIdx, message);
        String reply = chatService.generateAssistantReply(history, message);
        chatService.appendAssistantTurn(meta.getSessionId(), nextIdx + 1, reply, chatService.hasChatClient() ? "chat-client" : "stub-chat");

        List<VawkChatService.Turn> newHistory = new java.util.ArrayList<>(history);
        newHistory.add(new VawkChatService.Turn(nextIdx, "user", message, null));
        newHistory.add(new VawkChatService.Turn(nextIdx + 1, "assistant", reply, chatService.hasChatClient() ? "chat-client" : "stub-chat"));
        return new VawkChatService.SessionData(meta, newHistory, nextIdx + 2);
    }

    /**
     * Handles a code-oriented turn in REPL mode by wrapping for structured output, validating the
     * model reply, attempting one auto-fix, and printing either the structured answer or a
     * user-friendly error. Always appends the full turn sequence to the session log.
     */
    private VawkChatService.SessionData handleCodeTurn(VawkChatService.SessionData sessionData, String originalMessage) throws Exception {
        List<VawkChatService.Turn> history = new java.util.ArrayList<>(sessionData.turns());
        int nextIdx = sessionData.nextIdx();
        String wrapped = wrapOneShotMessage(originalMessage);

        chatService.appendUserTurn(sessionId, nextIdx, wrapped);
        history.add(new VawkChatService.Turn(nextIdx, "user", wrapped, null));
        nextIdx++;

        String firstReply = chatService.generateAssistantReply(history, wrapped);
        chatService.appendAssistantTurn(sessionId, nextIdx, firstReply, chatService.hasChatClient() ? "chat-client" : "stub-chat");
        history.add(new VawkChatService.Turn(nextIdx, "assistant", firstReply, chatService.hasChatClient() ? "chat-client" : "stub-chat"));
        nextIdx++;

        if (isStructured(firstReply)) {
            System.out.println(firstReply);
            return new VawkChatService.SessionData(sessionData.meta(), history, nextIdx);
        }

        String correction = "Please rewrite your last answer strictly in the required format: PLAN:, CODE: (with AWK header and ```awk fences), TESTS:, NOTES:. Do not add any other sections.";
        chatService.appendUserTurn(sessionId, nextIdx, correction);
        history.add(new VawkChatService.Turn(nextIdx, "user", correction, null));
        nextIdx++;

        String secondReply = chatService.generateAssistantReply(history, correction);
        chatService.appendAssistantTurn(sessionId, nextIdx, secondReply, chatService.hasChatClient() ? "chat-client" : "stub-chat");
        history.add(new VawkChatService.Turn(nextIdx, "assistant", secondReply, chatService.hasChatClient() ? "chat-client" : "stub-chat"));
        nextIdx++;

        var finalError = validationError(secondReply);
        if (finalError.isEmpty()) {
            System.out.println(secondReply);
        } else {
            printFailure(finalError.get(), false);
        }
        return new VawkChatService.SessionData(sessionData.meta(), history, nextIdx);
    }

    private int handleOneShot(VawkChatService.SessionData sessionData, String originalMessage) throws Exception {
        IntentDetector.ChatIntent intent = IntentDetector.detectIntent(originalMessage);
        if (intent == IntentDetector.ChatIntent.EXPLAIN) {
            // explanation-only: no wrapping, no validation
            chatService.appendUserTurn(sessionId, sessionData.nextIdx(), originalMessage);
            String reply = chatService.generateAssistantReply(sessionData.turns(), originalMessage);
            chatService.appendAssistantTurn(sessionId, sessionData.nextIdx() + 1, reply, chatService.hasChatClient() ? "chat-client" : "stub-chat");
            System.out.println(reply);
            return 0;
        }

        List<VawkChatService.Turn> history = new java.util.ArrayList<>(sessionData.turns());
        int nextIdx = sessionData.nextIdx();
        String wrapped = wrapOneShotMessage(originalMessage);

        chatService.appendUserTurn(sessionId, nextIdx, wrapped);
        nextIdx++;

        String firstReply = chatService.generateAssistantReply(history, wrapped);
        chatService.appendAssistantTurn(sessionId, nextIdx, firstReply, chatService.hasChatClient() ? "chat-client" : "stub-chat");
        history.add(new VawkChatService.Turn(nextIdx - 1, "user", wrapped, null));
        history.add(new VawkChatService.Turn(nextIdx, "assistant", firstReply, chatService.hasChatClient() ? "chat-client" : "stub-chat"));

        if (isStructured(firstReply)) {
            System.out.println(firstReply);
            return 0;
        }

        // auto-fix attempt
        String correction = "Please rewrite your last answer strictly in the required format: PLAN:, CODE: (with AWK header and ```awk fences), TESTS:, NOTES:. Do not add any other sections.";
        nextIdx++;
        chatService.appendUserTurn(sessionId, nextIdx, correction);
        history.add(new VawkChatService.Turn(nextIdx, "user", correction, null));

        nextIdx++;
        String secondReply = chatService.generateAssistantReply(history, correction);
        chatService.appendAssistantTurn(sessionId, nextIdx, secondReply, chatService.hasChatClient() ? "chat-client" : "stub-chat");
        history.add(new VawkChatService.Turn(nextIdx, "assistant", secondReply, chatService.hasChatClient() ? "chat-client" : "stub-chat"));

        var finalError = validationError(secondReply);
        if (finalError.isEmpty()) {
            System.out.println(secondReply);
            return 0;
        }

        printFailure(finalError.get(), true);
        return 1;
    }

    private boolean isStructured(String reply) {
        return validationError(reply).isEmpty();
    }

    private java.util.Optional<String> validationError(String reply) {
        try {
            com.vawk.util.SectionValidator.requirePlanCodeTestsNotes(com.vawk.util.SectionParser.parse(reply));
            return java.util.Optional.empty();
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Could not parse structured reply.";
            return java.util.Optional.of(mapReason(msg));
        }
    }

    private String mapReason(String raw) {
        String lower = raw.toLowerCase();
        if (lower.contains("awk header")) {
            return "CODE section missing required AWK header (# VAWK / Purpose / Intent / Input / Output).";
        }
        if (lower.contains("missing plan") || lower.contains("missing code") || lower.contains("missing tests") || lower.contains("missing notes")) {
            return "Missing PLAN/CODE/TESTS/NOTES sections.";
        }
        if (lower.contains("missing") && lower.contains("section")) {
            return "Structured sections are incomplete (PLAN/CODE/TESTS/NOTES).";
        }
        return "Could not parse structured PLAN/CODE/TESTS/NOTES reply.";
    }

    private void printFailure(String reason, boolean includeSuggestions) {
        System.out.println("[vawk] Error: model did not produce a valid PLAN/CODE/TESTS/NOTES reply.");
        System.out.println("[vawk] Reason: " + reason);
        System.out.println("[vawk] Suggestions:");
        if (includeSuggestions) {
            System.out.println("- Try rephrasing your request, e.g.: \"Write awk script to ... with PLAN, CODE, TESTS, NOTES\".");
            System.out.println("- If this keeps happening, check your prompts (vawk.system.md / vawk.developer.md) for formatting.");
        } else {
            System.out.println("- Rephrase and ask again.");
            System.out.println("- Ask for an explanation only (\"Explain how to ... in awk\") if code is not required.");
        }
    }

    private String wrapOneShotMessage(String original) {
        StringBuilder wrappedBuilder = new StringBuilder();
        wrappedBuilder.append("Task: ").append(original).append("\n\n");
        wrappedBuilder.append("Please respond in this exact format:\n");
        wrappedBuilder.append("PLAN:\n- steps\n\n");
        wrappedBuilder.append("CODE:\n```awk\n# VAWK: <short_id>\n# Purpose: <what this script does>\n# Intent: <why it was built / how it should be used in the future>\n# Input: <input assumptions>\n# Output: <output description>\n...\n```\n\n");
        wrappedBuilder.append("TESTS:\n- test descriptions\n\n");
        wrappedBuilder.append("NOTES:\n- additional notes\n");
        return wrappedBuilder.toString();
    }
}
