package com.vawk.util;

import com.vawk.ai.AiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SectionParserTest {

    @Test
    void parseReturnsEmptySectionsForNull() {
        AiResponse response = SectionParser.parse(null);

        assertThat(response.getPlan()).isNull();
        assertThat(response.getCode()).isNull();
        assertThat(response.getTests()).isNull();
        assertThat(response.getNotes()).isNull();
    }

    @Test
    void parseExtractsSectionsAndStripsFences() {
        String raw = """
PLAN:
- step

CODE:
```awk
# VAWK: demo
print \"ok\"
```

TESTS:
- test

NOTES:
- note
""";

        AiResponse response = SectionParser.parse(raw);

        assertThat(response.getPlan()).contains("step");
        assertThat(response.getCode()).contains("# VAWK: demo");
        assertThat(response.getCode()).doesNotContain("```");
        assertThat(response.getTests()).contains("test");
        assertThat(response.getNotes()).contains("note");
    }

    @Test
    void parseTestsListNormalizesBullets() {
        String tests = """
- first
second

- third
""";

        List<String> list = SectionParser.parseTestsList(tests);

        assertThat(list).containsExactly("first", "second", "third");
    }

    @Test
    void stripCodeFencesReturnsTrimmedWhenNoFence() {
        String code = "  print \"ok\"  ";

        assertThat(SectionParser.stripCodeFences(code)).isEqualTo("print \"ok\"");
    }
}
