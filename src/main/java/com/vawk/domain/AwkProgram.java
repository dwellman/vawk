package com.vawk.domain;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a single AWK program on disk, including its absolute path, contents, and a checksum
 * for traceability.
 */
public class AwkProgram {
    private Path path;
    private String content;
    private String hash;

    public AwkProgram() {
    }

    public AwkProgram(Path path, String content, String hash) {
        this.path = path;
        this.content = content;
        this.hash = hash;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Programs are equal if their path, content, and hash all match.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AwkProgram that)) return false;
        return Objects.equals(path, that.path) && Objects.equals(content, that.content) && Objects.equals(hash, that.hash);
    }

    /**
     * Hashes path, content, and checksum for collection usage.
     */
    @Override
    public int hashCode() {
        return Objects.hash(path, content, hash);
    }
}
