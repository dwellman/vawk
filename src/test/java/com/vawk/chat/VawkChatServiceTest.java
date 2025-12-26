package com.vawk.chat;

import com.vawk.store.RagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VawkChatServiceTest {

    @Test
    void createSessionWritesMeta(@TempDir Path tempDir) throws Exception {
        TestDirectoryService directoryService = new TestDirectoryService(tempDir.resolve(".vawk"));
        VawkChatService service = new VawkChatService(directoryService, Optional.empty(), new VawkChatPromptBuilder(new VawkPromptLoader()), null);

        VawkChatSession session = service.createSession("title");

        Path file = directoryService.baseDir().resolve("chat").resolve(session.getSessionId() + ".vawk");
        assertThat(Files.exists(file)).isTrue();
        String meta = Files.readString(file, StandardCharsets.UTF_8);
        assertThat(meta).contains("\"kind\":\"meta\"");
        assertThat(meta).contains("\"title\":\"title\"");
    }

    @Test
    void loadSessionThrowsWhenMissing(@TempDir Path tempDir) {
        TestDirectoryService directoryService = new TestDirectoryService(tempDir.resolve(".vawk"));
        VawkChatService service = new VawkChatService(directoryService, Optional.empty(), new VawkChatPromptBuilder(new VawkPromptLoader()), null);

        assertThrows(IOException.class, () -> service.loadSession("missing"));
    }

    @Test
    void loadSessionRejectsUnknownKind(@TempDir Path tempDir) throws Exception {
        TestDirectoryService directoryService = new TestDirectoryService(tempDir.resolve(".vawk"));
        VawkChatService service = new VawkChatService(directoryService, Optional.empty(), new VawkChatPromptBuilder(new VawkPromptLoader()), null);

        Path file = directoryService.baseDir().resolve("chat").resolve("abc.vawk");
        Files.createDirectories(file.getParent());
        String content = "{\"kind\":\"meta\",\"sessionId\":\"abc\",\"createdAt\":\"2025-12-08T00:00:00Z\",\"cwd\":\".\"}\n" +
                "{\"kind\":\"bad\"}\n";
        Files.writeString(file, content, StandardCharsets.UTF_8);

        assertThrows(IOException.class, () -> service.loadSession("abc"));
    }

    @Test
    void loadSessionRejectsMissingMeta(@TempDir Path tempDir) throws Exception {
        TestDirectoryService directoryService = new TestDirectoryService(tempDir.resolve(".vawk"));
        VawkChatService service = new VawkChatService(directoryService, Optional.empty(), new VawkChatPromptBuilder(new VawkPromptLoader()), null);

        Path file = directoryService.baseDir().resolve("chat").resolve("abc.vawk");
        Files.createDirectories(file.getParent());
        String content = "{\"kind\":\"turn\",\"idx\":1,\"ts\":\"2025-12-08T00:00:00Z\",\"role\":\"user\",\"msg\":\"hi\"}\n";
        Files.writeString(file, content, StandardCharsets.UTF_8);

        assertThrows(IOException.class, () -> service.loadSession("abc"));
    }

    @Test
    void appendTurnsWriteEntries(@TempDir Path tempDir) throws Exception {
        TestDirectoryService directoryService = new TestDirectoryService(tempDir.resolve(".vawk"));
        VawkChatService service = new VawkChatService(directoryService, Optional.empty(), new VawkChatPromptBuilder(new VawkPromptLoader()), null);

        VawkChatSession session = service.createSession("title");
        service.appendUserTurn(session.getSessionId(), 1, "hi");
        service.appendAssistantTurn(session.getSessionId(), 2, "hello", "stub");

        List<String> lines = Files.readAllLines(directoryService.baseDir().resolve("chat").resolve(session.getSessionId() + ".vawk"));
        assertThat(lines).hasSize(3);
        assertThat(lines.get(1)).contains("\"role\":\"user\"");
        assertThat(lines.get(2)).contains("\"role\":\"assistant\"");
    }

    @Test
    void generateAssistantReplyUsesStub() {
        VawkChatService service = new VawkChatService(new TestDirectoryService(Path.of(".vawk")), Optional.empty(), new VawkChatPromptBuilder(new VawkPromptLoader()), null);

        String reply = service.generateAssistantReply(List.of(), "hello");

        assertThat(reply).isEqualTo("[stub] hello");
    }

    @Test
    void buildRagContextReturnsFormattedDocs(@TempDir Path tempDir) {
        RagRepository ragRepository = new RagRepository() {
            @Override
            public List<RagDocument> searchForChat(String query, int limit) {
                return List.of(new RagDocument("id", "title", "content"));
            }
        };
        VawkChatService service = new VawkChatService(new TestDirectoryService(tempDir.resolve(".vawk")), Optional.empty(), new VawkChatPromptBuilder(new VawkPromptLoader()), ragRepository);

        String context = service.buildRagContext("query");

        assertThat(context).contains("Reference context");
        assertThat(context).contains("Doc 1");
    }

    @Test
    void buildRagContextReturnsEmptyForBlankQuery(@TempDir Path tempDir) {
        VawkChatService service = new VawkChatService(new TestDirectoryService(tempDir.resolve(".vawk")), Optional.empty(), new VawkChatPromptBuilder(new VawkPromptLoader()), new RagRepository());

        assertThat(service.buildRagContext(" ")).isEmpty();
    }

    @Test
    void generateAssistantReplyUsesChatClientAndTrimsHistory() {
        CapturingPromptBuilder promptBuilder = new CapturingPromptBuilder();
        ChatClient chatClient = ChatClient.create(new StubChatModel("reply"));
        VawkChatService service = new VawkChatService(new TestDirectoryService(Path.of(".vawk")), Optional.of(chatClient), promptBuilder, null);

        List<VawkChatService.Turn> history = new java.util.ArrayList<>();
        for (int i = 0; i < 25; i++) {
            history.add(new VawkChatService.Turn(i, "user", "m" + i, null));
        }

        String reply = service.generateAssistantReply(history, "hello");

        assertThat(reply).isEqualTo("reply");
        assertThat(promptBuilder.lastHistorySize).isEqualTo(20);
    }

    @Test
    void buildRagContextReturnsEmptyWhenRepositoryErrors(@TempDir Path tempDir) {
        RagRepository ragRepository = new RagRepository() {
            @Override
            public List<RagDocument> searchForChat(String query, int limit) throws IOException {
                throw new IOException("boom");
            }
        };
        VawkChatService service = new VawkChatService(new TestDirectoryService(tempDir.resolve(".vawk")), Optional.empty(), new VawkChatPromptBuilder(new VawkPromptLoader()), ragRepository);

        assertThat(service.buildRagContext("query")).isEmpty();
    }

    @Test
    void buildRagContextTruncatesLongContent(@TempDir Path tempDir) {
        RagRepository ragRepository = new RagRepository() {
            @Override
            public List<RagDocument> searchForChat(String query, int limit) {
                String longContent = "line\n".repeat(300);
                return List.of(new RagDocument("id", "title", longContent));
            }
        };
        VawkChatService service = new VawkChatService(new TestDirectoryService(tempDir.resolve(".vawk")), Optional.empty(), new VawkChatPromptBuilder(new VawkPromptLoader()), ragRepository);

        String context = service.buildRagContext("query");

        assertThat(context.length()).isLessThanOrEqualTo(1200);
        assertThat(context).contains("Reference context");
    }

    @Test
    void generateAssistantReplyHandlesNullHistory() {
        CapturingPromptBuilder promptBuilder = new CapturingPromptBuilder();
        ChatClient chatClient = ChatClient.create(new StubChatModel("reply"));
        VawkChatService service = new VawkChatService(new TestDirectoryService(Path.of(".vawk")), Optional.of(chatClient), promptBuilder, null);

        String reply = service.generateAssistantReply(null, "hello");

        assertThat(reply).isEqualTo("reply");
        assertThat(promptBuilder.lastHistorySize).isEqualTo(0);
    }

    @Test
    void buildRagContextReturnsEmptyWhenDocsEmpty(@TempDir Path tempDir) {
        RagRepository ragRepository = new RagRepository() {
            @Override
            public List<RagDocument> searchForChat(String query, int limit) {
                return List.of();
            }
        };
        VawkChatService service = new VawkChatService(new TestDirectoryService(tempDir.resolve(".vawk")), Optional.empty(), new VawkChatPromptBuilder(new VawkPromptLoader()), ragRepository);

        assertThat(service.buildRagContext("query")).isEmpty();
    }

    @Test
    void buildRagContextHandlesNullDocContent(@TempDir Path tempDir) {
        RagRepository ragRepository = new RagRepository() {
            @Override
            public List<RagDocument> searchForChat(String query, int limit) {
                return List.of(new RagDocument("id", "title", null));
            }
        };
        VawkChatService service = new VawkChatService(new TestDirectoryService(tempDir.resolve(".vawk")), Optional.empty(), new VawkChatPromptBuilder(new VawkPromptLoader()), ragRepository);

        String context = service.buildRagContext("query");

        assertThat(context).contains("Reference context");
    }

    private static final class TestDirectoryService extends com.vawk.runtime.DirectoryService {
        private final Path base;

        private TestDirectoryService(Path base) {
            this.base = base;
        }

        @Override
        public Path baseDir() {
            return base;
        }

        @Override
        public void ensureBaseDirs() throws IOException {
            Files.createDirectories(base.resolve("chat"));
            Files.createDirectories(base.resolve("logs"));
        }
    }

    private static final class CapturingPromptBuilder extends VawkChatPromptBuilder {
        private int lastHistorySize;

        private CapturingPromptBuilder() {
            super(new VawkPromptLoader());
        }

        @Override
        public List<Message> buildMessages(String newUserMessage, List<VawkChatService.Turn> history, String ragContext) {
            lastHistorySize = history == null ? -1 : history.size();
            return List.of();
        }
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
