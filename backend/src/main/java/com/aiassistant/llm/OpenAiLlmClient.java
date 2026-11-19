package com.aiassistant.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class OpenAiLlmClient implements LlmClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiLlmClient.class);

    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;
    private final DeterministicHybridLlmClient fallback;

    public OpenAiLlmClient(
            @Value("${app.llm.openai.api-key:}") String apiKey,
            @Value("${app.llm.openai.model:gpt-4o-mini}") String model,
            DeterministicHybridLlmClient fallback) {
        this.apiKey = apiKey;
        this.model = model;
        this.fallback = fallback;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProviderName() {
        return "OpenAI (" + model + ")";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.startsWith("YOUR_");
    }

    @Override
    public Map<String, Object> extractParametersAndIntent(String question, String retrievedContext) {
        if (!isAvailable()) {
            return fallback.extractParametersAndIntent(question, retrievedContext);
        }

        try {
            Map<String, Object> payload = Map.of(
                    "model", model,
                    "response_format", Map.of("type", "json_object"),
                    "messages", List.of(
                            Map.of("role", "system", "content", "Extract structured parameters from question as JSON with keys: gpa (number/null), year (string/null), hasSportsScholarship (boolean/null), amount (number/null), expenseType (string/null), leadTimeDays (number/null)."),
                            Map.of("role", "user", "content", question)
                    )
            );

            String responseBody = sendPost("https://api.openai.com/v1/chat/completions", objectMapper.writeValueAsString(payload));
            if (responseBody != null) {
                JsonNode root = objectMapper.readTree(responseBody);
                String jsonContent = root.path("choices").get(0).path("message").path("content").asText();
                return objectMapper.readValue(jsonContent, Map.class);
            }
        } catch (Exception e) {
            logger.warn("OpenAI extraction error: {}, falling back", e.getMessage());
        }

        return fallback.extractParametersAndIntent(question, retrievedContext);
    }

    @Override
    public String synthesizeAnswer(String question, String retrievedContext, String decisionSummary, List<String> formattedReferences) {
        if (!isAvailable()) {
            return fallback.synthesizeAnswer(question, retrievedContext, decisionSummary, formattedReferences);
        }

        try {
            String systemPrompt = "You are a helpful assistant. Answer strictly grounded in the document context and decision engine outcome. Do not invent answers.";
            String userContent = "Document Context:\n" + retrievedContext + "\n\n" +
                    "Decision Engine Results:\n" + decisionSummary + "\n\n" +
                    "Question: " + question + "\n\n" +
                    "References: " + String.join(", ", formattedReferences);

            Map<String, Object> payload = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userContent)
                    )
            );

            String responseBody = sendPost("https://api.openai.com/v1/chat/completions", objectMapper.writeValueAsString(payload));
            if (responseBody != null) {
                JsonNode root = objectMapper.readTree(responseBody);
                return root.path("choices").get(0).path("message").path("content").asText().trim();
            }
        } catch (Exception e) {
            logger.warn("OpenAI answer synthesis error: {}, falling back", e.getMessage());
        }

        return fallback.synthesizeAnswer(question, retrievedContext, decisionSummary, formattedReferences);
    }

    private String sendPost(String endpoint, String jsonPayload) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(20000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line.trim());
                    }
                    return response.toString();
                }
            }
        } catch (Exception e) {
            logger.warn("HTTP POST error to {}: {}", endpoint, e.getMessage());
        }
        return null;
    }
}
