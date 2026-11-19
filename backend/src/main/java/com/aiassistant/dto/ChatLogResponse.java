package com.aiassistant.dto;

import java.time.LocalDateTime;

public class ChatLogResponse {
    private Long id;
    private String question;
    private String answer;
    private String status;
    private String questionType;
    private Boolean decisionEngineUsed;
    private String decisionSummary;
    private Long responseTimeMs;
    private LocalDateTime createdAt;

    public ChatLogResponse() {}

    public ChatLogResponse(Long id, String question, String answer, String status, String questionType, Boolean decisionEngineUsed, String decisionSummary, Long responseTimeMs, LocalDateTime createdAt) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.status = status;
        this.questionType = questionType;
        this.decisionEngineUsed = decisionEngineUsed;
        this.decisionSummary = decisionSummary;
        this.responseTimeMs = responseTimeMs;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
