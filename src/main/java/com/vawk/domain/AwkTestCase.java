package com.vawk.domain;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Describes a single AWK test case with input, expected output, optional variable overrides, and an
 * optional program override.
 */
public class AwkTestCase {
    private String name;
    private Path inputPath;
    private Path expectedPath;
    private Map<String, String> variables = new HashMap<>();
    private Path programOverride;

    public AwkTestCase() {
    }

    public AwkTestCase(String name, Path inputPath, Path expectedPath, Map<String, String> variables, Path programOverride) {
        this.name = name;
        this.inputPath = inputPath;
        this.expectedPath = expectedPath;
        if (variables != null) {
            this.variables.putAll(variables);
        }
        this.programOverride = programOverride;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Path getInputPath() {
        return inputPath;
    }

    public void setInputPath(Path inputPath) {
        this.inputPath = inputPath;
    }

    public Path getExpectedPath() {
        return expectedPath;
    }

    public void setExpectedPath(Path expectedPath) {
        this.expectedPath = expectedPath;
    }

    public Map<String, String> getVariables() {
        return Collections.unmodifiableMap(variables);
    }

    public void setVariables(Map<String, String> variables) {
        this.variables.clear();
        if (variables != null) {
            this.variables.putAll(variables);
        }
    }

    public Path getProgramOverride() {
        return programOverride;
    }

    public void setProgramOverride(Path programOverride) {
        this.programOverride = programOverride;
    }

    /**
     * Test cases are equal if all fields match, including variables and program overrides.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AwkTestCase that)) return false;
        return Objects.equals(name, that.name) && Objects.equals(inputPath, that.inputPath) && Objects.equals(expectedPath, that.expectedPath) && Objects.equals(variables, that.variables) && Objects.equals(programOverride, that.programOverride);
    }

    /**
     * Hashes all fields to support collection usage.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, inputPath, expectedPath, variables, programOverride);
    }
}
