package com.vawk.ai;

import com.vawk.util.SectionParser;
import org.springframework.ai.chat.client.ChatClient;

/**
 * AiClient implementation backed by Spring AI's ChatClient. Sends system + user prompts and parses
 * the response into PLAN/CODE/TESTS/NOTES sections.
 */
public class SpringAiClient implements AiClient {
    private final ChatClient chatClient;

    /**
     * Constructs a client that delegates to Spring AI.
     */
    public SpringAiClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Sends a prompt to the model and parses the structured sections.
     */
    @Override
    public AiResponse generate(String promptText, String systemPrompt) {
        String content = chatClient
                .prompt()
                .system(systemPrompt)
                .user(promptText)
                .call()
                .content();
        return SectionParser.parse(content);
    }
}
