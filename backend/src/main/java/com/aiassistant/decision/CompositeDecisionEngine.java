package com.aiassistant.decision;

import com.aiassistant.dto.DecisionTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class CompositeDecisionEngine {

    private static final Logger logger = LoggerFactory.getLogger(CompositeDecisionEngine.class);

    private final List<DecisionModel> decisionModels;
    private final ExecutorService executor = Executors.newFixedThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable, "decision-worker");
        thread.setDaemon(true);
        return thread;
    });

    public CompositeDecisionEngine(List<DecisionModel> decisionModels) {
        this.decisionModels = decisionModels;
    }

    @jakarta.annotation.PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }

    public static class CombinedDecisionOutcome {
        private final boolean executed;
        private final String overallOutcome;
        private final String summary;
        private final List<DecisionTrace> traces;

        public CombinedDecisionOutcome(boolean executed, String overallOutcome, String summary, List<DecisionTrace> traces) {
            this.executed = executed;
            this.overallOutcome = overallOutcome;
            this.summary = summary;
            this.traces = traces;
        }

        public boolean isExecuted() { return executed; }
        public String getOverallOutcome() { return overallOutcome; }
        public String getSummary() { return summary; }
        public List<DecisionTrace> getTraces() { return traces; }
    }

    public CombinedDecisionOutcome executeDecisions(DecisionContext context) {
        // Find matching models
        List<DecisionModel> applicableModels = decisionModels.stream()
                .filter(m -> m.supports(context))
                .collect(Collectors.toList());

        if (applicableModels.isEmpty()) {
            return new CombinedDecisionOutcome(false, "NONE", "No structured decision required", new ArrayList<>());
        }

        long startTime = System.currentTimeMillis();
        logger.info("Executing {} System 1 decision models in parallel...", applicableModels.size());

        // Execute in parallel using CompletableFuture
        List<CompletableFuture<DecisionResult>> futures = applicableModels.stream()
                .map(model -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return model.evaluate(context);
                    } catch (Exception e) {
                        logger.error("Error executing model {}: {}", model.getName(), e.getMessage());
                        DecisionResult err = new DecisionResult(model.getName(), model.getType(), false, "ERROR", 0.0);
                        err.addRule("Model evaluation error: " + e.getMessage());
                        return err;
                    }
                }, executor))
                .collect(Collectors.toList());

        // Await all results
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<DecisionResult> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        long elapsed = System.currentTimeMillis() - startTime;
        logger.info("Completed parallel System 1 execution in {} ms", elapsed);

        // Combine decision results through application logic
        List<DecisionTrace> traces = new ArrayList<>();
        StringBuilder summaryBuilder = new StringBuilder();
        boolean allPassed = true;
        boolean hasConditional = false;
        String finalOutcome = "ELIGIBLE";

        for (DecisionResult res : results) {
            DecisionTrace trace = new DecisionTrace(
                    res.getType().name(),
                    res.getDecisionName(),
                    res.getOutcome(),
                    res.getScore(),
                    res.getPassed(),
                    res.getAttributes(),
                    res.getRuleBreakdown()
            );
            traces.add(trace);

            if (Boolean.FALSE.equals(res.getPassed())) {
                allPassed = false;
            }
            if (res.getOutcome() != null && res.getOutcome().contains("CONDITIONAL")) {
                hasConditional = true;
            }

            summaryBuilder.append("[").append(res.getType().name()).append("] ")
                    .append(res.getDecisionName()).append(" => ").append(res.getOutcome())
                    .append(" (").append(String.join("; ", res.getRuleBreakdown())).append("). ");
        }

        if (!allPassed) {
            finalOutcome = "INELIGIBLE";
        } else if (hasConditional) {
            finalOutcome = "CONDITIONALLY_ELIGIBLE";
        }

        return new CombinedDecisionOutcome(true, finalOutcome, summaryBuilder.toString().trim(), traces);
    }
}
