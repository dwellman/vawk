package com.vawk.store;

import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Loads AGENTS guidance from the project root. Prefers AGENTS.md beside the working directory and
 * falls back to prompts/AGENTS.md when present.
 */
@Repository
public class AgentsFileRepository {
    private final Path primary = Paths.get("AGENTS.md");
    private final Path fallback = Paths.get("prompts", "AGENTS.md");

    /**
     * Reads the AGENTS markdown content if present in the preferred locations.
     *
     * @return AGENTS text when available
     */
    // Pattern: Trust UX
    // - Personalizes behavior via explicit project guidance instead of hidden assumptions.
    public Optional<String> read() throws IOException {
        Path path = resolvePath();
        if (path == null) {
            return Optional.empty();
        }
        return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
    }

    private Path resolvePath() {
        if (Files.exists(primary)) {
            return primary;
        }
        if (Files.exists(fallback)) {
            return fallback;
        }
        return null;
    }
}
