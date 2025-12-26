package com.vawk.cli;

import com.vawk.runtime.AwkRunner;
import com.vawk.store.ProgramRepository;
import com.vawk.store.SessionLogRepository;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunCommandIntegrationTest {

    @Test
    void runsMainAwkAgainstInput() throws Exception {
        RunCommand command = new RunCommand(new ProgramRepository(), new AwkRunner(), new SessionLogRepository(), new com.vawk.runtime.DirectoryService());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        CommandLine cmd = new CommandLine(command);
        cmd.setOut(new PrintWriter(out, true));
        cmd.setErr(new PrintWriter(err, true));

        int exit = cmd.execute("tests/input_basic.log");
        assertEquals(0, exit);

        String expected = Files.readString(Path.of("tests/expect_basic.txt"), StandardCharsets.UTF_8).trim();
        assertEquals(expected, out.toString(StandardCharsets.UTF_8).trim());
        assertTrue(err.toString(StandardCharsets.UTF_8).isBlank());
    }
}
