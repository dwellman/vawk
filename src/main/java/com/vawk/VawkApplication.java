package com.vawk;

import com.vawk.cli.VawkCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

/**
 * Launches the VAWK CLI using Spring Boot and Picocli. This is the handoff point from the JVM into
 * the command hierarchy defined under com.vawk.cli.
 */
@SpringBootApplication
public class VawkApplication implements CommandLineRunner {

    @Autowired
    private VawkCommand vawkCommand;

    @Autowired
    private CommandLine.IFactory picocliFactory;

    /**
     * Boots the Spring application and dispatches to the Picocli entry point.
     *
     * @param args CLI arguments forwarded from the JVM
     */
    public static void main(String[] args) {
        SpringApplication.run(VawkApplication.class, args);
    }

    /**
     * Executes the root Picocli command and exits with its status code.
     *
     * @param args raw CLI arguments passed to the application
     */
    @Override
    public void run(String... args) {
        int exitCode = new CommandLine(vawkCommand, picocliFactory).execute(args);
        System.exit(exitCode);
    }
}
