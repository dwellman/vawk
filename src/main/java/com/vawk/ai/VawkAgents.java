package com.vawk.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vawk.domain.AwkProgram;
import com.vawk.domain.GenerationMode;
import com.vawk.domain.VawkSpec;
import org.springframework.stereotype.Component;

/**
 * Orchestrates AI prompts for VAWK tasks (spec, plan, code, refine, explain, lint). Builds
 * system/user prompts from inputs and delegates to AiClient. This is the main entry point for AI
 * generation behavior and should be extended when altering prompt shapes or modes.
 */
@Component
public class VawkAgents {
    private final AiClient aiClient;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    /**
     * Constructs agents backed by the configured AiClient.
     */
    public VawkAgents(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    /**
     * Generates a YAML spec from a natural language description using agents/prompting.
     */
    public AiResponse specFromVibe(String description, String agentsMd, String ragContext, GenerationMode mode) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(SystemPrompts.PLAN_FIRST).append("\n");
        prompt.append("Derive a YAML spec for an AWK script from this description. Return PLAN/CODE/TESTS/NOTES. Put the YAML spec in CODE (inside ```yaml fences).");
        prompt.append("\nDESCRIPTION:\n").append(description).append("\n");
        if (ragContext != null && !ragContext.isBlank()) {
            prompt.append("\nRAG CONTEXT:\n").append(ragContext).append("\n");
        }
        return aiClient.generate(prompt.toString(), buildSystemPrompt(agentsMd, mode));
    }

    /**
     * Produces a plan from an existing spec.
     */
    public AiResponse planFromSpec(VawkSpec spec, String agentsMd, String ragContext, GenerationMode mode) {
        String prompt = SystemPrompts.PLAN_FIRST + "\n" +
                SystemPrompts.PLAN_FROM_SPEC + "\n" +
                "SPEC (YAML):\n" + toYaml(spec) + "\n" +
                "Respond with PLAN/CODE/TESTS/NOTES; leave CODE empty if you must.\n";
        if (ragContext != null && !ragContext.isBlank()) {
            prompt = prompt + "\nRAG CONTEXT:\n" + ragContext + "\n";
        }
        return aiClient.generate(prompt, buildSystemPrompt(agentsMd, mode));
    }

    /**
     * Produces AWK code from a plan and spec.
     */
    public AiResponse awkFromPlan(String plan, VawkSpec spec, String agentsMd, String ragContext, GenerationMode mode) {
        String prompt = SystemPrompts.PLAN_FIRST + "\n" +
                SystemPrompts.CODE_FROM_PLAN + "\n" +
                "PLAN:\n" + plan + "\n\n" +
                "SPEC (YAML):\n" + toYaml(spec) + "\n";
        if (ragContext != null && !ragContext.isBlank()) {
            prompt = prompt + "\nRAG CONTEXT:\n" + ragContext + "\n";
        }
        return aiClient.generate(prompt, buildSystemPrompt(agentsMd, mode));
    }

    /**
     * Refines existing AWK code per change request.
     */
    public AiResponse refine(String changeRequest, VawkSpec spec, AwkProgram program, String agentsMd, String ragContext, GenerationMode mode) {
        String prompt = SystemPrompts.PLAN_FIRST + "\n" +
                SystemPrompts.REFINE + "\n" +
                "CURRENT SPEC (YAML):\n" + toYaml(spec) + "\n" +
                "CURRENT CODE:\n```\n" + program.getContent() + "\n```\n" +
                "CHANGE REQUEST:\n" + changeRequest + "\n";
        if (ragContext != null && !ragContext.isBlank()) {
            prompt = prompt + "\nRAG CONTEXT:\n" + ragContext + "\n";
        }
        return aiClient.generate(prompt, buildSystemPrompt(agentsMd, mode));
    }

    /**
     * Asks the model to explain AWK code.
     */
    public AiResponse explain(AwkProgram program, String agentsMd, String ragContext) {
        String prompt = SystemPrompts.PLAN_FIRST + "\n" +
                SystemPrompts.EXPLAIN + "\n" +
                "CODE:\n```\n" + program.getContent() + "\n```\n";
        if (ragContext != null && !ragContext.isBlank()) {
            prompt = prompt + "\nRAG CONTEXT:\n" + ragContext + "\n";
        }
        return aiClient.generate(prompt, buildSystemPrompt(agentsMd, GenerationMode.SINGLE));
    }

    /**
     * Requests lint/critique for AWK code.
     */
    public AiResponse lint(AwkProgram program, String agentsMd, String ragContext) {
        String prompt = SystemPrompts.PLAN_FIRST + "\n" +
                SystemPrompts.LINT + "\n" +
                "CODE:\n```\n" + program.getContent() + "\n```\n";
        if (ragContext != null && !ragContext.isBlank()) {
            prompt = prompt + "\nRAG CONTEXT:\n" + ragContext + "\n";
        }
        return aiClient.generate(prompt, buildSystemPrompt(agentsMd, GenerationMode.SINGLE));
    }

    private String toYaml(VawkSpec spec) {
        try {
            return yamlMapper.writeValueAsString(spec);
        } catch (JsonProcessingException e) {
            return "# failed to serialize spec\n";
        }
    }

    private String buildSystemPrompt(String agentsMd, GenerationMode mode) {
        StringBuilder promptBuilder = new StringBuilder(SystemPrompts.PLAN_FIRST);
        promptBuilder.append("\nMode: ").append(mode != null ? mode.name() : "SINGLE").append(". ");
        if (agentsMd != null && !agentsMd.isBlank()) {
            promptBuilder.append("\nGUIDANCE:\n").append(agentsMd);
        }
        return promptBuilder.toString();
    }
}
