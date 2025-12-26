package com.vawk.cli;

import com.vawk.domain.GenerationMode;
import com.vawk.orchestration.GenerateResult;
import com.vawk.orchestration.VawkRefinementService;
import com.vawk.runtime.DirectoryService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

import java.util.concurrent.Callable;

/**
 * CLI command to refine existing AWK code based on a change request. Uses VawkRefinementService to
 * generate updated PLAN/CODE/TESTS/NOTES and optionally auto-test, logging results.
 */
@Component
@Command(name = "refine", description = "Refine existing AWK code per change request")
public class RefineCommand implements Callable<Integer> {

    @Parameters(index = "0..*", arity = "1..*", description = "Change request description")
    String[] changeRequestParts;

    @Option(names = {"--mode"}, defaultValue = "ITERATIVE", description = "Generation mode (SINGLE, COT, ITERATIVE, RAG)")
    GenerationMode mode;

    @Option(names = {"--model"}, description = "Model name to record in session log (default from vawk.ai.model)")
    String modelOverride;

    @Option(names = {"--auto-test"}, description = "Run vawk test after refinement and record results", defaultValue = "false")
    boolean autoTest;

    @Spec
    CommandSpec spec;

    private final VawkRefinementService refinementService;
    private final DirectoryService directoryService;

    /**
     * Builds the refine command with services and directory management.
     */
    public RefineCommand(VawkRefinementService refinementService, DirectoryService directoryService) {
        this.refinementService = refinementService;
        this.directoryService = directoryService;
    }

    /**
     * Executes refinement and prints the new plan snippet.
     */
    @Override
    public Integer call() throws Exception {
        directoryService.ensureBaseDirs();
        String changeRequest = String.join(" ", changeRequestParts).trim();
        GenerateResult result = refinementService.refine(changeRequest, mode, modelOverride, autoTest);
        spec.commandLine().getOut().println("Refined main.awk");
        spec.commandLine().getOut().println("PLAN snippet:");
        spec.commandLine().getOut().println(result.getSessionLog().getPlanText());
        return 0;
    }
}
