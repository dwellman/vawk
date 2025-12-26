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

/**
 * Refines existing AWK programs based on change requests: reuses spec/program, calls AI to produce
 * updated PLAN/CODE/TESTS/NOTES, validates, writes outputs, and logs/tests results.
 */
@Service
public class VawkRefinementService {
    private final VawkAgents vawkAgents;
    private final SpecRepository specRepository;
    private final ProgramRepository programRepository;
    private final SessionLogRepository sessionLogRepository;
    private final AgentsFileRepository agentsFileRepository;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final VawkSpecBuilder specBuilder;
    private final RagRepository ragRepository;
    private final AwkTestRunner awkTestRunner;
    private final TestRepository testRepository;
    private final String modelName;

    /**
     * Constructs the refinement service with repositories, agents, and runners.
     */
    public VawkRefinementService(VawkAgents vawkAgents,
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
        this.ragRepository = ragRepository;
        this.awkTestRunner = awkTestRunner;
        this.testRepository = testRepository;
        this.modelName = modelName;
    }

    /**
     * Runs the refinement pipeline for a change request with optional model override and auto-test.
     *
     * @param changeRequest description of requested code change
     * @param mode generation mode
     * @param modelOverride optional model name to record
     * @param autoTest whether to run tests after refinement
     */
    // Pattern: Orchestration + Learning
    // - Reuses prior spec/program, generates new PLAN/CODE/TESTS/NOTES, and logs iteration results.
    public GenerateResult refine(String changeRequest, GenerationMode mode, String modelOverride, boolean autoTest) throws IOException {
        Optional<String> agentsMd = agentsFileRepository.read();
        String ragContext = buildRagContext(changeRequest);
        VawkSpec spec = readSpecOrDefault(descriptionFromChange(changeRequest), agentsMd, ragContext);
        AwkProgram program = programRepository.read();

        AiResponse response = vawkAgents.refine(changeRequest, spec, program, agentsMd.orElse(""), ragContext, mode);
        SectionValidator.requirePlanCodeTestsNotes(response);

        String code = SectionParser.stripCodeFences(response.getCode());
        programRepository.write(code);
        AwkProgram updatedProgram = programRepository.read();

        specRepository.write(spec);
        String specHash = hashSpec(spec);

        SessionLog log = new SessionLog();
        log.setCommand("REFINE");
        log.setMode(mode);
        log.setTimestamp(Instant.now());
        log.setModel(modelOverride != null && !modelOverride.isBlank() ? modelOverride : modelName);
        log.setPlanText(response.getPlan());
        log.setCode(code);
        log.setTestsText(response.getTests());
        log.setNotes(response.getNotes());
        log.setProgramHash(updatedProgram.getHash());
        log.setSpecHash(specHash);
        log.setTestsRequested(SectionParser.parseTestsList(response.getTests()));
        if (autoTest) {
            List<SessionLog.TestRun> runs = runAutoTests(updatedProgram);
            log.setTestsRun(runs);
        }
        sessionLogRepository.write(log);

        return new GenerateResult(spec, updatedProgram, log);
    }

    private VawkSpec readSpecOrDefault(String description, Optional<String> agentsMd, String ragContext) {
        try {
            return specRepository.read();
        } catch (IOException e) {
            return specBuilder.build(description, agentsMd, ragContext);
        }
    }

    private String hashSpec(VawkSpec spec) throws JsonProcessingException {
        byte[] bytes = yamlMapper.writeValueAsBytes(spec);
        return Hashing.sha256(new String(bytes, StandardCharsets.UTF_8));
    }

    private String descriptionFromChange(String changeRequest) {
        if (changeRequest == null || changeRequest.isBlank()) {
            return "Refine AWK script";
        }
        return "Refine: " + changeRequest;
    }

    private List<SessionLog.TestRun> runAutoTests(AwkProgram program) {
        try {
            var cases = testRepository.load();
            if (cases.isEmpty()) {
                return List.of();
            }
            return awkTestRunner.runTests(cases, program);
        } catch (Exception e) {
            return List.of(new SessionLog.TestRun("auto", false, e.getMessage()));
        }
    }

    // Pattern: Grounding
    // - Pulls a small, relevant context slice to constrain refinement requests.
    private String buildRagContext(String query) {
        if (ragRepository == null) {
            return "";
        }
        try {
            List<RagRepository.RagEntry> relevant = ragRepository.findRelevant(query, 3);
            StringBuilder contextBuilder = new StringBuilder();
            for (RagRepository.RagEntry entry : relevant) {
                try {
                    String content = ragRepository.readContent(entry);
                    if (content.length() > 2000) content = content.substring(0, 2000);
                    contextBuilder.append("## RAG: ").append(entry.id).append(" (").append(entry.path).append(")\n").append(content).append("\n\n");
                } catch (IOException ignored) {
                }
            }
            return contextBuilder.toString().trim();
        } catch (IOException e) {
            return "";
        }
    }
}
