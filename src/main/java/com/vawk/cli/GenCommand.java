package com.vawk.cli;

import com.vawk.domain.GenerationMode;
import com.vawk.orchestration.GenerateResult;
import com.vawk.orchestration.VawkGeneratorService;
import com.vawk.runtime.DirectoryService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

import java.util.concurrent.Callable;

/**
 * CLI command to generate a VAWK spec, AWK program, and session log from a natural language
 * description. Uses VawkGeneratorService for plan/code generation and optional auto-testing.
 */
@Component
@Command(name = "gen", description = "Generate spec.yaml, main.awk, and session log for an AWK task")
public class GenCommand implements Callable<Integer> {

    @Parameters(index = "0..*", arity = "1..*", description = "Natural language task description")
    String[] descriptionParts;

    @Option(names = {"--mode"}, defaultValue = "COT", description = "Generation mode (SINGLE, COT, ITERATIVE, RAG)")
    GenerationMode mode;

    @Option(names = {"--model"}, description = "Model name to record in session log (default from vawk.ai.model)")
    String modelOverride;

    @Option(names = {"--auto-test"}, description = "Run vawk test after generation and record results", defaultValue = "false")
    boolean autoTest;

    @Option(names = {"--tests-dir"}, description = "Tests directory for auto-test (default tests)", defaultValue = "tests")
    String testsDir;

    @Spec
    CommandSpec spec;

    private final VawkGeneratorService generatorService;
    private final DirectoryService directoryService;

    /**
     * Constructs the generator CLI command.
     */
    public GenCommand(VawkGeneratorService generatorService, DirectoryService directoryService) {
        this.generatorService = generatorService;
        this.directoryService = directoryService;
    }

    @Override
    public Integer call() throws Exception {
        directoryService.ensureBaseDirs();
        String description = String.join(" ", descriptionParts).trim();
        GenerateResult result = generatorService.generate(description, mode, modelOverride, autoTest, testsDir);
        spec.commandLine().getOut().println("Generated spec.yaml and main.awk");
        spec.commandLine().getOut().println("PLAN snippet:");
        spec.commandLine().getOut().println(result.getSessionLog().getPlanText());
        spec.commandLine().getOut().println("Session log written with program hash " + result.getSessionLog().getProgramHash());
        return 0;
    }
}
