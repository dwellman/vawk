package com.vawk.runtime;

import com.vawk.domain.AwkProgram;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeModelTest {

    @Test
    void awkRunRequestCopiesVariables() {
        AwkProgram program = new AwkProgram(Path.of("main.awk"), "code", "hash");
        AwkRunRequest request = new AwkRunRequest(program, Path.of("input.txt"), Map.of("by_user", "1"));

        assertThat(request.getProgram()).isEqualTo(program);
        assertThat(request.getInputFile()).isEqualTo(Path.of("input.txt"));
        assertThat(request.getVariables()).containsEntry("by_user", "1");
    }

    @Test
    void awkRunRequestConstructorHandlesNullVariables() {
        AwkProgram program = new AwkProgram(Path.of("main.awk"), "code", "hash");
        AwkRunRequest request = new AwkRunRequest(program, null, null);

        assertThat(request.getVariables()).isEmpty();
    }

    @Test
    void awkRunResultReportsSuccess() {
        AwkRunResult result = new AwkRunResult(0, "out", "");

        assertThat(result.isSuccess()).isTrue();
        result.setExitCode(2);
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    void awkRunRequestEqualityChecksFields() {
        AwkProgram program = new AwkProgram(Path.of("main.awk"), "code", "hash");
        AwkRunRequest first = new AwkRunRequest(program, Path.of("input.txt"), Map.of("x", "1"));
        AwkRunRequest second = new AwkRunRequest(program, Path.of("other.txt"), Map.of("x", "1"));

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void awkRunRequestEqualityHandlesSameInstance() {
        AwkRunRequest request = new AwkRunRequest();

        assertThat(request).isEqualTo(request);
    }

    @Test
    void awkRunResultEqualityChecksFields() {
        AwkRunResult first = new AwkRunResult(0, "out", "");
        AwkRunResult second = new AwkRunResult(1, "out", "");

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void awkRunResultEqualityDetectsFieldDifferences() {
        AwkRunResult base = new AwkRunResult(0, "out", "err");
        AwkRunResult diffStdout = new AwkRunResult(0, "other", "err");
        AwkRunResult diffStderr = new AwkRunResult(0, "out", "other");

        assertThat(base).isNotEqualTo(diffStdout);
        assertThat(base).isNotEqualTo(diffStderr);
    }

    @Test
    void awkRunResultEqualityMatchesFields() {
        AwkRunResult first = new AwkRunResult(0, "out", "err");
        AwkRunResult second = new AwkRunResult(0, "out", "err");

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void awkRunResultEqualityHandlesNullFields() {
        AwkRunResult first = new AwkRunResult(0, null, null);
        AwkRunResult second = new AwkRunResult(0, null, null);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void awkRunRequestHandlesNullVariables() {
        AwkRunRequest request = new AwkRunRequest();
        request.setVariables(null);

        assertThat(request.getVariables()).isEmpty();
    }

    @Test
    void awkRunRequestReplacesVariables() {
        AwkRunRequest request = new AwkRunRequest();
        request.setVariables(Map.of("a", "1"));
        request.setVariables(Map.of("b", "2"));

        assertThat(request.getVariables()).containsExactlyEntriesOf(Map.of("b", "2"));
    }

    @Test
    void awkRunRequestEqualityMatchesFields() {
        AwkProgram program = new AwkProgram(Path.of("main.awk"), "code", "hash");
        AwkRunRequest first = new AwkRunRequest(program, Path.of("input.txt"), Map.of("x", "1"));
        AwkRunRequest second = new AwkRunRequest(program, Path.of("input.txt"), Map.of("x", "1"));

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void awkRunRequestEqualityHandlesNullFields() {
        AwkRunRequest first = new AwkRunRequest(null, null, null);
        AwkRunRequest second = new AwkRunRequest(null, null, null);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void equalityHandlesSameInstanceAndDifferentType() {
        AwkRunResult result = new AwkRunResult();
        assertThat(result).isEqualTo(result);
        assertThat(result).isNotEqualTo("nope");

        AwkRunRequest request = new AwkRunRequest();
        assertThat(request).isNotEqualTo("nope");
    }
}
