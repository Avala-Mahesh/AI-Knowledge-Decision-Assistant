package com.aiassistant.dto;

import java.util.List;
import java.util.Map;

public class DecisionTrace {
    private String decisionType; // "CHOICE", "BOOLEAN", "SCORE", "CLASSIFICATION"
    private String decisionName;
    private String outcome; // e.g., "ELIGIBLE", "TIER_1_MERIT", "92/100"
    private Double score;
    private Boolean passed;
    private Map<String, Object> details;
    private List<String> ruleBreakdown;

    public DecisionTrace() {}

    public DecisionTrace(String decisionType, String decisionName, String outcome, Double score, Boolean passed, Map<String, Object> details, List<String> ruleBreakdown) {
        this.decisionType = decisionType;
        this.decisionName = decisionName;
        this.outcome = outcome;
        this.score = score;
        this.passed = passed;
        this.details = details;
        this.ruleBreakdown = ruleBreakdown;
    }

    public String getDecisionType() { return decisionType; }
    public void setDecisionType(String decisionType) { this.decisionType = decisionType; }

    public String getDecisionName() { return decisionName; }
    public void setDecisionName(String decisionName) { this.decisionName = decisionName; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }

    public List<String> getRuleBreakdown() { return ruleBreakdown; }
    public void setRuleBreakdown(List<String> ruleBreakdown) { this.ruleBreakdown = ruleBreakdown; }
}
