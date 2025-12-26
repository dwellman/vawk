package com.vawk.runtime;

import com.vawk.domain.AwkProgram;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Executes AWK programs with POSIX awk. Builds the command line with variables and input file,
 * runs the process, and captures stdout/stderr and exit code.
 */
@Component
public class AwkRunner {

    /**
     * Runs an AWK program with optional variables and input file.
     *
     * @param request run parameters including program path, variables, and input file
     * @return result containing exit code, stdout, stderr
     */
    public AwkRunResult run(AwkRunRequest request) throws IOException, InterruptedException {
        AwkProgram program = request.getProgram();
        if (program == null || program.getPath() == null) {
            throw new IllegalArgumentException("Program path is required");
        }
        List<String> command = new ArrayList<>();
        command.add("awk");
        for (Map.Entry<String, String> entry : request.getVariables().entrySet()) {
            command.add("-v");
            command.add(entry.getKey() + "=" + entry.getValue());
        }
        command.add("-f");
        command.add(program.getPath().toString());
        if (request.getInputFile() != null) {
            command.add(request.getInputFile().toString());
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();
        String stdout = readAll(process.getInputStream());
        String stderr = readAll(process.getErrorStream());
        int exit = process.waitFor();
        return new AwkRunResult(exit, stdout, stderr);
    }

    private String readAll(InputStream in) throws IOException {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
}
