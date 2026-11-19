package com.aiassistant.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    private static final Logger logger = LoggerFactory.getLogger(LlmService.class);

    private final String preferredProvider;
    private final OllamaLlmClient ollamaClient;
    private final OpenAiLlmClient openAiClient;
    private final DeterministicHybridLlmClient hybridClient;

    public LlmService(
            @Value("${app.llm.provider:ollama}") String preferredProvider,
            OllamaLlmClient ollamaClient,
            OpenAiLlmClient openAiClient,
            DeterministicHybridLlmClient hybridClient) {
        this.preferredProvider = preferredProvider != null ? preferredProvider.toLowerCase() : "ollama";
        this.ollamaClient = ollamaClient;
        this.openAiClient = openAiClient;
        this.hybridClient = hybridClient;
        logger.info("LLM Service initialized with primary provider: '{}'", this.preferredProvider);
    }

    public LlmClient getActiveClient() {
        if ("ollama".equalsIgnoreCase(preferredProvider)) {
            return ollamaClient;
        } else if ("openai".equalsIgnoreCase(preferredProvider)) {
            return openAiClient;
        } else if ("hybrid".equalsIgnoreCase(preferredProvider)) {
            return hybridClient;
        }
        return ollamaClient;
    }

    public Map<String, Object> extractParameters(String question, String retrievedContext) {
        LlmClient client = getActiveClient();
        logger.info("Extracting question parameters with LLM provider: {}", client.getProviderName());
        return client.extractParametersAndIntent(question, retrievedContext);
    }

    public String synthesizeAnswer(String question, String retrievedContext, String decisionSummary, List<String> references) {
        LlmClient client = getActiveClient();
        logger.info("Synthesizing grounded answer with LLM provider: {}", client.getProviderName());
        return client.synthesizeAnswer(question, retrievedContext, decisionSummary, references);
    }
}
