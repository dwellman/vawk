package com.vawk.runtime;

import java.util.Objects;

/**
 * Captures the result of running an AWK script: exit code, stdout, and stderr.
 */
public class AwkRunResult {
    private int exitCode;
    private String stdout;
    private String stderr;

    public AwkRunResult() {
    }

    public AwkRunResult(int exitCode, String stdout, String stderr) {
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public boolean isSuccess() {
        return exitCode == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AwkRunResult that)) return false;
        return exitCode == that.exitCode && Objects.equals(stdout, that.stdout) && Objects.equals(stderr, that.stderr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exitCode, stdout, stderr);
    }
}
