package com.vawk.orchestration;

import com.vawk.ai.AiResponse;
import com.vawk.ai.VawkAgents;
import com.vawk.domain.AwkProgram;
import com.vawk.domain.GenerationMode;
import com.vawk.domain.SessionLog;
import com.vawk.store.AgentsFileRepository;
import com.vawk.store.ProgramRepository;
import com.vawk.store.SessionLogRepository;
import com.vawk.util.Hashing;
import com.vawk.util.SectionParser;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;

/**
 * Coordinates explanation runs: loads program/spec context, calls the explain agent, validates PLAN
 * presence, and records a SessionLog.
 */
@Service
public class VawkExplainService {
    private final VawkAgents vawkAgents;
    private final AgentsFileRepository agentsFileRepository;
    private final ProgramRepository programRepository;
    private final SessionLogRepository sessionLogRepository;
    private final String modelName;

    /**
     * Wires dependencies for explanation runs.
     */
    public VawkExplainService(VawkAgents vawkAgents,
                              AgentsFileRepository agentsFileRepository,
                              ProgramRepository programRepository,
                              SessionLogRepository sessionLogRepository,
                              @Value("${vawk.ai.model:local-stub}") String modelName) {
        this.vawkAgents = vawkAgents;
        this.agentsFileRepository = agentsFileRepository;
        this.programRepository = programRepository;
        this.sessionLogRepository = sessionLogRepository;
        this.modelName = modelName;
    }

    /**
     * Explains an AWK program via AI, ensures PLAN exists, and writes a SessionLog.
     *
     * @param program AWK program to explain
     * @param modelOverride optional model name to record
     * @return AI response with PLAN/CODE/TESTS/NOTES for the explanation
     */
    public AiResponse explain(AwkProgram program, String modelOverride) throws IOException {
        Optional<String> agentsMd = agentsFileRepository.read();
        AiResponse response = vawkAgents.explain(program, agentsMd.orElse(""), "");
        ensurePlan(response);

        SessionLog log = new SessionLog();
        log.setCommand("EXPLAIN");
        log.setMode(GenerationMode.SINGLE);
        log.setTimestamp(Instant.now());
        log.setModel(modelOverride != null && !modelOverride.isBlank() ? modelOverride : modelName);
        log.setPlanText(response.getPlan());
        log.setCode(response.getCode());
        log.setTestsText(response.getTests());
        log.setNotes(response.getNotes());
        log.setProgramHash(program.getHash());
        if (Files.exists(Paths.get("spec.yaml"))) {
            byte[] specBytes = Files.readAllBytes(Paths.get("spec.yaml"));
            log.setSpecHash(Hashing.sha256(new String(specBytes, StandardCharsets.UTF_8)));
        }
        log.setTestsRequested(SectionParser.parseTestsList(response.getTests()));
        sessionLogRepository.write(log);

        return response;
    }

    private void ensurePlan(AiResponse response) {
        if (response == null || response.getPlan() == null || response.getPlan().isBlank()) {
            throw new IllegalStateException("AI response missing PLAN section");
        }
    }
}
