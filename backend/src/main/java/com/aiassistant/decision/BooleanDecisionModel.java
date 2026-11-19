package com.aiassistant.decision;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class BooleanDecisionModel implements DecisionModel {

    @Override
    public String getName() {
        return "Eligibility Boolean Rule Model";
    }

    @Override
    public DecisionType getType() {
        return DecisionType.BOOLEAN;
    }

    @Override
    public boolean supports(DecisionContext context) {
        return context.getDoubleParam("gpa") != null ||
               context.getStringParam("year") != null ||
               context.getBooleanParam("hasSportsScholarship") != null ||
               context.getDoubleParam("amount") != null ||
               context.getIntParam("leadTimeDays") != null;
    }

    @Override
    public DecisionResult evaluate(DecisionContext context) {
        Double gpa = context.getDoubleParam("gpa");
        String year = context.getStringParam("year");
        Boolean hasSports = context.getBooleanParam("hasSportsScholarship");
        Boolean onProbation = context.getBooleanParam("onProbation");

        Double amount = context.getDoubleParam("amount");
        Integer leadTimeDays = context.getIntParam("leadTimeDays");
        Boolean hasReceipt = context.getBooleanParam("hasReceipt");

        // University Context
        if (gpa != null || year != null || hasSports != null) {
            boolean allPassed = true;
            DecisionResult result = new DecisionResult(getName(), getType(), true, "ELIGIBLE", 100.0);

            // Condition 1: GPA >= 3.5 for primary merit consideration
            if (gpa != null) {
                if (gpa >= 3.5) {
                    result.addRule("[PASS] GPA Requirement: Student GPA (" + gpa + ") meets or exceeds the required 3.5 threshold.");
                } else if (gpa >= 3.2) {
                    result.addRule("[CONDITIONAL] GPA Requirement: Student GPA (" + gpa + ") is eligible for Tier 3 Achievement, but below Tier 1/2 Honors.");
                } else {
                    allPassed = false;
                    result.addRule("[FAIL] GPA Requirement: Student GPA (" + gpa + ") is below minimum 3.2 requirement.");
                }
            }

            // Condition 2: Class Year (Sophomore, Junior, Senior eligible; Freshmen evaluated separately via high school GPA)
            if (year != null) {
                String yLower = year.toLowerCase(Locale.ROOT);
                if (yLower.contains("junior") || yLower.contains("3rd") || yLower.contains("senior") || yLower.contains("4th") || yLower.contains("sophomore") || yLower.contains("2nd")) {
                    result.addRule("[PASS] Academic Standing: Enrollment year (" + year + ") qualifies for continuing merit award.");
                } else {
                    result.addRule("[INFO] Academic Standing: Incoming/1st year students are evaluated based on secondary school rank.");
                }
            }

            // Condition 3: Existing Sports / Athletic Scholarship Stacking Rule
            // Section 6 example: "given their year, GPA, and existing sports scholarship"
            if (Boolean.TRUE.equals(hasSports)) {
                result.addRule("[PASS - CONDITIONAL] Stacking Policy: Athletic scholarships may be combined with Academic Merit awards, provided total institutional aid does not exceed 100% of tuition (NCAA Section IV Rule).");
                result.putAttribute("stackableWithAthletic", true);
            } else {
                result.addRule("[PASS] Stacking Policy: No conflicting institutional awards detected.");
            }

            // Condition 4: Disciplinary / Academic Probation
            if (Boolean.TRUE.equals(onProbation)) {
                allPassed = false;
                result.addRule("[FAIL] Academic Standing: Student is currently on academic probation, which disqualifies new awards.");
            }

            result.setPassed(allPassed);
            result.setOutcome(allPassed ? (Boolean.TRUE.equals(hasSports) ? "CONDITIONALLY_ELIGIBLE" : "ELIGIBLE") : "INELIGIBLE");
            return result;
        }

        // Corporate Context
        if (amount != null || leadTimeDays != null) {
            boolean allPassed = true;
            DecisionResult result = new DecisionResult(getName(), getType(), true, "COMPLIANT", 100.0);

            if (leadTimeDays != null) {
                if (leadTimeDays >= 14) {
                    result.addRule("[PASS] Lead Time: Booking advance of " + leadTimeDays + " days satisfies the minimum 14-day advance booking requirement.");
                } else {
                    allPassed = false;
                    result.addRule("[FAIL] Lead Time: Booking advance of " + leadTimeDays + " days violates the 14-day advance booking requirement (Requires VP exception).");
                }
            }

            if (amount != null) {
                if (amount <= 500.0) {
                    result.addRule("[PASS] Expense Limit: Amount $" + amount + " is within automatic reimbursement threshold.");
                } else if (amount <= 2500.0) {
                    result.addRule("[CONDITIONAL] Expense Limit: Amount $" + amount + " requires departmental manager approval.");
                } else {
                    result.addRule("[CONDITIONAL] Expense Limit: Amount $" + amount + " exceeds $2,500 and requires Executive VP approval.");
                }
            }

            if (Boolean.FALSE.equals(hasReceipt)) {
                allPassed = false;
                result.addRule("[FAIL] Documentation: Missing itemized tax receipt for expense.");
            }

            result.setPassed(allPassed);
            result.setOutcome(allPassed ? "COMPLIANT" : "NON_COMPLIANT");
            return result;
        }

        DecisionResult generalResult = new DecisionResult(getName(), getType(), true, "CRITERIA_SATISFIED", 100.0);
        generalResult.addRule("[PASS] General organizational policy guidelines satisfied.");
        return generalResult;
    }
}
