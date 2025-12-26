package com.vawk.cli;

import com.vawk.chat.VawkChatPromptBuilder;
import com.vawk.chat.VawkChatService;
import com.vawk.chat.VawkPromptLoader;
import com.vawk.runtime.DirectoryService;
import com.vawk.store.RagRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientPromptRequest;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequest;
import org.springframework.ai.chat.messages.Message;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatCommandPlanFirstOneShotTest {

    @TempDir
    Path tempDir;

    private String originalPrompts;
    private String originalUserDir;

    @AfterEach
    void restoreProps() {
        if (originalPrompts != null) {
            System.setProperty("vawk.prompts.dir", originalPrompts);
        } else {
            System.clearProperty("vawk.prompts.dir");
        }
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void oneShotStructuredOnFirstReply() throws Exception {
        setupPrompts();
        DirectoryService dirService = new DirectoryService();
        CapturingChatClient chatClient = new CapturingChatClient(
                List.of(structuredReply("pipe-to-csv"))
        );
        VawkChatService chatService = new VawkChatService(dirService, Optional.of(chatClient), new VawkChatPromptBuilder(new VawkPromptLoader()), new RagRepository());
        ChatCommand command = new ChatCommand(chatService, dirService);
        CommandLine cmd = new CommandLine(command);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cmd.setOut(new PrintWriter(out, true));

        int exit = cmd.execute("--one-shot", "How do I reformat pipe logs into CSV?");
        assertEquals(0, exit);

        Path chatFile = Files.list(dirService.baseDir().resolve("chat"))
                .max(java.util.Comparator.comparingLong(p -> p.toFile().lastModified()))
                .orElseThrow();
        List<String> lines = Files.readAllLines(chatFile);
        String assistantMsg = new com.fasterxml.jackson.databind.ObjectMapper().readTree(lines.get(lines.size() - 1)).path("msg").asText();
        assertTrue(assistantMsg.contains("PLAN:"));
        assertTrue(assistantMsg.contains("CODE:"));
        assertTrue(assistantMsg.contains("# VAWK:"));
        assertTrue(assistantMsg.contains("TESTS:"));
        assertTrue(assistantMsg.contains("NOTES:"));
    }

    @Test
    void oneShotAutoFixesInvalidFirstReply() throws Exception {
        setupPrompts();
        DirectoryService dirService = new DirectoryService();
        CapturingChatClient chatClient = new CapturingChatClient(
                List.of("Here is a high-level answer without structure.",
                        structuredReply("pipe-to-csv-fixed"))
        );
        VawkChatService chatService = new VawkChatService(dirService, Optional.of(chatClient), new VawkChatPromptBuilder(new VawkPromptLoader()), new RagRepository());
        ChatCommand command = new ChatCommand(chatService, dirService);
        CommandLine cmd = new CommandLine(command);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cmd.setOut(new PrintWriter(out, true));

        int exit = cmd.execute("--one-shot", "How do I reformat pipe logs into CSV?");
        assertEquals(0, exit);
        Path chatFile = Files.list(dirService.baseDir().resolve("chat"))
                .max(java.util.Comparator.comparingLong(p -> p.toFile().lastModified()))
                .orElseThrow();
        List<String> lines = Files.readAllLines(chatFile);
        // meta + user + assistant (unstructured) + user (correction) + assistant (structured)
        assertEquals(5, lines.size());
        String finalAssistant = new com.fasterxml.jackson.databind.ObjectMapper().readTree(lines.get(4)).path("msg").asText();
        assertTrue(finalAssistant.contains("PLAN:"));
        assertTrue(finalAssistant.contains("pipe-to-csv-fixed"));
    }

    @Test
    void oneShotExplainBypassesStructure() throws Exception {
        setupPrompts();
        DirectoryService dirService = new DirectoryService();
        CapturingChatClient chatClient = new CapturingChatClient(
                List.of("FS is the field separator in AWK.")
        );
        VawkChatService chatService = new VawkChatService(dirService, Optional.of(chatClient), new VawkChatPromptBuilder(new VawkPromptLoader()), new RagRepository());
        ChatCommand command = new ChatCommand(chatService, dirService);
        CommandLine cmd = new CommandLine(command);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cmd.setOut(new PrintWriter(out, true));

        int exit = cmd.execute("--one-shot", "Explain what FS does in awk");
        assertEquals(0, exit);

        Path chatFile = Files.list(dirService.baseDir().resolve("chat"))
                .max(java.util.Comparator.comparingLong(p -> p.toFile().lastModified()))
                .orElseThrow();
        List<String> lines = Files.readAllLines(chatFile);
        assertTrue(lines.size() >= 3);
        String userMsg = new com.fasterxml.jackson.databind.ObjectMapper().readTree(lines.get(1)).path("msg").asText();
        String assistantMsg = new com.fasterxml.jackson.databind.ObjectMapper().readTree(lines.get(2)).path("msg").asText();
        assertTrue(userMsg.startsWith("Explain what FS does in awk"));
        assertTrue(!assistantMsg.contains("PLAN:"));
        assertTrue(assistantMsg.contains("FS is the field separator"));
    }

    @Test
    void oneShotFailureShowsReasonAndSuggestions() throws Exception {
        setupPrompts();
        DirectoryService dirService = new DirectoryService();
        CapturingChatClient chatClient = new CapturingChatClient(
                List.of("unstructured", "still unstructured")
        );
        VawkChatService chatService = new VawkChatService(dirService, Optional.of(chatClient), new VawkChatPromptBuilder(new VawkPromptLoader()), new RagRepository());
        ChatCommand command = new ChatCommand(chatService, dirService);
        CommandLine cmd = new CommandLine(command);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out, true));

        int exit = cmd.execute("--one-shot", "Write awk script to sum column 3");
        System.setOut(originalOut);
        assertEquals(1, exit);
        String output = out.toString();
        assertTrue(output.contains("Error"));
        assertTrue(output.contains("Reason"));
        assertTrue(output.contains("Suggestions"));

        Path chatFile = Files.list(dirService.baseDir().resolve("chat"))
                .max(Comparator.comparingLong(p -> p.toFile().lastModified()))
                .orElseThrow();
        List<String> lines = Files.readAllLines(chatFile);
        // meta + user(wrapped) + assistant(invalid) + user(correction) + assistant(invalid)
        assertEquals(5, lines.size());
    }

    private void setupPrompts() throws Exception {
        originalPrompts = System.getProperty("vawk.prompts.dir");
        originalUserDir = System.getProperty("user.dir");
        Path work = tempDir.resolve(java.util.UUID.randomUUID().toString());
        Files.createDirectories(work);
        System.setProperty("user.dir", work.toString());
        Path prompts = work.resolve("prompts");
        Files.createDirectories(prompts);
        Files.writeString(prompts.resolve("vawk.system.md"), "SYSTEM");
        Files.writeString(prompts.resolve("vawk.developer.md"), "DEV");
        Files.writeString(prompts.resolve("vawk.project.example.md"), "PROJECT");
        System.setProperty("vawk.prompts.dir", prompts.toString());
    }

    private String structuredReply(String id) {
        return """
PLAN:
- step one

CODE:
```awk
# VAWK: %s
# Purpose: demo
# Intent: demo intent
# Input: demo input
# Output: demo output
BEGIN { print "ok" }
```

TESTS:
- test

NOTES:
- note
""".formatted(id);
    }

    private static class CapturingChatClient implements ChatClient {
        private final List<String> responses;
        private final AtomicInteger callCount = new AtomicInteger(0);

        CapturingChatClient(List<String> responses) {
            this.responses = responses;
        }

        @Override
        public ChatClientRequest prompt() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ChatClientPromptRequest prompt(org.springframework.ai.chat.prompt.Prompt prompt) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Builder mutate() {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.springframework.ai.chat.model.ChatResponse call(org.springframework.ai.chat.prompt.Prompt prompt) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String call(Message... messages) {
            int idx = callCount.getAndIncrement();
            return responses.get(idx);
        }
    }
}
