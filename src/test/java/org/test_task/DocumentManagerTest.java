package org.test_task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentManagerTest {

    private DocumentManager documentManager;

    @BeforeEach
    void setUp() {
        documentManager = new DocumentManager();
    }

    @Test
    void testSaveNewDocument() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Document Name")
                .content("Content text")
                .author(DocumentManager.Author.builder().id("1").name("Author Name").build())
                .created(Instant.now())
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);

        assertThat(savedDocument.getId()).isNotNull();
        assertThat(documentManager.findById(savedDocument.getId())).contains(savedDocument);
    }

    @Test
    void testUpdateExistingDocument() {
        String documentId = UUID.randomUUID().toString();
        DocumentManager.Document document = DocumentManager.Document.builder()
                .id(documentId)
                .title("Original Title")
                .content("Original content.")
                .author(DocumentManager.Author.builder().id("1").name("Author Name").build())
                .created(Instant.now())
                .build();

        documentManager.save(document);

        DocumentManager.Document updatedDocument = DocumentManager.Document.builder()
                .id(documentId)
                .title("Updated Title")
                .content("Updated content.")
                .author(document.getAuthor())
                .created(document.getCreated())
                .build();

        documentManager.save(updatedDocument);

        assertThat(documentManager.findById(documentId)).contains(updatedDocument);
        assertThat(documentManager.findById(documentId).get().getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void testSearchByTitlePrefix() {
        DocumentManager.Document document1 = DocumentManager.Document.builder()
                .title("Java Programming")
                .content("Content about Java.")
                .author(DocumentManager.Author.builder().id("1").name("Author One").build())
                .created(Instant.now())
                .build();

        DocumentManager.Document document2 = DocumentManager.Document.builder()
                .title("Python Programming")
                .content("Content about Python.")
                .author(DocumentManager.Author.builder().id("2").name("Author Two").build())
                .created(Instant.now())
                .build();

        documentManager.save(document1);
        documentManager.save(document2);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Java"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Java Programming");
    }

    @Test
    void testSearchByContent() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Random Title")
                .content("Important content about something.")
                .author(DocumentManager.Author.builder().id("3").name("Author Three").build())
                .created(Instant.now())
                .build();

        documentManager.save(document);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .containsContents(List.of("Important"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getContent()).contains("Important");
    }

    @Test
    void testSearchByAuthorIds() {
        DocumentManager.Document document1 = DocumentManager.Document.builder()
                .title("Doc 1")
                .content("Content 1")
                .author(DocumentManager.Author.builder().id("author1").name("Author One").build())
                .created(Instant.now())
                .build();

        DocumentManager.Document document2 = DocumentManager.Document.builder()
                .title("Doc 2")
                .content("Content 2")
                .author(DocumentManager.Author.builder().id("author2").name("Author Two").build())
                .created(Instant.now())
                .build();

        documentManager.save(document1);
        documentManager.save(document2);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .authorIds(List.of("author1"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAuthor().getId()).isEqualTo("author1");
    }

    @Test
    void testSearchByCreatedFrom() {
        Instant now = Instant.now();
        Instant past = now.minus(5, ChronoUnit.DAYS);

        DocumentManager.Document document1 = DocumentManager.Document.builder()
                .title("Old Doc")
                .content("Old content")
                .author(DocumentManager.Author.builder().id("author1").name("Author One").build())
                .created(past)
                .build();

        DocumentManager.Document document2 = DocumentManager.Document.builder()
                .title("New Doc")
                .content("New content")
                .author(DocumentManager.Author.builder().id("author2").name("Author Two").build())
                .created(now)
                .build();

        documentManager.save(document1);
        documentManager.save(document2);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .createdFrom(now.minus(1, ChronoUnit.DAYS))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("New Doc");
    }

    @Test
    void testSearchByCreatedTo() {
        Instant now = Instant.now();
        Instant past = now.minus(5, ChronoUnit.DAYS);

        DocumentManager.Document document1 = DocumentManager.Document.builder()
                .title("Old Doc")
                .content("Old content")
                .author(DocumentManager.Author.builder().id("author1").name("Author One").build())
                .created(past)
                .build();

        DocumentManager.Document document2 = DocumentManager.Document.builder()
                .title("New Doc")
                .content("New content")
                .author(DocumentManager.Author.builder().id("author2").name("Author Two").build())
                .created(now)
                .build();

        documentManager.save(document1);
        documentManager.save(document2);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .createdTo(now.minus(1, ChronoUnit.DAYS))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Old Doc");
    }

    @Test
    void testFindById() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Unique Document")
                .content("Unique content.")
                .author(DocumentManager.Author.builder().id("4").name("Author Four").build())
                .created(Instant.now())
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);
        Optional<DocumentManager.Document> foundDocument = documentManager.findById(savedDocument.getId());

        assertThat(foundDocument).isPresent();
        assertThat(foundDocument.get()).isEqualTo(savedDocument);
    }

    @Test
    void testFindByIdNotFound() {
        Optional<DocumentManager.Document> foundDocument = documentManager.findById("non-existent-id");

        assertThat(foundDocument).isEmpty();
    }
}
