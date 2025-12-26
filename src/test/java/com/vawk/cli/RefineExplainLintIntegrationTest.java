package com.vawk.cli;

import com.vawk.ai.LocalStubAiClient;
import com.vawk.orchestration.VawkExplainService;
import com.vawk.orchestration.VawkGeneratorService;
import com.vawk.orchestration.VawkLintService;
import com.vawk.orchestration.VawkRefinementService;
import com.vawk.store.AgentsFileRepository;
import com.vawk.store.ProgramRepository;
import com.vawk.store.SessionLogRepository;
import com.vawk.store.SpecRepository;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefineExplainLintIntegrationTest {

    private CommandLine initCommand(Object cmd, ByteArrayOutputStream out, ByteArrayOutputStream err) {
        CommandLine cl = new CommandLine(cmd);
        cl.setOut(new PrintWriter(out, true));
        cl.setErr(new PrintWriter(err, true));
        return cl;
    }

    @Test
    void refineExplainLintCommandsSucceed() throws Exception {
        LocalStubAiClient stub = new LocalStubAiClient();
        var agents = new com.vawk.ai.VawkAgents(stub);
        AgentsFileRepository agentsRepo = new AgentsFileRepository();
        SpecRepository specRepository = new SpecRepository();
        ProgramRepository programRepository = new ProgramRepository();
        SessionLogRepository sessionLogRepository = new SessionLogRepository();

        // ensure generator initialized artifacts
        var specBuilder = new com.vawk.orchestration.VawkSpecBuilder(agents);
        var ragRepo = new com.vawk.store.RagRepository();
        var awkRunner = new com.vawk.runtime.AwkRunner();
        var awkTestRunner = new com.vawk.runtime.AwkTestRunner(awkRunner);
        var testRepository = new com.vawk.store.TestRepository();
        VawkGeneratorService generator = new VawkGeneratorService(agents, specRepository, programRepository, sessionLogRepository, agentsRepo, specBuilder, ragRepo, awkTestRunner, testRepository, "local-stub");
        generator.generate("Summarize logs by level and optionally per user", com.vawk.domain.GenerationMode.COT);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        var dirService = new com.vawk.runtime.DirectoryService();
        RefineCommand refineCommand = new RefineCommand(new VawkRefinementService(agents, specRepository, programRepository, sessionLogRepository, agentsRepo, specBuilder, ragRepo, awkTestRunner, testRepository, "local-stub"), dirService);
        int refineExit = initCommand(refineCommand, out, err).execute("Handle WARN as caution", "--model", "local-stub");
        assertEquals(0, refineExit);

        ExplainCommand explainCommand = new ExplainCommand(programRepository, new VawkExplainService(agents, agentsRepo, programRepository, sessionLogRepository, "local-stub"), dirService);
        out.reset();
        int explainExit = initCommand(explainCommand, out, err).execute("--model", "local-stub");
        assertEquals(0, explainExit);
        assertTrue(out.toString().contains("PLAN"));

        LintCommand lintCommand = new LintCommand(programRepository, new VawkLintService(agents, agentsRepo, programRepository, sessionLogRepository, "local-stub"), dirService);
        out.reset();
        int lintExit = initCommand(lintCommand, out, err).execute("--model", "local-stub");
        assertEquals(0, lintExit);
        assertTrue(out.toString().contains("PLAN"));
    }
}
