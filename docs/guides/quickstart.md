# VAWK Quickstart

**BLUF:** Build the CLI, run chat in stub mode (no key), and optionally wire a real chat model. Includes the wrapper script so `vawk` is easy to invoke from any directory.

## 1) Prerequisites
- Java 17+
- Maven
- POSIX awk available (`/usr/bin/awk` or equivalent)

## 2) Build
```sh
mvn -q package spring-boot:repackage
```
Output: `target/vawk-0.2.0-SNAPSHOT.jar`

## 3) Run with the built-in stub (no API key)
```sh
SPRING_MAIN_WEB_APPLICATION_TYPE=none \
java -jar target/vawk-0.2.0-SNAPSHOT.jar chat --one-shot "Explain what FS does in awk"
```
- Uses the deterministic local stub (no network, no key).
- Creates `.vawk/chat/<session>.vawk` with NDJSON meta/turns.

## 4) Run with a real chat model (OpenAI/compatible)
- Ensure a `ChatModel` bean is on the classpath (e.g., Spring AI OpenAI starter).
- Enable real chat and provide your key, e.g.:
```sh
SPRING_MAIN_WEB_APPLICATION_TYPE=none \
vawk.ai.use-chat=true \
SPRING_AI_OPENAI_API_KEY=<your_key> \
java -jar target/vawk-0.2.0-SNAPSHOT.jar chat --one-shot "Write awk script to sum column 3"
```
- Behavior: layered prompts + RAG, PLAN/CODE/TESTS/NOTES validation with auto-fix, NDJSON logging as before.

## 5) Make it easy to call from anywhere
Add the wrapper script to your PATH:
```sh
# ~/.zshrc or ~/.bashrc
export PATH="/path/to/vawk/bin:$PATH"
```
Reload your shell (or `source ~/.zshrc`), then run:
```sh
vawk chat --one-shot "Explain what FS does in awk"
```

## 6) Promote and run a generated script
1) Generate code: `vawk chat --one-shot "Write awk script to sum column 3"`  
2) Promote: `vawk promote --session <sessionId> --turn <idx> --name sum_col3`  
3) Run: `awk -f vawk/jobs/sum_col3/script.awk input.txt > output.txt`

## 7) Tests
```sh
mvn -q test
```
Includes chat intent/validation, promotion, RAG prompting, and AWK runners.
