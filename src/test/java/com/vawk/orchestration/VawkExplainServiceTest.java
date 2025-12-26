package com.vawk.orchestration;

import com.vawk.ai.AiResponse;
import com.vawk.ai.LocalStubAiClient;
import com.vawk.ai.VawkAgents;
import com.vawk.domain.AwkProgram;
import com.vawk.domain.SessionLog;
import com.vawk.store.AgentsFileRepository;
import com.vawk.store.ProgramRepository;
import com.vawk.store.SessionLogRepository;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VawkExplainServiceTest {

    @Test
    void explainWritesLogWhenPlanPresent() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        VawkExplainService service = new VawkExplainService(
                new StubAgents(validResponse()),
                new StubAgentsFileRepository(Optional.empty()),
                new ProgramRepository(),
                sessionLogRepository,
                "model"
        );

        AiResponse response = service.explain(new AwkProgram(Path.of("main.awk"), "code", "hash"), null);

        assertThat(response.getPlan()).isEqualTo("plan");
        assertThat(sessionLogRepository.lastLog).isNotNull();
    }

    @Test
    void explainUsesModelOverrideAndSpecHash(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        VawkExplainService service = new VawkExplainService(
                new StubAgents(validResponse()),
                new StubAgentsFileRepository(Optional.of("agents")),
                new ProgramRepository(),
                sessionLogRepository,
                "model"
        );

        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            java.nio.file.Files.writeString(tempDir.resolve("spec.yaml"), "description: demo");

            service.explain(new AwkProgram(Path.of("main.awk"), "code", "hash"), "override");

            assertThat(sessionLogRepository.lastLog.getModel()).isEqualTo("override");
            assertThat(sessionLogRepository.lastLog.getSpecHash()).isNotBlank();
        } finally {
            if (originalDir != null) {
                System.setProperty("user.dir", originalDir);
            }
        }
    }

    @Test
    void explainThrowsWhenPlanMissing() {
        VawkExplainService service = new VawkExplainService(
                new StubAgents(new AiResponse("", "", "", "")),
                new StubAgentsFileRepository(Optional.empty()),
                new ProgramRepository(),
                new StubSessionLogRepository(),
                "model"
        );

        assertThrows(IllegalStateException.class, () -> service.explain(new AwkProgram(Path.of("main.awk"), "code", "hash"), null));
    }

    @Test
    void explainThrowsWhenResponseNull() {
        VawkExplainService service = new VawkExplainService(
                new StubAgents(null),
                new StubAgentsFileRepository(Optional.empty()),
                new ProgramRepository(),
                new StubSessionLogRepository(),
                "model"
        );

        assertThrows(IllegalStateException.class, () -> service.explain(new AwkProgram(Path.of("main.awk"), "code", "hash"), null));
    }

    @Test
    void explainUsesDefaultModelWhenOverrideBlank() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        VawkExplainService service = new VawkExplainService(
                new StubAgents(validResponse()),
                new StubAgentsFileRepository(Optional.empty()),
                new ProgramRepository(),
                sessionLogRepository,
                "model"
        );

        service.explain(new AwkProgram(Path.of("main.awk"), "code", "hash"), " ");

        assertThat(sessionLogRepository.lastLog.getModel()).isEqualTo("model");
    }

    private static AiResponse validResponse() {
        return new AiResponse("plan", "code", "tests", "notes");
    }

    private static final class StubAgents extends VawkAgents {
        private final AiResponse response;

        private StubAgents(AiResponse response) {
            super(new LocalStubAiClient());
            this.response = response;
        }

        @Override
        public AiResponse explain(AwkProgram program, String agentsMd, String ragContext) {
            return response;
        }
    }

    private static final class StubSessionLogRepository extends SessionLogRepository {
        private SessionLog lastLog;

        @Override
        public Path write(SessionLog log) {
            this.lastLog = log;
            return Path.of(".vawk/logs/log.json");
        }
    }

    private static final class StubAgentsFileRepository extends AgentsFileRepository {
        private final Optional<String> agents;

        private StubAgentsFileRepository(Optional<String> agents) {
            this.agents = agents;
        }

        @Override
        public Optional<String> read() {
            return agents;
        }
    }
}
