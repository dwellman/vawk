package com.vawk.chat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VawkChatPromptBuilderTest {

    @Test
    void buildMessagesIncludesPromptsAndHistory(@TempDir Path tempDir) throws Exception {
        Path promptsDir = tempDir.resolve("prompts");
        Files.createDirectories(promptsDir);
        Files.writeString(promptsDir.resolve("vawk.system.md"), "system", StandardCharsets.UTF_8);
        Files.writeString(promptsDir.resolve("vawk.developer.md"), "dev", StandardCharsets.UTF_8);
        Files.writeString(promptsDir.resolve("vawk.project.md"), "project", StandardCharsets.UTF_8);
        String original = System.getProperty("vawk.prompts.dir");
        System.setProperty("vawk.prompts.dir", promptsDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            VawkChatPromptBuilder builder = new VawkChatPromptBuilder(loader);
            List<VawkChatService.Turn> history = List.of(
                    new VawkChatService.Turn(1, "user", "hi", null),
                    new VawkChatService.Turn(2, "assistant", "hello", "stub")
            );

            List<Message> messages = builder.buildMessages("new", history, "rag");

            assertThat(messages.get(0)).isInstanceOf(SystemMessage.class);
            assertThat(messages).anyMatch(m -> m instanceof SystemMessage && ((SystemMessage) m).getContent().contains("dev"));
            assertThat(messages).anyMatch(m -> m instanceof SystemMessage && ((SystemMessage) m).getContent().contains("project"));
            assertThat(messages).anyMatch(m -> m instanceof SystemMessage && ((SystemMessage) m).getContent().contains("rag"));
            assertThat(messages).anyMatch(m -> m instanceof UserMessage && ((UserMessage) m).getContent().equals("hi"));
            assertThat(messages).anyMatch(m -> m instanceof AssistantMessage && ((AssistantMessage) m).getContent().equals("hello"));
            assertThat(messages.get(messages.size() - 1)).isInstanceOf(UserMessage.class);
        } finally {
            if (original != null) {
                System.setProperty("vawk.prompts.dir", original);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void buildMessagesSkipsBlankPrompts(@TempDir Path tempDir) throws Exception {
        Path promptsDir = tempDir.resolve("prompts");
        Files.createDirectories(promptsDir);
        Files.writeString(promptsDir.resolve("vawk.system.md"), "system", StandardCharsets.UTF_8);
        Files.writeString(promptsDir.resolve("vawk.developer.md"), "", StandardCharsets.UTF_8);
        Files.writeString(promptsDir.resolve("vawk.project.md"), " ", StandardCharsets.UTF_8);
        String original = System.getProperty("vawk.prompts.dir");
        String originalDir = System.getProperty("user.dir");
        System.setProperty("vawk.prompts.dir", promptsDir.toString());
        System.setProperty("user.dir", tempDir.toString());
        try {
            VawkChatPromptBuilder builder = new VawkChatPromptBuilder(new VawkPromptLoader());
            List<Message> messages = builder.buildMessages("new", List.of(), null);

            assertThat(messages).hasSize(2);
            assertThat(messages.get(0)).isInstanceOf(SystemMessage.class);
            assertThat(messages.get(1)).isInstanceOf(UserMessage.class);
        } finally {
            if (originalDir != null) {
                System.setProperty("user.dir", originalDir);
            }
            if (original != null) {
                System.setProperty("vawk.prompts.dir", original);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void buildMessagesHandlesNullHistoryAndBlankRag(@TempDir Path tempDir) throws Exception {
        Path promptsDir = tempDir.resolve("prompts");
        Files.createDirectories(promptsDir);
        Files.writeString(promptsDir.resolve("vawk.system.md"), "system", StandardCharsets.UTF_8);
        String original = System.getProperty("vawk.prompts.dir");
        System.setProperty("vawk.prompts.dir", promptsDir.toString());
        try {
            VawkChatPromptBuilder builder = new VawkChatPromptBuilder(new VawkPromptLoader());
            List<Message> messages = builder.buildMessages("new", null, " ");

            assertThat(messages.get(0)).isInstanceOf(SystemMessage.class);
            assertThat(messages.get(messages.size() - 1)).isInstanceOf(UserMessage.class);
            assertThat(messages)
                    .filteredOn(m -> m instanceof SystemMessage)
                    .noneMatch(m -> ((SystemMessage) m).getContent().contains("Reference context"));
        } finally {
            if (original != null) {
                System.setProperty("vawk.prompts.dir", original);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }
}
