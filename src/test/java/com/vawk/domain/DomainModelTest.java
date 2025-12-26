package com.vawk.domain;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainModelTest {

    @Test
    void vawkSpecCopiesCollections() {
        VawkSpec spec = new VawkSpec();
        spec.setDescription("demo");
        spec.setInputs(List.of("input"));
        spec.setOutputs(List.of("output"));
        spec.setConstraints(List.of("constraint"));
        spec.setExamples(List.of("example"));

        assertThat(spec.getDescription()).isEqualTo("demo");
        assertThat(spec.getInputs()).containsExactly("input");
        assertThat(spec.getOutputs()).containsExactly("output");
        assertThat(spec.getConstraints()).containsExactly("constraint");
        assertThat(spec.getExamples()).containsExactly("example");
    }

    @Test
    void awkProgramEqualityUsesFields() {
        AwkProgram first = new AwkProgram(Path.of("main.awk"), "code", "hash");
        AwkProgram second = new AwkProgram(Path.of("main.awk"), "code", "hash");

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void awkTestCaseStoresOverrides() {
        AwkTestCase testCase = new AwkTestCase("case", Path.of("in"), Path.of("out"),
                java.util.Map.of("x", "1"), Path.of("prog"));

        assertThat(testCase.getName()).isEqualTo("case");
        assertThat(testCase.getVariables()).containsEntry("x", "1");
        assertThat(testCase.getProgramOverride()).isEqualTo(Path.of("prog"));
    }

    @Test
    void awkTestCaseHandlesNullVariablesInConstructor() {
        AwkTestCase testCase = new AwkTestCase("case", Path.of("in"), Path.of("out"), null, null);

        assertThat(testCase.getVariables()).isEmpty();
        assertThat(testCase.getProgramOverride()).isNull();
    }

    @Test
    void awkTestCaseSetVariablesReplacesExisting() {
        AwkTestCase testCase = new AwkTestCase();
        testCase.setVariables(java.util.Map.of("a", "1"));
        testCase.setVariables(java.util.Map.of("b", "2"));

        assertThat(testCase.getVariables()).containsExactlyEntriesOf(java.util.Map.of("b", "2"));
    }

    @Test
    void awkTestCaseEqualityMatchesFields() {
        AwkTestCase first = new AwkTestCase("case", Path.of("in"), Path.of("out"), java.util.Map.of("x", "1"), Path.of("prog"));
        AwkTestCase second = new AwkTestCase("case", Path.of("in"), Path.of("out"), java.util.Map.of("x", "1"), Path.of("prog"));

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void awkTestCaseEqualityHandlesNullOverrides() {
        AwkTestCase first = new AwkTestCase("case", Path.of("in"), Path.of("out"), null, null);
        AwkTestCase second = new AwkTestCase("case", Path.of("in"), Path.of("out"), null, null);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void awkTestCaseEqualityDetectsFieldDifferences() {
        AwkTestCase base = new AwkTestCase("case", Path.of("in"), Path.of("out"), java.util.Map.of("x", "1"), Path.of("prog"));
        AwkTestCase diffInput = new AwkTestCase("case", Path.of("in2"), Path.of("out"), java.util.Map.of("x", "1"), Path.of("prog"));
        AwkTestCase diffExpected = new AwkTestCase("case", Path.of("in"), Path.of("out2"), java.util.Map.of("x", "1"), Path.of("prog"));
        AwkTestCase diffVars = new AwkTestCase("case", Path.of("in"), Path.of("out"), java.util.Map.of("y", "2"), Path.of("prog"));
        AwkTestCase diffProgram = new AwkTestCase("case", Path.of("in"), Path.of("out"), java.util.Map.of("x", "1"), Path.of("prog2"));

        assertThat(base).isNotEqualTo(diffInput);
        assertThat(base).isNotEqualTo(diffExpected);
        assertThat(base).isNotEqualTo(diffVars);
        assertThat(base).isNotEqualTo(diffProgram);
    }

    @Test
    void sessionLogTestRunEqualityMatchesFields() {
        SessionLog.TestRun run = new SessionLog.TestRun("t", true, null);
        SessionLog.TestRun other = new SessionLog.TestRun("t", true, null);

        assertThat(run).isEqualTo(other);
        assertThat(run.isPassed()).isTrue();
    }

    @Test
    void sessionLogTestRunHandlesSameInstanceAndDifferentType() {
        SessionLog.TestRun run = new SessionLog.TestRun("t", true, "diff");
        assertThat(run).isEqualTo(run);
        assertThat(run).isNotEqualTo("nope");
    }

    @Test
    void sessionLogSupportsMetadata() {
        SessionLog log = new SessionLog();
        log.setId("id");
        log.setTimestamp(Instant.EPOCH);
        log.setCommand("GEN");
        log.setMode(GenerationMode.COT);

        assertThat(log.getId()).isEqualTo("id");
        assertThat(log.getTimestamp()).isEqualTo(Instant.EPOCH);
        assertThat(log.getCommand()).isEqualTo("GEN");
        assertThat(log.getMode()).isEqualTo(GenerationMode.COT);
    }

    @Test
    void domainEqualityFailsOnMismatchedFields() {
        AwkTestCase one = new AwkTestCase("one", Path.of("in"), Path.of("out"), null, null);
        AwkTestCase two = new AwkTestCase("two", Path.of("in"), Path.of("out"), null, null);

        assertThat(one).isNotEqualTo(two);

        SessionLog.TestRun run = new SessionLog.TestRun("t", true, null);
        SessionLog.TestRun other = new SessionLog.TestRun("t", false, null);
        assertThat(run).isNotEqualTo(other);
    }

    @Test
    void sessionLogEqualityDetectsFieldDifferences() {
        SessionLog base = baseSessionLog();

        SessionLog idDiff = baseSessionLog();
        idDiff.setId("other");
        SessionLog tsDiff = baseSessionLog();
        tsDiff.setTimestamp(Instant.ofEpochSecond(1));
        SessionLog cmdDiff = baseSessionLog();
        cmdDiff.setCommand("RUN");
        SessionLog modeDiff = baseSessionLog();
        modeDiff.setMode(GenerationMode.SINGLE);
        SessionLog modelDiff = baseSessionLog();
        modelDiff.setModel("model-2");
        SessionLog planDiff = baseSessionLog();
        planDiff.setPlanText("plan-2");
        SessionLog codeDiff = baseSessionLog();
        codeDiff.setCode("code-2");
        SessionLog testsDiff = baseSessionLog();
        testsDiff.setTestsText("tests-2");
        SessionLog testsRequestedDiff = baseSessionLog();
        testsRequestedDiff.setTestsRequested(List.of("other"));
        SessionLog testsRunDiff = baseSessionLog();
        testsRunDiff.setTestsRun(List.of(new SessionLog.TestRun("t2", true, null)));
        SessionLog notesDiff = baseSessionLog();
        notesDiff.setNotes("notes-2");
        SessionLog specHashDiff = baseSessionLog();
        specHashDiff.setSpecHash("spec-2");
        SessionLog programHashDiff = baseSessionLog();
        programHashDiff.setProgramHash("prog-2");

        assertThat(base).isNotEqualTo(idDiff);
        assertThat(base).isNotEqualTo(tsDiff);
        assertThat(base).isNotEqualTo(cmdDiff);
        assertThat(base).isNotEqualTo(modeDiff);
        assertThat(base).isNotEqualTo(modelDiff);
        assertThat(base).isNotEqualTo(planDiff);
        assertThat(base).isNotEqualTo(codeDiff);
        assertThat(base).isNotEqualTo(testsDiff);
        assertThat(base).isNotEqualTo(testsRequestedDiff);
        assertThat(base).isNotEqualTo(testsRunDiff);
        assertThat(base).isNotEqualTo(notesDiff);
        assertThat(base).isNotEqualTo(specHashDiff);
        assertThat(base).isNotEqualTo(programHashDiff);
    }

    @Test
    void specEqualityDetectsDifferences() {
        VawkSpec base = new VawkSpec("desc", List.of("in"), List.of("out"), List.of("c"), List.of("ex"));
        VawkSpec diffDesc = new VawkSpec("other", List.of("in"), List.of("out"), List.of("c"), List.of("ex"));
        VawkSpec diffInputs = new VawkSpec("desc", List.of("in2"), List.of("out"), List.of("c"), List.of("ex"));
        VawkSpec diffOutputs = new VawkSpec("desc", List.of("in"), List.of("out2"), List.of("c"), List.of("ex"));
        VawkSpec diffConstraints = new VawkSpec("desc", List.of("in"), List.of("out"), List.of("c2"), List.of("ex"));
        VawkSpec diffExamples = new VawkSpec("desc", List.of("in"), List.of("out"), List.of("c"), List.of("ex2"));

        assertThat(base).isNotEqualTo(diffDesc);
        assertThat(base).isNotEqualTo(diffInputs);
        assertThat(base).isNotEqualTo(diffOutputs);
        assertThat(base).isNotEqualTo(diffConstraints);
        assertThat(base).isNotEqualTo(diffExamples);
    }

    @Test
    void vawkSpecConstructorHandlesNullLists() {
        VawkSpec spec = new VawkSpec("desc", null, null, null, null);

        assertThat(spec.getInputs()).isEmpty();
        assertThat(spec.getOutputs()).isEmpty();
        assertThat(spec.getConstraints()).isEmpty();
        assertThat(spec.getExamples()).isEmpty();
    }

    @Test
    void equalityHandlesSameInstanceAndDifferentType() {
        AwkTestCase testCase = new AwkTestCase();
        assertThat(testCase).isEqualTo(testCase);
        assertThat(testCase).isNotEqualTo("nope");

        AwkProgram program = new AwkProgram(Path.of("main.awk"), "code", "hash");
        assertThat(program).isNotEqualTo("nope");
    }

    @Test
    void settersHandleNullCollections() {
        VawkSpec spec = new VawkSpec();
        spec.setInputs(null);
        spec.setOutputs(null);
        spec.setConstraints(null);
        spec.setExamples(null);

        assertThat(spec.getInputs()).isEmpty();
        assertThat(spec.getOutputs()).isEmpty();
        assertThat(spec.getConstraints()).isEmpty();
        assertThat(spec.getExamples()).isEmpty();

        AwkTestCase testCase = new AwkTestCase();
        testCase.setVariables(null);
        assertThat(testCase.getVariables()).isEmpty();
    }

    @Test
    void awkProgramEqualityDetectsDifferences() {
        AwkProgram base = new AwkProgram(Path.of("main.awk"), "code", "hash");
        AwkProgram diff = new AwkProgram(Path.of("main.awk"), "code2", "hash");

        assertThat(base).isNotEqualTo(diff);
    }

    @Test
    void awkProgramEqualityDetectsPathAndHashDifferences() {
        AwkProgram base = new AwkProgram(Path.of("main.awk"), "code", "hash");
        AwkProgram diffPath = new AwkProgram(Path.of("other.awk"), "code", "hash");
        AwkProgram diffHash = new AwkProgram(Path.of("main.awk"), "code", "hash2");

        assertThat(base).isNotEqualTo(diffPath);
        assertThat(base).isNotEqualTo(diffHash);
    }

    @Test
    void awkProgramEqualityHandlesSameInstance() {
        AwkProgram program = new AwkProgram(Path.of("main.awk"), "code", "hash");

        assertThat(program).isEqualTo(program);
    }

    @Test
    void sessionLogHandlesNullTestCollections() {
        SessionLog log = new SessionLog();
        log.setTestsRequested(null);
        log.setTestsRun(null);

        assertThat(log.getTestsRequested()).isEmpty();
        assertThat(log.getTestsRun()).isEmpty();
    }

    private SessionLog baseSessionLog() {
        SessionLog log = new SessionLog();
        log.setId("id");
        log.setTimestamp(Instant.EPOCH);
        log.setCommand("GEN");
        log.setMode(GenerationMode.COT);
        log.setModel("model");
        log.setPlanText("plan");
        log.setCode("code");
        log.setTestsText("tests");
        log.setTestsRequested(List.of("t1"));
        log.setTestsRun(List.of(new SessionLog.TestRun("t1", true, null)));
        log.setNotes("notes");
        log.setSpecHash("spec");
        log.setProgramHash("prog");
        return log;
    }
}
