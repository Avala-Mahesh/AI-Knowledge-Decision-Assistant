package com.aiassistant.decision;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class QuestionClassificationModel implements DecisionModel {

    @Override
    public String getName() {
        return "Question Classification Model";
    }

    @Override
    public DecisionType getType() {
        return DecisionType.CLASSIFICATION;
    }

    @Override
    public boolean supports(DecisionContext context) {
        return true;
    }

    @Override
    public DecisionResult evaluate(DecisionContext context) {
        String q = context.getUserQuestion().toLowerCase(Locale.ROOT);
        String category;
        String reason;

        if (q.contains("recipe") || q.contains("weather") || q.contains("football score") || q.contains("movie") || q.contains("cook") || q.contains("bake") || q.contains("pie")) {
            category = "OUT_OF_SCOPE";
            reason = "Query requests external consumer/general knowledge not covered by organizational policies.";
        } else if (q.contains("eligible") || q.contains("qualify") || q.contains("gpa") || q.contains("can i get") ||
                   q.contains("allowed to") || q.contains("reimburse") || q.contains("limit") || q.contains("approval required")) {
            category = "POLICY_ELIGIBILITY";
            reason = "Query requests eligibility, threshold verification, or multi-condition policy evaluation.";
        } else if (q.contains("compare") || q.contains("difference between") || q.contains("versus") || q.contains("vs")) {
            category = "COMPARISON";
            reason = "Query requests structural comparison between two or more organizational policies or tiers.";
        } else if (q.contains("step") || q.contains("how to") || q.contains("procedure") || q.contains("process") || q.contains("submit") || q.contains("apply")) {
            category = "PROCEDURAL";
            reason = "Query requests a step-by-step procedural guideline or submission workflow.";
        } else {
            category = "DIRECT_INFO";
            reason = "Query requests direct factual information or specific deadline/date/contact details.";
        }

        DecisionResult result = new DecisionResult(getName(), getType(), true, category, 100.0);
        result.addRule("Classification: " + category + " — " + reason);
        result.putAttribute("intent", category);
        return result;
    }
}
