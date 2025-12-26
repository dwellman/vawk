package com.vawk.orchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vawk.ai.AiResponse;
import com.vawk.ai.VawkAgents;
import com.vawk.domain.GenerationMode;
import com.vawk.domain.VawkSpec;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Builds VAWK specs from natural language descriptions using AI (when available) with a heuristic
 * fallback. Converts PLAN/CODE/TESTS/NOTES responses into VawkSpec objects.
 */
@Component
public class VawkSpecBuilder {
    private final VawkAgents vawkAgents;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    /**
     * Constructs a spec builder backed by VawkAgents.
     */
    public VawkSpecBuilder(VawkAgents vawkAgents) {
        this.vawkAgents = vawkAgents;
    }

    // for tests without Spring
    public VawkSpecBuilder() {
        this.vawkAgents = null;
    }

    /**
     * Builds a spec from description and optional agents guidance.
     */
    public VawkSpec build(String description, Optional<String> agentsMd) {
        return build(description, agentsMd, null);
    }

    /**
     * Builds a spec with optional RAG context, preferring AI output and falling back to heuristics.
     */
    public VawkSpec build(String description, Optional<String> agentsMd, String ragContext) {
        VawkSpec spec = null;
        if (vawkAgents != null) {
            try {
                AiResponse response = vawkAgents.specFromVibe(description, agentsMd.orElse(""), ragContext, GenerationMode.COT);
                spec = parseSpec(response);
            } catch (Exception ignored) {
                // fall back to heuristics
            }
        }
        if (spec == null) {
            spec = heuristicSpec(description, agentsMd);
        }
        return spec;
    }

    private VawkSpec parseSpec(AiResponse response) throws Exception {
        if (response == null || response.getCode() == null || response.getCode().isBlank()) {
            throw new IllegalArgumentException("No spec code in AI response");
        }
        String yaml = response.getCode();
        return yamlMapper.readValue(yaml, VawkSpec.class);
    }

    private VawkSpec heuristicSpec(String description, Optional<String> agentsMd) {
        VawkSpec spec = new VawkSpec();
        spec.setDescription(description);
        List<String> inputs = new ArrayList<>();
        List<String> outputs = new ArrayList<>();
        List<String> constraints = new ArrayList<>();

        if (agentsMd.isPresent()) {
            String agentsMarkdown = agentsMd.get();
            inputs.add("Log line: <timestamp> <level> <user> <message...>");
            outputs.add("Plain-text counts: total_lines, INFO, WARN, ERROR; optional per-user breakdown when by_user=1");
            constraints.add("POSIX awk only; no gawk extensions; single-pass streaming");
            constraints.add("No external commands; handle large files; skip comment/blank lines");
            if (agentsMarkdown.contains("by_user")) {
                constraints.add("Respect -v by_user flag to group by user");
            }
        } else {
            inputs.add("Text lines; define fields as needed");
            outputs.add("AWK-generated text output per task description");
            constraints.add("POSIX awk, streaming");
        }

        spec.setInputs(inputs);
        spec.setOutputs(outputs);
        spec.setConstraints(constraints);
        return spec;
    }
}
