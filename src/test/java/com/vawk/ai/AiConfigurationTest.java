package com.vawk.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AiConfigurationTest {

    @Test
    void usesStubWhenChatDisabledOrMissing() {
        AiConfiguration config = new AiConfiguration();
        LocalStubAiClient stub = new LocalStubAiClient();

        AiClient client = config.aiClient(Optional.empty(), stub, false);

        assertThat(client).isSameAs(stub);
    }

    @Test
    void usesStubWhenChatEnabledButClientMissing() {
        AiConfiguration config = new AiConfiguration();
        LocalStubAiClient stub = new LocalStubAiClient();

        AiClient client = config.aiClient(Optional.empty(), stub, true);

        assertThat(client).isSameAs(stub);
    }

    @Test
    void usesSpringClientWhenEnabledAndPresent() {
        AiConfiguration config = new AiConfiguration();
        LocalStubAiClient stub = new LocalStubAiClient();
        ChatClient chatClient = ChatClient.create(new StubChatModel("PLAN:\nOK\nCODE:\n# VAWK: test\n# Purpose: demo\n# Intent: demo\n# Input: demo\n# Output: demo\nprint \"ok\"\nTESTS:\n- none\nNOTES:\nnotes"));

        AiClient client = config.aiClient(Optional.of(chatClient), stub, true);

        assertThat(client).isInstanceOf(SpringAiClient.class);
    }

    @Test
    void buildsChatClientFromModel() {
        AiConfiguration config = new AiConfiguration();

        ChatClient client = config.chatClient(new StubChatModel("PLAN:\nOK\nCODE:\n# VAWK: test\n# Purpose: demo\n# Intent: demo\n# Input: demo\n# Output: demo\nprint \"ok\"\nTESTS:\n- none\nNOTES:\nnotes"));

        assertThat(client).isNotNull();
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
