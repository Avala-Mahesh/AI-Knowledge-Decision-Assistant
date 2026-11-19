package com.aiassistant.service;

import com.aiassistant.dto.ChatLogResponse;
import com.aiassistant.dto.DashboardStatsResponse;
import com.aiassistant.entity.Organization;
import com.aiassistant.exception.ResourceNotFoundException;
import com.aiassistant.repository.ChatLogRepository;
import com.aiassistant.repository.DocumentChunkRepository;
import com.aiassistant.repository.DocumentRepository;
import com.aiassistant.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final OrganizationRepository organizationRepository;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final ChatLogRepository chatLogRepository;
    private final String publicBaseUrl;

    public StatsService(
            OrganizationRepository organizationRepository,
            DocumentRepository documentRepository,
            DocumentChunkRepository chunkRepository,
            ChatLogRepository chatLogRepository,
            @Value("${app.frontend.base-url:http://localhost:3000}") String publicBaseUrl) {
        this.organizationRepository = organizationRepository;
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.chatLogRepository = chatLogRepository;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats(Long organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + organizationId));

        DashboardStatsResponse stats = new DashboardStatsResponse();
        stats.setTotalDocuments(documentRepository.countByOrganizationId(organizationId));
        stats.setMaxAllowedDocuments(10);
        stats.setTotalChunks(chunkRepository.countByOrganizationId(organizationId));
        stats.setTotalQuestionsAsked(chatLogRepository.countByOrganizationId(organizationId));
        stats.setTotalQuestionsAnswered(chatLogRepository.countByOrganizationIdAndStatus(organizationId, "ANSWERED"));
        stats.setTotalQuestionsUnableToAnswer(chatLogRepository.countByOrganizationIdAndStatus(organizationId, "UNABLE_TO_ANSWER"));
        stats.setAssistantToken(org.getAssistantToken());

        String assistantUrl = publicBaseUrl + "/chat/" + org.getAssistantToken();
        stats.setAssistantUrl(assistantUrl);

        String iframeEmbed = "<iframe\n" +
                "  src=\"" + assistantUrl + "\"\n" +
                "  width=\"100%\"\n" +
                "  height=\"600\"\n" +
                "  frameborder=\"0\"\n" +
                "  style=\"border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.1);\">\n" +
                "</iframe>";
        stats.setEmbedCode(iframeEmbed);

        return stats;
    }

    @Transactional
    public String regenerateAssistantToken(Long organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + organizationId));

        String newToken = "ast_" + UUID.randomUUID().toString().replace("-", "");
        org.setAssistantToken(newToken);
        organizationRepository.save(org);
        return newToken;
    }

    @Transactional(readOnly = true)
    public List<ChatLogResponse> getRecentChatLogs(Long organizationId, int limit) {
        return chatLogRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId, PageRequest.of(0, limit))
                .stream()
                .map(log -> new ChatLogResponse(
                        log.getId(),
                        log.getQuestion(),
                        log.getAnswer(),
                        log.getStatus(),
                        log.getQuestionType(),
                        log.getDecisionEngineUsed(),
                        log.getDecisionSummary(),
                        log.getResponseTimeMs(),
                        log.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
