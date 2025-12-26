package com.vawk.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Captures a single VAWK generation or chat session for auditability. Stores command metadata,
 * model details, PLAN/CODE/TESTS/NOTES text, hashes, requested and executed tests, plus optional
 * notes. Persisted to .vawk/logs for proof-of-work.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionLog {
    private String id;
    private Instant timestamp;
    private String command;
    private GenerationMode mode;
    private String model;
    private String planText;
    private String code;
    private String testsText;
    // Pattern: Learning
    // - Tracks which tests the model said it would run for later feedback.
    private List<String> testsRequested = new ArrayList<>();
    // Pattern: Learning
    // - Captures executed test results to close the feedback loop.
    private List<TestRun> testsRun = new ArrayList<>();
    private String notes;
    private String specHash;
    private String programHash;

    public SessionLog() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public GenerationMode getMode() {
        return mode;
    }

    public void setMode(GenerationMode mode) {
        this.mode = mode;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPlanText() {
        return planText;
    }

    public void setPlanText(String planText) {
        this.planText = planText;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTestsText() {
        return testsText;
    }

    public void setTestsText(String testsText) {
        this.testsText = testsText;
    }

    public List<String> getTestsRequested() {
        return testsRequested;
    }

    public void setTestsRequested(List<String> testsRequested) {
        this.testsRequested = testsRequested != null ? new ArrayList<>(testsRequested) : new ArrayList<>();
    }

    public List<TestRun> getTestsRun() {
        return testsRun;
    }

    public void setTestsRun(List<TestRun> testsRun) {
        this.testsRun = testsRun != null ? new ArrayList<>(testsRun) : new ArrayList<>();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSpecHash() {
        return specHash;
    }

    public void setSpecHash(String specHash) {
        this.specHash = specHash;
    }

    public String getProgramHash() {
        return programHash;
    }

    public void setProgramHash(String programHash) {
        this.programHash = programHash;
    }

    /**
     * Logs are equal when all metadata and text fields match.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionLog that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(timestamp, that.timestamp) && Objects.equals(command, that.command) && mode == that.mode && Objects.equals(model, that.model) && Objects.equals(planText, that.planText) && Objects.equals(code, that.code) && Objects.equals(testsText, that.testsText) && Objects.equals(testsRequested, that.testsRequested) && Objects.equals(testsRun, that.testsRun) && Objects.equals(notes, that.notes) && Objects.equals(specHash, that.specHash) && Objects.equals(programHash, that.programHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp, command, mode, model, planText, code, testsText, testsRequested, testsRun, notes, specHash, programHash);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TestRun {
        private String name;
        private boolean passed;
        private String diff;

        public TestRun() {
        }

        public TestRun(String name, boolean passed, String diff) {
            this.name = name;
            this.passed = passed;
            this.diff = diff;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        public String getDiff() {
            return diff;
        }

        public void setDiff(String diff) {
            this.diff = diff;
        }

        /**
         * Test runs are equal when name, status, and diff match.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestRun testRun)) return false;
            return passed == testRun.passed && Objects.equals(name, testRun.name) && Objects.equals(diff, testRun.diff);
        }

        /**
         * Hashes name, status, and diff for collection usage.
         */
        @Override
        public int hashCode() {
            return Objects.hash(name, passed, diff);
        }
    }
}
