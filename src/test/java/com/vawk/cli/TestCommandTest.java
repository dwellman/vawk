package com.vawk.cli;

import com.vawk.domain.AwkProgram;
import com.vawk.domain.AwkTestCase;
import com.vawk.domain.SessionLog;
import com.vawk.runtime.AwkTestRunner;
import com.vawk.runtime.DirectoryService;
import com.vawk.store.ProgramRepository;
import com.vawk.store.SessionLogRepository;
import com.vawk.store.SpecRepository;
import com.vawk.store.TestRepository;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestCommandTest {

    @Test
    void returnsErrorWhenNoTestsFound() throws Exception {
        TestCommand command = new TestCommand(new StubTestRepository(List.of()), new StubProgramRepository(), new SpecRepository(), new StubSessionLogRepository(), new StubTestRunner(List.of()), new StubDirectoryService());

        int exit = command.call();

        assertThat(exit).isEqualTo(1);
    }

    @Test
    void returnsNonZeroWhenTestsFail() throws Exception {
        TestCommand command = new TestCommand(new StubTestRepository(List.of(new AwkTestCase())), new StubProgramRepository(), new SpecRepository(), new StubSessionLogRepository(), new StubTestRunner(List.of(new SessionLog.TestRun("t", false, "diff"))), new StubDirectoryService());

        CommandLine cmd = new CommandLine(command);
        cmd.parseArgs();
        int exit = command.call();

        assertThat(exit).isEqualTo(1);
    }

    @Test
    void returnsZeroWhenAllTestsPass() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        TestCommand command = new TestCommand(new StubTestRepository(List.of(new AwkTestCase())), new StubProgramRepository(), new SpecRepository(), sessionLogRepository, new StubTestRunner(List.of(new SessionLog.TestRun("t", true, null))), new StubDirectoryService());

        int exit = command.call();

        assertThat(exit).isZero();
        assertThat(sessionLogRepository.lastLog).isNotNull();
    }

    @Test
    void capturesSpecHashWhenSpecExists() throws Exception {
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        TestCommand command = new TestCommand(new StubTestRepository(List.of(new AwkTestCase())), new StubProgramRepository(), new SpecRepository(), sessionLogRepository, new StubTestRunner(List.of(new SessionLog.TestRun("t", true, null))), new StubDirectoryService());

        Path specPath = Path.of("spec.yaml");
        java.nio.file.Files.writeString(specPath, "description: demo");
        try {
            int exit = command.call();

            assertThat(exit).isZero();
            assertThat(sessionLogRepository.lastLog.getSpecHash()).isNotBlank();
        } finally {
            java.nio.file.Files.deleteIfExists(specPath);
        }
    }

    private static final class StubTestRepository extends TestRepository {
        private final List<AwkTestCase> cases;

        private StubTestRepository(List<AwkTestCase> cases) {
            this.cases = cases;
        }

        @Override
        public List<AwkTestCase> load(Path testsDir) {
            return cases;
        }
    }

    private static final class StubProgramRepository extends ProgramRepository {
        @Override
        public AwkProgram read(Path path) {
            return new AwkProgram(path, "code", "hash");
        }
    }

    private static final class StubTestRunner extends AwkTestRunner {
        private final List<SessionLog.TestRun> runs;

        private StubTestRunner(List<SessionLog.TestRun> runs) {
            super(new com.vawk.runtime.AwkRunner());
            this.runs = runs;
        }

        @Override
        public List<SessionLog.TestRun> runTests(List<AwkTestCase> testCases, AwkProgram defaultProgram) {
            return runs;
        }
    }

    private static final class StubSessionLogRepository extends SessionLogRepository {
        private SessionLog lastLog;

        @Override
        public Path write(SessionLog log) {
            this.lastLog = log;
            return Path.of(".vawk/logs/log.json");
        }
    }

    private static final class StubDirectoryService extends DirectoryService {
        @Override
        public void ensureBaseDirs() {
        }
    }
}
