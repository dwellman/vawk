package com.vawk.cli;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.vawk.chat.VawkChatService;
import com.vawk.util.SectionParser;
import com.vawk.util.SectionValidator;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * CLI command to promote a structured chat turn (PLAN/CODE/TESTS/NOTES with AWK header) into a
 * runnable VAWK job. Reads the chat log, validates the selected assistant turn, and writes
 * script/spec artifacts under vawk/jobs for later execution and traceability.
 */
@Component
@Command(name = "promote", description = "Promote a structured chat turn into a VAWK job")
public class PromoteCommand implements java.util.concurrent.Callable<Integer> {

    @Option(names = "--session", required = true, description = "Session id from .vawk/chat/<sessionId>.vawk")
    String sessionId;

    @Option(names = "--turn", required = true, description = "Assistant turn idx to promote")
    int turnIdx;

    @Option(names = "--name", description = "Job name (short id)")
    String name;

    @Override
    // Pattern: Orchestration + Verification
    // - Promotes only valid structured turns into jobs and rejects malformed outputs.
    public Integer call() {
        try {
            String jobName = (name == null || name.isBlank()) ? defaultName() : name;
            Path base = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath();
            Path chatFile = base.resolve(".vawk").resolve("chat").resolve(sessionId + ".vawk");
            if (!Files.exists(chatFile)) {
                System.err.println("[vawk] Error: session file not found: " + chatFile);
                return 1;
            }
            TurnData turn = findTurn(chatFile, turnIdx);
            var response = SectionParser.parse(turn.msg);
            try {
                SectionValidator.requirePlanCodeTestsNotes(response);
            } catch (Exception e) {
                System.err.println("[vawk] Error: turn " + turnIdx + " is not a valid PLAN/CODE/TESTS/NOTES reply.");
                System.err.println("[vawk] Reason: " + (e.getMessage() == null ? "Invalid structured format" : e.getMessage()));
                return 1;
            }

            Path jobDir = base.resolve("vawk").resolve("jobs").resolve(jobName);
            if (Files.exists(jobDir)) {
                System.err.println("[vawk] Error: job directory already exists: " + jobDir);
                return 1;
            }
            Files.createDirectories(jobDir);

            // script.awk
            String code = SectionParser.stripCodeFences(response.getCode());
            Path script = jobDir.resolve("script.awk");
            Files.writeString(script, code, StandardCharsets.UTF_8);

            // spec.yaml
            Path spec = jobDir.resolve("spec.yaml");
            YAMLMapper mapper = new YAMLMapper();
            Map<String, Object> specMap = Map.of(
                    "id", jobName,
                    "source", Map.of(
                            "sessionId", sessionId,
                            "turnIdx", turnIdx,
                            "promotedAt", Instant.now().toString()
                    ),
                    "plan", response.getPlan(),
                    "tests", response.getTests()
            );
            Files.writeString(spec, mapper.writeValueAsString(specMap), StandardCharsets.UTF_8);

            System.out.println("[vawk] Promoted chat turn " + turnIdx + " from session " + sessionId + " into vawk/jobs/" + jobName);
            return 0;
        } catch (Exception e) {
            System.err.println("[vawk] Error promoting chat turn: " + e.getMessage());
            return 1;
        }
    }

    private TurnData findTurn(Path chatFile, int targetIdx) throws IOException {
        List<String> lines = Files.readAllLines(chatFile, StandardCharsets.UTF_8);
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            if (!line.contains("\"kind\":\"turn\"")) continue;
            int idxPos = line.indexOf("\"idx\":");
            if (idxPos >= 0) {
                int start = idxPos + 6;
                int end = line.indexOf(",", start);
                int idx = Integer.parseInt(line.substring(start, end));
                if (idx == targetIdx && line.contains("\"role\":\"assistant\"")) {
                    int msgPos = line.indexOf("\"msg\":\"");
                    if (msgPos >= 0) {
                        int msgStart = msgPos + 7;
                        int msgEnd = line.lastIndexOf("\"");
                        String msg = line.substring(msgStart, msgEnd);
                        msg = msg.replace("\\n", "\n").replace("\\\"", "\"");
                        return new TurnData(idx, msg);
                    }
                }
            }
        }
        throw new IOException("Assistant turn idx " + targetIdx + " not found in " + chatFile);
    }

    private String defaultName() {
        String base = sessionId != null && sessionId.length() > 8 ? sessionId.substring(0, 8) : "job";
        return "job-" + base + "-" + turnIdx;
    }

    private record TurnData(int idx, String msg) {}
}
