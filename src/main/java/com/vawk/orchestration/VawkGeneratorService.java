package com.vawk.orchestration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vawk.ai.AiResponse;
import com.vawk.ai.VawkAgents;
import com.vawk.domain.AwkProgram;
import com.vawk.domain.GenerationMode;
import com.vawk.domain.SessionLog;
import com.vawk.domain.VawkSpec;
import com.vawk.orchestration.VawkSpecBuilder;
import com.vawk.runtime.AwkTestRunner;
import com.vawk.store.AgentsFileRepository;
import com.vawk.store.ProgramRepository;
import com.vawk.store.RagRepository;
import com.vawk.store.SessionLogRepository;
import com.vawk.store.SpecRepository;
import com.vawk.store.TestRepository;
import com.vawk.util.Hashing;
import com.vawk.util.SectionParser;
import com.vawk.util.SectionValidator;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.nio.file.Paths;

/**
 * Orchestrates the end-to-end generation flow: build spec (with RAG/context), generate plan and AWK
 * code, validate structured output, write program/spec/session log, and optionally auto-test.
 */
@Service
public class VawkGeneratorService {
    private final VawkAgents vawkAgents;
    private final SpecRepository specRepository;
    private final ProgramRepository programRepository;
    private final SessionLogRepository sessionLogRepository;
    private final AgentsFileRepository agentsFileRepository;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final VawkSpecBuilder specBuilder;
    private final String modelName;
    private final RagRepository ragRepository;
    private final AwkTestRunner awkTestRunner;
    private final TestRepository testRepository;

    /**
     * Constructs the generator service with all required repositories, agents, and runners.
     */
    public VawkGeneratorService(VawkAgents vawkAgents,
                                SpecRepository specRepository,
                                ProgramRepository programRepository,
                                SessionLogRepository sessionLogRepository,
                                AgentsFileRepository agentsFileRepository,
                                VawkSpecBuilder specBuilder,
                                RagRepository ragRepository,
                                AwkTestRunner awkTestRunner,
                                TestRepository testRepository,
                                @Value("${vawk.ai.model:local-stub}") String modelName) {
        this.vawkAgents = vawkAgents;
        this.specRepository = specRepository;
        this.programRepository = programRepository;
        this.sessionLogRepository = sessionLogRepository;
        this.agentsFileRepository = agentsFileRepository;
        this.specBuilder = specBuilder;
        this.modelName = modelName;
        this.ragRepository = ragRepository;
        this.awkTestRunner = awkTestRunner;
        this.testRepository = testRepository;
    }

    /**
     * Generates spec/program/log for a description using default settings.
     */
    public GenerateResult generate(String description, GenerationMode mode) throws IOException {
        return generate(description, mode, null, false, "tests");
    }

    /**
     * Full generation pipeline with optional model override and auto-test.
     *
     * @param description   natural language task
     * @param mode          generation mode
     * @param modelOverride optional model name to record
     * @param autoTest      whether to run tests after generation
     * @return result containing spec, program, and session log
     */
    // Pattern: Orchestration + Verification + Learning
    // - Builds spec with RAG, generates PLAN/CODE/TESTS/NOTES, validates structure, and logs receipts.
    public GenerateResult generate(String description, GenerationMode mode, String modelOverride, boolean autoTest, String testsDir) throws IOException {
        Optional<String> agentsMd = agentsFileRepository.read();
        String ragContext = buildRagContext(description);
        VawkSpec spec = specBuilder.build(description, agentsMd, ragContext);
        specRepository.write(spec);

        String prompt = buildPrompt(description, agentsMd.orElse(""));
        AiResponse planResponse = vawkAgents.planFromSpec(spec, agentsMd.orElse(""), ragContext, mode);
        ensurePlan(planResponse);

        AiResponse codeResponse = vawkAgents.awkFromPlan(planResponse.getPlan(), spec, agentsMd.orElse(""), ragContext, mode);
        SectionValidator.requirePlanCodeTestsNotes(codeResponse);

        String code = SectionParser.stripCodeFences(codeResponse.getCode());
        programRepository.write(code);
        AwkProgram program = programRepository.read();

        String specHash = hashSpec(spec);

        SessionLog sessionLog = new SessionLog();
        sessionLog.setId(null);
        sessionLog.setCommand("GEN");
        sessionLog.setMode(mode);
        sessionLog.setTimestamp(Instant.now());
        sessionLog.setModel(modelOverride != null && !modelOverride.isBlank() ? modelOverride : modelName);
        sessionLog.setPlanText(codeResponse.getPlan());
        sessionLog.setCode(code);
        sessionLog.setTestsText(codeResponse.getTests());
        sessionLog.setNotes(codeResponse.getNotes());
        sessionLog.setProgramHash(program.getHash());
        sessionLog.setSpecHash(specHash);
        sessionLog.setTestsRequested(SectionParser.parseTestsList(codeResponse.getTests()));
        if (autoTest) {
            List<SessionLog.TestRun> testRuns = runAutoTests(program, testsDir);
            sessionLog.setTestsRun(testRuns);
        }
        sessionLogRepository.write(sessionLog);

        return new GenerateResult(spec, program, sessionLog);
    }

    private String hashSpec(VawkSpec spec) throws JsonProcessingException {
        byte[] bytes = yamlMapper.writeValueAsBytes(spec);
        String yaml = new String(bytes, StandardCharsets.UTF_8);
        return Hashing.sha256(yaml);
    }

    private String buildPrompt(String description, String agentsMd) {
        StringBuilder promptBuilder = new StringBuilder();
        if (!agentsMd.isEmpty()) {
            promptBuilder.append("GUIDANCE:\n").append(agentsMd).append("\n\n");
        }
        promptBuilder.append("TASK:\n").append(description);
        return promptBuilder.toString();
    }

    private void ensurePlan(AiResponse response) {
        if (response == null || response.getPlan() == null || response.getPlan().isBlank()) {
            throw new IllegalStateException("AI response missing PLAN section");
        }
    }

    // Pattern: Verification + Learning
    // - Runs tests to create PASS/FAIL receipts that feed back into session logs.
    private List<SessionLog.TestRun> runAutoTests(AwkProgram program, String testsDir) {
        try {
            var cases = testRepository.load(Paths.get(testsDir == null || testsDir.isBlank() ? "tests" : testsDir));
            if (cases.isEmpty()) {
                return List.of();
            }
            return awkTestRunner.runTests(cases, program);
        } catch (Exception e) {
            return List.of(new SessionLog.TestRun("auto", false, e.getMessage()));
        }
    }

    // Pattern: Grounding
    // - Selects small, relevant doc snippets to anchor spec generation.
    private String buildRagContext(String query) {
        if (ragRepository == null) {
            return "";
        }
        try {
            List<RagRepository.RagEntry> relevant = ragRepository.findRelevant(query, 3);
            return relevant.stream()
                    .map(entry -> formatRagEntry(entry))
                    .collect(Collectors.joining("\n\n"));
        } catch (IOException e) {
            return "";
        }
    }

    private String formatRagEntry(RagRepository.RagEntry entry) {
        try {
            String content = ragRepository.readContent(entry);
            if (content.length() > 2000) {
                content = content.substring(0, 2000);
            }
            return "## RAG: %s (%s)\n%s".formatted(entry.id, entry.path, content);
        } catch (IOException e) {
            return "## RAG: %s (%s)\n<unavailable>".formatted(entry.id, entry.path);
        }
    }
}
