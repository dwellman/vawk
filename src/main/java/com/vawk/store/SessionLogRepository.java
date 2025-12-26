package com.vawk.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vawk.domain.SessionLog;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Persists SessionLog entries under .vawk/logs with timestamped filenames for auditability.
 */
@Repository
public class SessionLogRepository {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
            .withZone(ZoneId.systemDefault());
    private final Path logsDir = Paths.get(".vawk", "logs");

    /**
     * Writes a session log to disk, assigning a timestamp and unique filename.
     *
     * @param log session data to persist
     * @return path to the written JSON log file
     */
    // Pattern: Trust UX
    // - Persists receipts to disk so users can audit model behavior.
    public Path write(SessionLog log) throws IOException {
        if (log.getTimestamp() == null) {
            log.setTimestamp(java.time.Instant.now());
        }
        if (!Files.exists(logsDir)) {
            Files.createDirectories(logsDir);
        }
        String shortId = UUID.randomUUID().toString().substring(0, 8);
        String timestampPart = FORMATTER.format(log.getTimestamp());
        String command = log.getCommand() != null ? log.getCommand() : "session";
        String filename = "%s-%s-%s.json".formatted(timestampPart, command.toLowerCase(), shortId);
        Path target = logsDir.resolve(filename);
        byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(log);
        Files.write(target, bytes);
        return target;
    }
}
