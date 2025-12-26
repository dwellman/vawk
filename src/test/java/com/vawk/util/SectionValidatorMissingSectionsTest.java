package com.vawk.util;

import com.vawk.ai.AiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SectionValidatorMissingSectionsTest {

    @Test
    void throwsWhenResponseMissing() {
        assertThrows(IllegalStateException.class, () -> SectionValidator.requirePlanCodeTestsNotes(null));
    }

    @Test
    void throwsWhenPlanMissing() {
        AiResponse response = new AiResponse(null, "code", "tests", "notes");
        assertThrows(IllegalStateException.class, () -> SectionValidator.requirePlanCodeTestsNotes(response));
    }

    @Test
    void throwsWhenCodeMissing() {
        AiResponse response = new AiResponse("plan", null, "tests", "notes");
        assertThrows(IllegalStateException.class, () -> SectionValidator.requirePlanCodeTestsNotes(response));
    }

    @Test
    void throwsWhenTestsMissing() {
        AiResponse response = new AiResponse("plan", "# VAWK: demo\n# Purpose: demo\n# Intent: demo\n# Input: demo\n# Output: demo", null, "notes");
        assertThrows(IllegalStateException.class, () -> SectionValidator.requirePlanCodeTestsNotes(response));
    }

    @Test
    void throwsWhenNotesMissing() {
        AiResponse response = new AiResponse("plan", "# VAWK: demo\n# Purpose: demo\n# Intent: demo\n# Input: demo\n# Output: demo", "tests", null);
        assertThrows(IllegalStateException.class, () -> SectionValidator.requirePlanCodeTestsNotes(response));
    }

    @Test
    void throwsWhenAwkHeaderMissing() {
        AiResponse response = new AiResponse("plan", "print \"ok\"", "tests", "notes");
        assertThrows(IllegalStateException.class, () -> SectionValidator.requirePlanCodeTestsNotes(response));
    }

    @Test
    void acceptsCodeWithShebangAndFence() {
        String code = """
```awk
#!/usr/bin/awk -f

# VAWK: demo
# Purpose: demo
# Intent: demo
# Input: demo
# Output: demo
print "ok"
```
""";
        AiResponse response = new AiResponse("plan", code, "tests", "notes");

        SectionValidator.requirePlanCodeTestsNotes(response);
    }
}
