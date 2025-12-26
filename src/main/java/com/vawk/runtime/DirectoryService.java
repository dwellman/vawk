package com.vawk.runtime;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages creation and access to the VAWK working directories (.vawk/, chat/, logs/). All commands
 * should use this service to ensure directories exist before reading or writing artifacts.
 */
@Component
public class DirectoryService {
    private final Path baseDir = Paths.get(".vawk");

    /**
     * @return the base .vawk directory path
     */
    public Path baseDir() {
        return baseDir;
    }

    /**
     * Ensures .vawk/chat and .vawk/logs exist; safe to call repeatedly.
     */
    public void ensureBaseDirs() throws IOException {
        Files.createDirectories(baseDir);
        Files.createDirectories(baseDir.resolve("chat"));
        Files.createDirectories(baseDir.resolve("logs"));
    }
}
