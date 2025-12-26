package com.vawk.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RagRepositoryTest {

    @Test
    void findRelevantReturnsEmptyForNullQuery() throws Exception {
        RagRepository repository = new RagRepository();

        List<RagRepository.RagEntry> entries = repository.findRelevant(null, 3);

        assertThat(entries).isEmpty();
    }

    @Test
    void listAllEntriesLoadsIndexes() throws Exception {
        RagRepository repository = new RagRepository();

        List<RagRepository.RagEntry> entries = repository.listAllEntries();

        assertThat(entries).isNotEmpty();
    }

    @Test
    void readContentRejectsPathEscape() {
        RagRepository repository = new RagRepository();
        RagRepository.RagEntry entry = new RagRepository.RagEntry("bad", "bad", "../secret.txt", "snippets");

        assertThrows(IOException.class, () -> repository.readContent(entry));
    }

    @Test
    void findRelevantRespectsLimitAndScoresMatches() throws Exception {
        RagRepository repository = new RagRepository() {
            @Override
            public List<RagEntry> listAllEntries() {
                return new java.util.ArrayList<>(List.of(
                        new RagEntry("alpha", "first entry", "snippets/a.md", "snippets"),
                        new RagEntry("beta", "second entry", "snippets/b.md", "snippets")
                ));
            }
        };

        List<RagRepository.RagEntry> limited = repository.findRelevant("alpha", 1);

        assertThat(limited).hasSize(1);
        assertThat(limited.get(0).id).isEqualTo("alpha");
    }

    @Test
    void parseIndexSkipsInvalidPaths(@TempDir java.nio.file.Path tempDir) throws Exception {
        java.nio.file.Path index = tempDir.resolve("index.md");
        java.nio.file.Files.writeString(index, """
- `ok` – description (snippets/ok.md)
- `bad` – description (../escape.md)
- `abs` – description (/abs.md)
""");

        RagRepository repository = new RagRepository();
        java.lang.reflect.Method method = RagRepository.class.getDeclaredMethod("parseIndex", java.nio.file.Path.class, String.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<RagRepository.RagEntry> entries = (List<RagRepository.RagEntry>) method.invoke(repository, index, "snippets");

        assertThat(entries).extracting(e -> e.id).containsExactly("ok");
    }

    @Test
    void readContentLoadsSnippetAndBookPaths() throws Exception {
        RagRepository repository = new RagRepository();
        RagRepository.RagEntry snippet = new RagRepository.RagEntry("print-columns-basic", "desc", "snippets/print-columns-basic.md", "snippets");

        assertThat(repository.readContent(snippet)).isNotBlank();

        java.nio.file.Path bookDir = java.nio.file.Paths.get("docs", "book", "learn.awk");
        java.nio.file.Files.createDirectories(bookDir);
        java.nio.file.Path bookFile = bookDir.resolve("chapter.md");
        java.nio.file.Files.writeString(bookFile, "book ok");
        try {
            RagRepository.RagEntry book = new RagRepository.RagEntry("chapter", "desc", "learn.awk/chapter.md", "book");
            assertThat(repository.readContent(book)).isEqualTo("book ok");
        } finally {
            java.nio.file.Files.deleteIfExists(bookFile);
        }
    }
}
