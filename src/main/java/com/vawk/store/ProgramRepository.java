package com.vawk.store;

import com.vawk.domain.AwkProgram;
import com.vawk.util.Hashing;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Persists and retrieves AWK programs from disk, computing content hashes for traceability.
 */
@Repository
public class ProgramRepository {
    private final Path defaultPath = Paths.get("main.awk");

    /**
     * Reads the default AWK program at main.awk.
     */
    // Pattern: Learning
    // - Loads the current program so refinements build on the last known state.
    public AwkProgram read() throws IOException {
        return read(defaultPath);
    }

    /**
     * Reads an AWK program from the given path and calculates its SHA-256 hash.
     *
     * @param path location of the AWK script
     * @return AwkProgram containing path, content, and hash
     */
    public AwkProgram read(Path path) throws IOException {
        Path target = path.toAbsolutePath();
        byte[] bytes = Files.readAllBytes(target);
        String content = new String(bytes, StandardCharsets.UTF_8);
        String hash = Hashing.sha256(content);
        return new AwkProgram(target, content, hash);
    }

    /**
     * Writes AWK content to the default main.awk location.
     */
    // Pattern: Learning
    // - Persists the latest generated script for iterative improvement.
    public void write(String content) throws IOException {
        write(defaultPath, content);
    }

    /**
     * Writes AWK content to a specific path.
     *
     * @param path destination for the AWK file
     * @param content AWK source code to persist
     */
    public void write(Path path, String content) throws IOException {
        Path target = path.toAbsolutePath();
        Files.write(target, content.getBytes(StandardCharsets.UTF_8));
    }
}
