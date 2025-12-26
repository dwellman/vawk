package com.vawk.orchestration;

import com.vawk.domain.AwkProgram;
import com.vawk.domain.SessionLog;
import com.vawk.domain.VawkSpec;

/**
 * Bundles the spec, generated AWK program, and corresponding session log from a generation step.
 */
public class GenerateResult {
    private final VawkSpec spec;
    private final AwkProgram program;
    private final SessionLog sessionLog;

    /**
     * Creates a result wrapper for downstream use (promotion, reporting).
     */
    public GenerateResult(VawkSpec spec, AwkProgram program, SessionLog sessionLog) {
        this.spec = spec;
        this.program = program;
        this.sessionLog = sessionLog;
    }

    /**
     * @return generated or refined spec
     */
    public VawkSpec getSpec() {
        return spec;
    }

    /**
     * @return generated AWK program
     */
    public AwkProgram getProgram() {
        return program;
    }

    /**
     * @return session log capturing PLAN/CODE/TESTS/NOTES and hashes
     */
    public SessionLog getSessionLog() {
        return sessionLog;
    }
}
