package com.aiassistant.llm;

import java.util.List;
import java.util.Map;

public interface LlmClient {
    String getProviderName();
    boolean isAvailable();
    Map<String, Object> extractParametersAndIntent(String question, String retrievedContext);
    String synthesizeAnswer(String question, String retrievedContext, String decisionSummary, List<String> formattedReferences);
}
