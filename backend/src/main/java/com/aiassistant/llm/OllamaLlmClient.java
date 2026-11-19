package com.aiassistant.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class OllamaLlmClient implements LlmClient {

    private static final Logger logger = LoggerFactory.getLogger(OllamaLlmClient.class);

    private final String baseUrl;
    private final String model;
    private final ObjectMapper objectMapper;
    private final DeterministicHybridLlmClient fallback;

    public OllamaLlmClient(
            @Value("${spring.ai.ollama.base-url:${app.llm.ollama.base-url:http://localhost:11434}}") String baseUrl,
            @Value("${spring.ai.ollama.chat.options.model:${app.llm.ollama.model:llama3.2:3b}}") String model,
            DeterministicHybridLlmClient fallback) {
        this.baseUrl = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl.replaceAll("/+$", "") : "http://localhost:11434";
        this.model = (model != null && !model.isBlank()) ? model : "llama3.2:3b";
        this.fallback = fallback;
        this.objectMapper = new ObjectMapper();
        logger.info("Ollama client configured: BaseUrl={}, Model={}", this.baseUrl, this.model);
    }

    @Override
    public String getProviderName() {
        return "Ollama (" + model + ")";
    }

    @Override
    public boolean isAvailable() {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return false;
        }
        try {
            URL url = new URL(baseUrl + "/api/tags");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            int code = conn.getResponseCode();
            conn.disconnect();
            return code == 200;
        } catch (Exception e) {
            logger.debug("Ollama availability probe to {} failed: {}", baseUrl, e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> extractParametersAndIntent(String question, String retrievedContext) {
        if (!isAvailable()) {
            logger.debug("Ollama is not responding at {}, using deterministic extractor fallback", baseUrl);
            return fallback.extractParametersAndIntent(question, retrievedContext);
        }

        try {
            String prompt = "You are an AI Question Understanding and Decomposition engine.\n" +
                    "Extract structured parameters from the user question as JSON with keys:\n" +
                    "- gpa (number or null)\n" +
                    "- year (string e.g. 'Junior', '3rd year', or null)\n" +
                    "- hasSportsScholarship (boolean or null)\n" +
                    "- amount (number or null)\n" +
                    "- expenseType (string or null)\n" +
                    "- leadTimeDays (number or null)\n\n" +
                    "Question: " + question + "\n" +
                    "Return ONLY valid JSON.";

            Map<String, Object> payload = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false,
                    "format", "json"
            );

            String responseBody = sendPost(baseUrl + "/api/generate", objectMapper.writeValueAsString(payload));
            if (responseBody != null && !responseBody.isBlank()) {
                JsonNode root = objectMapper.readTree(responseBody);
                String responseText = root.path("response").asText();
                if (responseText != null && !responseText.isBlank()) {
                    return objectMapper.readValue(responseText, Map.class);
                }
            }
        } catch (Exception e) {
            logger.warn("Ollama parameter extraction failed: {}, using deterministic extractor fallback", e.getMessage());
        }

        return fallback.extractParametersAndIntent(question, retrievedContext);
    }

    @Override
    public String synthesizeAnswer(String question, String retrievedContext, String decisionSummary, List<String> formattedReferences) {
        if (!isAvailable()) {
            logger.warn("Ollama server at {} is currently unreachable. Make sure 'ollama serve' is running and '{}' is pulled. Using grounded synthesis fallback.", baseUrl, model);
            return fallback.synthesizeAnswer(question, retrievedContext, decisionSummary, formattedReferences);
        }

        try {
            String prompt = "You are a professional organizational assistant. Answer the user question strictly grounded in the provided document context and System 1 decision engine results. Do not invent facts.\n\n" +
                    "Document Context:\n" + retrievedContext + "\n\n" +
                    "System 1 Decision Engine Outcome:\n" + (decisionSummary != null ? decisionSummary : "None") + "\n\n" +
                    "User Question: " + question + "\n\n" +
                    "Citations to include:\n" + (formattedReferences != null && !formattedReferences.isEmpty() ? String.join("\n", formattedReferences) : "None") + "\n\n" +
                    "Please formulate a clear, authoritative, and polite answer grounded ONLY in the above context:";

            Map<String, Object> payload = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false
            );

            String responseBody = sendPost(baseUrl + "/api/generate", objectMapper.writeValueAsString(payload));
            if (responseBody != null && !responseBody.isBlank()) {
                JsonNode root = objectMapper.readTree(responseBody);
                String responseText = root.path("response").asText();
                if (responseText != null && !responseText.isBlank()) {
                    String answer = responseText.trim();
                    if (formattedReferences != null && !formattedReferences.isEmpty() && !answer.contains("Page")) {
                        StringBuilder citationSuffix = new StringBuilder("\n\n**Sources:**\n");
                        for (String ref : formattedReferences) {
                            citationSuffix.append("- [").append(ref).append("]\n");
                        }
                        answer = answer + citationSuffix.toString();
                    }
                    return answer;
                }
            }
        } catch (Exception e) {
            logger.warn("Ollama answer synthesis failed: {}, using grounded synthesis fallback", e.getMessage());
        }

        return fallback.synthesizeAnswer(question, retrievedContext, decisionSummary, formattedReferences);
    }

    private String sendPost(String endpoint, String jsonPayload) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(90000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int statusCode = conn.getResponseCode();
            if (statusCode == 200) {
                return readStream(conn.getInputStream());
            } else {
                String errorBody = readStream(conn.getErrorStream());
                logger.error("Ollama HTTP {} from {}: {}", statusCode, endpoint, errorBody);
            }
        } catch (Exception e) {
            logger.warn("HTTP POST error communicating with Ollama at {}: {}", endpoint, e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    private String readStream(InputStream is) {
        if (is == null) return null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }
}
