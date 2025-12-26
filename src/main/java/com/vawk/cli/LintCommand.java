package com.vawk.cli;

import com.vawk.ai.AiResponse;
import com.vawk.domain.AwkProgram;
import com.vawk.orchestration.VawkLintService;
import com.vawk.runtime.DirectoryService;
import com.vawk.store.ProgramRepository;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * CLI command to lint an AWK program using AI suggestions. Runs VawkLintService and prints
 * PLAN/TESTS while recording logs.
 */
@Component
@Command(name = "lint", description = "Lint an AWK program with AI suggestions")
public class LintCommand implements Callable<Integer> {

    @Option(names = {"--awk"}, defaultValue = "main.awk", description = "Path to AWK file to lint")
    Path awkPath;

    @Option(names = {"--model"}, description = "Model name to record in session log (default from vawk.ai.model)")
    String modelOverride;

    @Spec
    CommandSpec spec;

    private final ProgramRepository programRepository;
    private final VawkLintService lintService;
    private final DirectoryService directoryService;

    /**
     * Constructs the lint command with repositories and lint service.
     */
    public LintCommand(ProgramRepository programRepository, VawkLintService lintService, DirectoryService directoryService) {
        this.programRepository = programRepository;
        this.lintService = lintService;
        this.directoryService = directoryService;
    }

    /**
     * Loads the program, requests lint feedback, and prints PLAN/TESTS.
     */
    @Override
    public Integer call() throws Exception {
        directoryService.ensureBaseDirs();
        AwkProgram program = programRepository.read(awkPath);
        AiResponse response = lintService.lint(program, modelOverride);
        spec.commandLine().getOut().println("PLAN:");
        spec.commandLine().getOut().println(response.getPlan());
        spec.commandLine().getOut().println();
        spec.commandLine().getOut().println("TESTS:");
        spec.commandLine().getOut().println(response.getTests());
        return 0;
    }
}
