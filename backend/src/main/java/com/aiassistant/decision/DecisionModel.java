package com.aiassistant.decision;

public interface DecisionModel {
    String getName();
    DecisionType getType();
    boolean supports(DecisionContext context);
    DecisionResult evaluate(DecisionContext context);
}
