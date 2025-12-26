package com.vawk.store;

import com.vawk.domain.AwkTestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestRepositoryTest {

    @Test
    void loadReturnsEmptyWhenMissingDirectory(@TempDir Path tempDir) throws Exception {
        TestRepository repository = new TestRepository();

        List<AwkTestCase> cases = repository.load(tempDir.resolve("missing"));

        assertThat(cases).isEmpty();
    }

    @Test
    void loadFromYamlDefinitions(@TempDir Path tempDir) throws Exception {
        Path input = tempDir.resolve("input_sample.txt");
        Path expected = tempDir.resolve("expect_sample.txt");
        Files.writeString(input, "hello", StandardCharsets.UTF_8);
        Files.writeString(expected, "hello", StandardCharsets.UTF_8);
        Path yaml = tempDir.resolve("testcases.yaml");
        Files.writeString(yaml, "cases:\n  - name: sample\n    input: input_sample.txt\n    expected: expect_sample.txt\n", StandardCharsets.UTF_8);

        TestRepository repository = new TestRepository();
        List<AwkTestCase> cases = repository.load(tempDir);

        assertThat(cases).hasSize(1);
        assertThat(cases.get(0).getName()).isEqualTo("sample");
        assertThat(cases.get(0).getInputPath()).isEqualTo(input);
        assertThat(cases.get(0).getExpectedPath()).isEqualTo(expected);
    }

    @Test
    void scanDirectoryFindsInputExpectPairs(@TempDir Path tempDir) throws Exception {
        Path input = tempDir.resolve("input_alpha.log");
        Path expected = tempDir.resolve("expect_alpha.txt");
        Files.writeString(input, "alpha", StandardCharsets.UTF_8);
        Files.writeString(expected, "alpha", StandardCharsets.UTF_8);

        TestRepository repository = new TestRepository();
        List<AwkTestCase> cases = repository.load(tempDir);

        assertThat(cases).hasSize(1);
        assertThat(cases.get(0).getName()).isEqualTo("alpha");
    }

    @Test
    void loadSkipsInvalidYamlEntries(@TempDir Path tempDir) throws Exception {
        Path yaml = tempDir.resolve("testcases.yaml");
        Files.writeString(yaml, "cases:\n  - name: bad\n    input: missing.txt\n", StandardCharsets.UTF_8);

        TestRepository repository = new TestRepository();
        List<AwkTestCase> cases = repository.load(tempDir);

        assertThat(cases).isEmpty();
    }

    @Test
    void loadSkipsYamlWhenCasesNotList(@TempDir Path tempDir) throws Exception {
        Path yaml = tempDir.resolve("testcases.yaml");
        Files.writeString(yaml, "cases: invalid", StandardCharsets.UTF_8);

        TestRepository repository = new TestRepository();
        List<AwkTestCase> cases = repository.load(tempDir);

        assertThat(cases).isEmpty();
    }

    @Test
    void loadSkipsYamlEntriesThatAreNotMaps(@TempDir Path tempDir) throws Exception {
        Path yaml = tempDir.resolve("testcases.yaml");
        Files.writeString(yaml, "cases:\n  - not-a-map\n", StandardCharsets.UTF_8);

        TestRepository repository = new TestRepository();
        List<AwkTestCase> cases = repository.load(tempDir);

        assertThat(cases).isEmpty();
    }

    @Test
    void loadFromYmlSupportsVarsAndProgram(@TempDir Path tempDir) throws Exception {
        Path program = tempDir.resolve("script.awk");
        Path input = tempDir.resolve("input.txt");
        Path expected = tempDir.resolve("expect.txt");
        Files.writeString(program, "BEGIN { print \"ok\" }", StandardCharsets.UTF_8);
        Files.writeString(input, "x", StandardCharsets.UTF_8);
        Files.writeString(expected, "ok", StandardCharsets.UTF_8);
        Path yaml = tempDir.resolve("testcases.yml");
        Files.writeString(yaml, """
cases:
  - name: sample
    input: input.txt
    expected: expect.txt
    program: script.awk
    vars:
      by_user: "1"
""", StandardCharsets.UTF_8);

        TestRepository repository = new TestRepository();
        List<AwkTestCase> cases = repository.load(tempDir);

        assertThat(cases).hasSize(1);
        assertThat(cases.get(0).getVariables()).containsEntry("by_user", "1");
        assertThat(cases.get(0).getProgramOverride()).isEqualTo(program);
    }

    @Test
    void scanDirectorySkipsMissingExpected(@TempDir Path tempDir) throws Exception {
        Path input = tempDir.resolve("input_missing.log");
        Files.writeString(input, "alpha", StandardCharsets.UTF_8);

        TestRepository repository = new TestRepository();
        List<AwkTestCase> cases = repository.load(tempDir);

        assertThat(cases).isEmpty();
    }

    @Test
    void scanDirectoryFindsCsvExpected(@TempDir Path tempDir) throws Exception {
        Path input = tempDir.resolve("input_beta.log");
        Path expected = tempDir.resolve("expect_beta.csv");
        Files.writeString(input, "beta", StandardCharsets.UTF_8);
        Files.writeString(expected, "beta", StandardCharsets.UTF_8);

        TestRepository repository = new TestRepository();
        List<AwkTestCase> cases = repository.load(tempDir);

        assertThat(cases).hasSize(1);
        assertThat(cases.get(0).getExpectedPath()).isEqualTo(expected);
    }

    @Test
    void scanDirectoryHandlesInputWithoutExtension(@TempDir Path tempDir) throws Exception {
        Path input = tempDir.resolve("input_gamma");
        Path expected = tempDir.resolve("expect_gamma.txt");
        Files.writeString(input, "gamma", StandardCharsets.UTF_8);
        Files.writeString(expected, "gamma", StandardCharsets.UTF_8);

        TestRepository repository = new TestRepository();
        List<AwkTestCase> cases = repository.load(tempDir);

        assertThat(cases).hasSize(1);
        assertThat(cases.get(0).getName()).isEqualTo("gamma");
    }

    @Test
    void scanDirectorySkipsNonMatchingInputs(@TempDir Path tempDir) throws Exception {
        Path input = tempDir.resolve("sample.log");
        Files.writeString(input, "alpha", StandardCharsets.UTF_8);

        TestRepository repository = new TestRepository();
        List<AwkTestCase> cases = repository.load(tempDir);

        assertThat(cases).isEmpty();
    }
}
