package com.vawk.runtime;

import com.vawk.domain.AwkProgram;
import com.vawk.domain.AwkTestCase;
import com.vawk.domain.SessionLog;
import com.vawk.store.ProgramRepository;
import com.vawk.store.TestRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AwkTestRunnerTest {

    private final AwkRunner awkRunner = new AwkRunner();
    private final AwkTestRunner awkTestRunner = new AwkTestRunner(awkRunner);
    private final TestRepository testRepository = new TestRepository();
    private final ProgramRepository programRepository = new ProgramRepository();

    @Test
    void runsAllFixtures() throws Exception {
        List<AwkTestCase> cases = testRepository.load();
        AwkProgram defaultProgram = programRepository.read();
        List<SessionLog.TestRun> results = awkTestRunner.runTests(cases, defaultProgram);

        assertFalse(results.isEmpty(), "Expected fixtures to be discovered");
        for (SessionLog.TestRun run : results) {
            assertTrue(run.isPassed(), "Fixture failed: " + run.getName() + "\n" + run.getDiff());
        }
    }
}
