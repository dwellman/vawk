package com.vawk.cli;

import com.vawk.domain.AwkProgram;
import com.vawk.domain.AwkTestCase;
import com.vawk.domain.GenerationMode;
import com.vawk.domain.SessionLog;
import com.vawk.runtime.AwkTestRunner;
import com.vawk.runtime.DirectoryService;
import com.vawk.store.ProgramRepository;
import com.vawk.store.SessionLogRepository;
import com.vawk.store.SpecRepository;
import com.vawk.store.TestRepository;
import com.vawk.util.Hashing;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.nio.file.Files;

/**
 * CLI command to run AWK tests against fixtures in a tests directory. Executes programs, diffs
 * outputs, prints PASS/FAIL, and records results in the session log.
 */
@Component
@Command(name = "test", description = "Run AWK tests using fixtures under tests/")
public class TestCommand implements Callable<Integer> {

    @Option(names = {"--tests-dir"}, description = "Directory containing input/expected fixtures", defaultValue = "tests")
    Path testsDir;

    @Option(names = {"--awk"}, description = "Path to AWK program (default main.awk)", defaultValue = "main.awk")
    Path awkPath;

    private final TestRepository testRepository;
    private final ProgramRepository programRepository;
    private final SpecRepository specRepository;
    private final SessionLogRepository sessionLogRepository;
    private final AwkTestRunner testRunner;
    private final DirectoryService directoryService;

    /**
     * Constructs the test CLI command with repositories and runners.
     */
    public TestCommand(TestRepository testRepository,
                       ProgramRepository programRepository,
                       SpecRepository specRepository,
                       SessionLogRepository sessionLogRepository,
                       AwkTestRunner testRunner,
                       DirectoryService directoryService) {
        this.testRepository = testRepository;
        this.programRepository = programRepository;
        this.specRepository = specRepository;
        this.sessionLogRepository = sessionLogRepository;
        this.testRunner = testRunner;
        this.directoryService = directoryService;
    }

    /**
     * Runs AWK tests, prints results, and writes a session log entry.
     */
    @Override
    // Pattern: Verification + Trust UX
    // - Runs fixtures and logs PASS/FAIL so users can inspect test receipts.
    public Integer call() throws Exception {
        directoryService.ensureBaseDirs();
        List<AwkTestCase> testCases = testRepository.load(testsDir);
        if (testCases.isEmpty()) {
            System.err.println("No test cases found under " + testsDir);
            return 1;
        }

        AwkProgram program = programRepository.read(awkPath);
        List<SessionLog.TestRun> results = testRunner.runTests(testCases, program);

        boolean allPass = true;
        for (SessionLog.TestRun run : results) {
            if (run.isPassed()) {
                System.out.println("[PASS] " + run.getName());
            } else {
                allPass = false;
                System.out.println("[FAIL] " + run.getName());
                if (run.getDiff() != null) {
                    System.out.println(run.getDiff());
                }
            }
        }

        SessionLog log = new SessionLog();
        log.setCommand("TEST");
        log.setTimestamp(Instant.now());
        log.setMode(GenerationMode.SINGLE);
        log.setTestsRun(results);
        try {
            if (Files.exists(Paths.get("spec.yaml"))) {
                byte[] specBytes = java.nio.file.Files.readAllBytes(Paths.get("spec.yaml"));
                log.setSpecHash(Hashing.sha256(new String(specBytes, StandardCharsets.UTF_8)));
            }
        } catch (IOException ignored) {
        }
        log.setProgramHash(program.getHash());
        sessionLogRepository.write(log);

        return allPass ? 0 : 1;
    }
}
