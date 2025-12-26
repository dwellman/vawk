package com.vawk.orchestration;

import com.vawk.ai.AiClient;
import com.vawk.ai.AiResponse;
import com.vawk.ai.VawkAgents;
import com.vawk.domain.VawkSpec;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class VawkSpecBuilderTest {

    @Test
    void buildFallsBackToHeuristicWhenNoAgents() {
        VawkSpecBuilder builder = new VawkSpecBuilder();

        VawkSpec spec = builder.build("Summarize logs", Optional.empty(), null);

        assertThat(spec.getDescription()).isEqualTo("Summarize logs");
        assertThat(spec.getInputs()).isNotEmpty();
        assertThat(spec.getConstraints()).contains("POSIX awk, streaming");
    }

    @Test
    void buildIncludesByUserConstraintWhenMentioned() {
        VawkSpecBuilder builder = new VawkSpecBuilder();

        VawkSpec spec = builder.build("Summarize logs", Optional.of("by_user"), null);

        assertThat(spec.getConstraints()).anyMatch(c -> c.contains("by_user"));
    }

    @Test
    void buildParsesAiSpecWhenValidYamlProvided() {
        AiClient stubClient = (prompt, system) -> new AiResponse(
                "plan",
                "description: demo\ninputs: [input]\noutputs: [output]\nconstraints: [constraint]\nexamples: []",
                "tests",
                "notes"
        );
        VawkSpecBuilder builder = new VawkSpecBuilder(new VawkAgents(stubClient));

        VawkSpec spec = builder.build("demo", Optional.empty(), null);

        assertThat(spec.getDescription()).isEqualTo("demo");
        assertThat(spec.getInputs()).containsExactly("input");
    }

    @Test
    void buildFallsBackWhenAiSpecIsInvalid() {
        AiClient stubClient = (prompt, system) -> new AiResponse("plan", "not: [yaml", "tests", "notes");
        VawkSpecBuilder builder = new VawkSpecBuilder(new VawkAgents(stubClient));

        VawkSpec spec = builder.build("fallback", Optional.empty(), null);

        assertThat(spec.getDescription()).isEqualTo("fallback");
        assertThat(spec.getOutputs()).isNotEmpty();
    }

    @Test
    void buildFallsBackWhenAiSpecMissingCode() {
        AiClient stubClient = (prompt, system) -> new AiResponse("plan", "  ", "tests", "notes");
        VawkSpecBuilder builder = new VawkSpecBuilder(new VawkAgents(stubClient));

        VawkSpec spec = builder.build("fallback", Optional.of("agents"), null);

        assertThat(spec.getDescription()).isEqualTo("fallback");
        assertThat(spec.getInputs()).isNotEmpty();
    }
}
