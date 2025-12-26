package com.vawk.util;

import com.vawk.ai.AiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SectionValidatorAwkHeaderTest {

    @Test
    void acceptsCodeWithHeaderBlock() {
        String code = """
```awk
# VAWK: sample_script
# Purpose: Demonstrate header validation.
# Intent: Ensure generated scripts stay self-documenting.
# Input: Sample input description.
# Output: Sample output description.
BEGIN { print "ok" }
```
""";
        AiResponse response = new AiResponse("plan", code, "tests", "notes");
        assertDoesNotThrow(() -> SectionValidator.requirePlanCodeTestsNotes(response));
    }

    @Test
    void rejectsCodeMissingHeaderFields() {
        String code = """
```awk
# Purpose: Missing VAWK line and intent.
BEGIN { print "bad" }
```
""";
        AiResponse response = new AiResponse("plan", code, "tests", "notes");
        assertThrows(IllegalStateException.class, () -> SectionValidator.requirePlanCodeTestsNotes(response));
    }
}
