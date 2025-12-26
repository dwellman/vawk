package com.vawk.cli;

import com.vawk.domain.AwkProgram;
import com.vawk.domain.GenerationMode;
import com.vawk.domain.SessionLog;
import com.vawk.runtime.AwkRunRequest;
import com.vawk.runtime.AwkRunResult;
import com.vawk.runtime.AwkRunner;
import com.vawk.runtime.DirectoryService;
import com.vawk.store.ProgramRepository;
import com.vawk.store.SessionLogRepository;
import com.vawk.util.Hashing;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * CLI command to run an AWK program (default main.awk) against optional input with -v variables,
 * echoing stdout/stderr and recording a RUN session log entry.
 */
@Component
@Command(name = "run", description = "Run AWK program against input (default main.awk)")
public class RunCommand implements Callable<Integer> {

    @Option(names = {"--awk"}, defaultValue = "main.awk", description = "Path to AWK program")
    Path awkPath;

    @Option(names = {"--var"}, description = "AWK -v assignment (name=value)", paramLabel = "name=value")
    List<String> varAssignments;

    @Parameters(index = "0", arity = "0..1", description = "Input file (optional, STDIN if omitted)")
    Path inputFile;

    private final ProgramRepository programRepository;
    private final AwkRunner awkRunner;
    private final SessionLogRepository sessionLogRepository;
    private final DirectoryService directoryService;

    @Spec
    CommandSpec spec;

    /**
     * Builds the run command with repositories and runner.
     */
    public RunCommand(ProgramRepository programRepository,
                      AwkRunner awkRunner,
                      SessionLogRepository sessionLogRepository,
                      DirectoryService directoryService) {
        this.programRepository = programRepository;
        this.awkRunner = awkRunner;
        this.sessionLogRepository = sessionLogRepository;
        this.directoryService = directoryService;
    }

    /**
     * Executes the AWK program with provided vars/input and writes a session log entry.
     */
    @Override
    public Integer call() throws Exception {
        directoryService.ensureBaseDirs();
        Map<String, String> vars = parseVarAssignments(varAssignments);
        if (vars == null) {
            spec.commandLine().getErr().println("Invalid --var assignment, expected name=value");
            return 1;
        }

        AwkProgram program = programRepository.read(awkPath);
        AwkRunRequest request = new AwkRunRequest(program, inputFile, vars);
        AwkRunResult result = awkRunner.run(request);

        if (!result.getStdout().isEmpty()) {
            spec.commandLine().getOut().print(result.getStdout());
        }
        if (!result.getStderr().isEmpty()) {
            spec.commandLine().getErr().print(result.getStderr());
        }
        spec.commandLine().getOut().flush();
        spec.commandLine().getErr().flush();

        SessionLog log = new SessionLog();
        log.setCommand("RUN");
        log.setMode(GenerationMode.SINGLE);
        log.setTimestamp(Instant.now());
        log.setProgramHash(program.getHash());
        if (Files.exists(Paths.get("spec.yaml"))) {
            byte[] specBytes = Files.readAllBytes(Paths.get("spec.yaml"));
            log.setSpecHash(Hashing.sha256(new String(specBytes, StandardCharsets.UTF_8)));
        }
        sessionLogRepository.write(log);

        return result.getExitCode();
    }

    private Map<String, String> parseVarAssignments(List<String> assignments) {
        Map<String, String> map = new HashMap<>();
        if (assignments == null) {
            return map;
        }
        for (String assignment : assignments) {
            int idx = assignment.indexOf('=');
            if (idx <= 0 || idx == assignment.length() - 1) {
                return null;
            }
            String name = assignment.substring(0, idx);
            String value = assignment.substring(idx + 1);
            map.put(name, value);
        }
        return map;
    }
}
