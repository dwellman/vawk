package com.vawk.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Optional;

/**
 * Configures AI clients for VAWK. Prefers a real ChatClient-backed model when enabled, otherwise
 * falls back to the deterministic local stub. Also exposes a ChatClient bean when a ChatModel is
 * available.
 */
@Configuration
public class AiConfiguration {

    /**
     * Chooses between ChatClient-backed AI and the local stub based on configuration.
     */
    // Pattern: Trust UX
    // - Falls back to a deterministic stub when live AI is not enabled or available.
    @Bean
    @Primary
    public AiClient aiClient(Optional<ChatClient> chatClient,
                             LocalStubAiClient stub,
                             @Value("${vawk.ai.use-chat:false}") boolean useChat) {
        if (useChat && chatClient.isPresent()) {
            return new SpringAiClient(chatClient.get());
        }
        return stub;
    }

    /**
     * Exposes a ChatClient when a ChatModel bean exists.
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}
