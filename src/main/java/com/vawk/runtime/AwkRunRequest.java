package com.vawk.runtime;

import com.vawk.domain.AwkProgram;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Request parameters for running an AWK program: program path/content, optional input file, and
 * -v variables.
 */
public class AwkRunRequest {
    private AwkProgram program;
    private Path inputFile;
    private Map<String, String> variables = new HashMap<>();

    public AwkRunRequest() {
    }

    public AwkRunRequest(AwkProgram program, Path inputFile, Map<String, String> variables) {
        this.program = program;
        this.inputFile = inputFile;
        if (variables != null) {
            this.variables.putAll(variables);
        }
    }

    public AwkProgram getProgram() {
        return program;
    }

    public void setProgram(AwkProgram program) {
        this.program = program;
    }

    public Path getInputFile() {
        return inputFile;
    }

    public void setInputFile(Path inputFile) {
        this.inputFile = inputFile;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AwkRunRequest that)) return false;
        return Objects.equals(program, that.program) && Objects.equals(inputFile, that.inputFile) && Objects.equals(variables, that.variables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(program, inputFile, variables);
    }
}
