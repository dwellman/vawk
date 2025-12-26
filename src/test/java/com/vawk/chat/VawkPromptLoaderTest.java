package com.vawk.chat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VawkPromptLoaderTest {

    @Test
    void loadSystemPromptUsesOverride(@TempDir Path tempDir) throws Exception {
        Files.writeString(tempDir.resolve("vawk.system.md"), "system", StandardCharsets.UTF_8);
        String original = System.getProperty("vawk.prompts.dir");
        System.setProperty("vawk.prompts.dir", tempDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            assertThat(loader.loadSystemPrompt()).isEqualTo("system");
        } finally {
            if (original != null) {
                System.setProperty("vawk.prompts.dir", original);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void loadSystemPromptFallsBackToClasspath(@TempDir Path tempDir) {
        String original = System.getProperty("vawk.prompts.dir");
        System.setProperty("vawk.prompts.dir", tempDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            assertThat(loader.loadSystemPrompt()).isNotBlank();
        } finally {
            if (original != null) {
                System.setProperty("vawk.prompts.dir", original);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void loadProjectPromptCombinesAgentsAndProject(@TempDir Path tempDir) throws Exception {
        Path promptsDir = tempDir.resolve("prompts");
        Files.createDirectories(promptsDir);
        Files.writeString(promptsDir.resolve("vawk.system.md"), "system", StandardCharsets.UTF_8);
        Files.writeString(promptsDir.resolve("vawk.project.md"), "project", StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("AGENTS.md"), "agents", StandardCharsets.UTF_8);

        String originalDir = System.getProperty("user.dir");
        String originalPrompts = System.getProperty("vawk.prompts.dir");
        System.setProperty("user.dir", tempDir.toString());
        System.setProperty("vawk.prompts.dir", promptsDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            String project = loader.loadProjectPrompt();
            assertThat(project).contains("agents");
            assertThat(project).contains("Additional project notes");
            assertThat(project).contains("project");
        } finally {
            if (originalDir != null) {
                System.setProperty("user.dir", originalDir);
            }
            if (originalPrompts != null) {
                System.setProperty("vawk.prompts.dir", originalPrompts);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void loadProjectPromptUsesAgentsVawkWhenPresent(@TempDir Path tempDir) throws Exception {
        Path promptsDir = tempDir.resolve("prompts");
        Files.createDirectories(promptsDir);
        Files.writeString(promptsDir.resolve("vawk.system.md"), "system", StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("AGENTS.vawk.md"), "agents-vawk", StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("AGENTS.md"), "agents-default", StandardCharsets.UTF_8);

        String originalDir = System.getProperty("user.dir");
        String originalPrompts = System.getProperty("vawk.prompts.dir");
        System.setProperty("user.dir", tempDir.toString());
        System.setProperty("vawk.prompts.dir", promptsDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            String project = loader.loadProjectPrompt();
            assertThat(project).contains("agents-vawk");
            assertThat(project).doesNotContain("agents-default");
        } finally {
            if (originalDir != null) {
                System.setProperty("user.dir", originalDir);
            }
            if (originalPrompts != null) {
                System.setProperty("vawk.prompts.dir", originalPrompts);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void loadProjectPromptFallsBackToExampleWhenProjectMissing(@TempDir Path tempDir) throws Exception {
        Path promptsDir = tempDir.resolve("prompts");
        Files.createDirectories(promptsDir);
        Files.writeString(promptsDir.resolve("vawk.system.md"), "system", StandardCharsets.UTF_8);
        Files.writeString(promptsDir.resolve("vawk.project.example.md"), "example", StandardCharsets.UTF_8);

        String originalDir = System.getProperty("user.dir");
        String originalPrompts = System.getProperty("vawk.prompts.dir");
        System.setProperty("user.dir", tempDir.toString());
        System.setProperty("vawk.prompts.dir", promptsDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            String project = loader.loadProjectPrompt();
            assertThat(project).contains("example");
        } finally {
            if (originalDir != null) {
                System.setProperty("user.dir", originalDir);
            }
            if (originalPrompts != null) {
                System.setProperty("vawk.prompts.dir", originalPrompts);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void loadProjectPromptUsesAgentsMdFallback(@TempDir Path tempDir) throws Exception {
        Path promptsDir = tempDir.resolve("prompts");
        Files.createDirectories(promptsDir);
        Files.writeString(promptsDir.resolve("vawk.system.md"), "system", StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("AGENTS.md"), "agents-default", StandardCharsets.UTF_8);

        String originalDir = System.getProperty("user.dir");
        String originalPrompts = System.getProperty("vawk.prompts.dir");
        System.setProperty("user.dir", tempDir.toString());
        System.setProperty("vawk.prompts.dir", promptsDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            String project = loader.loadProjectPrompt();
            assertThat(project).contains("agents-default");
        } finally {
            if (originalDir != null) {
                System.setProperty("user.dir", originalDir);
            }
            if (originalPrompts != null) {
                System.setProperty("vawk.prompts.dir", originalPrompts);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void loadProjectPromptFallsBackWhenAgentsVawkUnreadable(@TempDir Path tempDir) throws Exception {
        Path promptsDir = tempDir.resolve("prompts");
        Files.createDirectories(promptsDir);
        Files.writeString(promptsDir.resolve("vawk.system.md"), "system", StandardCharsets.UTF_8);
        Files.createDirectories(tempDir.resolve("AGENTS.vawk.md"));
        Files.writeString(tempDir.resolve("AGENTS.md"), "agents-default", StandardCharsets.UTF_8);

        String originalDir = System.getProperty("user.dir");
        String originalPrompts = System.getProperty("vawk.prompts.dir");
        System.setProperty("user.dir", tempDir.toString());
        System.setProperty("vawk.prompts.dir", promptsDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            String project = loader.loadProjectPrompt();
            assertThat(project).contains("agents-default");
        } finally {
            if (originalDir != null) {
                System.setProperty("user.dir", originalDir);
            }
            if (originalPrompts != null) {
                System.setProperty("vawk.prompts.dir", originalPrompts);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void loadProjectPromptReturnsEmptyWhenNoAgentsOrProject(@TempDir Path tempDir) throws Exception {
        Path promptsDir = tempDir.resolve("prompts");
        Files.createDirectories(promptsDir);
        Files.writeString(promptsDir.resolve("vawk.system.md"), "system", StandardCharsets.UTF_8);

        String originalDir = System.getProperty("user.dir");
        String originalPrompts = System.getProperty("vawk.prompts.dir");
        System.setProperty("user.dir", tempDir.toString());
        System.setProperty("vawk.prompts.dir", promptsDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            String project = loader.loadProjectPrompt();
            assertThat(project).contains("Project Overview");
        } finally {
            if (originalDir != null) {
                System.setProperty("user.dir", originalDir);
            }
            if (originalPrompts != null) {
                System.setProperty("vawk.prompts.dir", originalPrompts);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void loadProjectPromptUsesProjectWhenNoAgents(@TempDir Path tempDir) throws Exception {
        Path promptsDir = tempDir.resolve("prompts");
        Files.createDirectories(promptsDir);
        Files.writeString(promptsDir.resolve("vawk.system.md"), "system", StandardCharsets.UTF_8);
        Files.writeString(promptsDir.resolve("vawk.project.md"), "project-only", StandardCharsets.UTF_8);

        String originalDir = System.getProperty("user.dir");
        String originalPrompts = System.getProperty("vawk.prompts.dir");
        System.setProperty("user.dir", tempDir.toString());
        System.setProperty("vawk.prompts.dir", promptsDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            String project = loader.loadProjectPrompt();
            assertThat(project).isEqualTo("project-only");
        } finally {
            if (originalDir != null) {
                System.setProperty("user.dir", originalDir);
            }
            if (originalPrompts != null) {
                System.setProperty("vawk.prompts.dir", originalPrompts);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void loadProjectPromptUsesAgentsWhenProjectEmpty(@TempDir Path tempDir) throws Exception {
        Path promptsDir = tempDir.resolve("prompts");
        Files.createDirectories(promptsDir);
        Files.writeString(promptsDir.resolve("vawk.system.md"), "system", StandardCharsets.UTF_8);
        Files.writeString(promptsDir.resolve("vawk.project.md"), "", StandardCharsets.UTF_8);
        Files.writeString(promptsDir.resolve("vawk.project.example.md"), "", StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("AGENTS.md"), "agents-only", StandardCharsets.UTF_8);

        String originalDir = System.getProperty("user.dir");
        String originalPrompts = System.getProperty("vawk.prompts.dir");
        System.setProperty("user.dir", tempDir.toString());
        System.setProperty("vawk.prompts.dir", promptsDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            String project = loader.loadProjectPrompt();
            assertThat(project).isEqualTo("agents-only");
        } finally {
            if (originalDir != null) {
                System.setProperty("user.dir", originalDir);
            }
            if (originalPrompts != null) {
                System.setProperty("vawk.prompts.dir", originalPrompts);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void loadDeveloperPromptReturnsEmptyWhenMissing(@TempDir Path tempDir) {
        String original = System.getProperty("vawk.prompts.dir");
        try {
            Files.writeString(tempDir.resolve("vawk.developer.md"), " ");
            System.setProperty("vawk.prompts.dir", tempDir.toString());
            VawkPromptLoader loader = new VawkPromptLoader();
            assertThat(loader.loadDeveloperPrompt()).isBlank();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (original != null) {
                System.setProperty("vawk.prompts.dir", original);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void loadDeveloperPromptCachesResult(@TempDir Path tempDir) throws Exception {
        Files.writeString(tempDir.resolve("vawk.developer.md"), "dev-1", StandardCharsets.UTF_8);
        String original = System.getProperty("vawk.prompts.dir");
        System.setProperty("vawk.prompts.dir", tempDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            String first = loader.loadDeveloperPrompt();
            Files.writeString(tempDir.resolve("vawk.developer.md"), "dev-2", StandardCharsets.UTF_8);
            String second = loader.loadDeveloperPrompt();

            assertThat(first).isEqualTo("dev-1");
            assertThat(second).isEqualTo(first);
        } finally {
            if (original != null) {
                System.setProperty("vawk.prompts.dir", original);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void loadDeveloperPromptFallsBackWhenOverrideUnreadable(@TempDir Path tempDir) {
        String original = System.getProperty("vawk.prompts.dir");
        try {
            Files.createDirectories(tempDir.resolve("vawk.developer.md"));
            System.setProperty("vawk.prompts.dir", tempDir.toString());
            VawkPromptLoader loader = new VawkPromptLoader();
            assertThat(loader.loadDeveloperPrompt()).isNotBlank();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (original != null) {
                System.setProperty("vawk.prompts.dir", original);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void loadSystemPromptFallsBackWhenOverrideUnreadable(@TempDir Path tempDir) {
        String original = System.getProperty("vawk.prompts.dir");
        try {
            Files.createDirectories(tempDir.resolve("vawk.system.md"));
            System.setProperty("vawk.prompts.dir", tempDir.toString());
            VawkPromptLoader loader = new VawkPromptLoader();
            assertThat(loader.loadSystemPrompt()).isNotBlank();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (original != null) {
                System.setProperty("vawk.prompts.dir", original);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void loadSystemPromptThrowsWhenBlankOverride(@TempDir Path tempDir) throws Exception {
        Files.writeString(tempDir.resolve("vawk.system.md"), "   ", StandardCharsets.UTF_8);
        String original = System.getProperty("vawk.prompts.dir");
        System.setProperty("vawk.prompts.dir", tempDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            assertThrows(IllegalStateException.class, loader::loadSystemPrompt);
        } finally {
            if (original != null) {
                System.setProperty("vawk.prompts.dir", original);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void cachesSystemPromptAfterFirstLoad(@TempDir Path tempDir) throws Exception {
        Path system = tempDir.resolve("vawk.system.md");
        Files.writeString(system, "system-1", StandardCharsets.UTF_8);
        String original = System.getProperty("vawk.prompts.dir");
        System.setProperty("vawk.prompts.dir", tempDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            assertThat(loader.loadSystemPrompt()).isEqualTo("system-1");
            Files.writeString(system, "system-2", StandardCharsets.UTF_8);
            assertThat(loader.loadSystemPrompt()).isEqualTo("system-1");
        } finally {
            if (original != null) {
                System.setProperty("vawk.prompts.dir", original);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void loadProjectPromptCachesResult(@TempDir Path tempDir) throws Exception {
        Path promptsDir = tempDir.resolve("prompts");
        Files.createDirectories(promptsDir);
        Files.writeString(promptsDir.resolve("vawk.system.md"), "system", StandardCharsets.UTF_8);
        Files.writeString(promptsDir.resolve("vawk.project.md"), "project-1", StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("AGENTS.md"), "agents-1", StandardCharsets.UTF_8);

        String originalDir = System.getProperty("user.dir");
        String originalPrompts = System.getProperty("vawk.prompts.dir");
        System.setProperty("user.dir", tempDir.toString());
        System.setProperty("vawk.prompts.dir", promptsDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            String first = loader.loadProjectPrompt();
            Files.writeString(tempDir.resolve("AGENTS.md"), "agents-2", StandardCharsets.UTF_8);
            String second = loader.loadProjectPrompt();

            assertThat(first).contains("agents-1");
            assertThat(second).isEqualTo(first);
        } finally {
            if (originalDir != null) {
                System.setProperty("user.dir", originalDir);
            }
            if (originalPrompts != null) {
                System.setProperty("vawk.prompts.dir", originalPrompts);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void readAgentsReturnsEmptyWhenUserDirMissing() throws Exception {
        String originalDir = System.getProperty("user.dir");
        System.clearProperty("user.dir");
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            java.lang.reflect.Method method = VawkPromptLoader.class.getDeclaredMethod("readAgents");
            method.setAccessible(true);
            String result = (String) method.invoke(loader);
            assertThat(result).isEmpty();
        } finally {
            if (originalDir != null) {
                System.setProperty("user.dir", originalDir);
            }
        }
    }

    @Test
    void readFileReturnsNullWhenMissingEverywhere(@TempDir Path tempDir) throws Exception {
        String original = System.getProperty("vawk.prompts.dir");
        System.setProperty("vawk.prompts.dir", tempDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            java.lang.reflect.Method method = VawkPromptLoader.class.getDeclaredMethod("readFile", String.class);
            method.setAccessible(true);
            String result = (String) method.invoke(loader, "missing.prompt.md");
            assertThat(result).isNull();
        } finally {
            if (original != null) {
                System.setProperty("vawk.prompts.dir", original);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void readRequiredThrowsWhenContentMissing(@TempDir Path tempDir) throws Exception {
        String original = System.getProperty("vawk.prompts.dir");
        System.setProperty("vawk.prompts.dir", tempDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            java.lang.reflect.Method method = VawkPromptLoader.class.getDeclaredMethod("readRequired", String.class);
            method.setAccessible(true);
            org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
                try {
                    method.invoke(loader, "missing.prompt.md");
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw (RuntimeException) e.getCause();
                }
            });
        } finally {
            if (original != null) {
                System.setProperty("vawk.prompts.dir", original);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void readFileUsesClasspathWhenOnlyTestResourceExists(@TempDir Path tempDir) throws Exception {
        String original = System.getProperty("vawk.prompts.dir");
        System.setProperty("vawk.prompts.dir", tempDir.toString());
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            java.lang.reflect.Method method = VawkPromptLoader.class.getDeclaredMethod("readFile", String.class);
            method.setAccessible(true);
            String result = (String) method.invoke(loader, "test-only.md");
            assertThat(result).isEqualTo("test-only");
        } finally {
            if (original != null) {
                System.setProperty("vawk.prompts.dir", original);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }

    @Test
    void readFileSkipsBlankOverrideDir() throws Exception {
        String original = System.getProperty("vawk.prompts.dir");
        System.setProperty("vawk.prompts.dir", " ");
        try {
            VawkPromptLoader loader = new VawkPromptLoader();
            java.lang.reflect.Method method = VawkPromptLoader.class.getDeclaredMethod("readFile", String.class);
            method.setAccessible(true);
            String result = (String) method.invoke(loader, "test-only.md");
            assertThat(result).isEqualTo("test-only");
        } finally {
            if (original != null) {
                System.setProperty("vawk.prompts.dir", original);
            } else {
                System.clearProperty("vawk.prompts.dir");
            }
        }
    }
}
