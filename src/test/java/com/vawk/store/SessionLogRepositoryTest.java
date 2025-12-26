package com.vawk.store;

import com.vawk.domain.SessionLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

class SessionLogRepositoryTest {

    @Test
    void writeCreatesLogFile(@TempDir Path tempDir) throws Exception {
        String original = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            SessionLogRepository repository = new SessionLogRepository();
            java.lang.reflect.Field field = SessionLogRepository.class.getDeclaredField("logsDir");
            field.setAccessible(true);
            Path logsDir = tempDir.resolve("logs-missing");
            field.set(repository, logsDir);

            SessionLog log = new SessionLog();
            log.setCommand("GEN");

            Path written = repository.write(log);

            assertThat(Files.exists(written)).isTrue();
        } finally {
            if (original != null) {
                System.setProperty("user.dir", original);
            }
        }
    }

    @Test
    void writeUsesDefaultCommandAndKeepsTimestamp(@TempDir Path tempDir) throws Exception {
        String original = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            SessionLogRepository repository = new SessionLogRepository();
            SessionLog log = new SessionLog();
            Instant timestamp = Instant.ofEpochSecond(0);
            log.setTimestamp(timestamp);

            Path written = repository.write(log);

            String expectedStamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
                    .withZone(ZoneId.systemDefault())
                    .format(timestamp);
            assertThat(written.getFileName().toString()).contains(expectedStamp);
            assertThat(written.getFileName().toString()).contains("session");
            assertThat(log.getTimestamp()).isEqualTo(timestamp);
        } finally {
            if (original != null) {
                System.setProperty("user.dir", original);
            }
        }
    }

    @Test
    void writeUsesExistingLogsDirectory(@TempDir Path tempDir) throws Exception {
        SessionLogRepository repository = new SessionLogRepository();
        java.lang.reflect.Field field = SessionLogRepository.class.getDeclaredField("logsDir");
        field.setAccessible(true);
        Path logsDir = tempDir.resolve("logs-existing");
        Files.createDirectories(logsDir);
        field.set(repository, logsDir);
        SessionLog log = new SessionLog();
        log.setCommand("GEN");

        Path written = repository.write(log);

        assertThat(Files.exists(written)).isTrue();
        Files.deleteIfExists(written);
    }
}
