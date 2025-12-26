package com.vawk.cli;

import com.vawk.domain.AwkProgram;
import com.vawk.domain.GenerationMode;
import com.vawk.domain.SessionLog;
import com.vawk.domain.VawkSpec;
import com.vawk.orchestration.GenerateResult;
import com.vawk.orchestration.VawkGeneratorService;
import com.vawk.runtime.DirectoryService;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class GenCommandTest {

    @Test
    void callWritesPlanSnippet() throws Exception {
        StubDirectoryService directoryService = new StubDirectoryService();
        StubGeneratorService generatorService = new StubGeneratorService();
        GenCommand command = new GenCommand(generatorService, directoryService);

        CommandLine cmd = new CommandLine(command);
        StringWriter buffer = new StringWriter();
        cmd.setOut(new PrintWriter(buffer, true));
        cmd.parseArgs("summarize", "logs");

        int exit = command.call();

        assertThat(exit).isZero();
        assertThat(directoryService.ensureCalled).isTrue();
        assertThat(buffer.toString()).contains("Generated spec.yaml and main.awk");
        assertThat(buffer.toString()).contains("PLAN snippet");
        assertThat(buffer.toString()).contains("stub-plan");
    }

    private static final class StubDirectoryService extends DirectoryService {
        private boolean ensureCalled;

        @Override
        public void ensureBaseDirs() {
            ensureCalled = true;
        }
    }

    private static final class StubGeneratorService extends VawkGeneratorService {
        private StubGeneratorService() {
            super(null, null, null, null, null, null, null, null, null, "local-stub");
        }

        @Override
        public GenerateResult generate(String description, GenerationMode mode, String modelOverride, boolean autoTest, String testsDir) {
            VawkSpec spec = new VawkSpec();
            AwkProgram program = new AwkProgram(null, "code", "hash");
            SessionLog log = new SessionLog();
            log.setPlanText("stub-plan");
            log.setProgramHash("hash");
            return new GenerateResult(spec, program, log);
        }
    }
}
