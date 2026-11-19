package com.aiassistant.service;

import com.aiassistant.entity.DocumentChunk;
import com.aiassistant.repository.DocumentChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeRetrievalService {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeRetrievalService.class);
    private static final int DEFAULT_RETRIEVAL_LIMIT = 5;

    private final DocumentChunkRepository chunkRepository;
    private final boolean isMysqlProfile;

    public KnowledgeRetrievalService(
            DocumentChunkRepository chunkRepository,
            @org.springframework.beans.factory.annotation.Value("${spring.profiles.active:mysql}") String activeProfile) {
        this.chunkRepository = chunkRepository;
        this.isMysqlProfile = activeProfile != null && activeProfile.toLowerCase().contains("mysql");
    }

    public static class ScoredChunk {
        private final DocumentChunk chunk;
        private final double score;

        public ScoredChunk(DocumentChunk chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }

        public DocumentChunk getChunk() { return chunk; }
        public double getScore() { return score; }
    }

    public List<DocumentChunk> retrieveRelevantChunks(Long organizationId, String userQuestion) {
        if (userQuestion == null || userQuestion.trim().isEmpty() || organizationId == null) {
            return Collections.emptyList();
        }

        String sanitizedQuery = prepareSearchQuery(userQuestion);

        // 1. Attempt MySQL FULLTEXT search if MySQL profile is active and query is valid
        if (isMysqlProfile && sanitizedQuery != null && !sanitizedQuery.trim().isEmpty()) {
            try {
                List<DocumentChunk> fullTextResults = chunkRepository.searchFullTextNative(organizationId, sanitizedQuery, DEFAULT_RETRIEVAL_LIMIT);
                if (fullTextResults != null && !fullTextResults.isEmpty()) {
                    logger.info("Retrieved {} chunks via MySQL FULLTEXT search for Org ID: {}", fullTextResults.size(), organizationId);
                    return fullTextResults;
                }
            } catch (Exception e) {
                logger.warn("MySQL FULLTEXT search not applicable or failed ({}). Gracefully falling back to token scoring.", e.getMessage());
            }
        }

        // 2. Resilient Keyword & Semantic Token Match Fallback (Works seamlessly on H2 & MySQL)
        return retrieveViaTokenScoring(organizationId, userQuestion, DEFAULT_RETRIEVAL_LIMIT);
    }

    private List<DocumentChunk> retrieveViaTokenScoring(Long organizationId, String question, int limit) {
        List<DocumentChunk> orgChunks = chunkRepository.findByOrganizationId(organizationId);
        if (orgChunks == null || orgChunks.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> searchTerms = extractSearchTerms(question);
        if (searchTerms.isEmpty()) {
            return Collections.emptyList();
        }

        List<ScoredChunk> scoredList = new ArrayList<>();
        for (DocumentChunk chunk : orgChunks) {
            String lowerContent = chunk.getContent().toLowerCase(Locale.ROOT);
            double score = 0.0;

            for (String term : searchTerms) {
                if (lowerContent.contains(term)) {
                    score += 1.0;
                    // Extra weight if term appears multiple times
                    int count = (lowerContent.length() - lowerContent.replace(term, "").length()) / term.length();
                    score += Math.min(count - 1, 3) * 0.5;
                }
            }

            if (score > 0) {
                scoredList.add(new ScoredChunk(chunk, score));
            }
        }

        // Sort by relevance score descending
        scoredList.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        return scoredList.stream()
                .limit(limit)
                .map(ScoredChunk::getChunk)
                .collect(Collectors.toList());
    }

    private Set<String> extractSearchTerms(String text) {
        Set<String> stopWords = Set.of(
                "what", "when", "where", "which", "who", "whom", "this", "that", "these", "those",
                "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had",
                "do", "does", "did", "the", "a", "an", "and", "but", "if", "or", "because",
                "as", "until", "while", "of", "at", "by", "for", "with", "about", "against",
                "between", "into", "through", "during", "before", "after", "above", "below",
                "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again",
                "further", "then", "once", "here", "there", "all", "any", "both", "each", "few",
                "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own",
                "same", "so", "than", "too", "very", "can", "will", "just", "should", "now", "i", "my", "me"
        );

        String[] tokens = text.toLowerCase(Locale.ROOT).replaceAll("[^a-zA-Z0-9\\s]", " ").split("\\s+");
        Set<String> terms = new HashSet<>();
        for (String t : tokens) {
            if (t.length() > 2 && !stopWords.contains(t)) {
                terms.add(t);
            }
        }
        return terms;
    }

    private String prepareSearchQuery(String text) {
        Set<String> terms = extractSearchTerms(text);
        if (terms.isEmpty()) {
            return text.replaceAll("[^a-zA-Z0-9\\s]", " ").trim();
        }
        return String.join(" ", terms);
    }
}
