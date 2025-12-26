package com.vawk.chat;

import java.nio.file.Path;
import java.time.Instant;

/**
 * Immutable metadata for a chat session. Captures session id, creation time, working directory, and
 * optional title for logging and downstream promotion.
 */
public final class VawkChatSession {
    private final String sessionId;
    private final Instant createdAt;
    private final Path workingDirectory;
    private final String title;

    /**
     * Creates a session descriptor.
     *
     * @param sessionId unique id (matches chat log filename)
     * @param createdAt timestamp in UTC
     * @param workingDirectory cwd for the session
     * @param title optional human-friendly title
     */
    public VawkChatSession(String sessionId, Instant createdAt, Path workingDirectory, String title) {
        this.sessionId = sessionId;
        this.createdAt = createdAt;
        this.workingDirectory = workingDirectory;
        this.title = title;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Path getWorkingDirectory() {
        return workingDirectory;
    }

    public String getTitle() {
        return title;
    }
}
