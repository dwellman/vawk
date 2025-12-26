package com.vawk.cli;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

class VawkCommandTest {

    @Test
    void runPrintsHint() {
        PrintStream original = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            new VawkCommand().run();
        } finally {
            System.setOut(original);
        }
        assertThat(out.toString()).contains("Use subcommands");
    }
}
