package com.vawk.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vawk.runtime.DirectoryService;
import com.vawk.store.RagRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Coordinates chat sessions end-to-end: creates/loads NDJSON chat logs, appends user/assistant
 * turns, builds prompts (including RAG context), and calls the ChatClient or stub to generate
 * replies. This is the main service behind the chat CLI and should be extended when changing chat
 * behavior or logging.
 */
@Service
public class VawkChatService {
    private static final int MAX_CONTEXT_TURNS = 20;
    private static final String VERSION = "0.2.0-SNAPSHOT";
    private final DirectoryService directoryService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Optional<ChatClient> chatClient;
    private final VawkChatPromptBuilder promptBuilder;
    private final RagRepository ragRepository;

    /**
     * Constructs a chat service with directory management, optional ChatClient, prompt builder, and
     * RAG repository.
     */
    public VawkChatService(DirectoryService directoryService,
                           Optional<ChatClient> chatClient,
                           VawkChatPromptBuilder promptBuilder,
                           RagRepository ragRepository) {
        this.directoryService = directoryService;
        this.chatClient = chatClient;
        this.promptBuilder = promptBuilder;
        this.ragRepository = ragRepository;
    }

    /**
     * @return true when a real ChatClient is available instead of the stub
     */
    public boolean hasChatClient() {
        return chatClient.isPresent();
    }

    /**
     * Creates a new chat session and writes the meta record to .vawk/chat.
     *
     * @param title optional session title
     * @return session metadata
     */
    // Pattern: Trust UX
    // - Creates a durable audit trail so users can inspect what the system saw and said.
    public VawkChatSession createSession(String title) throws IOException {
        directoryService.ensureBaseDirs();
        String sessionId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Path cwd = Path.of("").toAbsolutePath();
        Path sessionFile = sessionFile(sessionId);

        ObjectNode meta = objectMapper.createObjectNode();
        meta.put("kind", "meta");
        meta.put("sessionId", sessionId);
        meta.put("createdAt", formatInstantUtc(now));
        meta.put("cwd", cwd.toString());
        meta.put("vawkVersion", VERSION);
        if (title != null && !title.isBlank()) {
            meta.put("title", title);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(sessionFile, StandardCharsets.UTF_8)) {
            writer.write(objectMapper.writeValueAsString(meta));
            writer.write("\n");
        }
        return new VawkChatSession(sessionId, now, cwd, title);
    }

    /**
     * Loads an existing session file and returns turns plus next index.
     */
    // Pattern: Learning
    // - Rehydrates prior turns so future prompts can reuse verified history.
    public SessionData loadSession(String sessionId) throws IOException {
        directoryService.ensureBaseDirs();
        Path file = sessionFile(sessionId);
        if (!Files.exists(file)) {
            throw new IOException("Session file not found: " + file);
        }
        List<Turn> turns = new ArrayList<>();
        VawkChatSession meta = null;
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int maxTurnIndex = 0;
        for (String line : lines) {
            if (line.isBlank()) continue;
            ObjectNode node = (ObjectNode) objectMapper.readTree(line);
            String kind = node.path("kind").asText();
            if ("meta".equals(kind)) {
                meta = new VawkChatSession(
                        node.path("sessionId").asText(),
                        Instant.parse(node.path("createdAt").asText()),
                        Path.of(node.path("cwd").asText()),
                        node.path("title").isMissingNode() ? null : node.path("title").asText()
                );
            } else if ("turn".equals(kind)) {
                int turnIndex = node.path("idx").asInt();
                String role = node.path("role").asText();
                String msg = node.path("msg").asText();
                String model = node.path("model").isMissingNode() ? null : node.path("model").asText();
                turns.add(new Turn(turnIndex, role, msg, model));
                maxTurnIndex = Math.max(maxTurnIndex, turnIndex);
            } else {
                throw new IOException("Unknown record kind: " + kind);
            }
        }
        if (meta == null) {
            throw new IOException("Missing meta record in session: " + sessionId);
        }
        return new SessionData(meta, turns, maxTurnIndex + 1);
    }

