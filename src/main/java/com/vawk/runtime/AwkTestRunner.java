package com.vawk.runtime;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.vawk.domain.AwkProgram;
import com.vawk.domain.AwkTestCase;
import com.vawk.domain.SessionLog;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;

/**
 * Executes AWK test cases by running scripts against fixtures and comparing outputs. Produces
 * SessionLog.TestRun records to document pass/fail and diffs. Used by CLI commands and generators
 * to enforce deterministic AWK behavior.
 */
@Component
public class AwkTestRunner {
    private final AwkRunner awkRunner;

    /**
     * Builds a test runner using the provided AwkRunner.
     */
    public AwkTestRunner(AwkRunner awkRunner) {
        this.awkRunner = awkRunner;
    }

    /**
     * Runs the supplied test cases against the default program (or per-test override) and captures
     * pass/fail with diffs.
     *
     * @param testCases      AWK test cases with inputs/expected outputs
     * @param defaultProgram program used when no override is provided
     * @return list of test run results suitable for logging
     */
    // Pattern: Verification
    // - Produces PASS/FAIL receipts with diffs to validate deterministic behavior.
    public List<SessionLog.TestRun> runTests(List<AwkTestCase> testCases, AwkProgram defaultProgram) throws IOException, InterruptedException {
        List<SessionLog.TestRun> results = new ArrayList<>();
        for (AwkTestCase testCase : testCases) {
            AwkProgram program = defaultProgram;
            if (testCase.getProgramOverride() != null) {
                Path overridePath = testCase.getProgramOverride().toAbsolutePath();
                String content = Files.readString(overridePath, StandardCharsets.UTF_8);
                program = new AwkProgram(overridePath, content, null);
            }
            AwkRunRequest request = new AwkRunRequest(program, testCase.getInputPath(), testCase.getVariables());
            AwkRunResult result = awkRunner.run(request);
            String expected = Files.readString(testCase.getExpectedPath(), StandardCharsets.UTF_8);
            String actual = result.getStdout();

            boolean passed = result.isSuccess() && normalize(expected).equals(normalize(actual));
            String diff = passed ? null : diff(expected, actual);
            if (!result.isSuccess() && diff == null) {
                diff = result.getStderr();
            }
            results.add(new SessionLog.TestRun(testCase.getName(), passed, diff));
        }
        return results;
    }

    private String normalize(String content) {
        return content.trim();
    }

    private String diff(String expected, String actual) {
        List<String> expectedLines = Arrays.asList(expected.split("\\R", -1));
        List<String> actualLines = Arrays.asList(actual.split("\\R", -1));
        Patch<String> patch = DiffUtils.diff(expectedLines, actualLines);
        List<String> unified = UnifiedDiffUtils.generateUnifiedDiff("expected", "actual", expectedLines, patch, 3);
        return String.join("\n", unified);
    }
}
