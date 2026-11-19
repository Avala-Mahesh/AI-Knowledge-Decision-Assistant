package com.aiassistant;

import com.aiassistant.decision.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DecisionEngineTest {

    private ChoiceDecisionModel choiceModel;
    private BooleanDecisionModel booleanModel;
    private ScoreDecisionModel scoreModel;
    private QuestionClassificationModel classificationModel;
    private CompositeDecisionEngine compositeDecisionEngine;

    @BeforeEach
    public void setup() {
        choiceModel = new ChoiceDecisionModel();
        booleanModel = new BooleanDecisionModel();
        scoreModel = new ScoreDecisionModel();
        classificationModel = new QuestionClassificationModel();
        compositeDecisionEngine = new CompositeDecisionEngine(
                List.of(choiceModel, booleanModel, scoreModel, classificationModel)
        );
    }

    @AfterEach
    public void tearDown() {
        if (compositeDecisionEngine != null) {
            compositeDecisionEngine.shutdown();
        }
    }

    @Test
    @DisplayName("Test Choice Decision: GPA Tier Mapping")
    public void testChoiceDecision() {
        DecisionContext contextTier1 = new DecisionContext("Can I get merit scholarship?", 1L, "Apex", Collections.emptyList(), Map.of("gpa", 3.9));
        DecisionResult resTier1 = choiceModel.evaluate(contextTier1);
        assertEquals("TIER_1_PRESIDENTIAL_MERIT", resTier1.getOutcome());
        assertEquals(100.0, resTier1.getScore());

        DecisionContext contextTier2 = new DecisionContext("Can I get merit scholarship?", 1L, "Apex", Collections.emptyList(), Map.of("gpa", 3.6));
        DecisionResult resTier2 = choiceModel.evaluate(contextTier2);
        assertEquals("TIER_2_DEANS_EXCELLENCE", resTier2.getOutcome());

        DecisionContext contextIneligible = new DecisionContext("Can I get merit scholarship?", 1L, "Apex", Collections.emptyList(), Map.of("gpa", 2.8));
        DecisionResult resIneligible = choiceModel.evaluate(contextIneligible);
        assertEquals("TIER_NONE_INELIGIBLE", resIneligible.getOutcome());
    }

    @Test
    @DisplayName("Test Boolean Decision: Multi-condition Student Eligibility")
    public void testBooleanDecision() {
        Map<String, Object> params = new HashMap<>();
        params.put("gpa", 3.8);
        params.put("year", "Junior");
        params.put("hasSportsScholarship", true);

        DecisionContext context = new DecisionContext("I have a 3.8 GPA and sports scholarship", 1L, "Apex", Collections.emptyList(), params);
        DecisionResult result = booleanModel.evaluate(context);

        assertTrue(result.getPassed());
        assertEquals("CONDITIONALLY_ELIGIBLE", result.getOutcome());
        assertTrue(result.getRuleBreakdown().size() >= 3);
        assertTrue(result.getRuleBreakdown().stream().anyMatch(r -> r.contains("NCAA Section IV Rule")));
    }

    @Test
    @DisplayName("Test Score Decision: Quantitative Match Rating")
    public void testScoreDecision() {
        Map<String, Object> params = Map.of("gpa", 3.9, "year", "Senior", "hasSportsScholarship", true);
        DecisionContext context = new DecisionContext("Am I eligible?", 1L, "Apex", Collections.emptyList(), params);
        DecisionResult result = scoreModel.evaluate(context);

        assertTrue(result.getPassed());
        assertTrue(result.getScore() >= 90.0, "Score should be >= 90 for senior with 3.9 GPA and athletic award");
    }

    @Test
    @DisplayName("Test Question Classification: Out-of-Scope vs Policy Query")
    public void testClassificationDecision() {
        DecisionContext outOfScopeContext = new DecisionContext("What is the recipe for chocolate chip cookies?", 1L, "Apex", Collections.emptyList(), Map.of());
        DecisionResult resOutOfScope = classificationModel.evaluate(outOfScopeContext);
        assertEquals("OUT_OF_SCOPE", resOutOfScope.getOutcome());

        DecisionContext policyContext = new DecisionContext("Am I eligible for a merit scholarship?", 1L, "Apex", Collections.emptyList(), Map.of());
        DecisionResult resPolicy = classificationModel.evaluate(policyContext);
        assertEquals("POLICY_ELIGIBILITY", resPolicy.getOutcome());
    }

    @Test
    @DisplayName("Test Composite Decision Engine: Parallel Execution and Combination")
    public void testCompositeParallelExecution() {
        Map<String, Object> params = new HashMap<>();
        params.put("gpa", 3.85);
        params.put("year", "Junior");
        params.put("hasSportsScholarship", true);

        DecisionContext context = new DecisionContext("Junior student with 3.85 GPA and sports award", 1L, "Apex", Collections.emptyList(), params);
        CompositeDecisionEngine.CombinedDecisionOutcome outcome = compositeDecisionEngine.executeDecisions(context);

        assertTrue(outcome.isExecuted());
        assertEquals("CONDITIONALLY_ELIGIBLE", outcome.getOverallOutcome());
        assertFalse(outcome.getTraces().isEmpty());
        assertTrue(outcome.getSummary().contains("TIER_1_PRESIDENTIAL_MERIT"));
    }
}
