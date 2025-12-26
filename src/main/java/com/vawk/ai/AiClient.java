package com.vawk.ai;

/**
 * Abstraction over chat model providers used for VAWK prompts.
 */
public interface AiClient {
    /**
     * Executes a prompt with a system message and returns parsed PLAN/CODE/TESTS/NOTES content.
     *
     * @param promptText user prompt content
     * @param systemPrompt system instructions to prepend
     * @return parsed AI response sections
     */
    AiResponse generate(String promptText, String systemPrompt);
}