    /**
     * Appends a user turn to the session log.
     */
    // Pattern: Trust UX
    // - Persists user input verbatim to keep the interaction transparent.
    public String appendUserTurn(String sessionId, int turnIndex, String message) throws IOException {
        return appendTurn(sessionId, turnIndex, "user", message, null);
    }

    /**
     * Appends an assistant turn (with optional model name) to the session log.
     */
    // Pattern: Trust UX
    // - Persists assistant output with model metadata for post-hoc inspection.
    public String appendAssistantTurn(String sessionId, int turnIndex, String message, String model) throws IOException {
        return appendTurn(sessionId, turnIndex, "assistant", message, model);
    }

    /**
     * Builds messages, optional RAG context, and invokes the ChatClient (or stub) to generate an
     * assistant reply for the given user message and history.
     */
    // Pattern: Orchestration + Grounding
    // - Layers prompts and RAG context, then calls the model or stub to produce a reply.
    public String generateAssistantReply(List<Turn> history, String userMessage) {
        if (chatClient.isPresent()) {
            String ragContext = buildRagContext(userMessage);
            List<Message> messages = promptBuilder.buildMessages(userMessage, trimHistory(history), ragContext);
            return chatClient.get().call(messages.toArray(new Message[0]));
        }
        return "[stub] " + userMessage;
    }

    // Pattern: Grounding
    // - Injects curated reference snippets so replies are grounded in known AWK docs.
    String buildRagContext(String query) {
        if (ragRepository == null || query == null || query.isBlank()) {
            return "";
        }
        try {
            List<RagRepository.RagDocument> docs = ragRepository.searchForChat(query, 3);
            if (docs.isEmpty()) {
                return "";
            }
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("# Reference context for this question\n\n");
            contextBuilder.append("Below are a few relevant AWK references. Use them if helpful.\n\n");
            int docNum = 1;
            for (RagRepository.RagDocument doc : docs) {
                contextBuilder.append("## Doc ").append(docNum++).append(": ").append(doc.title).append("\n");
                contextBuilder.append(truncate(doc.content)).append("\n\n");
            }
            return contextBuilder.toString().trim();
        } catch (IOException e) {
            return "";
        }
    }

    private String truncate(String content) {
        if (content == null) {
            return "";
        }
        String[] lines = content.split("\\R");
        StringBuilder truncated = new StringBuilder();
        int count = 0;
        for (String line : lines) {
            if (count >= 20) break;
            truncated.append(line).append("\n");
            count++;
            if (truncated.length() > 1000) {
                break;
            }
        }
        String trimmed = truncated.toString().trim();
        if (trimmed.length() > 1000) {
            return trimmed.substring(0, 1000);
        }
        return trimmed;
    }

    private List<Turn> trimHistory(List<Turn> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }
        int start = Math.max(0, history.size() - MAX_CONTEXT_TURNS);
        return history.subList(start, history.size());
    }

    private String appendTurn(String sessionId, int turnIndex, String role, String message, String model) throws IOException {
        ObjectNode turn = objectMapper.createObjectNode();
        turn.put("kind", "turn");
        turn.put("idx", turnIndex);
        turn.put("ts", formatInstantUtc(Instant.now()));
        turn.put("role", role);
        turn.put("msg", message);
        if (model != null) {
            turn.put("model", model);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(sessionFile(sessionId), StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND)) {
            writer.write(objectMapper.writeValueAsString(turn));
            writer.write("\n");
        }
        return message;
    }

    private Path sessionFile(String sessionId) {
        return directoryService.baseDir().resolve("chat").resolve(sessionId + ".vawk");
    }

    private String formatInstantUtc(Instant instant) {
        return DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC).format(instant);
    }

    /**
     * Immutable chat turn with idx/role/message/model metadata.
     */
    public record Turn(int idx, String role, String msg, String model) {}

    /**
     * Session state containing metadata, accumulated turns, and next idx to use.
     */
    public record SessionData(VawkChatSession meta, List<Turn> turns, int nextIdx) {}
}
