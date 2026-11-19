package com.aiassistant.llm;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DeterministicHybridLlmClient implements LlmClient {

    private static final Pattern GPA_PATTERN = Pattern.compile("(?i)(?:gpa\\s*(?:of|is|:)?\\s*|with\\s*(?:a)?\\s*)?(\\b[2-4]\\.\\d{1,2}\\b)(?:\\s*gpa)?");
    private static final Pattern YEAR_PATTERN = Pattern.compile("(?i)\\b(1st|2nd|3rd|4th|freshman|sophomore|junior|senior|first|second|third|fourth)\\b(?:\\s*year)?");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(?i)\\$\\s*([0-9]+(?:\\.[0-9]{2})?)|([0-9]+(?:\\.[0-9]{2})?)\\s*(?:dollars|usd)");
    private static final Pattern DAYS_PATTERN = Pattern.compile("(?i)(\\d+)\\s*(?:days?|weeks?)\\s*(?:in advance|advance|before)?");

    @Override
    public String getProviderName() {
        return "Deterministic Hybrid NLP Client";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Map<String, Object> extractParametersAndIntent(String question, String retrievedContext) {
        Map<String, Object> params = new HashMap<>();
        String qLower = question.toLowerCase(Locale.ROOT);

        // Extract GPA
        Matcher gpaMatcher = GPA_PATTERN.matcher(question);
        if (gpaMatcher.find()) {
            try {
                params.put("gpa", Double.parseDouble(gpaMatcher.group(1)));
            } catch (Exception ignored) {}
        }

        // Extract Year / Standing
        Matcher yearMatcher = YEAR_PATTERN.matcher(question);
        if (yearMatcher.find()) {
            params.put("year", yearMatcher.group(1));
        }

        // Extract sports/athletic scholarship presence
        if (qLower.contains("sport") || qLower.contains("athletic")) {
            params.put("hasSportsScholarship", true);
        }

        // Extract amount
        Matcher amountMatcher = AMOUNT_PATTERN.matcher(question);
        if (amountMatcher.find()) {
            String val = amountMatcher.group(1) != null ? amountMatcher.group(1) : amountMatcher.group(2);
            try {
                params.put("amount", Double.parseDouble(val));
            } catch (Exception ignored) {}
        }

        // Extract expense types
        if (qLower.contains("flight") || qLower.contains("airfare")) {
            params.put("expenseType", "Flight");
        } else if (qLower.contains("hotel") || qLower.contains("lodging")) {
            params.put("expenseType", "Hotel");
        } else if (qLower.contains("meal") || qLower.contains("food") || qLower.contains("dinner") || qLower.contains("lunch")) {
            params.put("expenseType", "Meal");
        } else if (qLower.contains("laptop") || qLower.contains("monitor") || qLower.contains("equipment")) {
            params.put("expenseType", "Equipment");
        }

        // Extract lead time in days
        Matcher daysMatcher = DAYS_PATTERN.matcher(question);
        if (daysMatcher.find()) {
            try {
                int num = Integer.parseInt(daysMatcher.group(1));
                if (daysMatcher.group(0).toLowerCase().contains("week")) {
                    num *= 7;
                }
                params.put("leadTimeDays", num);
            } catch (Exception ignored) {}
        }

        return params;
    }

    @Override
    public String synthesizeAnswer(String question, String retrievedContext, String decisionSummary, List<String> formattedReferences) {
        StringBuilder answer = new StringBuilder();

        if (retrievedContext == null || retrievedContext.trim().isEmpty()) {
            return "I could not find information regarding this question in the organization's uploaded knowledge base. Please consult the administrative office or verify that the relevant policy documents have been uploaded.";
        }

        // If System 1 decisions were made, highlight structured decision outcome first
        if (decisionSummary != null && !decisionSummary.isEmpty() && !decisionSummary.contains("No structured decision")) {
            answer.append("### Structured Decision Assessment\n");
            answer.append(decisionSummary).append("\n\n");
            answer.append("### Grounded Policy Details\n");
        }

        // Grounded synthesis using retrieved chunks
        String cleanContext = retrievedContext.replace("\r", " ").replace("\n", " ").replaceAll("\\s+", " ").trim();
        if (cleanContext.length() > 600) {
            cleanContext = cleanContext.substring(0, 600) + "...";
        }
        answer.append(cleanContext).append("\n\n");

        if (formattedReferences != null && !formattedReferences.isEmpty()) {
            answer.append("**References:**\n");
            for (String ref : formattedReferences) {
                answer.append("- ").append(ref).append("\n");
            }
        }

        return answer.toString().trim();
    }
}
