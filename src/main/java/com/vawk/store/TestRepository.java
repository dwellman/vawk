package com.vawk.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vawk.domain.AwkTestCase;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Loads AWK test cases from the tests directory. Supports YAML-based testcases files or directory
 * scanning of input_/expect_ fixtures.
 */
@Repository
public class TestRepository {
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final Pattern INPUT_PATTERN = Pattern.compile("^input_(.+)$");

    /**
     * Loads test cases from the default tests directory.
     */
    public List<AwkTestCase> load() throws IOException {
        return load(Paths.get("tests"));
    }

    /**
     * Loads test cases from a specific directory, preferring YAML definitions when present and
     * otherwise scanning for input_/expect_ fixture pairs.
     *
     * @param testsDir directory containing test fixtures
     * @return list of parsed test cases
     */
    public List<AwkTestCase> load(Path testsDir) throws IOException {
        if (!Files.exists(testsDir)) {
            return Collections.emptyList();
        }

        Optional<Path> testcasesFile = findTestcasesFile(testsDir);
        if (testcasesFile.isPresent()) {
            return readFromYaml(testsDir, testcasesFile.get());
        }
        return scanDirectory(testsDir);
    }

    private Optional<Path> findTestcasesFile(Path testsDir) {
        Path yaml = testsDir.resolve("testcases.yaml");
        if (Files.exists(yaml)) {
            return Optional.of(yaml);
        }
        Path yml = testsDir.resolve("testcases.yml");
        if (Files.exists(yml)) {
            return Optional.of(yml);
        }
        return Optional.empty();
    }

    private List<AwkTestCase> readFromYaml(Path testsDir, Path yamlFile) throws IOException {
        Map<?, ?> root = yamlMapper.readValue(yamlFile.toFile(), Map.class);
        Object casesNode = root.get("cases");
        if (!(casesNode instanceof List<?> list)) {
            return Collections.emptyList();
        }

        List<AwkTestCase> cases = new ArrayList<>();
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?> map)) {
                continue;
            }
            String name = Objects.toString(map.get("name"), null);
            String input = Objects.toString(map.get("input"), null);
            String expected = Objects.toString(map.get("expected"), null);
            String programPath = map.get("program") != null ? map.get("program").toString() : null;
            Map<String, String> vars = new HashMap<>();
            Object varsNode = map.get("vars");
            if (varsNode instanceof Map<?, ?> varMap) {
                for (Map.Entry<?, ?> entry : varMap.entrySet()) {
                    vars.put(entry.getKey().toString(), entry.getValue().toString());
                }
            }

            if (name == null || input == null || expected == null) {
                continue;
            }
            Path inputPath = testsDir.resolve(input);
            Path expectedPath = testsDir.resolve(expected);
            Path programOverride = programPath != null ? testsDir.resolve(programPath) : null;
            cases.add(new AwkTestCase(name, inputPath, expectedPath, vars, programOverride));
        }
        return cases;
    }

    private List<AwkTestCase> scanDirectory(Path testsDir) throws IOException {
        List<Path> inputs = Files.list(testsDir)
                .filter(p -> p.getFileName().toString().startsWith("input_"))
                .collect(Collectors.toList());
        List<AwkTestCase> cases = new ArrayList<>();
        for (Path input : inputs) {
            String baseName = extractBaseName(input.getFileName().toString());
            if (baseName == null) {
                continue;
            }
            Path expectedTxt = testsDir.resolve("expect_" + baseName + ".txt");
            Path expectedCsv = testsDir.resolve("expect_" + baseName + ".csv");
            Path expected = Files.exists(expectedTxt) ? expectedTxt : expectedCsv;
            if (expected == null || !Files.exists(expected)) {
                continue;
            }
            cases.add(new AwkTestCase(baseName, input, expected, Collections.emptyMap(), null));
        }
        return cases;
    }

    private String extractBaseName(String fileName) {
        Matcher matcher = INPUT_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            String remainder = matcher.group(1);
            int dot = remainder.lastIndexOf('.');
            if (dot > -1) {
                return remainder.substring(0, dot);
            }
            return remainder;
        }
        return null;
    }
}
