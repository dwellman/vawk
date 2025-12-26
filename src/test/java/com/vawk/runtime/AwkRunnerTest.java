package com.vawk.runtime;

import com.vawk.domain.AwkProgram;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AwkRunnerTest {

    private final AwkRunner awkRunner = new AwkRunner();

    @Test
    void runsLogSummarizerAgainstBasicFixture() throws Exception {
        Path main = Path.of("main.awk").toAbsolutePath();
        Path input = Path.of("tests/input_basic.log");
        String code = Files.readString(main, StandardCharsets.UTF_8);
        AwkProgram program = new AwkProgram(main, code, null);

        AwkRunResult result = awkRunner.run(new AwkRunRequest(program, input, null));
        assertEquals(0, result.getExitCode());

        String expected = Files.readString(Path.of("tests/expect_basic.txt"), StandardCharsets.UTF_8).trim();
        assertEquals(expected, result.getStdout().trim());
    }

    @Test
    void runsEmployeesPowFixture() throws Exception {
        Path programPath = Path.of("tests/fixtures/employees.awk").toAbsolutePath();
        Path input = Path.of("tests/employees_raw.txt");
        String code = Files.readString(programPath, StandardCharsets.UTF_8);
        AwkProgram program = new AwkProgram(programPath, code, null);

        AwkRunResult result = awkRunner.run(new AwkRunRequest(program, input, null));
        assertEquals(0, result.getExitCode());

        String expected = Files.readString(Path.of("tests/employees_clean.csv"), StandardCharsets.UTF_8).trim();
        assertEquals(expected, result.getStdout().trim());
        assertTrue(result.getStderr().isBlank());
    }

    @Test
    void requiresProgramPath() {
        AwkRunRequest request = new AwkRunRequest(null, null, null);

        IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> awkRunner.run(request));
        assertTrue(ex.getMessage().contains("Program path"));
    }

    @Test
    void rejectsRequestWhenProgramPathMissing() {
        AwkRunRequest request = new AwkRunRequest(new AwkProgram(null, "", null), null, null);

        IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> awkRunner.run(request));
        assertTrue(ex.getMessage().contains("Program path"));
    }

    @Test
    void runsWithoutInputFile() throws Exception {
        Path programPath = Files.createTempFile("vawk-noinput", ".awk");
        Files.writeString(programPath, "BEGIN { print \"ok\" }", StandardCharsets.UTF_8);
        AwkProgram program = new AwkProgram(programPath, "BEGIN { print \"ok\" }", null);

        AwkRunResult result = awkRunner.run(new AwkRunRequest(program, null, null));

        assertEquals(0, result.getExitCode());
        assertEquals("ok", result.getStdout().trim());
    }

    @Test
    void passesVariablesToProgram() throws Exception {
        Path programPath = Files.createTempFile("vawk-vars", ".awk");
        Files.writeString(programPath, "BEGIN { print name }", StandardCharsets.UTF_8);
        AwkProgram program = new AwkProgram(programPath, "BEGIN { print name }", null);

        AwkRunResult result = awkRunner.run(new AwkRunRequest(program, null, java.util.Map.of("name", "demo")));

        assertEquals(0, result.getExitCode());
        assertEquals("demo", result.getStdout().trim());
    }
}
