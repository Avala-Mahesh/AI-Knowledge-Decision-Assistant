package com.aiassistant.decision;

import com.aiassistant.entity.DocumentChunk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecisionContext {
    private String userQuestion;
    private Long organizationId;
    private String organizationName;
    private List<DocumentChunk> retrievedChunks;
    private Map<String, Object> extractedParameters = new HashMap<>();

    public DecisionContext() {}

    public DecisionContext(String userQuestion, Long organizationId, String organizationName, List<DocumentChunk> retrievedChunks, Map<String, Object> extractedParameters) {
        this.userQuestion = userQuestion;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.retrievedChunks = retrievedChunks;
        if (extractedParameters != null) {
            this.extractedParameters = extractedParameters;
        }
    }

    public Object getParam(String key) {
        return extractedParameters.get(key);
    }

    public Double getDoubleParam(String key) {
        Object val = extractedParameters.get(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        if (val instanceof String) {
            try {
                return Double.parseDouble((String) val);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    public Integer getIntParam(String key) {
        Object val = extractedParameters.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        if (val instanceof String) {
            try {
                return Integer.parseInt((String) val);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    public Boolean getBooleanParam(String key) {
        Object val = extractedParameters.get(key);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        if (val instanceof String) {
            return Boolean.parseBoolean((String) val);
        }
        return null;
    }

    public String getStringParam(String key) {
        Object val = extractedParameters.get(key);
        return val != null ? val.toString() : null;
    }

    public String getUserQuestion() { return userQuestion; }
    public void setUserQuestion(String userQuestion) { this.userQuestion = userQuestion; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public List<DocumentChunk> getRetrievedChunks() { return retrievedChunks; }
    public void setRetrievedChunks(List<DocumentChunk> retrievedChunks) { this.retrievedChunks = retrievedChunks; }

    public Map<String, Object> getExtractedParameters() { return extractedParameters; }
    public void setExtractedParameters(Map<String, Object> extractedParameters) { this.extractedParameters = extractedParameters; }
}
