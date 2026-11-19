package com.aiassistant.service;

import com.aiassistant.decision.CompositeDecisionEngine;
import com.aiassistant.decision.DecisionContext;
import com.aiassistant.dto.AskRequest;
import com.aiassistant.dto.AskResponse;
import com.aiassistant.dto.Citation;
import com.aiassistant.dto.PipelineTrace;
import com.aiassistant.entity.DocumentChunk;
import com.aiassistant.entity.Organization;
import com.aiassistant.exception.ResourceNotFoundException;
import com.aiassistant.llm.LlmService;
import com.aiassistant.repository.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuestionPipelineService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionPipelineService.class);

    private final OrganizationRepository organizationRepository;
    private final KnowledgeRetrievalService knowledgeRetrievalService;
    private final CompositeDecisionEngine decisionEngine;
    private final LlmService llmService;
    private final ChatLogService chatLogService;

    @Autowired
    public QuestionPipelineService(
            OrganizationRepository organizationRepository,
            KnowledgeRetrievalService knowledgeRetrievalService,
            CompositeDecisionEngine decisionEngine,
            LlmService llmService,
            ChatLogService chatLogService) {
        this.organizationRepository = organizationRepository;
        this.knowledgeRetrievalService = knowledgeRetrievalService;
        this.decisionEngine = decisionEngine;
        this.llmService = llmService;
        this.chatLogService = chatLogService;
    }

    public AskResponse processQuestion(AskRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. Stage 1 & 2: Token Validation & Organization Identification
        Organization organization = organizationRepository.findByAssistantToken(request.getAssistantToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or unknown assistant token. Please verify your assistant URL."));

        logger.info("Processing question for Organization: '{}' (ID: {})", organization.getName(), organization.getId());

        // 2. Stage 3: Knowledge Retrieval (Organization Scoped)
        List<DocumentChunk> retrievedChunks = knowledgeRetrievalService.retrieveRelevantChunks(
                organization.getId(), request.getQuestion()
        );

        PipelineTrace trace = new PipelineTrace();
        trace.setOrganizationName(organization.getName());
        trace.setRetrievedChunkCount(retrievedChunks.size());
        trace.setExecutionMode("System 1 Deterministic Engine + Ollama LLM");

        // Format Citations
        List<Citation> citations = new ArrayList<>();
        List<String> formattedReferences = new ArrayList<>();
        Set<String> uniqueReferences = new LinkedHashSet<>();

        StringBuilder contextBuilder = new StringBuilder();
        for (DocumentChunk chunk : retrievedChunks) {
            String docName = chunk.getDocument().getOriginalFileName();
            int page = chunk.getPageNumber();
            String refKey = docName + " — Page " + page;

            if (uniqueReferences.add(refKey)) {
                String snippet = chunk.getContent();
                if (snippet.length() > 200) {
                    snippet = snippet.substring(0, 200) + "...";
                }
                citations.add(new Citation(docName, page, snippet));
                formattedReferences.add(refKey);
            }

            contextBuilder.append("Source [").append(docName).append(", Page ").append(page).append("]:\n")
                    .append(chunk.getContent()).append("\n\n");
        }

        // If no relevant knowledge found in organization's documents
        if (retrievedChunks.isEmpty()) {
            String unanswerableAnswer = "I cannot find information regarding this question in " + organization.getName() + "'s uploaded documents. The knowledge base does not contain relevant policies for this topic.";
            trace.setQuestionType("OUT_OF_SCOPE");
            trace.setDecisionEngineUsed(false);
            trace.setTotalProcessingTimeMs(System.currentTimeMillis() - startTime);

            chatLogService.recordChatLog(organization, request.getQuestion(), unanswerableAnswer, "UNABLE_TO_ANSWER", "OUT_OF_SCOPE", false, "No matching knowledge chunks found", trace.getTotalProcessingTimeMs());

            return new AskResponse(unanswerableAnswer, Collections.emptyList(), "UNABLE_TO_ANSWER", trace);
        }

        // 3. Stage 4: Question Understanding & Parameter Decomposition
        Map<String, Object> extractedParams = llmService.extractParameters(request.getQuestion(), contextBuilder.toString());
        trace.setExtractedParameters(extractedParams);

        // 4. Stage 5: Decision / Reasoning with System 1 Deterministic Engine
        DecisionContext decisionContext = new DecisionContext(
                request.getQuestion(),
                organization.getId(),
                organization.getName(),
                retrievedChunks,
                extractedParams
        );

        CompositeDecisionEngine.CombinedDecisionOutcome decisionOutcome = decisionEngine.executeDecisions(decisionContext);
        trace.setDecisionEngineUsed(decisionOutcome.isExecuted());
        trace.setDecisions(decisionOutcome.getTraces());
        trace.setDecisionSummary(decisionOutcome.getSummary());

        String questionType = decisionOutcome.getTraces().stream()
                .filter(t -> "CLASSIFICATION".equals(t.getDecisionType()))
                .map(t -> t.getOutcome())
                .findFirst()
                .orElse("DIRECT_INFO");
        trace.setQuestionType(questionType);

        if ("OUT_OF_SCOPE".equals(questionType)) {
            String unanswerableAnswer = "I cannot find information regarding this question in " + organization.getName() + "'s uploaded documents. The query appears to be outside the scope of organizational policy.";
            trace.setTotalProcessingTimeMs(System.currentTimeMillis() - startTime);

            chatLogService.recordChatLog(organization, request.getQuestion(), unanswerableAnswer, "UNABLE_TO_ANSWER", "OUT_OF_SCOPE", true, decisionOutcome.getSummary(), trace.getTotalProcessingTimeMs());

            return new AskResponse(unanswerableAnswer, Collections.emptyList(), "UNABLE_TO_ANSWER", trace);
        }

        // 5. Stage 6: Answer Generation (Ollama LLM grounded synthesis)
        String finalAnswer = llmService.synthesizeAnswer(
                request.getQuestion(),
                contextBuilder.toString(),
                decisionOutcome.getSummary(),
                formattedReferences
        );

        long totalElapsed = System.currentTimeMillis() - startTime;
        trace.setTotalProcessingTimeMs(totalElapsed);

        // 6. Stage 7: Log usage metrics in isolated transaction & return
        chatLogService.recordChatLog(organization, request.getQuestion(), finalAnswer, "ANSWERED", questionType, decisionOutcome.isExecuted(), decisionOutcome.getSummary(), totalElapsed);

        return new AskResponse(finalAnswer, citations, "ANSWERED", trace);
    }
}
