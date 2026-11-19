package com.aiassistant;

import com.aiassistant.decision.*;
import com.aiassistant.dto.AskRequest;
import com.aiassistant.dto.AskResponse;
import com.aiassistant.entity.Document;
import com.aiassistant.entity.DocumentChunk;
import com.aiassistant.entity.Organization;
import com.aiassistant.llm.DeterministicHybridLlmClient;
import com.aiassistant.llm.LlmService;
import com.aiassistant.llm.OllamaLlmClient;
import com.aiassistant.llm.OpenAiLlmClient;
import com.aiassistant.service.KnowledgeRetrievalService;
import com.aiassistant.service.QuestionPipelineService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QuestionPipelineTest {

    private MultiTenantIsolationTest.InMemoryOrgRepository orgRepo;
    private MultiTenantIsolationTest.InMemoryChunkRepository chunkRepo;
    private MultiTenantIsolationTest.InMemoryChatLogRepository chatLogRepo;
    private CompositeDecisionEngine decisionEngine;
    private QuestionPipelineService questionPipelineService;
    private Organization testOrg;

    @BeforeEach
    public void setup() {
        orgRepo = new MultiTenantIsolationTest.InMemoryOrgRepository();
        chunkRepo = new MultiTenantIsolationTest.InMemoryChunkRepository();
        chatLogRepo = new MultiTenantIsolationTest.InMemoryChatLogRepository();

        KnowledgeRetrievalService retrievalService = new KnowledgeRetrievalService(chunkRepo, "h2");

        ChoiceDecisionModel choiceModel = new ChoiceDecisionModel();
        BooleanDecisionModel booleanModel = new BooleanDecisionModel();
        ScoreDecisionModel scoreModel = new ScoreDecisionModel();
        QuestionClassificationModel classificationModel = new QuestionClassificationModel();
        decisionEngine = new CompositeDecisionEngine(List.of(choiceModel, booleanModel, scoreModel, classificationModel));

        DeterministicHybridLlmClient hybrid = new DeterministicHybridLlmClient();
        OllamaLlmClient ollama = new OllamaLlmClient("http://localhost:11434", "llama3", hybrid);
        OpenAiLlmClient openai = new OpenAiLlmClient("", "gpt-4o-mini", hybrid);
        LlmService llmService = new LlmService("hybrid", ollama, openai, hybrid);

        questionPipelineService = new QuestionPipelineService(orgRepo, retrievalService, decisionEngine, llmService, new com.aiassistant.service.ChatLogService(chatLogRepo));

        testOrg = new Organization("State University", "state-university");
        testOrg.setId(10L);
        testOrg.setAssistantToken("ast_state_univ_987");
        orgRepo.save(testOrg);

        Document doc = new Document(testOrg, "scholarship_doc.pdf", "Scholarship_Rules.pdf", 1200L, 4, 3);
        doc.setId(501L);

        DocumentChunk chunk1 = new DocumentChunk(
                testOrg, doc, 0, 4,
                "Undergraduate merit scholarship recipients must maintain a 3.5 cumulative GPA. The application deadline for fall term is July 1st annually."
        );
        chunkRepo.save(chunk1);
    }

    @AfterEach
    public void cleanup() {
        if (decisionEngine != null) {
            decisionEngine.shutdown();
        }
    }

    @Test
    @DisplayName("Verify end-to-end question processing and citation generation")
    public void testSuccessfulQuestionProcessing() {
        AskRequest request = new AskRequest(testOrg.getAssistantToken(), "What is the application deadline for fall merit scholarships?");
        AskResponse response = questionPipelineService.processQuestion(request);

        assertEquals("ANSWERED", response.getStatus());
        assertNotNull(response.getAnswer());
        assertFalse(response.getReferences().isEmpty(), "References must not be empty");
        assertEquals("Scholarship_Rules.pdf", response.getReferences().get(0).getDocumentName());
        assertEquals(4, response.getReferences().get(0).getPageNumber());
        assertEquals("Scholarship_Rules.pdf — Page 4", response.getReferences().get(0).getFormattedReference());
    }

    @Test
    @DisplayName("Verify out-of-scope question returns proper grounded unanswerable response")
    public void testOutOfScopeQuestion() {
        AskRequest request = new AskRequest(testOrg.getAssistantToken(), "How do I bake a blueberry pie?");
        AskResponse response = questionPipelineService.processQuestion(request);

        assertEquals("UNABLE_TO_ANSWER", response.getStatus());
        assertTrue(response.getReferences().isEmpty());
        assertTrue(response.getAnswer().contains("cannot find information") || response.getAnswer().contains("State University"));
    }
}
