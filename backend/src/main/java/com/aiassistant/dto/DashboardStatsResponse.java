package com.aiassistant.dto;

public class DashboardStatsResponse {
    private long totalDocuments;
    private long maxAllowedDocuments = 10;
    private long totalChunks;
    private long totalQuestionsAsked;
    private long totalQuestionsAnswered;
    private long totalQuestionsUnableToAnswer;
    private String assistantToken;
    private String assistantUrl;
    private String embedCode;

    public DashboardStatsResponse() {}

    public long getTotalDocuments() { return totalDocuments; }
    public void setTotalDocuments(long totalDocuments) { this.totalDocuments = totalDocuments; }

    public long getMaxAllowedDocuments() { return maxAllowedDocuments; }
    public void setMaxAllowedDocuments(long maxAllowedDocuments) { this.maxAllowedDocuments = maxAllowedDocuments; }

    public long getTotalChunks() { return totalChunks; }
    public void setTotalChunks(long totalChunks) { this.totalChunks = totalChunks; }

    public long getTotalQuestionsAsked() { return totalQuestionsAsked; }
    public void setTotalQuestionsAsked(long totalQuestionsAsked) { this.totalQuestionsAsked = totalQuestionsAsked; }

    public long getTotalQuestionsAnswered() { return totalQuestionsAnswered; }
    public void setTotalQuestionsAnswered(long totalQuestionsAnswered) { this.totalQuestionsAnswered = totalQuestionsAnswered; }

    public long getTotalQuestionsUnableToAnswer() { return totalQuestionsUnableToAnswer; }
    public void setTotalQuestionsUnableToAnswer(long totalQuestionsUnableToAnswer) { this.totalQuestionsUnableToAnswer = totalQuestionsUnableToAnswer; }

    public String getAssistantToken() { return assistantToken; }
    public void setAssistantToken(String assistantToken) { this.assistantToken = assistantToken; }

    public String getAssistantUrl() { return assistantUrl; }
    public void setAssistantUrl(String assistantUrl) { this.assistantUrl = assistantUrl; }

    public String getEmbedCode() { return embedCode; }
    public void setEmbedCode(String embedCode) { this.embedCode = embedCode; }
}
