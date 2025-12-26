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
import static org.junit.jupiter.api.Assertions.assertThrows;

class VawkGeneratorServiceTest {

    @Test
    void generateWritesSessionLogWithoutAutoTest() throws Exception {
        StubSpecRepository specRepository = new StubSpecRepository();
        StubProgramRepository programRepository = new StubProgramRepository();
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        StubAgentsFileRepository agentsRepository = new StubAgentsFileRepository(Optional.empty());
        StubSpecBuilder specBuilder = new StubSpecBuilder(new VawkSpec("desc", List.of(), List.of(), List.of(), List.of()));
        StubTestRepository testRepository = new StubTestRepository(List.of());

        VawkGeneratorService service = new VawkGeneratorService(
                new StubAgents(validPlanResponse(), validCodeResponse()),
                specRepository,
                programRepository,
                sessionLogRepository,
                agentsRepository,
                specBuilder,
                null,
                new StubAwkTestRunner(),
                testRepository,
                "model"
        );

        GenerateResult result = service.generate("task", GenerationMode.COT, null, false, "tests");

        assertThat(result.getSessionLog().getPlanText()).isEqualTo("plan");
        assertThat(sessionLogRepository.lastLog).isNotNull();
        assertThat(programRepository.lastProgram.getContent()).contains("# VAWK: demo");
        assertThat(specRepository.lastSpec.getDescription()).isEqualTo("desc");
    }

    @Test
    void generateThrowsWhenPlanMissing() {
        VawkGeneratorService service = new VawkGeneratorService(
                new StubAgents(new AiResponse("", "", "", ""), validCodeResponse()),
                new StubSpecRepository(),
                new StubProgramRepository(),
                new StubSessionLogRepository(),
                new StubAgentsFileRepository(Optional.empty()),
                new StubSpecBuilder(new VawkSpec()),
                null,
                new StubAwkTestRunner(),
                new StubTestRepository(List.of()),
                "model"
        );

        assertThrows(IllegalStateException.class, () -> service.generate("task", GenerationMode.COT, null, false, "tests"));
    }

    @Test
    void generateAutoTestFailureIsCaptured() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();

        VawkGeneratorService service = new VawkGeneratorService(
                new StubAgents(validPlanResponse(), validCodeResponse()),
                new StubSpecRepository(),
                new StubProgramRepository(),
                sessionLogRepository,
                new StubAgentsFileRepository(Optional.empty()),
                new StubSpecBuilder(new VawkSpec()),
                null,
                new StubAwkTestRunner(),
                new StubTestRepository(null),
                "model"
        );

        GenerateResult result = service.generate("task", GenerationMode.COT, null, true, "tests");

