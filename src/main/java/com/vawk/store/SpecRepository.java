package com.vawk.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vawk.domain.VawkSpec;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Reads and writes VawkSpec definitions in YAML form to link natural language intents with AWK
 * programs.
 */
@Repository
public class SpecRepository {
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final Path defaultPath = Paths.get("spec.yaml");

    /**
     * Reads the default spec.yaml file.
     */
    // Pattern: Learning
    // - Loads persisted specs to keep iterative runs grounded in past intent.
    public VawkSpec read() throws IOException {
        return read(defaultPath);
    }

    /**
     * Reads a VawkSpec from the given path.
     *
     * @param path YAML file to parse
     */
    public VawkSpec read(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        return yamlMapper.readValue(bytes, VawkSpec.class);
    }

    /**
     * Writes a VawkSpec to the default spec.yaml path.
     */
    // Pattern: Learning
    // - Persists specs so future refinements can reuse the same constraints.
    public void write(VawkSpec spec) throws IOException {
        write(defaultPath, spec);
    }

    /**
     * Writes a VawkSpec to a specific path using pretty-printed YAML.
     *
     * @param path destination file
     * @param spec spec content to serialize
     */
    public void write(Path path, VawkSpec spec) throws IOException {
        byte[] serialized = yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(spec);
        Files.write(path, serialized);
    }
}
