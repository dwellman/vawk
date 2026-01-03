# Pattern Index (VAWK)

## Grounding
- `src/main/java/com/vawk/store/RagRepository.java:57` - `searchForChat(...)` (RAG lookup)
- `src/main/java/com/vawk/store/RagRepository.java:72` - `findRelevant(...)` (RAG lookup)
- `src/main/java/com/vawk/store/RagRepository.java:90` - `readContent(...)` (RAG lookup)
- `src/main/java/com/vawk/chat/VawkChatService.java:170` - `buildRagContext(...)` (injects docs into chat)
- `src/main/java/com/vawk/orchestration/VawkGeneratorService.java:173` - `buildRagContext(...)` (spec build from RAG context)
- `src/main/java/com/vawk/orchestration/VawkGeneratorService.java:95` - `generate(...)` (spec build from RAG context)
- `src/main/java/com/vawk/orchestration/VawkRefinementService.java:155` - `buildRagContext(...)` (change requests grounded with docs)

## Orchestration
- `src/main/java/com/vawk/orchestration/VawkGeneratorService.java:95` - `generate(...)` (spec -> plan -> code -> validate -> write -> optional tests)
- `src/main/java/com/vawk/orchestration/VawkRefinementService.java:84` - `refine(...)` (reuse spec/program, generate updated PLAN/CODE/TESTS/NOTES)
- `src/main/java/com/vawk/chat/VawkChatService.java:159` - `generateAssistantReply(...)` (build prompts + call ChatClient/stub)
- `src/main/java/com/vawk/chat/VawkChatPromptBuilder.java:47` - `buildMessages(...)` (ordering of system/dev/project/RAG/history)
- `src/main/java/com/vawk/cli/PromoteCommand.java:40` - `call()` (promote structured chat turn -> job artifacts)

## Verification
- `src/main/java/com/vawk/util/SectionValidator.java:19` - `requirePlanCodeTestsNotes(...)`
- `src/main/java/com/vawk/util/SectionValidator.java:44` - `requireAwkHeader(...)`
- `src/main/java/com/vawk/runtime/AwkTestRunner.java:46` - `runTests(...)` (PASS/FAIL + diff receipts)
- `src/main/java/com/vawk/cli/TestCommand.java:71` - `call()` (test runner wiring)
- `src/main/java/com/vawk/cli/PromoteCommand.java:40` - `call()` (rejects invalid PLAN/CODE/TESTS/NOTES)
- `src/main/java/com/vawk/orchestration/VawkLintService.java:59` - `lint(...)` (lint agent + logging)

## Trust UX
- `src/main/java/com/vawk/cli/ExplainCommand.java:53` - `call()` (explainability path)
- `src/main/java/com/vawk/ai/AiConfiguration.java:27` - `aiClient(...)` (fallback to deterministic stub)
- `src/main/java/com/vawk/ai/LocalStubAiClient.java:15` - `generate(...)` (deterministic fallback output)
- `src/main/java/com/vawk/store/SessionLogRepository.java:34` - `write(...)` (visible receipts in `.vawk/logs`)
- `src/main/java/com/vawk/chat/VawkChatService.java:69` - `createSession(...)` (audit trail in `.vawk/chat`)
- `src/main/java/com/vawk/chat/VawkChatService.java:140` - `appendUserTurn(...)` (audit trail in `.vawk/chat`)
- `src/main/java/com/vawk/chat/VawkChatService.java:149` - `appendAssistantTurn(...)` (audit trail in `.vawk/chat`)
- `src/main/java/com/vawk/store/AgentsFileRepository.java:28` - `read()` (project guidance / personalization)

## Learning
- `src/main/java/com/vawk/domain/SessionLog.java:27` - `testsRequested` (feedback loop)
- `src/main/java/com/vawk/domain/SessionLog.java:30` - `testsRun` (feedback loop)
- `src/main/java/com/vawk/orchestration/VawkGeneratorService.java:95` - `generate(...)` (capture tests and logs)
- `src/main/java/com/vawk/orchestration/VawkGeneratorService.java:159` - `runAutoTests(...)` (capture tests and logs)
- `src/main/java/com/vawk/orchestration/VawkRefinementService.java:84` - `refine(...)` (iterative improvement with prior spec/program)
- `src/main/java/com/vawk/store/SpecRepository.java:27` - `read(...)` (spec persistence)
- `src/main/java/com/vawk/store/SpecRepository.java:46` - `write(...)` (spec persistence)
- `src/main/java/com/vawk/store/ProgramRepository.java:25` - `read(...)` (program persistence)
- `src/main/java/com/vawk/store/ProgramRepository.java:48` - `write(...)` (program persistence)
- `src/main/java/com/vawk/chat/VawkChatService.java:97` - `loadSession(...)` (history for later reuse)
