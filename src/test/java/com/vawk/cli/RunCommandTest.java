package com.vawk.cli;

import com.vawk.domain.AwkProgram;
import com.vawk.domain.SessionLog;
import com.vawk.runtime.AwkRunRequest;
import com.vawk.runtime.AwkRunResult;
import com.vawk.runtime.AwkRunner;
import com.vawk.runtime.DirectoryService;
import com.vawk.store.ProgramRepository;
import com.vawk.store.SessionLogRepository;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class RunCommandTest {

    @Test
    void invalidVarAssignmentReturnsError() throws Exception {
        StubProgramRepository programRepository = new StubProgramRepository();
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        RunCommand command = new RunCommand(programRepository, new StubAwkRunner(), sessionLogRepository, new StubDirectoryService());

        CommandLine cmd = new CommandLine(command);
        StringWriter err = new StringWriter();
        cmd.setErr(new PrintWriter(err, true));
        cmd.parseArgs("--var", "bad", "input.txt");

        int exit = command.call();

        assertThat(exit).isEqualTo(1);
        assertThat(err.toString()).contains("Invalid --var assignment");
    }

    @Test
    void runWritesSessionLogWhenValid() throws Exception {
        StubProgramRepository programRepository = new StubProgramRepository();
        StubSessionLogRepository sessionLogRepository = new StubSessionLogRepository();
        RunCommand command = new RunCommand(programRepository, new StubAwkRunner(), sessionLogRepository, new StubDirectoryService());

        CommandLine cmd = new CommandLine(command);
        StringWriter out = new StringWriter();
        cmd.setOut(new PrintWriter(out, true));
        cmd.parseArgs("--var", "by_user=1", "input.txt");

        int exit = command.call();

        assertThat(exit).isZero();
        assertThat(out.toString()).contains("ok");
        assertThat(sessionLogRepository.lastLog).isNotNull();
    }

    private static final class StubProgramRepository extends ProgramRepository {
        @Override
        public AwkProgram read(Path path) {
            return new AwkProgram(path, "BEGIN { print \"ok\" }", "hash");
        }
    }

    private static final class StubAwkRunner extends AwkRunner {
        @Override
        public AwkRunResult run(AwkRunRequest request) {
            return new AwkRunResult(0, "ok\n", "");
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
