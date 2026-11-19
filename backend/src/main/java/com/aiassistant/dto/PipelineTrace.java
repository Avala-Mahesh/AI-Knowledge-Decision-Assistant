package com.aiassistant.dto;

import java.util.List;
import java.util.Map;

public class PipelineTrace {
    private String organizationName;
    private String questionType;
    private int retrievedChunkCount;
    private Map<String, Object> extractedParameters;
    private boolean decisionEngineUsed;
    private List<DecisionTrace> decisions;
    private String decisionSummary;
    private long totalProcessingTimeMs;
    private String executionMode; // e.g., "System 1 Deterministic Engine + LLM Hybrid"

    public PipelineTrace() {}

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public int getRetrievedChunkCount() { return retrievedChunkCount; }
    public void setRetrievedChunkCount(int retrievedChunkCount) { this.retrievedChunkCount = retrievedChunkCount; }

    public Map<String, Object> getExtractedParameters() { return extractedParameters; }
    public void setExtractedParameters(Map<String, Object> extractedParameters) { this.extractedParameters = extractedParameters; }

    public boolean isDecisionEngineUsed() { return decisionEngineUsed; }
    public void setDecisionEngineUsed(boolean decisionEngineUsed) { this.decisionEngineUsed = decisionEngineUsed; }

    public List<DecisionTrace> getDecisions() { return decisions; }
    public void setDecisions(List<DecisionTrace> decisions) { this.decisions = decisions; }

    public String getDecisionSummary() { return decisionSummary; }
    public void setDecisionSummary(String decisionSummary) { this.decisionSummary = decisionSummary; }

    public long getTotalProcessingTimeMs() { return totalProcessingTimeMs; }
    public void setTotalProcessingTimeMs(long totalProcessingTimeMs) { this.totalProcessingTimeMs = totalProcessingTimeMs; }

    public String getExecutionMode() { return executionMode; }
    public void setExecutionMode(String executionMode) { this.executionMode = executionMode; }
}
