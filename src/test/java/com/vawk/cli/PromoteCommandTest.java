package com.vawk.cli;

import com.vawk.util.SectionValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromoteCommandTest {

    @TempDir
    Path tempDir;

    @Test
    void promotesValidTurn() throws Exception {
        Path cwd = tempDir;
        Path vawkDir = cwd.resolve(".vawk/chat");
        Files.createDirectories(vawkDir);
        String sessionId = "abc123";
        Path chatFile = vawkDir.resolve(sessionId + ".vawk");
        String content = """
{"kind":"meta","sessionId":"abc123","createdAt":"2025-12-08T00:00:00Z","cwd":"%s","vawkVersion":"0.2.0-SNAPSHOT"}
{"kind":"turn","idx":1,"ts":"2025-12-08T00:00:01Z","role":"assistant","msg":"PLAN:\\n- demo\\n\\nCODE:\\n```awk\\n# VAWK: demo\\n# Purpose: demo\\n# Intent: demo\\n# Input: demo\\n# Output: demo\\nBEGIN { print \\\"ok\\\" }\\n```\\n\\nTESTS:\\n- t\\n\\nNOTES:\\n- n\\n","model":"stub"}
""".formatted(cwd.toString());
        Files.writeString(chatFile, content, StandardCharsets.UTF_8);

        PromoteCommand promote = new PromoteCommand();
        CommandLine cmd = new CommandLine(promote);
        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", cwd.toString());
        try {
            int exit = cmd.execute("--session", sessionId, "--turn", "1", "--name", "demo_job");
            assertEquals(0, exit);
            Path script = cwd.resolve("vawk/jobs/demo_job/script.awk");
            Path spec = cwd.resolve("vawk/jobs/demo_job/spec.yaml");
            assertTrue(Files.exists(script));
            assertTrue(Files.exists(spec));
            String code = Files.readString(script);
            SectionValidator.requirePlanCodeTestsNotes(new com.vawk.ai.AiResponse("p", code, "t", "n"));
            String specText = Files.readString(spec);
            Map<?, ?> yaml = new com.fasterxml.jackson.dataformat.yaml.YAMLMapper().readValue(specText, Map.class);
            assertEquals(sessionId, ((Map<?, ?>) yaml.get("source")).get("sessionId"));
            assertEquals(1, ((Map<?, ?>) yaml.get("source")).get("turnIdx"));
            assertTrue(yaml.get("plan").toString().contains("demo"));
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void failsOnInvalidTurn() throws Exception {
        Path cwd = tempDir;
        Path vawkDir = cwd.resolve(".vawk/chat");
        Files.createDirectories(vawkDir);
        String sessionId = "bad123";
        Path chatFile = vawkDir.resolve(sessionId + ".vawk");
        String content = """
{"kind":"meta","sessionId":"bad123","createdAt":"2025-12-08T00:00:00Z","cwd":"%s","vawkVersion":"0.2.0-SNAPSHOT"}
{"kind":"turn","idx":2,"ts":"2025-12-08T00:00:01Z","role":"assistant","msg":"This is not structured.","model":"stub"}
""".formatted(cwd.toString());
        Files.writeString(chatFile, content, StandardCharsets.UTF_8);

        PromoteCommand promote = new PromoteCommand();
        CommandLine cmd = new CommandLine(promote);
        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", cwd.toString());
        try {
            int exit = cmd.execute("--session", sessionId, "--turn", "2", "--name", "bad_job");
            assertTrue(exit != 0);
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void failsWhenSessionFileMissing() throws Exception {
        Path cwd = tempDir;
        PromoteCommand promote = new PromoteCommand();
        CommandLine cmd = new CommandLine(promote);
        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", cwd.toString());
        try {
            int exit = cmd.execute("--session", "missing", "--turn", "1", "--name", "job");
            assertTrue(exit != 0);
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void failsWhenJobDirectoryExists() throws Exception {
        Path cwd = tempDir;
        Path vawkDir = cwd.resolve(".vawk/chat");
        Files.createDirectories(vawkDir);
        String sessionId = "exists";
        Path chatFile = vawkDir.resolve(sessionId + ".vawk");
        String content = """
{"kind":"meta","sessionId":"exists","createdAt":"2025-12-08T00:00:00Z","cwd":"%s","vawkVersion":"0.2.0-SNAPSHOT"}
{"kind":"turn","idx":1,"ts":"2025-12-08T00:00:01Z","role":"assistant","msg":"PLAN:\\n- demo\\n\\nCODE:\\n```awk\\n# VAWK: demo\\n# Purpose: demo\\n# Intent: demo\\n# Input: demo\\n# Output: demo\\nBEGIN { print \\\"ok\\\" }\\n```\\n\\nTESTS:\\n- t\\n\\nNOTES:\\n- n\\n","model":"stub"}
""".formatted(cwd.toString());
        Files.writeString(chatFile, content, StandardCharsets.UTF_8);

        Path jobDir = cwd.resolve("vawk/jobs/job-exists-1");
        Files.createDirectories(jobDir);

        PromoteCommand promote = new PromoteCommand();
        CommandLine cmd = new CommandLine(promote);
        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", cwd.toString());
        try {
            int exit = cmd.execute("--session", sessionId, "--turn", "1", "--name", "job-exists-1");
            assertTrue(exit != 0);
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void defaultsJobNameWhenMissing() throws Exception {
        Path cwd = tempDir;
        Path vawkDir = cwd.resolve(".vawk/chat");
        Files.createDirectories(vawkDir);
        String sessionId = "abcdefghi";
        Path chatFile = vawkDir.resolve(sessionId + ".vawk");
        String content = """
{"kind":"meta","sessionId":"abcdefghi","createdAt":"2025-12-08T00:00:00Z","cwd":"%s","vawkVersion":"0.2.0-SNAPSHOT"}
{"kind":"turn","idx":3,"ts":"2025-12-08T00:00:01Z","role":"assistant","msg":"PLAN:\\n- demo\\n\\nCODE:\\n```awk\\n# VAWK: demo\\n# Purpose: demo\\n# Intent: demo\\n# Input: demo\\n# Output: demo\\nBEGIN { print \\\"ok\\\" }\\n```\\n\\nTESTS:\\n- t\\n\\nNOTES:\\n- n\\n","model":"stub"}
""".formatted(cwd.toString());
        Files.writeString(chatFile, content, StandardCharsets.UTF_8);

        PromoteCommand promote = new PromoteCommand();
        CommandLine cmd = new CommandLine(promote);
        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", cwd.toString());
        try {
            int exit = cmd.execute("--session", sessionId, "--turn", "3");
            assertTrue(exit == 0);
            Path jobDir = cwd.resolve("vawk/jobs/job-abcdefgh-3");
            assertTrue(Files.exists(jobDir));
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void defaultsJobNameWhenSessionIdShort() throws Exception {
        Path cwd = tempDir;
        Path vawkDir = cwd.resolve(".vawk/chat");
        Files.createDirectories(vawkDir);
        String sessionId = "short";
        Path chatFile = vawkDir.resolve(sessionId + ".vawk");
        String content = """
{"kind":"meta","sessionId":"short","createdAt":"2025-12-08T00:00:00Z","cwd":"%s","vawkVersion":"0.2.0-SNAPSHOT"}
{"kind":"turn","idx":2,"ts":"2025-12-08T00:00:01Z","role":"assistant","msg":"PLAN:\\n- demo\\n\\nCODE:\\n```awk\\n# VAWK: demo\\n# Purpose: demo\\n# Intent: demo\\n# Input: demo\\n# Output: demo\\nBEGIN { print \\\"ok\\\" }\\n```\\n\\nTESTS:\\n- t\\n\\nNOTES:\\n- n\\n","model":"stub"}
""".formatted(cwd.toString());
        Files.writeString(chatFile, content, StandardCharsets.UTF_8);

        PromoteCommand promote = new PromoteCommand();
        CommandLine cmd = new CommandLine(promote);
        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", cwd.toString());
        try {
            int exit = cmd.execute("--session", sessionId, "--turn", "2");
            assertTrue(exit == 0);
            Path jobDir = cwd.resolve("vawk/jobs/job-job-2");
            assertTrue(Files.exists(jobDir));
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void failsWhenAssistantTurnMissing() throws Exception {
        Path cwd = tempDir;
        Path vawkDir = cwd.resolve(".vawk/chat");
        Files.createDirectories(vawkDir);
        String sessionId = "missing-assistant";
        Path chatFile = vawkDir.resolve(sessionId + ".vawk");
        String content = """
{"kind":"meta","sessionId":"missing-assistant","createdAt":"2025-12-08T00:00:00Z","cwd":"%s","vawkVersion":"0.2.0-SNAPSHOT"}
{"kind":"turn","idx":1,"ts":"2025-12-08T00:00:01Z","role":"user","msg":"hi","model":"stub"}
""".formatted(cwd.toString());
        Files.writeString(chatFile, content, StandardCharsets.UTF_8);

        PromoteCommand promote = new PromoteCommand();
        CommandLine cmd = new CommandLine(promote);
        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", cwd.toString());
        try {
            int exit = cmd.execute("--session", sessionId, "--turn", "1", "--name", "bad");
            assertTrue(exit != 0);
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void failsWhenAssistantTurnHasNoMessage() throws Exception {
        Path cwd = tempDir;
        Path vawkDir = cwd.resolve(".vawk/chat");
        Files.createDirectories(vawkDir);
        String sessionId = "missing-msg";
        Path chatFile = vawkDir.resolve(sessionId + ".vawk");
        String content = """
{"kind":"meta","sessionId":"missing-msg","createdAt":"2025-12-08T00:00:00Z","cwd":"%s","vawkVersion":"0.2.0-SNAPSHOT"}
{"kind":"turn","idx":4,"ts":"2025-12-08T00:00:01Z","role":"assistant","model":"stub"}
""".formatted(cwd.toString());
        Files.writeString(chatFile, content, StandardCharsets.UTF_8);

        PromoteCommand promote = new PromoteCommand();
        CommandLine cmd = new CommandLine(promote);
        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", cwd.toString());
        try {
            int exit = cmd.execute("--session", sessionId, "--turn", "4", "--name", "bad");
            assertTrue(exit != 0);
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void failsWhenTurnIndexIsNotNumeric() throws Exception {
        Path cwd = tempDir;
        Path vawkDir = cwd.resolve(".vawk/chat");
        Files.createDirectories(vawkDir);
        String sessionId = "bad-idx";
        Path chatFile = vawkDir.resolve(sessionId + ".vawk");
        String content = """
{"kind":"meta","sessionId":"bad-idx","createdAt":"2025-12-08T00:00:00Z","cwd":"%s","vawkVersion":"0.2.0-SNAPSHOT"}
{"kind":"turn","idx":"oops","ts":"2025-12-08T00:00:01Z","role":"assistant","msg":"PLAN:\\n- demo\\n\\nCODE:\\n```awk\\n# VAWK: demo\\n# Purpose: demo\\n# Intent: demo\\n# Input: demo\\n# Output: demo\\nBEGIN { print \\\"ok\\\" }\\n```\\n\\nTESTS:\\n- t\\n\\nNOTES:\\n- n\\n","model":"stub"}
""".formatted(cwd.toString());
        Files.writeString(chatFile, content, StandardCharsets.UTF_8);

        PromoteCommand promote = new PromoteCommand();
        CommandLine cmd = new CommandLine(promote);
        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", cwd.toString());
        try {
            int exit = cmd.execute("--session", sessionId, "--turn", "1", "--name", "bad");
            assertTrue(exit != 0);
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void defaultsJobNameWhenNameBlank() throws Exception {
        Path cwd = tempDir;
        Path vawkDir = cwd.resolve(".vawk/chat");
        Files.createDirectories(vawkDir);
        String sessionId = "blankname";
        Path chatFile = vawkDir.resolve(sessionId + ".vawk");
        String content = """
{"kind":"meta","sessionId":"blankname","createdAt":"2025-12-08T00:00:00Z","cwd":"%s","vawkVersion":"0.2.0-SNAPSHOT"}
{"kind":"turn","idx":5,"ts":"2025-12-08T00:00:01Z","role":"assistant","msg":"PLAN:\\n- demo\\n\\nCODE:\\n```awk\\n# VAWK: demo\\n# Purpose: demo\\n# Intent: demo\\n# Input: demo\\n# Output: demo\\nBEGIN { print \\\"ok\\\" }\\n```\\n\\nTESTS:\\n- t\\n\\nNOTES:\\n- n\\n","model":"stub"}
""".formatted(cwd.toString());
        Files.writeString(chatFile, content, StandardCharsets.UTF_8);

        PromoteCommand promote = new PromoteCommand();
        CommandLine cmd = new CommandLine(promote);
        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", cwd.toString());
        try {
            int exit = cmd.execute("--session", sessionId, "--turn", "5", "--name", " ");
            assertTrue(exit == 0);
            Path jobDir = cwd.resolve("vawk/jobs/job-blanknam-5");
            assertTrue(Files.exists(jobDir));
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void failsWhenTurnIndexMissing() throws Exception {
        Path cwd = tempDir;
        Path vawkDir = cwd.resolve(".vawk/chat");
        Files.createDirectories(vawkDir);
        String sessionId = "no-idx";
        Path chatFile = vawkDir.resolve(sessionId + ".vawk");
        String content = """
{"kind":"meta","sessionId":"no-idx","createdAt":"2025-12-08T00:00:00Z","cwd":"%s","vawkVersion":"0.2.0-SNAPSHOT"}

{"kind":"turn","ts":"2025-12-08T00:00:01Z","role":"assistant","msg":"hi","model":"stub"}
""".formatted(cwd.toString());
        Files.writeString(chatFile, content, StandardCharsets.UTF_8);

        PromoteCommand promote = new PromoteCommand();
        CommandLine cmd = new CommandLine(promote);
        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", cwd.toString());
        try {
            int exit = cmd.execute("--session", sessionId, "--turn", "1", "--name", "bad");
            assertTrue(exit != 0);
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }
}
