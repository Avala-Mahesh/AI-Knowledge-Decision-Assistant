package com.aiassistant;

import com.aiassistant.entity.Document;
import com.aiassistant.entity.DocumentChunk;
import com.aiassistant.entity.Organization;
import com.aiassistant.exception.DocumentLimitExceededException;
import com.aiassistant.exception.InvalidDocumentException;
import com.aiassistant.repository.DocumentChunkRepository;
import com.aiassistant.repository.DocumentRepository;
import com.aiassistant.repository.OrganizationRepository;
import com.aiassistant.service.DocumentService;
import com.aiassistant.service.PdfExtractionService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentValidationTest {

    private StubOrganizationRepository orgRepo;
    private StubDocumentRepository docRepo;
    private StubDocumentChunkRepository chunkRepo;
    private PdfExtractionService pdfExtractionService;
    private DocumentService documentService;
    private Organization testOrg;

    @BeforeEach
    public void setup() {
        orgRepo = new StubOrganizationRepository();
        docRepo = new StubDocumentRepository();
        chunkRepo = new StubDocumentChunkRepository();
        pdfExtractionService = new PdfExtractionService();
        documentService = new DocumentService(docRepo, chunkRepo, orgRepo, pdfExtractionService);

        testOrg = new Organization("Test Validation Org", "test-val-org");
        testOrg.setId(1L);
        orgRepo.save(testOrg);
    }

    @Test
    @DisplayName("Verify rejection when document limit of 10 is exceeded")
    public void testMaxDocumentsLimit() {
        docRepo.setCount(10L);

        MockMultipartFile file = new MockMultipartFile(
                "file", "extra_11.pdf", "application/pdf", createSamplePdfBytes(1)
        );

        DocumentLimitExceededException ex = assertThrows(DocumentLimitExceededException.class, () -> {
            documentService.uploadDocument(1L, file);
        });

        assertTrue(ex.getMessage().contains("maximum limit of 10 documents"));
    }

    @Test
    @DisplayName("Verify rejection of non-PDF files")
    public void testNonPdfRejection() {
        docRepo.setCount(3L);

        MockMultipartFile txtFile = new MockMultipartFile(
                "file", "policy.txt", "text/plain", "This is plain text".getBytes()
        );

        InvalidDocumentException ex = assertThrows(InvalidDocumentException.class, () -> {
            documentService.uploadDocument(1L, txtFile);
        });

        assertTrue(ex.getMessage().contains("Only PDF documents (.pdf) are allowed"));
    }

    @Test
    @DisplayName("Verify rejection when PDF exceeds 20 pages limit")
    public void testMaxPagesLimit() {
        docRepo.setCount(3L);

        byte[] largePdfBytes = createSamplePdfBytes(22);
        MockMultipartFile file = new MockMultipartFile("file", "large.pdf", "application/pdf", largePdfBytes);

        InvalidDocumentException ex = assertThrows(InvalidDocumentException.class, () -> {
            documentService.uploadDocument(1L, file);
        });

        assertTrue(ex.getMessage().contains("exceeds the maximum limit of 20 pages"));
    }

    @Test
    @DisplayName("Verify rejection when PDF exceeds 10 MB limit")
    public void testMaxFileSizeLimit() {
        docRepo.setCount(2L);

        byte[] bigData = new byte[11 * 1024 * 1024]; // 11 MB
        MockMultipartFile file = new MockMultipartFile("file", "huge.pdf", "application/pdf", bigData);

        DocumentLimitExceededException ex = assertThrows(DocumentLimitExceededException.class, () -> {
            documentService.uploadDocument(1L, file);
        });

        assertTrue(ex.getMessage().contains("exceeds the maximum limit of 10 MB"));
    }

    private byte[] createSamplePdfBytes(int pages) {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            for (int i = 0; i < pages; i++) {
                doc.addPage(new PDPage());
            }
            doc.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Lightweight stubs without byte-buddy agent hooks
    static class StubOrganizationRepository implements OrganizationRepository {
        private final Map<Long, Organization> map = new HashMap<>();

        @Override public Optional<Organization> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public <S extends Organization> S save(S entity) { map.put(entity.getId(), entity); return entity; }
        @Override public Optional<Organization> findBySlug(String slug) { return Optional.empty(); }
        @Override public Optional<Organization> findByAssistantToken(String assistantToken) { return Optional.empty(); }
        @Override public boolean existsByName(String name) { return false; }
        @Override public boolean existsBySlug(String slug) { return false; }
        @Override public void flush() {}
        @Override public <S extends Organization> S saveAndFlush(S entity) { return null; }
        @Override public <S extends Organization> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<Organization> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Organization getOne(Long aLong) { return null; }
        @Override public Organization getById(Long aLong) { return null; }
        @Override public Organization getReferenceById(Long aLong) { return null; }
        @Override public <S extends Organization> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Organization> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends Organization> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends Organization> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends Organization> long count(Example<S> example) { return 0; }
        @Override public <S extends Organization> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Organization, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends Organization> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<Organization> findAll() { return null; }
        @Override public List<Organization> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return 0; }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(Organization entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Organization> entities) {}
        @Override public void deleteAll() {}
        @Override public List<Organization> findAll(Sort sort) { return null; }
        @Override public Page<Organization> findAll(Pageable pageable) { return null; }
    }

    static class StubDocumentRepository implements DocumentRepository {
        private long count = 0L;
        public void setCount(long c) { this.count = c; }
        @Override public long countByOrganizationId(Long organizationId) { return count; }
        @Override public List<Document> findByOrganizationIdOrderByUploadedAtDesc(Long organizationId) { return Collections.emptyList(); }
        @Override public Optional<Document> findByIdAndOrganizationId(Long id, Long organizationId) { return Optional.empty(); }
        @Override public void deleteByIdAndOrganizationId(Long id, Long organizationId) {}
        @Override public void flush() {}
        @Override public <S extends Document> S saveAndFlush(S entity) { return null; }
        @Override public <S extends Document> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<Document> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Document getOne(Long aLong) { return null; }
        @Override public Document getById(Long aLong) { return null; }
        @Override public Document getReferenceById(Long aLong) { return null; }
        @Override public <S extends Document> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Document> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends Document> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends Document> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends Document> long count(Example<S> example) { return 0; }
        @Override public <S extends Document> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Document, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends Document> S save(S entity) { return entity; }
        @Override public <S extends Document> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<Document> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<Document> findAll() { return null; }
        @Override public List<Document> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return 0; }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(Document entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Document> entities) {}
        @Override public void deleteAll() {}
        @Override public List<Document> findAll(Sort sort) { return null; }
        @Override public Page<Document> findAll(Pageable pageable) { return null; }
    }

    static class StubDocumentChunkRepository implements DocumentChunkRepository {
        @Override public List<DocumentChunk> findByDocumentId(Long documentId) { return Collections.emptyList(); }
        @Override public long countByOrganizationId(Long organizationId) { return 0; }
        @Override public void deleteByDocumentId(Long documentId) {}
        @Override public List<DocumentChunk> searchFullTextNative(Long orgId, String query, int limit) { return Collections.emptyList(); }
        @Override public List<DocumentChunk> searchByKeyword(Long orgId, String keyword) { return Collections.emptyList(); }
        @Override public List<DocumentChunk> findByOrganizationId(Long organizationId) { return Collections.emptyList(); }
        @Override public void flush() {}
        @Override public <S extends DocumentChunk> S saveAndFlush(S entity) { return null; }
        @Override public <S extends DocumentChunk> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<DocumentChunk> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public DocumentChunk getOne(Long aLong) { return null; }
        @Override public DocumentChunk getById(Long aLong) { return null; }
        @Override public DocumentChunk getReferenceById(Long aLong) { return null; }
        @Override public <S extends DocumentChunk> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends DocumentChunk> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends DocumentChunk> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends DocumentChunk> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends DocumentChunk> long count(Example<S> example) { return 0; }
        @Override public <S extends DocumentChunk> boolean exists(Example<S> example) { return false; }
        @Override public <S extends DocumentChunk, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends DocumentChunk> S save(S entity) { return entity; }
        @Override public <S extends DocumentChunk> List<S> saveAll(Iterable<S> entities) { return (List<S>) entities; }
        @Override public Optional<DocumentChunk> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<DocumentChunk> findAll() { return null; }
        @Override public List<DocumentChunk> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return 0; }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(DocumentChunk entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends DocumentChunk> entities) {}
        @Override public void deleteAll() {}
        @Override public List<DocumentChunk> findAll(Sort sort) { return null; }
        @Override public Page<DocumentChunk> findAll(Pageable pageable) { return null; }
    }
}
