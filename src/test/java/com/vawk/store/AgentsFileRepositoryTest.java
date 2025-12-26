package com.vawk.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AgentsFileRepositoryTest {

    @Test
    void readsPrimaryAgentsFileWhenPresent() throws Exception {
        Optional<String> content = new AgentsFileRepository().read();

        assertThat(content.orElse("")).contains("# Agents Guide (VAWK)");
    }

    @Test
    void readsFallbackAgentsFileWhenPrimaryMissing(@TempDir Path tempDir) throws Exception {
        Path cwd = Path.of("").toAbsolutePath();
        Path primary = cwd.resolve("AGENTS.md");
        Path backup = tempDir.resolve("AGENTS.md.bak");
        Path promptsDir = cwd.resolve("prompts");
        Path fallback = promptsDir.resolve("AGENTS.md");
        Path fallbackBackup = tempDir.resolve("AGENTS.md.fallback.bak");

        boolean hadPrimary = Files.exists(primary);
        boolean hadFallback = Files.exists(fallback);
        if (hadPrimary) {
            Files.move(primary, backup);
        }
        if (hadFallback) {
            Files.move(fallback, fallbackBackup);
        }
        Files.createDirectories(promptsDir);
        Files.writeString(fallback, "fallback", StandardCharsets.UTF_8);
        try {
            Optional<String> content = new AgentsFileRepository().read();
            assertThat(content.orElse("")).contains("fallback");
        } finally {
            Files.deleteIfExists(fallback);
            if (hadFallback) {
                Files.move(fallbackBackup, fallback);
            }
            if (hadPrimary) {
                Files.move(backup, primary);
            }
        }
    }

    @Test
    void returnsEmptyWhenNoAgentsFiles(@TempDir Path tempDir) throws Exception {
        Path cwd = Path.of("").toAbsolutePath();
        Path primary = cwd.resolve("AGENTS.md");
        Path backup = tempDir.resolve("AGENTS.md.bak");
        Path fallback = cwd.resolve("prompts").resolve("AGENTS.md");
        Path fallbackBackup = tempDir.resolve("AGENTS.md.fallback.bak");

        boolean hadPrimary = Files.exists(primary);
        boolean hadFallback = Files.exists(fallback);
        if (hadPrimary) {
            Files.move(primary, backup);
        }
        if (hadFallback) {
            Files.move(fallback, fallbackBackup);
        }
        try {
            Optional<String> content = new AgentsFileRepository().read();
            assertThat(content).isEmpty();
        } finally {
            if (hadFallback) {
                Files.move(fallbackBackup, fallback);
            }
            if (hadPrimary) {
                Files.move(backup, primary);
            }
        }
    }
}
