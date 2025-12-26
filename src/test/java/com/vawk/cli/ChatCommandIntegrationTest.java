package com.vawk.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vawk.chat.VawkChatService;
import com.vawk.runtime.DirectoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientPromptRequest;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClient.Builder;
import picocli.CommandLine;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ChatCommandIntegrationTest {

    @TempDir
    Path tempDir;

    private String originalUserDir;

    @AfterEach
    void restoreDir() {
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void createsChatSessionAndAppendsTurns() throws Exception {
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        Path promptsDir = tempDir.resolve("prompts");
        Files.createDirectories(promptsDir);
        Files.writeString(promptsDir.resolve("vawk.system.md"), "SYSTEM PROMPT");
        Files.writeString(promptsDir.resolve("vawk.developer.md"), "DEV PROMPT");
        Files.writeString(promptsDir.resolve("vawk.project.example.md"), "PROJECT PROMPT");
        String originalPrompts = System.getProperty("vawk.prompts.dir");
        System.setProperty("vawk.prompts.dir", promptsDir.toString());

        DirectoryService dirService = new DirectoryService();

        var promptLoader = new com.vawk.chat.VawkPromptLoader();
        var promptBuilder = new com.vawk.chat.VawkChatPromptBuilder(promptLoader);
        ChatClient stubClient = new ChatClient() {
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
            public String call(org.springframework.ai.chat.messages.Message... messages) {
                return """
PLAN:
- demo

CODE:
```awk
# VAWK: demo
# Purpose: demo
# Intent: demo
# Input: demo
# Output: demo
BEGIN { print "ok" }
```

TESTS:
- t

NOTES:
- n
""";
            }
        };
        VawkChatService chatService = new VawkChatService(dirService, Optional.of(stubClient), promptBuilder, new com.vawk.store.RagRepository());
        ChatCommand chatCommand = new ChatCommand(chatService, dirService);
        CommandLine cmd = new CommandLine(chatCommand);

        int exit = cmd.execute("--one-shot", "How do I sum column 3?");
        assertEquals(0, exit);

        Path chatDir = dirService.baseDir().resolve("chat").toAbsolutePath();
        assertTrue(Files.exists(chatDir));
        List<Path> files = Files.list(chatDir)
                .filter(p -> p.getFileName().toString().endsWith(".vawk"))
                .sorted((a, b) -> {
                    try {
                        return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a));
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .toList();
        assertTrue(files.size() >= 1);

        Path sessionFile = files.get(0);
        List<String> lines = Files.readAllLines(sessionFile, StandardCharsets.UTF_8);
        assertTrue(lines.size() >= 3);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode meta = (ObjectNode) mapper.readTree(lines.get(0));
        assertEquals("meta", meta.path("kind").asText());
        String sessionId = meta.path("sessionId").asText();
        assertFalse(sessionId.isBlank());

        ObjectNode userTurn = (ObjectNode) mapper.readTree(lines.get(1));
        ObjectNode assistantTurn = (ObjectNode) mapper.readTree(lines.get(2));
        assertEquals("turn", userTurn.path("kind").asText());
        assertEquals("user", userTurn.path("role").asText());
        assertEquals("assistant", assistantTurn.path("role").asText());

        // resume
        chatCommand = new ChatCommand(chatService, dirService);
        cmd = new CommandLine(chatCommand);
        int exit2 = cmd.execute("--session", sessionId, "--one-shot", "And average?");
        assertEquals(0, exit2);

        List<String> resumedLines = Files.readAllLines(sessionFile, StandardCharsets.UTF_8);
        assertTrue(resumedLines.size() >= 5);
        ObjectNode lastTurn = (ObjectNode) mapper.readTree(resumedLines.get(resumedLines.size() - 1));
        assertEquals("assistant", lastTurn.path("role").asText());
        assertTrue(lastTurn.path("idx").asInt() >= 4);
        if (originalPrompts != null) {
            System.setProperty("vawk.prompts.dir", originalPrompts);
        } else {
            System.clearProperty("vawk.prompts.dir");
        }
    }
}
