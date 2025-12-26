package com.vawk.store;

import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides guarded access to RAG documents (snippets and learn.awk sections). Parses indexes,
 * scores relevance heuristically, and returns document content for chat context without allowing
 * path escapes outside docs.
 */
@Repository
public class RagRepository {
    private static final Path ROOT_INDEX = Paths.get("docs", "rag-index.md");
    private static final Pattern INDEX_ENTRY = Pattern.compile("-\\s+`([^`]+)`\\s+\\u2013\\s+(.*)\\(([^)]+)\\)");

    public static class RagDocument {
        public final String id;
        public final String title;
        public final String content;

        public RagDocument(String id, String title, String content) {
            this.id = id;
            this.title = title;
            this.content = content;
        }
    }

    /**
     * Lists all RAG entries from snippet and book indexes.
     */
    public List<RagEntry> listAllEntries() throws IOException {
        Path snippetsIndex = Paths.get("docs", "examples", "awk-snippets-index.md");
        Path bookIndex = Paths.get("docs", "book", "learn.awk-index.md");
        List<RagEntry> entries = new ArrayList<>();
        entries.addAll(parseIndex(snippetsIndex, "snippets"));
        entries.addAll(parseIndex(bookIndex, "book"));
        return entries;
    }

    /**
     * Finds the top documents for a chat query and returns full content.
     */
    // Pattern: Grounding
    // - Retrieves relevant RAG docs so answers are constrained by project references.
    public List<RagDocument> searchForChat(String query, int limit) throws IOException {
        List<RagEntry> top = findRelevant(query, limit);
        List<RagDocument> docs = new ArrayList<>();
        for (RagEntry entry : top) {
            String content = readContent(entry);
            docs.add(new RagDocument(entry.id, entry.description, content));
        }
        return docs;
    }

    /**
     * Heuristically scores entries against query tokens and returns the top matches.
     */
    // Pattern: Grounding
    // - Scores and selects references to bound the model's context to known docs.
    public List<RagEntry> findRelevant(String query, int limit) throws IOException {
        if (query == null) {
            return Collections.emptyList();
        }
        String[] tokens = query.toLowerCase(Locale.ROOT).split("\\W+");
        List<RagEntry> entries = listAllEntries();
        entries.sort(Comparator.comparingInt((RagEntry e) -> -score(e, tokens)));
        if (entries.size() > limit) {
            entries = entries.subList(0, limit);
        }
        return entries;
    }

    /**
     * Reads content for a given entry, enforcing that the path stays under docs.
     */
    // Pattern: Grounding + Verification
    // - Loads only allowed doc paths to prevent context escapes or prompt injection via filesystem.
    public String readContent(RagEntry entry) throws IOException {
        Path base = Paths.get("docs");
        if (entry.path.startsWith("snippets/")) {
            base = base.resolve("examples");
        } else if (entry.path.startsWith("learn.awk/")) {
            base = base.resolve("book");
        }
        Path target = base.resolve(entry.path).normalize();
        if (!target.startsWith(base.normalize())) {
            throw new IOException("RAG path escapes docs: " + entry.path);
        }
        return Files.readString(target, StandardCharsets.UTF_8);
    }

    private List<RagEntry> parseIndex(Path indexPath, String group) throws IOException {
        if (!Files.exists(indexPath)) {
            return Collections.emptyList();
        }
        List<RagEntry> entries = new ArrayList<>();
        List<String> lines = Files.readAllLines(indexPath, StandardCharsets.UTF_8);
        for (String line : lines) {
            Matcher m = INDEX_ENTRY.matcher(line);
            if (m.find()) {
                String id = m.group(1).trim();
                String desc = m.group(2).trim();
                String relPath = m.group(3).trim();
                if (relPath.contains("..") || relPath.startsWith("/")) {
                    continue;
                }
                entries.add(new RagEntry(id, desc, relPath, group));
            }
        }
        return entries;
    }

    private int score(RagEntry entry, String[] tokens) {
        int score = 0;
        String hay = (entry.id + " " + entry.description).toLowerCase(Locale.ROOT);
        for (String t : tokens) {
            if (t.isEmpty()) continue;
            if (hay.contains(t)) {
                score += 1;
            }
        }
        return score;
    }

    public static class RagEntry {
        public final String id;
        public final String description;
        public final String path;
        public final String group;

        public RagEntry(String id, String description, String path, String group) {
            this.id = id;
            this.description = description;
            this.path = path;
            this.group = group;
        }
    }
}
