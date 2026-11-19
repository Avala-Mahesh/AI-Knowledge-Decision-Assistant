package com.aiassistant.decision;

import org.springframework.stereotype.Component;

@Component
public class ScoreDecisionModel implements DecisionModel {

    @Override
    public String getName() {
        return "Eligibility & Confidence Score Model";
    }

    @Override
    public DecisionType getType() {
        return DecisionType.SCORE;
    }

    @Override
    public boolean supports(DecisionContext context) {
        return context.getDoubleParam("gpa") != null ||
               context.getDoubleParam("amount") != null ||
               context.getIntParam("leadTimeDays") != null;
    }

    @Override
    public DecisionResult evaluate(DecisionContext context) {
        Double gpa = context.getDoubleParam("gpa");
        String year = context.getStringParam("year");
        Boolean hasSports = context.getBooleanParam("hasSportsScholarship");

        Double amount = context.getDoubleParam("amount");
        Integer leadTimeDays = context.getIntParam("leadTimeDays");

        // University Context: Merit Award Confidence Score (0-100)
        if (gpa != null) {
            double score = 0.0;
            // GPA component: up to 50 points (scaled from 3.0 = 20 pts to 4.0 = 50 pts)
            if (gpa >= 4.0) {
                score += 50.0;
            } else if (gpa >= 3.0) {
                score += 20.0 + ((gpa - 3.0) / 1.0) * 30.0;
            }

            // Academic Standing component: up to 30 points
            if (year != null) {
                String y = year.toLowerCase();
                if (y.contains("junior") || y.contains("3rd") || y.contains("senior") || y.contains("4th")) {
                    score += 30.0;
                } else if (y.contains("sophomore") || y.contains("2nd")) {
                    score += 20.0;
                } else {
                    score += 15.0;
                }
            } else {
                score += 15.0; // default average
            }

            // Extracurricular / Athletic Co-curricular component: up to 20 points
            if (Boolean.TRUE.equals(hasSports)) {
                score += 20.0; // strong profile
            } else {
                score += 10.0;
            }

            double roundedScore = Math.round(score * 10.0) / 10.0;
            String rating;
            if (roundedScore >= 85.0) {
                rating = "HIGH_CONFIDENCE_ELIGIBILITY (" + roundedScore + "/100)";
            } else if (roundedScore >= 70.0) {
                rating = "MODERATE_CONFIDENCE_ELIGIBILITY (" + roundedScore + "/100)";
            } else {
                rating = "LOW_CONFIDENCE_BORDERLINE (" + roundedScore + "/100)";
            }

            DecisionResult result = new DecisionResult(getName(), getType(), roundedScore >= 70.0, rating, roundedScore);
            result.addRule("Quantitative Match Score: " + roundedScore + "/100 based on GPA (" + gpa + "), Academic Year (" + (year != null ? year : "N/A") + "), and Co-curricular Standing.");
            result.putAttribute("numericalScore", roundedScore);
            return result;
        }

        // Corporate Context: Expense Compliance Score (0-100)
        if (amount != null || leadTimeDays != null) {
            double score = 100.0;
            if (leadTimeDays != null && leadTimeDays < 14) {
                score -= 30.0; // penalty for late booking
            }
            if (amount != null && amount > 2500.0) {
                score -= 25.0; // higher risk/approval required
            } else if (amount != null && amount > 500.0) {
                score -= 10.0;
            }

            double roundedScore = Math.max(0, Math.min(100, Math.round(score * 10.0) / 10.0));
            String rating = "COMPLIANCE_SCORE (" + roundedScore + "/100)";

            DecisionResult result = new DecisionResult(getName(), getType(), roundedScore >= 70.0, rating, roundedScore);
            result.addRule("Quantitative Policy Compliance Score: " + roundedScore + "/100 based on lead time and reimbursement amount.");
            result.putAttribute("numericalScore", roundedScore);
            return result;
        }

        DecisionResult defaultResult = new DecisionResult(getName(), getType(), true, "STANDARD_SCORE (100/100)", 100.0);
        defaultResult.addRule("Standard baseline policy scoring applied.");
        return defaultResult;
    }
}
