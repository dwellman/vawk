package com.vawk.chat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.chat.messages.Message;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VawkPromptLoaderAgentsTest {

    @TempDir
    Path tempDir;

    private String originalUserDir;
    private String originalPromptsDir;

    @AfterEach
    void restore() {
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
        if (originalPromptsDir != null) {
            System.setProperty("vawk.prompts.dir", originalPromptsDir);
        } else {
            System.clearProperty("vawk.prompts.dir");
        }
    }

    @Test
    void usesAgentsFileAsProjectPrompt() throws Exception {
        originalUserDir = System.getProperty("user.dir");
        originalPromptsDir = System.getProperty("vawk.prompts.dir");

        Path work = tempDir.resolve("proj");
        Files.createDirectories(work);
        System.setProperty("user.dir", work.toString());

        Path promptsDir = work.resolve("prompts");
        Files.createDirectories(promptsDir);
        Files.writeString(promptsDir.resolve("vawk.system.md"), "SYSTEM");
        Files.writeString(promptsDir.resolve("vawk.developer.md"), "DEV");
        Files.writeString(promptsDir.resolve("vawk.project.md"), "Project notes: old style");
        System.setProperty("vawk.prompts.dir", promptsDir.toString());

        Files.writeString(work.resolve("AGENTS.md"), "Project: Pipe Log Summarizer (AGENTS)");

        VawkPromptLoader loader = new VawkPromptLoader();
        VawkChatPromptBuilder builder = new VawkChatPromptBuilder(loader);
        List<Message> messages = builder.buildMessages("What does this project do?", List.of(), null);

        assertEquals("system", messages.get(2).getMessageType().getValue());
        String projectPrompt = messages.get(2).getContent();
        assertTrue(projectPrompt.contains("Project: Pipe Log Summarizer (AGENTS)"));
        assertTrue(projectPrompt.contains("Project notes: old style"));
        assertTrue(projectPrompt.contains("---"));
    }
}
