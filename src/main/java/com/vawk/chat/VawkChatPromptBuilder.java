package com.vawk.chat;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembles the full message list for a chat call, layering system/developer/project prompts,
 * optional RAG context, prior turns, and the new user message. This is the only place that decides
 * message ordering for ChatClient calls.
 */
@Component
public class VawkChatPromptBuilder {
    private final VawkPromptLoader promptLoader;

    public VawkChatPromptBuilder(VawkPromptLoader promptLoader) {
        this.promptLoader = promptLoader;
    }

    /**
     * Builds messages without RAG context for compatibility callers.
     *
     * @param newUserMessage latest user message
     * @param history        trimmed session history
     * @return ordered messages for ChatClient
     */
    public List<Message> buildMessages(String newUserMessage, List<VawkChatService.Turn> history) {
        return buildMessages(newUserMessage, history, null);
    }

    /**
     * Builds the layered message list including prompts, optional RAG context, history, and the new
     * user message.
     *
     * @param newUserMessage latest user input
     * @param history        prior turns
     * @param ragContext     optional RAG context injected as a system message
     * @return ordered messages ready for ChatClient
     */
    // Pattern: Orchestration
    // - Defines the exact message ordering to keep system/dev/project guidance deterministic.
    public List<Message> buildMessages(String newUserMessage, List<VawkChatService.Turn> history, String ragContext) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(promptLoader.loadSystemPrompt()));
        String dev = promptLoader.loadDeveloperPrompt();
        if (!dev.isBlank()) {
            messages.add(new SystemMessage(dev));
        }
        String project = promptLoader.loadProjectPrompt();
        if (!project.isBlank()) {
            messages.add(new SystemMessage(project));
        }
        if (ragContext != null && !ragContext.isBlank()) {
            messages.add(new SystemMessage(ragContext));
        }
        if (history != null) {
            for (VawkChatService.Turn turn : history) {
                if ("user".equals(turn.role())) {
                    messages.add(new UserMessage(turn.msg()));
                } else if ("assistant".equals(turn.role())) {
                    messages.add(new AssistantMessage(turn.msg()));
                }
            }
        }
        messages.add(new UserMessage(newUserMessage));
        return messages;
    }
}
