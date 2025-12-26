package com.vawk.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatCommandReplCodeTurnTest {

    @TempDir
    Path tempDir;

    private String originalPrompts;
    private String originalUserDir;
    private java.io.InputStream originalIn;

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
        if (originalIn != null) {
            System.setIn(originalIn);
        }
    }

    @Test
    void replHandlesExplainThenStructuredCodeTurn() throws Exception {
        setupPrompts();
        DirectoryService dirService = new DirectoryService();
        CapturingChatClient chatClient = new CapturingChatClient(
                List.of("Plain explanation.", structuredReply("sum3"))
        );
        VawkChatService chatService = new VawkChatService(dirService, Optional.of(chatClient), new VawkChatPromptBuilder(new VawkPromptLoader()), new RagRepository());
        ChatCommand command = new ChatCommand(chatService, dirService);
        CommandLine cmd = new CommandLine(command);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cmd.setOut(new PrintWriter(out, true));

        String input = "explain what FS does in awk\nwrite awk script to sum column 3\n:q\n";
        originalIn = System.in;
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        int exit = cmd.execute();
        assertEquals(0, exit);

        Path chatFile = Files.list(dirService.baseDir().resolve("chat"))
                .max(java.util.Comparator.comparingLong(p -> p.toFile().lastModified()))
                .orElseThrow();
        List<String> lines = Files.readAllLines(chatFile, StandardCharsets.UTF_8);
        // meta + explain user + explain assistant + code user + code assistant
        assertTrue(lines.size() >= 5);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode explainAssistant = (ObjectNode) mapper.readTree(lines.get(2));
        assertEquals("assistant", explainAssistant.path("role").asText());
        String explainMsg = explainAssistant.path("msg").asText();
        assertTrue(!explainMsg.contains("PLAN:"));

        ObjectNode codeAssistant = (ObjectNode) mapper.readTree(lines.get(lines.size() - 1));
        assertTrue(codeAssistant.path("msg").asText().contains("PLAN:"));
        assertTrue(codeAssistant.path("msg").asText().contains("# VAWK:"));
    }

    @Test
    void replAutoFixesInvalidCodeTurn() throws Exception {
        setupPrompts();
        DirectoryService dirService = new DirectoryService();
        CapturingChatClient chatClient = new CapturingChatClient(
                List.of("unstructured answer", structuredReply("fixed"))
        );
        VawkChatService chatService = new VawkChatService(dirService, Optional.of(chatClient), new VawkChatPromptBuilder(new VawkPromptLoader()), new RagRepository());
        ChatCommand command = new ChatCommand(chatService, dirService);
        CommandLine cmd = new CommandLine(command);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cmd.setOut(new PrintWriter(out, true));

        String input = "write awk script to sum column 3\n:q\n";
        originalIn = System.in;
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        int exit = cmd.execute();
        assertEquals(0, exit);

        Path chatFile = Files.list(dirService.baseDir().resolve("chat"))
                .max(java.util.Comparator.comparingLong(p -> p.toFile().lastModified()))
                .orElseThrow();
        List<String> lines = Files.readAllLines(chatFile, StandardCharsets.UTF_8);
        // meta + user(wrapped) + assistant(unstructured) + user(correction) + assistant(structured)
        assertEquals(5, lines.size());
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode correctionTurn = (ObjectNode) mapper.readTree(lines.get(3));
        assertEquals("user", correctionTurn.path("role").asText());
        assertTrue(correctionTurn.path("msg").asText().contains("rewrite your last answer"));

        ObjectNode finalAssistant = (ObjectNode) mapper.readTree(lines.get(4));
        assertTrue(finalAssistant.path("msg").asText().contains("PLAN:"));
        assertTrue(finalAssistant.path("msg").asText().contains("fixed"));
    }

    @Test
    void replTreatsMixedAsCode() throws Exception {
        setupPrompts();
        DirectoryService dirService = new DirectoryService();
        CapturingChatClient chatClient = new CapturingChatClient(
                List.of(structuredReply("mixed"))
        );
        VawkChatService chatService = new VawkChatService(dirService, Optional.of(chatClient), new VawkChatPromptBuilder(new VawkPromptLoader()), new RagRepository());
        ChatCommand command = new ChatCommand(chatService, dirService);
        CommandLine cmd = new CommandLine(command);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cmd.setOut(new PrintWriter(out, true));

        String input = "I have logs like X; how should I analyze them with awk?\n:q\n";
        originalIn = System.in;
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        int exit = cmd.execute();
        assertEquals(0, exit);

        Path chatFile = Files.list(dirService.baseDir().resolve("chat"))
                .max(java.util.Comparator.comparingLong(p -> p.toFile().lastModified()))
                .orElseThrow();
        List<String> lines = Files.readAllLines(chatFile, StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode assistant = (ObjectNode) mapper.readTree(lines.get(lines.size() - 1));
        assertTrue(assistant.path("msg").asText().contains("PLAN:"));
        assertTrue(assistant.path("msg").asText().contains("# VAWK:"));
    }

    @Test
    void replFailureShowsReasonKeepsRunning() throws Exception {
        setupPrompts();
        DirectoryService dirService = new DirectoryService();
        CapturingChatClient chatClient = new CapturingChatClient(
                List.of("bad reply", "still bad")
        );
        VawkChatService chatService = new VawkChatService(dirService, Optional.of(chatClient), new VawkChatPromptBuilder(new VawkPromptLoader()), new RagRepository());
        ChatCommand command = new ChatCommand(chatService, dirService);
        CommandLine cmd = new CommandLine(command);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out, true));

        String input = "write awk script to sum column 3\n:q\n";
        originalIn = System.in;
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        int exit = cmd.execute();
        System.setOut(originalOut);
        assertEquals(0, exit);
        String output = out.toString();
        assertTrue(output.contains("Error"));
        assertTrue(output.contains("Reason"));
        assertTrue(output.contains("Suggestions"));

        Path chatFile = Files.list(dirService.baseDir().resolve("chat"))
                .max(Comparator.comparingLong(p -> p.toFile().lastModified()))
                .orElseThrow();
        List<String> lines = Files.readAllLines(chatFile, StandardCharsets.UTF_8);
        assertEquals(5, lines.size()); // meta + user + assistant + correction + assistant
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
        private final List<List<Message>> seenMessages = new ArrayList<>();

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
            seenMessages.add(List.of(messages));
            int idx = callCount.getAndIncrement();
            return responses.get(idx);
        }
    }
}
