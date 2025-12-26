package com.vawk.cli;

import com.vawk.chat.VawkChatService;
import com.vawk.chat.VawkChatSession;
import com.vawk.runtime.DirectoryService;
import com.vawk.chat.VawkChatPromptBuilder;
import com.vawk.chat.VawkPromptLoader;
import com.vawk.store.RagRepository;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ChatCommandTest {

    @Test
    void oneShotExplainUsesPlainFlow() throws Exception {
        StubChatService chatService = new StubChatService(new ArrayDeque<>(List.of("plain reply")));
        ChatCommand command = new ChatCommand(chatService, new StubDirectoryService());
        command.oneShot = "Explain this";

        int exit = withCapturedOut(command::call).exitCode;

        assertThat(exit).isZero();
        assertThat(chatService.generatedReplies).isEqualTo(1);
    }

    @Test
    void oneShotAutoFixSucceeds() throws Exception {
        Deque<String> replies = new ArrayDeque<>();
        replies.add("not structured");
        replies.add("PLAN:\nplan\nCODE:\n# VAWK: demo\n# Purpose: demo\n# Intent: demo\n# Input: demo\n# Output: demo\nprint \"ok\"\nTESTS:\n- t\nNOTES:\n- n");
        StubChatService chatService = new StubChatService(replies);
        ChatCommand command = new ChatCommand(chatService, new StubDirectoryService());
        command.sessionId = "existing";
        command.oneShot = "Write awk";

        int exit = withCapturedOut(command::call).exitCode;

        assertThat(exit).isZero();
        assertThat(chatService.generatedReplies).isEqualTo(2);
    }

    @Test
    void oneShotAutoFixFails() throws Exception {
        Deque<String> replies = new ArrayDeque<>();
        replies.add("not structured");
        replies.add("still bad");
        StubChatService chatService = new StubChatService(replies);
        ChatCommand command = new ChatCommand(chatService, new StubDirectoryService());
        command.oneShot = "Write awk";

        int exit = withCapturedOut(command::call).exitCode;

        assertThat(exit).isEqualTo(1);
    }

    @Test
    void oneShotStructuredReplySkipsCorrection() throws Exception {
        Deque<String> replies = new ArrayDeque<>();
        replies.add("PLAN:\nplan\nCODE:\n# VAWK: demo\n# Purpose: demo\n# Intent: demo\n# Input: demo\n# Output: demo\nprint \"ok\"\nTESTS:\n- t\nNOTES:\n- n");
        StubChatService chatService = new StubChatService(replies);
        ChatCommand command = new ChatCommand(chatService, new StubDirectoryService());
        command.oneShot = "Write awk";

        int exit = withCapturedOut(command::call).exitCode;

        assertThat(exit).isZero();
        assertThat(chatService.generatedReplies).isEqualTo(1);
    }

    @Test
    void oneShotAutoFixReportsHeaderMissing() throws Exception {
        Deque<String> replies = new ArrayDeque<>();
        replies.add("not structured");
        replies.add("PLAN:\nplan\nCODE:\nprint \"ok\"\nTESTS:\n- t\nNOTES:\n- n");
        StubChatService chatService = new StubChatService(replies);
        ChatCommand command = new ChatCommand(chatService, new StubDirectoryService());
        command.oneShot = "Write awk";

        CaptureResult result = withCapturedOut(command::call);

        assertThat(result.exitCode()).isEqualTo(1);
        assertThat(result.output()).contains("AWK header");
    }

    @Test
    void oneShotAutoFixReportsMissingSections() throws Exception {
        Deque<String> replies = new ArrayDeque<>();
        replies.add("not structured");
        replies.add("PLAN:\nplan\nCODE:\n# VAWK: demo\n# Purpose: demo\n# Intent: demo\n# Input: demo\n# Output: demo\nprint \"ok\"\nNOTES:\n- n");
        StubChatService chatService = new StubChatService(replies);
        ChatCommand command = new ChatCommand(chatService, new StubDirectoryService());
        command.oneShot = "Write awk";

        CaptureResult result = withCapturedOut(command::call);

        assertThat(result.exitCode()).isEqualTo(1);
        assertThat(result.output()).contains("Missing PLAN/CODE/TESTS/NOTES");
    }

    @Test
    void interactiveCodeTurnPrintsStructuredReply() throws Exception {
        Deque<String> replies = new ArrayDeque<>();
        replies.add("PLAN:\nplan\nCODE:\n# VAWK: demo\n# Purpose: demo\n# Intent: demo\n# Input: demo\n# Output: demo\nprint \"ok\"\nTESTS:\n- t\nNOTES:\n- n");
        StubChatService chatService = new StubChatService(replies);
        ChatCommand command = new ChatCommand(chatService, new StubDirectoryService());

        CaptureResult result = withCapturedOut(() -> withInput("write awk\n:q\n", command::call));

        assertThat(result.exitCode()).isZero();
        assertThat(result.output()).contains("PLAN:");
    }

    @Test
    void interactiveCodeTurnReportsFailure() throws Exception {
        Deque<String> replies = new ArrayDeque<>();
        replies.add("bad reply");
        replies.add("still bad");
        StubChatService chatService = new StubChatService(replies);
        ChatCommand command = new ChatCommand(chatService, new StubDirectoryService());

        CaptureResult result = withCapturedOut(() -> withInput("write awk\n:q\n", command::call));

        assertThat(result.exitCode()).isZero();
        assertThat(result.output()).contains("Rephrase and ask again");
    }

    @Test
    void interactiveExplainTurnUsesPlainFlow() throws Exception {
        Deque<String> replies = new ArrayDeque<>();
        replies.add("plain reply");
        StubChatService chatService = new StubChatService(replies);
        ChatCommand command = new ChatCommand(chatService, new StubDirectoryService());

        CaptureResult result = withCapturedOut(() -> withInput("explain this\n:q\n", command::call));

        assertThat(result.exitCode()).isZero();
        assertThat(chatService.generatedReplies).isEqualTo(1);
    }

    @Test
    void interactiveCodeTurnAutoFixSucceeds() throws Exception {
        Deque<String> replies = new ArrayDeque<>();
        replies.add("not structured");
        replies.add("PLAN:\nplan\nCODE:\n# VAWK: demo\n# Purpose: demo\n# Intent: demo\n# Input: demo\n# Output: demo\nprint \"ok\"\nTESTS:\n- t\nNOTES:\n- n");
        StubChatService chatService = new StubChatService(replies);
        ChatCommand command = new ChatCommand(chatService, new StubDirectoryService());

        CaptureResult result = withCapturedOut(() -> withInput("write awk\n:q\n", command::call));

        assertThat(result.exitCode()).isZero();
        assertThat(result.output()).contains("PLAN:");
    }

    @Test
    void interactiveSkipsBlankLines() throws Exception {
        Deque<String> replies = new ArrayDeque<>();
        StubChatService chatService = new StubChatService(replies);
        ChatCommand command = new ChatCommand(chatService, new StubDirectoryService());

        CaptureResult result = withCapturedOut(() -> withInput("\n:q\n", command::call));

        assertThat(result.exitCode()).isZero();
        assertThat(chatService.generatedReplies).isEqualTo(0);
    }

    @Test
    void interactiveExitsOnEof() throws Exception {
        Deque<String> replies = new ArrayDeque<>();
        StubChatService chatService = new StubChatService(replies);
        ChatCommand command = new ChatCommand(chatService, new StubDirectoryService());

        CaptureResult result = withCapturedOut(() -> withInput("", command::call));

        assertThat(result.exitCode()).isZero();
        assertThat(chatService.generatedReplies).isEqualTo(0);
    }

    @Test
    void interactiveMixedIntentUsesStructuredPath() throws Exception {
        Deque<String> replies = new ArrayDeque<>();
        replies.add("PLAN:\nplan\nCODE:\n# VAWK: demo\n# Purpose: demo\n# Intent: demo\n# Input: demo\n# Output: demo\nprint \"ok\"\nTESTS:\n- t\nNOTES:\n- n");
        StubChatService chatService = new StubChatService(replies);
        ChatCommand command = new ChatCommand(chatService, new StubDirectoryService());

        CaptureResult result = withCapturedOut(() -> withInput("hello\n:q\n", command::call));

        assertThat(result.exitCode()).isZero();
        assertThat(result.output()).contains("PLAN:");
    }

    @Test
    void handleTurnUsesChatClientLabel() throws Exception {
        Deque<String> replies = new ArrayDeque<>();
        replies.add("plain reply");
        final String[] modelCapture = new String[1];
        StubChatService chatService = new StubChatService(replies) {
            @Override
            public boolean hasChatClient() {
                return true;
            }

            @Override
            public String appendAssistantTurn(String sessionId, int idx, String message, String model) {
                modelCapture[0] = model;
                return "ok";
            }
        };
        ChatCommand command = new ChatCommand(chatService, new StubDirectoryService());
        command.sessionId = "existing";

        CaptureResult result = withCapturedOut(() -> withInput("explain this\n:q\n", command::call));

        assertThat(result.exitCode()).isZero();
        assertThat(chatService.generatedReplies).isEqualTo(1);
        assertThat(modelCapture[0]).isEqualTo("chat-client");
    }

    private static CaptureResult withCapturedOut(CallableWithExit callable) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            return new CaptureResult(callable.call(), out.toString());
        } finally {
            System.setOut(original);
        }
    }

    private static int withInput(String input, CallableWithExit callable) throws Exception {
        java.io.InputStream originalIn = System.in;
        System.setIn(new java.io.ByteArrayInputStream(input.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        try {
            return callable.call();
        } finally {
            System.setIn(originalIn);
        }
    }

    private record CaptureResult(int exitCode, String output) {
    }

    @FunctionalInterface
    private interface CallableWithExit {
        int call() throws Exception;
    }

    private static final class StubDirectoryService extends DirectoryService {
        @Override
        public void ensureBaseDirs() {
        }
    }

    private static class StubChatService extends VawkChatService {
        private final Deque<String> replies;
        private final VawkChatSession session;
        private int generatedReplies;

        private StubChatService(Deque<String> replies) {
            super(new DirectoryService(), Optional.empty(), new VawkChatPromptBuilder(new VawkPromptLoader()), new RagRepository());
            this.replies = replies;
            this.session = new VawkChatSession("session", Instant.EPOCH, Path.of("."), "title");
        }

        @Override
        public VawkChatSession createSession(String title) {
            return session;
        }

        @Override
        public SessionData loadSession(String sessionId) {
            return new SessionData(session, List.of(), 1);
        }

        @Override
        public String appendUserTurn(String sessionId, int idx, String message) {
            return "ok";
        }

        @Override
        public String generateAssistantReply(List<Turn> history, String message) {
            generatedReplies++;
            return replies.removeFirst();
        }

        @Override
        public String appendAssistantTurn(String sessionId, int idx, String message, String model) {
            return "ok";
        }

        @Override
        public boolean hasChatClient() {
            return false;
        }
    }
}
