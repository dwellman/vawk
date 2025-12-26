package com.vawk.runtime;

import com.vawk.domain.AwkProgram;
import com.vawk.domain.AwkTestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AwkTestRunnerAdditionalTest {

    @Test
    void usesProgramOverrideWhenPresent(@TempDir Path tempDir) throws Exception {
        Path program = tempDir.resolve("override.awk");
        Files.writeString(program, "BEGIN { print \"ok\" }", StandardCharsets.UTF_8);
        Path input = tempDir.resolve("input.txt");
        Files.writeString(input, "x", StandardCharsets.UTF_8);
        Path expected = tempDir.resolve("expect.txt");
        Files.writeString(expected, "ok", StandardCharsets.UTF_8);

        AwkTestCase testCase = new AwkTestCase("override", input, expected, Map.of(), program);
        AwkTestRunner runner = new AwkTestRunner(new AwkRunner());

        List<com.vawk.domain.SessionLog.TestRun> runs = runner.runTests(List.of(testCase), new AwkProgram(program, "", null));

        assertThat(runs).hasSize(1);
        assertThat(runs.get(0).isPassed()).isTrue();
    }

    @Test
    void reportsDiffWhenOutputMismatches(@TempDir Path tempDir) throws Exception {
        Path program = tempDir.resolve("bad.awk");
        Files.writeString(program, "BEGIN { print \"bad\" }", StandardCharsets.UTF_8);
        Path input = tempDir.resolve("input.txt");
        Files.writeString(input, "x", StandardCharsets.UTF_8);
        Path expected = tempDir.resolve("expect.txt");
        Files.writeString(expected, "ok", StandardCharsets.UTF_8);

        AwkTestCase testCase = new AwkTestCase("diff", input, expected, Map.of(), program);
        AwkTestRunner runner = new AwkTestRunner(new AwkRunner());

        List<com.vawk.domain.SessionLog.TestRun> runs = runner.runTests(List.of(testCase), new AwkProgram(program, "", null));

        assertThat(runs.get(0).isPassed()).isFalse();
        assertThat(runs.get(0).getDiff()).isNotBlank();
    }

    @Test
    void usesDefaultProgramWhenNoOverride(@TempDir Path tempDir) throws Exception {
        Path program = tempDir.resolve("default.awk");
        Files.writeString(program, "BEGIN { print \"ok\" }", StandardCharsets.UTF_8);
        Path input = tempDir.resolve("input.txt");
        Files.writeString(input, "x", StandardCharsets.UTF_8);
        Path expected = tempDir.resolve("expect.txt");
        Files.writeString(expected, "ok", StandardCharsets.UTF_8);

        AwkTestCase testCase = new AwkTestCase("default", input, expected, Map.of(), null);
        AwkTestRunner runner = new AwkTestRunner(new AwkRunner());

        List<com.vawk.domain.SessionLog.TestRun> runs = runner.runTests(List.of(testCase), new AwkProgram(program, "", null));

        assertThat(runs).hasSize(1);
        assertThat(runs.get(0).isPassed()).isTrue();
    }

    @Test
    void reportsFailureWhenRunnerExitNonZero(@TempDir Path tempDir) throws Exception {
        Path input = tempDir.resolve("input.txt");
        Files.writeString(input, "x", StandardCharsets.UTF_8);
        Path expected = tempDir.resolve("expect.txt");
        Files.writeString(expected, "ok", StandardCharsets.UTF_8);

        AwkTestCase testCase = new AwkTestCase("fail", input, expected, Map.of(), null);
        AwkRunner failingRunner = new AwkRunner() {
            @Override
            public AwkRunResult run(AwkRunRequest request) {
                return new AwkRunResult(2, "bad", "boom");
            }
        };
        AwkTestRunner runner = new AwkTestRunner(failingRunner);

        List<com.vawk.domain.SessionLog.TestRun> runs = runner.runTests(List.of(testCase), new AwkProgram(Path.of("main.awk"), "", null));

        assertThat(runs).hasSize(1);
        assertThat(runs.get(0).isPassed()).isFalse();
        assertThat(runs.get(0).getDiff()).isNotNull();
    }
}
