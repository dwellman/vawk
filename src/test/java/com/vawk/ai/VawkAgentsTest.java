package com.vawk.ai;

import com.vawk.domain.AwkProgram;
import com.vawk.domain.GenerationMode;
import com.vawk.domain.VawkSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VawkAgentsTest {

    @Test
    void specFromVibeIncludesRagContext() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.specFromVibe("sum columns", "", "RAG TEXT", GenerationMode.COT);

        assertThat(client.lastPrompt).contains("RAG CONTEXT");
        assertThat(client.lastSystem).contains("Mode: COT");
    }

    @Test
    void buildSystemPromptIncludesGuidance() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.planFromSpec(new VawkSpec(), "guidance", "", GenerationMode.SINGLE);

        assertThat(client.lastSystem).contains("GUIDANCE");
        assertThat(client.lastSystem).contains("Mode: SINGLE");
    }

    @Test
    void refineUsesProgramContent() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.refine("change", new VawkSpec(), new AwkProgram(null, "code", "hash"), "", "", GenerationMode.COT);

        assertThat(client.lastPrompt).contains("CURRENT CODE");
    }

    @Test
    void specFromVibeOmitsRagWhenBlank() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.specFromVibe("sum columns", "", "", GenerationMode.SINGLE);

        assertThat(client.lastPrompt).doesNotContain("RAG CONTEXT");
    }

    @Test
    void specFromVibeOmitsRagWhenNull() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.specFromVibe("sum columns", "", null, GenerationMode.SINGLE);

        assertThat(client.lastPrompt).doesNotContain("RAG CONTEXT");
    }

    @Test
    void buildSystemPromptDefaultsModeWhenNull() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.planFromSpec(new VawkSpec(), "guidance", "", null);

        assertThat(client.lastSystem).contains("Mode: SINGLE");
    }

    @Test
    void buildSystemPromptOmitsGuidanceWhenBlank() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.planFromSpec(new VawkSpec(), "  ", "", GenerationMode.SINGLE);

        assertThat(client.lastSystem).doesNotContain("GUIDANCE");
    }

    @Test
    void buildSystemPromptOmitsGuidanceWhenNull() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.planFromSpec(new VawkSpec(), null, "", GenerationMode.SINGLE);

        assertThat(client.lastSystem).doesNotContain("GUIDANCE");
    }

    @Test
    void awkFromPlanIncludesRagContext() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.awkFromPlan("plan", new VawkSpec(), "", "RAG", GenerationMode.COT);

        assertThat(client.lastPrompt).contains("RAG CONTEXT");
    }

    @Test
    void awkFromPlanOmitsRagWhenBlank() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.awkFromPlan("plan", new VawkSpec(), "", " ", GenerationMode.COT);

        assertThat(client.lastPrompt).doesNotContain("RAG CONTEXT");
    }

    @Test
    void planFromSpecIncludesRagContext() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.planFromSpec(new VawkSpec(), "", "RAG", GenerationMode.COT);

        assertThat(client.lastPrompt).contains("RAG CONTEXT");
    }

    @Test
    void planFromSpecOmitsRagWhenBlank() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.planFromSpec(new VawkSpec(), "", " ", GenerationMode.COT);

        assertThat(client.lastPrompt).doesNotContain("RAG CONTEXT");
    }

    @Test
    void explainIncludesRagContextWhenProvided() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.explain(new AwkProgram(null, "code", "hash"), "", "RAG");

        assertThat(client.lastPrompt).contains("RAG CONTEXT");
    }

    @Test
    void explainOmitsRagWhenBlank() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.explain(new AwkProgram(null, "code", "hash"), "", " ");

        assertThat(client.lastPrompt).doesNotContain("RAG CONTEXT");
    }

    @Test
    void lintOmitsRagWhenBlank() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.lint(new AwkProgram(null, "code", "hash"), "", " ");

        assertThat(client.lastPrompt).doesNotContain("RAG CONTEXT");
    }

    @Test
    void lintIncludesRagWhenProvided() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.lint(new AwkProgram(null, "code", "hash"), "", "RAG");

        assertThat(client.lastPrompt).contains("RAG CONTEXT");
    }

    @Test
    void refineIncludesRagContextWhenProvided() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.refine("change", new VawkSpec(), new AwkProgram(null, "code", "hash"), "", "RAG", GenerationMode.COT);

        assertThat(client.lastPrompt).contains("RAG CONTEXT");
    }

    @Test
    void refineOmitsRagWhenBlank() {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        agents.refine("change", new VawkSpec(), new AwkProgram(null, "code", "hash"), "", " ", GenerationMode.COT);

        assertThat(client.lastPrompt).doesNotContain("RAG CONTEXT");
    }

    @Test
    void planFromSpecHandlesYamlSerializationFailure() throws Exception {
        CapturingAiClient client = new CapturingAiClient();
        VawkAgents agents = new VawkAgents(client);

        java.lang.reflect.Field field = VawkAgents.class.getDeclaredField("yamlMapper");
        field.setAccessible(true);
        field.set(agents, new com.fasterxml.jackson.databind.ObjectMapper() {
            @Override
            public String writeValueAsString(Object value) throws com.fasterxml.jackson.core.JsonProcessingException {
                throw new com.fasterxml.jackson.core.JsonProcessingException("boom") {
                };
            }
        });

        agents.planFromSpec(new VawkSpec(), "", "", GenerationMode.COT);

        assertThat(client.lastPrompt).contains("failed to serialize spec");
    }

    private static final class CapturingAiClient implements AiClient {
        private String lastPrompt;
        private String lastSystem;

        @Override
        public AiResponse generate(String promptText, String systemPrompt) {
            this.lastPrompt = promptText;
            this.lastSystem = systemPrompt;
            return new AiResponse("plan", "code", "tests", "notes");
        }
    }
}
