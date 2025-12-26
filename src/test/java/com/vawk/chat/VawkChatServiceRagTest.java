package com.vawk.chat;

import com.vawk.runtime.DirectoryService;
import com.vawk.store.RagRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientPromptRequest;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VawkChatServiceRagTest {

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
    void insertsRagContextIntoChatMessages() throws Exception {
        originalPrompts = System.getProperty("vawk.prompts.dir");
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        Path prompts = tempDir.resolve("prompts");
        Files.createDirectories(prompts);
        Files.writeString(prompts.resolve("vawk.system.md"), "SYSTEM");
        Files.writeString(prompts.resolve("vawk.developer.md"), "DEV");
        Files.writeString(prompts.resolve("vawk.project.example.md"), "PROJECT");
        System.setProperty("vawk.prompts.dir", prompts.toString());

        RagRepository ragRepository = new RagRepository() {
            @Override
            public List<RagDocument> searchForChat(String query, int limit) {
                return List.of(
                        new RagDocument("d1", "Doc One", "Content line one\nContent line two"),
                        new RagDocument("d2", "Doc Two", "Other content")
                );
            }
        };

        CapturingChatClient chatClient = new CapturingChatClient();

        VawkChatPromptBuilder builder = new VawkChatPromptBuilder(new VawkPromptLoader());
        VawkChatService service = new VawkChatService(new DirectoryService(), Optional.of(chatClient), builder, ragRepository);

        List<VawkChatService.Turn> history = List.of(
                new VawkChatService.Turn(1, "user", "hi", null),
                new VawkChatService.Turn(2, "assistant", "hello", null)
        );

        String reply = service.generateAssistantReply(history, "How do I format logs?");
        assertEquals("assistant reply", reply);

        List<Message> messages = chatClient.capturedMessages;
        assertTrue(messages.size() >= 6);
        assertEquals("system", messages.get(3).getMessageType().getValue());
        assertTrue(messages.get(3).getContent().contains("Doc One"));
        assertTrue(messages.get(3).getContent().contains("Doc Two"));
        assertEquals("How do I format logs?", messages.get(messages.size() - 1).getContent());
    }

    private static class CapturingChatClient implements ChatClient {
        List<Message> capturedMessages;

        @Override
        public ChatClientRequest prompt() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ChatClientPromptRequest prompt(Prompt prompt) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Builder mutate() {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.springframework.ai.chat.model.ChatResponse call(Prompt prompt) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String call(Message... messages) {
            this.capturedMessages = List.of(messages);
            return "assistant reply";
        }
    }
}
