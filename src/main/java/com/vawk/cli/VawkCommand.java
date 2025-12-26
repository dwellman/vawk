package com.vawk.cli;

import picocli.CommandLine.Command;
import org.springframework.stereotype.Component;

/**
 * Root CLI command wiring all VAWK subcommands (gen/test/run/refine/explain/lint/promote/chat).
 * Picocli entry point; delegates to subcommands for behavior.
 */
@Component
@Command(name = "vawk", mixinStandardHelpOptions = true, description = "VAWK CLI", subcommands = {
        GenCommand.class,
        TestCommand.class,
        RunCommand.class,
        RefineCommand.class,
        ExplainCommand.class,
        LintCommand.class,
        PromoteCommand.class,
        ChatCommand.class
})
public class VawkCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use subcommands (gen, test). Run --help for details.");
    }
}
