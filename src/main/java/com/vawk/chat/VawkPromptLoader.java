package com.vawk.chat;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Loads layered chat prompts (system, developer, project/AGENTS) from disk or classpath.
 * Caches content to avoid repeat I/O and assembles the project prompt from AGENTS plus optional
 * vawk.project notes. This is the single entry point for chat prompt content that downstream
 * builders and services should use.
 */
@Component
public final class VawkPromptLoader {
    private static final String SYSTEM_FILE = "vawk.system.md";
    private static final String DEVELOPER_FILE = "vawk.developer.md";
    private static final String PROJECT_FILE = "vawk.project.md";
    private static final String PROJECT_EXAMPLE_FILE = "vawk.project.example.md";
    private static final String AGENTS_VAWK_FILE = "AGENTS.vawk.md";
    private static final String AGENTS_FILE = "AGENTS.md";

    private final AtomicReference<String> systemCache = new AtomicReference<>();
    private final AtomicReference<String> developerCache = new AtomicReference<>();
    private final AtomicReference<String> projectCache = new AtomicReference<>();

    public String loadSystemPrompt() {
        return systemCache.updateAndGet(existing -> existing != null ? existing : readRequired(SYSTEM_FILE));
    }

    public String loadDeveloperPrompt() {
        return developerCache.updateAndGet(existing -> existing != null ? existing : readOptional(DEVELOPER_FILE));
    }

    public String loadProjectPrompt() {
        return projectCache.updateAndGet(existing -> {
            if (existing != null) return existing;
            String agents = readAgents();
            String project = readOptional(PROJECT_FILE);
            if (project.isEmpty()) {
                project = readOptional(PROJECT_EXAMPLE_FILE);
            }
            if (!agents.isEmpty()) {
                if (!project.isEmpty()) {
                    return agents + "\n\n---\n\nAdditional project notes:\n\n" + project;
                }
                return agents;
            }
            return project;
        });
    }

    private String readRequired(String filename) {
        String content = readFile(filename);
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("Missing required prompt: " + filename);
        }
        return content;
    }

    private String readOptional(String filename) {
        String content = readFile(filename);
        return content == null ? "" : content;
    }

    private String readAgents() {
        String cwd = System.getProperty("user.dir");
        if (cwd != null) {
            Path base = Paths.get(cwd);
            Path agentsVawk = base.resolve(AGENTS_VAWK_FILE);
            if (Files.exists(agentsVawk)) {
                try {
                    return Files.readString(agentsVawk, StandardCharsets.UTF_8);
                } catch (IOException ignored) {
                }
            }
            Path agents = base.resolve(AGENTS_FILE);
            if (Files.exists(agents)) {
                try {
                    return Files.readString(agents, StandardCharsets.UTF_8);
                } catch (IOException ignored) {
                }
            }
        }
        return "";
    }

    private String readFile(String filename) {
        String overrideDir = System.getProperty("vawk.prompts.dir");
        if (overrideDir != null && !overrideDir.isBlank()) {
            Path overridePath = Paths.get(overrideDir, filename);
            if (Files.exists(overridePath)) {
                try {
                    return Files.readString(overridePath, StandardCharsets.UTF_8);
                } catch (IOException ignored) {
                }
            }
        }
        // cwd/prompts
        Path cwdPath = Paths.get("prompts", filename);
        if (Files.exists(cwdPath)) {
            try {
                return Files.readString(cwdPath, StandardCharsets.UTF_8);
            } catch (IOException ignored) {
            }
        }
        // classpath
        String cp = "/prompts/" + filename;
        try (InputStream in = getClass().getResourceAsStream(cp)) {
            if (in != null) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {
        }
        return null;
    }
}
