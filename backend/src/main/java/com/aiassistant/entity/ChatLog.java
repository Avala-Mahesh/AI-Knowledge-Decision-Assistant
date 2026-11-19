package com.aiassistant.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_logs", indexes = {
    @Index(name = "idx_chat_org", columnList = "organization_id")
})
public class ChatLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "LONGTEXT")
    private String answer;

    @Column(nullable = false, length = 50)
    private String status; // "ANSWERED", "UNABLE_TO_ANSWER", "ERROR"

    @Column(length = 50)
    private String questionType; // "DIRECT_INFO", "POLICY_ELIGIBILITY", "PROCEDURAL", "COMPARISON", "OUT_OF_SCOPE"

    private Boolean decisionEngineUsed;

    @Column(columnDefinition = "TEXT")
    private String decisionSummary;

    private Long responseTimeMs;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public ChatLog() {}

    public ChatLog(Organization organization, String question, String answer, String status, String questionType, Boolean decisionEngineUsed, String decisionSummary, Long responseTimeMs) {
        this.organization = organization;
        this.question = question;
        this.answer = answer;
        this.status = status;
        this.questionType = questionType;
        this.decisionEngineUsed = decisionEngineUsed;
        this.decisionSummary = decisionSummary;
        this.responseTimeMs = responseTimeMs;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public Boolean getDecisionEngineUsed() { return decisionEngineUsed; }
    public void setDecisionEngineUsed(Boolean decisionEngineUsed) { this.decisionEngineUsed = decisionEngineUsed; }

    public String getDecisionSummary() { return decisionSummary; }
    public void setDecisionSummary(String decisionSummary) { this.decisionSummary = decisionSummary; }

    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
