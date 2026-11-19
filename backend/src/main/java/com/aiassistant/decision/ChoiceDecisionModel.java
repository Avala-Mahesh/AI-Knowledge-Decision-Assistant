package com.aiassistant.decision;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ChoiceDecisionModel implements DecisionModel {

    @Override
    public String getName() {
        return "Policy Category Choice Model";
    }

    @Override
    public DecisionType getType() {
        return DecisionType.CHOICE;
    }

    @Override
    public boolean supports(DecisionContext context) {
        // Active when parameters contain GPA or expense amounts or travel tiers
        return context.getDoubleParam("gpa") != null ||
               context.getDoubleParam("amount") != null ||
               context.getStringParam("expenseType") != null ||
               context.getStringParam("policyTier") != null;
    }

    @Override
    public DecisionResult evaluate(DecisionContext context) {
        Double gpa = context.getDoubleParam("gpa");
        Double amount = context.getDoubleParam("amount");
        String expenseType = context.getStringParam("expenseType");

        // University Context: Scholarship Policy Tier Selection
        if (gpa != null) {
            String selectedTier;
            double score;
            String desc;

            if (gpa >= 3.8) {
                selectedTier = "TIER_1_PRESIDENTIAL_MERIT";
                score = 100.0;
                desc = "GPA " + gpa + " qualifies for Tier 1: Presidential Merit Award ($12,000/year).";
            } else if (gpa >= 3.5) {
                selectedTier = "TIER_2_DEANS_EXCELLENCE";
                score = 80.0;
                desc = "GPA " + gpa + " qualifies for Tier 2: Dean's Excellence Scholarship ($8,000/year).";
            } else if (gpa >= 3.2) {
                selectedTier = "TIER_3_ACHIEVEMENT";
                score = 60.0;
                desc = "GPA " + gpa + " qualifies for Tier 3: Academic Achievement Grant ($4,000/year).";
            } else {
                selectedTier = "TIER_NONE_INELIGIBLE";
                score = 0.0;
                desc = "GPA " + gpa + " is below minimum 3.2 threshold for merit scholarship categories.";
            }

            DecisionResult result = new DecisionResult(getName(), getType(), !selectedTier.equals("TIER_NONE_INELIGIBLE"), selectedTier, score);
            result.addRule(desc);
            result.putAttribute("category", selectedTier);
            return result;
        }

        // Corporate Context: Expense Category Choice
        if (amount != null || expenseType != null) {
            String tier;
            double score = 100.0;
            String desc;

            double expAmount = (amount != null) ? amount : 0.0;
            String type = (expenseType != null) ? expenseType.toLowerCase(Locale.ROOT) : "general";

            if (expAmount > 2500.0 || type.contains("international") || type.contains("flight")) {
                tier = "EXECUTIVE_OR_VP_APPROVAL_TIER";
                desc = "Expense category requires VP approval (Amount: $" + expAmount + ", Type: " + type + ").";
            } else if (expAmount > 500.0) {
                tier = "STANDARD_MANAGER_APPROVAL_TIER";
                desc = "Expense category requires Direct Manager approval (Amount: $" + expAmount + ").";
            } else {
                tier = "AUTO_APPROVED_EXPENSE_TIER";
                desc = "Expense category eligible for automatic department sign-off (Under $500 threshold).";
            }

            DecisionResult result = new DecisionResult(getName(), getType(), true, tier, score);
            result.addRule(desc);
            result.putAttribute("category", tier);
            return result;
        }

        DecisionResult fallback = new DecisionResult(getName(), getType(), true, "GENERAL_POLICY", 50.0);
        fallback.addRule("Default general policy category applied.");
        return fallback;
    }
}
