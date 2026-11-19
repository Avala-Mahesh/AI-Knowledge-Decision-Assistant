package com.aiassistant;

import com.aiassistant.decision.ChoiceDecisionModel;
import com.aiassistant.decision.BooleanDecisionModel;
import com.aiassistant.decision.ScoreDecisionModel;
import com.aiassistant.decision.QuestionClassificationModel;
import com.aiassistant.decision.CompositeDecisionEngine;
import com.aiassistant.dto.AskRequest;
import com.aiassistant.dto.AskResponse;
import com.aiassistant.dto.DocumentResponse;
import com.aiassistant.entity.Document;
import com.aiassistant.entity.DocumentChunk;
import com.aiassistant.entity.Organization;
import com.aiassistant.llm.DeterministicHybridLlmClient;
import com.aiassistant.llm.LlmService;
import com.aiassistant.llm.OllamaLlmClient;
import com.aiassistant.llm.OpenAiLlmClient;
import com.aiassistant.repository.ChatLogRepository;
import com.aiassistant.repository.DocumentChunkRepository;
import com.aiassistant.repository.DocumentRepository;
import com.aiassistant.repository.OrganizationRepository;
import com.aiassistant.service.DocumentService;
import com.aiassistant.service.KnowledgeRetrievalService;
import com.aiassistant.service.PdfExtractionService;
import com.aiassistant.service.QuestionPipelineService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MultiTenantIsolationTest {

    private InMemoryOrgRepository orgRepo;
    private InMemoryDocRepository docRepo;
    private InMemoryChunkRepository chunkRepo;
    private InMemoryChatLogRepository chatLogRepo;

    private DocumentService documentService;
    private KnowledgeRetrievalService retrievalService;
    private CompositeDecisionEngine decisionEngine;
    private LlmService llmService;
    private QuestionPipelineService questionPipelineService;

    private Organization orgA;
    private Organization orgB;

    @BeforeEach
    public void setup() {
        orgRepo = new InMemoryOrgRepository();
        docRepo = new InMemoryDocRepository();
        chunkRepo = new InMemoryChunkRepository();
        chatLogRepo = new InMemoryChatLogRepository();

        documentService = new DocumentService(docRepo, chunkRepo, orgRepo, new PdfExtractionService());
        retrievalService = new KnowledgeRetrievalService(chunkRepo, "h2");

        ChoiceDecisionModel choiceModel = new ChoiceDecisionModel();
        BooleanDecisionModel booleanModel = new BooleanDecisionModel();
        ScoreDecisionModel scoreModel = new ScoreDecisionModel();
        QuestionClassificationModel classificationModel = new QuestionClassificationModel();
        decisionEngine = new CompositeDecisionEngine(List.of(choiceModel, booleanModel, scoreModel, classificationModel));

        DeterministicHybridLlmClient hybrid = new DeterministicHybridLlmClient();
        OllamaLlmClient ollama = new OllamaLlmClient("http://localhost:11434", "llama3", hybrid);
        OpenAiLlmClient openai = new OpenAiLlmClient("", "gpt-4o-mini", hybrid);
        llmService = new LlmService("hybrid", ollama, openai, hybrid);

        questionPipelineService = new QuestionPipelineService(orgRepo, retrievalService, decisionEngine, llmService, new com.aiassistant.service.ChatLogService(chatLogRepo));

        // Tenant A: Apex University
        orgA = new Organization("Apex University", "apex-university");
        orgA.setId(101L);
        orgA.setAssistantToken("ast_orgA_secret_token_123");
        orgRepo.save(orgA);

        Document docA = new Document(orgA, "apex_rules.pdf", "Apex_Scholarship_Rules_2024.pdf", 1024L, 3, 2);
        docA.setId(1001L);
        docRepo.save(docA);

        DocumentChunk chunkA = new DocumentChunk(orgA, docA, 0, 2, "Apex University Merit Scholarship requires a minimum 3.8 GPA and awards $12,000 annually.");
        chunkRepo.save(chunkA);

        // Tenant B: Nexus Corp
        orgB = new Organization("Nexus Corp", "nexus-corp");
        orgB.setId(202L);
        orgB.setAssistantToken("ast_orgB_secret_token_456");
        orgRepo.save(orgB);

        Document docB = new Document(orgB, "nexus_travel.pdf", "Nexus_Travel_Expense_Policy.pdf", 2048L, 2, 2);
        docB.setId(2001L);
        docRepo.save(docB);

        DocumentChunk chunkB = new DocumentChunk(orgB, docB, 0, 1, "Nexus Corp requires 14 days advance booking for all domestic flights and manager approval for expenses over $500.");
        chunkRepo.save(chunkB);
    }

    @AfterEach
    public void cleanup() {
        if (decisionEngine != null) {
            decisionEngine.shutdown();
        }
    }

    @Test
    @DisplayName("Verify Organization A cannot see Organization B's documents in administrative view")
    public void testDocumentListingIsolation() {
        List<DocumentResponse> docsA = documentService.getDocuments(orgA.getId());
        List<DocumentResponse> docsB = documentService.getDocuments(orgB.getId());

        assertEquals(1, docsA.size());
        assertEquals("Apex_Scholarship_Rules_2024.pdf", docsA.get(0).getOriginalFileName());

        assertEquals(1, docsB.size());
        assertEquals("Nexus_Travel_Expense_Policy.pdf", docsB.get(0).getOriginalFileName());

        assertFalse(docsA.stream().anyMatch(d -> d.getOriginalFileName().contains("Nexus")));
        assertFalse(docsB.stream().anyMatch(d -> d.getOriginalFileName().contains("Apex")));
    }

    @Test
    @DisplayName("Verify Assistant A never retrieves knowledge from Organization B")
    public void testAssistantKnowledgeRetrievalIsolation() {
        // Query Assistant A about Nexus Corp's policy
        AskRequest requestA = new AskRequest(orgA.getAssistantToken(), "What is the domestic flight advance booking rule?");
        AskResponse responseA = questionPipelineService.processQuestion(requestA);

        assertEquals("UNABLE_TO_ANSWER", responseA.getStatus());
        assertTrue(responseA.getReferences().isEmpty(), "No citations should be returned from another tenant's documents");
        assertTrue(responseA.getAnswer().contains("cannot find information") || responseA.getAnswer().contains("Apex University"));
    }

    @Test
    @DisplayName("Verify Assistant B only retrieves Organization B's knowledge with source citations")
    public void testAssistantBRetrieval() {
        AskRequest requestB = new AskRequest(orgB.getAssistantToken(), "How many days in advance must flights be booked?");
        AskResponse responseB = questionPipelineService.processQuestion(requestB);

        assertEquals("ANSWERED", responseB.getStatus());
        assertFalse(responseB.getReferences().isEmpty());
        assertEquals("Nexus_Travel_Expense_Policy.pdf", responseB.getReferences().get(0).getDocumentName());
        assertEquals(1, responseB.getReferences().get(0).getPageNumber());
        assertTrue(responseB.getAnswer().contains("14 days"));
    }

    @Test
    @DisplayName("Verify deleting Org A document does not affect Org B documents")
    public void testDocumentDeletionIsolation() {
        documentService.deleteDocument(orgA.getId(), 1001L);

        assertEquals(0, documentService.getDocuments(orgA.getId()).size());
        assertEquals(1, documentService.getDocuments(orgB.getId()).size());
    }

    // Lightweight in-memory test doubles
    static class InMemoryOrgRepository extends DocumentValidationTest.StubOrganizationRepository {
        private final Map<Long, Organization> byId = new HashMap<>();
        private final Map<String, Organization> byToken = new HashMap<>();

        @Override public <S extends Organization> S save(S entity) {
            byId.put(entity.getId(), entity);
            byToken.put(entity.getAssistantToken(), entity);
            return entity;
        }
        @Override public Optional<Organization> findById(Long id) { return Optional.ofNullable(byId.get(id)); }
        @Override public Optional<Organization> findByAssistantToken(String token) { return Optional.ofNullable(byToken.get(token)); }
    }

    static class InMemoryDocRepository extends DocumentValidationTest.StubDocumentRepository {
        private final List<Document> docs = new ArrayList<>();

        @Override public <S extends Document> S save(S entity) { docs.add(entity); return entity; }
        @Override public List<Document> findByOrganizationIdOrderByUploadedAtDesc(Long orgId) {
            List<Document> result = new ArrayList<>();
            for (Document d : docs) {
                if (d.getOrganization().getId().equals(orgId)) result.add(d);
            }
            return result;
        }
        @Override public Optional<Document> findByIdAndOrganizationId(Long id, Long orgId) {
            return docs.stream().filter(d -> d.getId().equals(id) && d.getOrganization().getId().equals(orgId)).findFirst();
        }
        @Override public void delete(Document entity) { docs.remove(entity); }
        @Override public long countByOrganizationId(Long orgId) {
            return docs.stream().filter(d -> d.getOrganization().getId().equals(orgId)).count();
        }
    }

    static class InMemoryChunkRepository extends DocumentValidationTest.StubDocumentChunkRepository {
        private final List<DocumentChunk> chunks = new ArrayList<>();

        @Override public <S extends DocumentChunk> S save(S entity) { chunks.add(entity); return entity; }
        @Override public List<DocumentChunk> findByOrganizationId(Long orgId) {
            List<DocumentChunk> res = new ArrayList<>();
            for (DocumentChunk c : chunks) {
                if (c.getOrganization().getId().equals(orgId)) res.add(c);
            }
            return res;
        }
        @Override public void deleteByDocumentId(Long docId) {
            chunks.removeIf(c -> c.getDocument().getId().equals(docId));
        }
        @Override public long countByOrganizationId(Long orgId) {
            return chunks.stream().filter(c -> c.getOrganization().getId().equals(orgId)).count();
        }
    }

    static class InMemoryChatLogRepository implements ChatLogRepository {
        private final List<com.aiassistant.entity.ChatLog> logs = new ArrayList<>();
        @Override public <S extends com.aiassistant.entity.ChatLog> S save(S entity) { logs.add(entity); return entity; }
        @Override public long countByOrganizationId(Long organizationId) { return logs.stream().filter(l -> l.getOrganization().getId().equals(organizationId)).count(); }
        @Override public long countByOrganizationIdAndStatus(Long organizationId, String status) { return logs.stream().filter(l -> l.getOrganization().getId().equals(organizationId) && status.equals(l.getStatus())).count(); }
        @Override public List<com.aiassistant.entity.ChatLog> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId, org.springframework.data.domain.Pageable pageable) { return logs; }
        @Override public void flush() {}
        @Override public <S extends com.aiassistant.entity.ChatLog> S saveAndFlush(S entity) { return null; }
        @Override public <S extends com.aiassistant.entity.ChatLog> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<com.aiassistant.entity.ChatLog> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public com.aiassistant.entity.ChatLog getOne(Long aLong) { return null; }
        @Override public com.aiassistant.entity.ChatLog getById(Long aLong) { return null; }
        @Override public com.aiassistant.entity.ChatLog getReferenceById(Long aLong) { return null; }
        @Override public <S extends com.aiassistant.entity.ChatLog> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends com.aiassistant.entity.ChatLog> List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
        @Override public <S extends com.aiassistant.entity.ChatLog> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
        @Override public <S extends com.aiassistant.entity.ChatLog> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
        @Override public <S extends com.aiassistant.entity.ChatLog> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends com.aiassistant.entity.ChatLog> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends com.aiassistant.entity.ChatLog, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends com.aiassistant.entity.ChatLog> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<com.aiassistant.entity.ChatLog> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<com.aiassistant.entity.ChatLog> findAll() { return null; }
        @Override public List<com.aiassistant.entity.ChatLog> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return 0; }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(com.aiassistant.entity.ChatLog entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends com.aiassistant.entity.ChatLog> entities) {}
        @Override public void deleteAll() {}
        @Override public List<com.aiassistant.entity.ChatLog> findAll(org.springframework.data.domain.Sort sort) { return null; }
        @Override public org.springframework.data.domain.Page<com.aiassistant.entity.ChatLog> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
    }
}
