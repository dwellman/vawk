package com.vawk.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpringAiClientTest {

    @Test
    void generateParsesStructuredResponse() {
        String content = "PLAN:\nDo the thing\nCODE:\n# VAWK: demo\n# Purpose: demo\n# Intent: demo\n# Input: demo\n# Output: demo\nprint \"ok\"\nTESTS:\n- test 1\nNOTES:\nnotes";
        ChatClient chatClient = ChatClient.create(new StubChatModel(content));
        SpringAiClient client = new SpringAiClient(chatClient);

        AiResponse response = client.generate("prompt", "system");

        assertThat(response.getPlan()).contains("Do the thing");
        assertThat(response.getCode()).contains("# VAWK: demo");
        assertThat(response.getTests()).contains("test 1");
        assertThat(response.getNotes()).contains("notes");
    }

    private static final class StubChatModel implements ChatModel {
        private final String content;

        private StubChatModel(String content) {
            this.content = content;
        }

        @Override
        public ChatResponse call(Prompt prompt) {
            return new ChatResponse(List.of(new Generation(content)));
        }

        @Override
        public ChatOptions getDefaultOptions() {
            return new ChatOptions() {
                @Override
                public Float getTemperature() {
                    return null;
                }

                @Override
                public Float getTopP() {
                    return null;
                }

                @Override
                public Integer getTopK() {
                    return null;
                }
            };
        }
    }
}