        assertThat(result.getSessionLog().getTestsRun()).hasSize(1);
        assertThat(result.getSessionLog().getTestsRun().get(0).isPassed()).isFalse();
    }

    @Test
    void generateAutoTestSuccessCapturesRuns() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        StubTestRepository testRepository = new StubTestRepository(List.of(new com.vawk.domain.AwkTestCase()));

        VawkGeneratorService service = new VawkGeneratorService(
                new StubAgents(validPlanResponse(), validCodeResponse()),
                new StubSpecRepository(),
                new StubProgramRepository(),
                sessionLogRepository,
                new StubAgentsFileRepository(Optional.of("by_user")),
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

        GenerateResult result = service.generate("task", GenerationMode.COT, null, true, "tests");

        assertThat(result.getSessionLog().getTestsRun()).hasSize(1);
        assertThat(result.getSessionLog().getTestsRun().get(0).isPassed()).isTrue();
    }

    @Test
    void generateUsesModelOverride() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();

        VawkGeneratorService service = new VawkGeneratorService(
                new StubAgents(validPlanResponse(), validCodeResponse()),
                new StubSpecRepository(),
                new StubProgramRepository(),
                sessionLogRepository,
                new StubAgentsFileRepository(Optional.empty()),
                new StubSpecBuilder(new VawkSpec()),
                null,
                new StubAwkTestRunner(),
                new StubTestRepository(List.of()),
                "model"
        );

        GenerateResult result = service.generate("task", GenerationMode.COT, "override", false, "tests");

        assertThat(result.getSessionLog().getModel()).isEqualTo("override");
    }

    @Test
    void generateUsesDefaultModelWhenOverrideBlank() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();

        VawkGeneratorService service = new VawkGeneratorService(
                new StubAgents(validPlanResponse(), validCodeResponse()),
                new StubSpecRepository(),
                new StubProgramRepository(),
                sessionLogRepository,
                new StubAgentsFileRepository(Optional.empty()),
                new StubSpecBuilder(new VawkSpec()),
                null,
                new StubAwkTestRunner(),
                new StubTestRepository(List.of()),
                "model"
        );

        GenerateResult result = service.generate("task", GenerationMode.COT, " ", false, "tests");

        assertThat(result.getSessionLog().getModel()).isEqualTo("model");
    }

    @Test
    void generateAutoTestSkipsWhenNoCases() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        StubTestRepository testRepository = new StubTestRepository(List.of());

        VawkGeneratorService service = new VawkGeneratorService(
                new StubAgents(validPlanResponse(), validCodeResponse()),
                new StubSpecRepository(),
                new StubProgramRepository(),
                sessionLogRepository,
                new StubAgentsFileRepository(Optional.empty()),
                new StubSpecBuilder(new VawkSpec()),
                null,
                new StubAwkTestRunner(),
                testRepository,
                "model"
        );

        GenerateResult result = service.generate("task", GenerationMode.COT, null, true, "tests");

        assertThat(result.getSessionLog().getTestsRun()).isEmpty();
    }

    @Test
    void generateUsesRagContextWhenAvailable() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        StubRagRepository ragRepository = new StubRagRepository();

        VawkGeneratorService service = new VawkGeneratorService(
                new StubAgents(validPlanResponse(), validCodeResponse()),
                new StubSpecRepository(),
                new StubProgramRepository(),
                sessionLogRepository,
                new StubAgentsFileRepository(Optional.empty()),
                new StubSpecBuilder(new VawkSpec()),
                ragRepository,
                new StubAwkTestRunner(),
                new StubTestRepository(List.of()),
                "model"
        );

        service.generate("task", GenerationMode.COT, null, false, "tests");

        assertThat(ragRepository.getQueries()).contains("task");
    }

    @Test
    void generateHandlesRagRepositoryErrors() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();

        VawkGeneratorService service = new VawkGeneratorService(
                new StubAgents(validPlanResponse(), validCodeResponse()),
                new StubSpecRepository(),
                new StubProgramRepository(),
                sessionLogRepository,
                new StubAgentsFileRepository(Optional.empty()),
                new StubSpecBuilder(new VawkSpec()),
                new FailingRagRepository(),
                new StubAwkTestRunner(),
                new StubTestRepository(List.of()),
                "model"
        );

        GenerateResult result = service.generate("task", GenerationMode.COT, null, false, "tests");

        assertThat(result.getSessionLog()).isNotNull();
    }

    @Test
    void generateTrimsLongRagEntries() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        CapturingAgents agents = new CapturingAgents(validPlanResponse(), validCodeResponse());
        LongRagRepository ragRepository = new LongRagRepository();

        VawkGeneratorService service = new VawkGeneratorService(
                agents,
                new StubSpecRepository(),
                new StubProgramRepository(),
                sessionLogRepository,
                new StubAgentsFileRepository(Optional.empty()),
                new StubSpecBuilder(new VawkSpec()),
                ragRepository,
                new StubAwkTestRunner(),
                new StubTestRepository(List.of()),
                "model"
        );

        service.generate("task", GenerationMode.COT, null, false, "tests");

        assertThat(agents.lastRagContext).contains("## RAG:");
        assertThat(agents.lastRagContext.length()).isLessThan(2200);
    }

    private static AiResponse validPlanResponse() {
        return new AiResponse("plan", "", "", "");
    }

    private static AiResponse validCodeResponse() {
        String code = "# VAWK: demo\n# Purpose: demo\n# Intent: demo\n# Input: demo\n# Output: demo\nprint \"ok\"";
        return new AiResponse("plan", code, "- test", "notes");
    }

    private static final class StubAgents extends VawkAgents {
        private final AiResponse plan;
        private final AiResponse code;

        private StubAgents(AiResponse plan, AiResponse code) {
            super(new LocalStubAiClient());
            this.plan = plan;
            this.code = code;
        }

        @Override
        public AiResponse planFromSpec(VawkSpec spec, String agentsMd, String ragContext, GenerationMode mode) {
            return plan;
        }

        @Override
        public AiResponse awkFromPlan(String plan, VawkSpec spec, String agentsMd, String ragContext, GenerationMode mode) {
            return code;
        }
    }

    private static final class CapturingAgents extends VawkAgents {
        private final AiResponse plan;
        private final AiResponse code;
        private String lastRagContext;

        private CapturingAgents(AiResponse plan, AiResponse code) {
            super(new LocalStubAiClient());
            this.plan = plan;
            this.code = code;
        }

        @Override
        public AiResponse planFromSpec(VawkSpec spec, String agentsMd, String ragContext, GenerationMode mode) {
            lastRagContext = ragContext;
            return plan;
        }

        @Override
        public AiResponse awkFromPlan(String plan, VawkSpec spec, String agentsMd, String ragContext, GenerationMode mode) {
            lastRagContext = ragContext;
            return code;
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

    private static final class StubSpecRepository extends SpecRepository {
        private VawkSpec lastSpec;

        @Override
        public void write(VawkSpec spec) {
            this.lastSpec = spec;
        }
    }

    private static final class StubProgramRepository extends ProgramRepository {
        private AwkProgram lastProgram;
        private String content;

        @Override
        public void write(String content) {
            this.content = content;
        }

        @Override
        public AwkProgram read() {
            if (content == null) {
                content = "";
            }
            lastProgram = new AwkProgram(Path.of("main.awk"), content, "hash");
            return lastProgram;
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
        public List<com.vawk.domain.AwkTestCase> load(java.nio.file.Path testsDir) throws IOException {
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
            return "content";
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

    private static final class LongRagRepository extends com.vawk.store.RagRepository {
        @Override
        public java.util.List<RagEntry> findRelevant(String query, int limit) {
            return java.util.List.of(new RagEntry("id", "desc", "snippets/demo.md", "snippets"));
        }

        @Override
        public String readContent(RagEntry entry) {
            return "x".repeat(3000);
        }
    }
}
