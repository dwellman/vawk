package com.vawk.orchestration;

import com.vawk.ai.AiResponse;
import com.vawk.ai.LocalStubAiClient;
import com.vawk.ai.VawkAgents;
import com.vawk.domain.AwkProgram;
import com.vawk.domain.GenerationMode;
import com.vawk.domain.SessionLog;
import com.vawk.domain.VawkSpec;
import com.vawk.runtime.AwkTestRunner;
import com.vawk.store.AgentsFileRepository;
import com.vawk.store.ProgramRepository;
import com.vawk.store.SessionLogRepository;
import com.vawk.store.SpecRepository;
import com.vawk.store.TestRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class VawkRefinementServiceTest {

    @Test
    void refineUsesFallbackSpecWhenMissing() throws Exception {
        StubSpecRepository specRepository = new StubSpecRepository(true);
        StubProgramRepository programRepository = new StubProgramRepository();
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        StubSpecBuilder specBuilder = new StubSpecBuilder(new VawkSpec("fallback", List.of(), List.of(), List.of(), List.of()));

        VawkRefinementService service = new VawkRefinementService(
                new StubAgents(validResponse()),
                specRepository,
                programRepository,
                sessionLogRepository,
                new StubAgentsFileRepository(Optional.empty()),
                specBuilder,
                null,
                new StubAwkTestRunner(),
                new StubTestRepository(List.of()),
                "model"
        );

        GenerateResult result = service.refine("add field", GenerationMode.COT, null, false);

        assertThat(result.getSpec().getDescription()).isEqualTo("fallback");
        assertThat(sessionLogRepository.lastLog.getCommand()).isEqualTo("REFINE");
        assertThat(programRepository.lastProgram.getContent()).contains("# VAWK: demo");
    }

    @Test
    void refineCapturesAutoTestFailure() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();

        VawkRefinementService service = new VawkRefinementService(
                new StubAgents(validResponse()),
                new StubSpecRepository(false),
                new StubProgramRepository(),
                sessionLogRepository,
                new StubAgentsFileRepository(Optional.empty()),
                new StubSpecBuilder(new VawkSpec()),
                null,
                new StubAwkTestRunner(),
                new StubTestRepository(null),
                "model"
        );

        GenerateResult result = service.refine("", GenerationMode.COT, null, true);

        assertThat(result.getSessionLog().getTestsRun()).hasSize(1);
        assertThat(result.getSessionLog().getTestsRun().get(0).isPassed()).isFalse();
    }

    @Test
    void refineUsesModelOverrideAndSkipsEmptyTests() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        StubTestRepository testRepository = new StubTestRepository(List.of());

        VawkRefinementService service = new VawkRefinementService(
                new StubAgents(validResponse()),
                new StubSpecRepository(false),
                new StubProgramRepository(),
                sessionLogRepository,
                new StubAgentsFileRepository(Optional.empty()),
                new StubSpecBuilder(new VawkSpec()),
                null,
                new StubAwkTestRunner(),
                testRepository,
                "model"
        );

        GenerateResult result = service.refine("add field", GenerationMode.COT, "override", true);

        assertThat(result.getSessionLog().getModel()).isEqualTo("override");
        assertThat(result.getSessionLog().getTestsRun()).isEmpty();
    }

    @Test
    void refineUsesRagContextWhenAvailable() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        StubRagRepository ragRepository = new StubRagRepository();

        VawkRefinementService service = new VawkRefinementService(
                new StubAgents(validResponse()),
                new StubSpecRepository(false),
                new StubProgramRepository(),
                sessionLogRepository,
                new StubAgentsFileRepository(Optional.empty()),
                new StubSpecBuilder(new VawkSpec()),
                ragRepository,
                new StubAwkTestRunner(),
                new StubTestRepository(List.of()),
                "model"
        );

        service.refine("expand", GenerationMode.COT, null, false);

        assertThat(ragRepository.getQueries()).contains("expand");
    }

    @Test
    void refineUsesExistingSpecWhenReadSucceeds() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        StubSpecRepository specRepository = new StubSpecRepository(false);

        VawkRefinementService service = new VawkRefinementService(
                new StubAgents(validResponse()),
                specRepository,
                new StubProgramRepository(),
                sessionLogRepository,
                new StubAgentsFileRepository(Optional.empty()),
                new StubSpecBuilder(new VawkSpec("fallback", List.of(), List.of(), List.of(), List.of())),
                null,
                new StubAwkTestRunner(),
                new StubTestRepository(List.of()),
                "model"
        );

        GenerateResult result = service.refine("change", GenerationMode.COT, null, false);

        assertThat(result.getSpec().getDescription()).isEqualTo("spec");
    }

    @Test
    void refineHandlesRagRepositoryErrors() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();

        VawkRefinementService service = new VawkRefinementService(
                new StubAgents(validResponse()),
                new StubSpecRepository(false),
                new StubProgramRepository(),
                sessionLogRepository,
                new StubAgentsFileRepository(Optional.empty()),
                new StubSpecBuilder(new VawkSpec()),
                new FailingRagRepository(),
                new StubAwkTestRunner(),
                new StubTestRepository(List.of()),
                "model"
        );

        GenerateResult result = service.refine("change", GenerationMode.COT, null, false);

        assertThat(result.getSessionLog()).isNotNull();
    }

    @Test
    void refineUsesDefaultModelWhenOverrideBlankAndNullChange() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        CapturingSpecBuilder specBuilder = new CapturingSpecBuilder(new VawkSpec("fallback", List.of(), List.of(), List.of(), List.of()));

        VawkRefinementService service = new VawkRefinementService(
                new StubAgents(validResponse()),
                new StubSpecRepository(true),
                new StubProgramRepository(),
                sessionLogRepository,
                new StubAgentsFileRepository(Optional.empty()),
                specBuilder,
                null,
                new StubAwkTestRunner(),
                new StubTestRepository(List.of()),
                "model"
        );

        GenerateResult result = service.refine(null, GenerationMode.COT, " ", false);

        assertThat(result.getSessionLog().getModel()).isEqualTo("model");
        assertThat(specBuilder.lastDescription).isEqualTo("Refine AWK script");
    }

    @Test
    void refineAutoTestRunsWhenCasesPresent() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        StubTestRepository testRepository = new StubTestRepository(List.of(new com.vawk.domain.AwkTestCase()));

        VawkRefinementService service = new VawkRefinementService(
                new StubAgents(validResponse()),
                new StubSpecRepository(false),
                new StubProgramRepository(),
                sessionLogRepository,
                new StubAgentsFileRepository(Optional.empty()),
                new StubSpecBuilder(new VawkSpec()),
                null,
                new StubAwkTestRunner() {
                    @Override
                    public List<SessionLog.TestRun> runTests(List<com.vawk.domain.AwkTestCase> testCases, AwkProgram defaultProgram) {
                        return List.of(new SessionLog.TestRun("t", true, null));
                    }
                },
                testRepository,
                "model"
        );

        GenerateResult result = service.refine("change", GenerationMode.COT, null, true);

        assertThat(result.getSessionLog().getTestsRun()).hasSize(1);
    }

    private static AiResponse validResponse() {
        String code = "# VAWK: demo\n# Purpose: demo\n# Intent: demo\n# Input: demo\n# Output: demo\nprint \"ok\"";
        return new AiResponse("plan", code, "- test", "notes");
    }

    private static final class StubAgents extends VawkAgents {
        private final AiResponse response;

        private StubAgents(AiResponse response) {
            super(new LocalStubAiClient());
            this.response = response;
        }

        @Override
        public AiResponse refine(String changeRequest, VawkSpec spec, AwkProgram program, String agentsMd, String ragContext, GenerationMode mode) {
            return response;
        }
    }

    private static final class StubSpecBuilder extends VawkSpecBuilder {
        private final VawkSpec spec;

        private StubSpecBuilder(VawkSpec spec) {
            this.spec = spec;
        }

        @Override
        public VawkSpec build(String description, Optional<String> agentsMd, String ragContext) {
            return spec;
        }
    }

    private static final class CapturingSpecBuilder extends VawkSpecBuilder {
        private final VawkSpec spec;
        private String lastDescription;

        private CapturingSpecBuilder(VawkSpec spec) {
            this.spec = spec;
        }

        @Override
        public VawkSpec build(String description, Optional<String> agentsMd, String ragContext) {
            lastDescription = description;
            return spec;
        }
    }

    private static final class StubSpecRepository extends SpecRepository {
        private final boolean throwOnRead;

        private StubSpecRepository(boolean throwOnRead) {
            this.throwOnRead = throwOnRead;
        }

        @Override
        public VawkSpec read() throws IOException {
            if (throwOnRead) {
                throw new IOException("missing");
            }
            return new VawkSpec("spec", List.of(), List.of(), List.of(), List.of());
        }

        @Override
        public void write(VawkSpec spec) {
        }
    }

    private static final class StubProgramRepository extends ProgramRepository {
        private AwkProgram lastProgram;
        private String content = "# VAWK: demo\n# Purpose: demo\n# Intent: demo\n# Input: demo\n# Output: demo\nprint \"ok\"";

        @Override
        public AwkProgram read() {
            lastProgram = new AwkProgram(Path.of("main.awk"), content, "hash");
            return lastProgram;
        }

        @Override
        public void write(String content) {
            this.content = content;
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

    private static class StubAwkTestRunner extends AwkTestRunner {
        private StubAwkTestRunner() {
            super(new com.vawk.runtime.AwkRunner());
        }

        @Override
        public List<SessionLog.TestRun> runTests(List<com.vawk.domain.AwkTestCase> testCases, AwkProgram defaultProgram) {
            return List.of();
        }
    }

    private static final class StubTestRepository extends TestRepository {
        private final List<com.vawk.domain.AwkTestCase> cases;

        private StubTestRepository(List<com.vawk.domain.AwkTestCase> cases) {
            this.cases = cases;
        }

        @Override
        public List<com.vawk.domain.AwkTestCase> load() throws IOException {
            if (cases == null) {
                throw new IOException("boom");
            }
            return cases;
        }
    }

    private static final class StubRagRepository extends com.vawk.store.RagRepository {
        private final java.util.List<String> queries = new java.util.ArrayList<>();

        @Override
        public java.util.List<RagEntry> findRelevant(String query, int limit) {
            queries.add(query);
            return java.util.List.of(new RagEntry("id", "desc", "snippets/demo.md", "snippets"));
        }

        @Override
        public String readContent(RagEntry entry) {
            return "x".repeat(2500);
        }

        private java.util.List<String> getQueries() {
            return queries;
        }
    }

    private static final class FailingRagRepository extends com.vawk.store.RagRepository {
        @Override
        public java.util.List<RagEntry> findRelevant(String query, int limit) throws IOException {
            throw new IOException("boom");
        }
    }
}
