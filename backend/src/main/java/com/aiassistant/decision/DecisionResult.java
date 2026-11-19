package com.aiassistant.decision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecisionResult {
    private String decisionName;
    private DecisionType type;
    private Boolean passed;
    private String outcome;
    private Double score;
    private List<String> ruleBreakdown = new ArrayList<>();
    private Map<String, Object> attributes = new HashMap<>();

    public DecisionResult() {}

    public DecisionResult(String decisionName, DecisionType type, Boolean passed, String outcome, Double score) {
        this.decisionName = decisionName;
        this.type = type;
        this.passed = passed;
        this.outcome = outcome;
        this.score = score;
    }

    public void addRule(String ruleDescription) {
        this.ruleBreakdown.add(ruleDescription);
    }

    public void putAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    public String getDecisionName() { return decisionName; }
    public void setDecisionName(String decisionName) { this.decisionName = decisionName; }

    public DecisionType getType() { return type; }
    public void setType(DecisionType type) { this.type = type; }

    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public List<String> getRuleBreakdown() { return ruleBreakdown; }
    public void setRuleBreakdown(List<String> ruleBreakdown) { this.ruleBreakdown = ruleBreakdown; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
}
