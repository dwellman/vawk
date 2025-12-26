package com.vawk.cli;

import com.vawk.ai.AiResponse;
import com.vawk.domain.AwkProgram;
import com.vawk.orchestration.VawkExplainService;
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
 * CLI command to ask the AI to explain an AWK program. Reads the program, invokes VawkExplainService
 * (PLAN/NOTES), and prints the explanation while recording logs.
 */
@Component
@Command(name = "explain", description = "Explain an AWK program (default main.awk)")
public class ExplainCommand implements Callable<Integer> {

    @Option(names = {"--awk"}, defaultValue = "main.awk", description = "Path to AWK file to explain")
    Path awkPath;

    @Option(names = {"--model"}, description = "Model name to record in session log (default from vawk.ai.model)")
    String modelOverride;

    @Spec
    CommandSpec spec;

    private final ProgramRepository programRepository;
    private final VawkExplainService explainService;
    private final DirectoryService directoryService;

    /**
     * Constructs the explain command with repositories and explain service.
     */
    public ExplainCommand(ProgramRepository programRepository, VawkExplainService explainService, DirectoryService directoryService) {
        this.programRepository = programRepository;
        this.explainService = explainService;
        this.directoryService = directoryService;
    }

    /**
     * Loads the AWK program, requests an explanation, and prints PLAN/NOTES.
     */
    @Override
    // Pattern: Trust UX
    // - Provides explainability output while still writing a session log.
    public Integer call() throws Exception {
        directoryService.ensureBaseDirs();
        AwkProgram program = programRepository.read(awkPath);
        AiResponse response = explainService.explain(program, modelOverride);
        spec.commandLine().getOut().println("PLAN:");
        spec.commandLine().getOut().println(response.getPlan());
        spec.commandLine().getOut().println();
        spec.commandLine().getOut().println("NOTES:");
        spec.commandLine().getOut().println(response.getNotes());
        return 0;
    }
}
