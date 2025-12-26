package com.vawk.cli;

import com.vawk.domain.GenerationMode;
import com.vawk.runtime.AwkRunner;
import com.vawk.runtime.AwkTestRunner;
import com.vawk.store.ProgramRepository;
import com.vawk.store.SessionLogRepository;
import com.vawk.store.SpecRepository;
import com.vawk.store.TestRepository;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestCommandIntegrationTest {

    @Test
    void testCommandPassesFixtures() throws Exception {
        TestRepository testRepository = new TestRepository();
        ProgramRepository programRepository = new ProgramRepository();
        SpecRepository specRepository = new SpecRepository();
        SessionLogRepository sessionLogRepository = new SessionLogRepository();
        AwkRunner awkRunner = new AwkRunner();
        AwkTestRunner awkTestRunner = new AwkTestRunner(awkRunner);

        TestCommand command = new TestCommand(testRepository, programRepository, specRepository, sessionLogRepository, awkTestRunner, new com.vawk.runtime.DirectoryService());
        CommandLine cmd = new CommandLine(command);
        int exitCode = cmd.execute();
        assertEquals(0, exitCode);
    }
}
