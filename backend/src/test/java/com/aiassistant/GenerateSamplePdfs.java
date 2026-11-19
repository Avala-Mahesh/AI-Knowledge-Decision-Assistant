package com.aiassistant;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;

public class GenerateSamplePdfs {

    @org.junit.jupiter.api.Test
    public void generateAllPdfs() throws IOException {
        main(new String[0]);
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("java.awt.headless", "true");

        // Org 1: Apex University
        createPdf("sample-data/org1_apex_university/Apex_Scholarship_Rules_2024.pdf", new String[][]{
            {
                "APEX UNIVERSITY - MERIT SCHOLARSHIP POLICIES & GUIDELINES (2024-2025)",
                "",
                "1. OVERVIEW AND GENERAL PURPOSE",
                "Apex University provides competitive merit-based institutional scholarships to recognize",
                "undergraduate students who demonstrate exemplary academic standing, leadership, and service.",
                "",
                "2. ACADEMIC MERIT TIERS & GPA THRESHOLDS",
                "- Tier 1: Presidential Merit Award ($12,000 per academic year)",
                "  Eligibility: Requires a minimum cumulative GPA of 3.80 or higher on a 4.0 scale.",
                "  Class Standing: Open to continuing Sophomores, Juniors (3rd year), and Seniors.",
                "- Tier 2: Dean's Excellence Scholarship ($8,000 per academic year)",
                "  Eligibility: Requires a cumulative GPA between 3.50 and 3.79.",
                "  Class Standing: Open to continuing undergraduate students in all cohorts.",
                "- Tier 3: Academic Achievement Grant ($4,000 per academic year)",
                "  Eligibility: Requires a cumulative GPA between 3.20 and 3.49.",
                "",
                "3. ENROLLMENT REQUIREMENTS",
                "Recipients must be enrolled full-time, defined as a minimum of 12 credit hours per semester.",
                "Students on disciplinary or academic probation are immediately disqualified from merit awards."
            },
            {
                "APEX UNIVERSITY - SCHOLARSHIP STACKING & ATHLETIC AWARDS",
                "",
                "4. SCHOLARSHIP COMBINATIONS AND STACKING POLICY",
                "A common student inquiry regards whether institutional merit awards can be combined with other funding.",
                "",
                "- Athletic & Sports Scholarships:",
                "  Under NCAA Division Section IV regulations and University Policy 402, students holding partial",
                "  or full athletic or sports scholarships ARE PERMITTED to combine them with Academic Merit Awards,",
                "  provided that the combined total institutional aid does not exceed 100% of assessed tuition.",
                "",
                "- Departmental Award Restrictions:",
                "  Students may NOT combine two primary academic merit awards (e.g., Presidential and Dean's).",
                "  Only the higher value award will be applied to the student's tuition account.",
                "",
                "- External Private Scholarships:",
                "  External foundation awards and third-party grants may be stacked without institutional reduction."
            },
            {
                "APEX UNIVERSITY - APPLICATION DEADLINES AND PROCEDURAL STEPS",
                "",
                "5. APPLICATION DEADLINES",
                "- Fall Term Priority Deadline: July 1st annually (5:00 PM EST).",
                "- Spring Term Priority Deadline: December 1st annually (5:00 PM EST).",
                "Late submissions will only be reviewed on a funds-available basis.",
                "",
                "6. APPLICATION SUBMISSION PROCEDURE",
                "Step 1: Complete the Free Application for Federal Student Aid (FAFSA) by the priority date.",
                "Step 2: Submit the Apex Institutional Scholarship Application through the Student Portal.",
                "Step 3: Upload official transcripts showing completed semester credits.",
                "Step 4: Receive formal notification from the Financial Aid Committee within 4 weeks.",
                "",
                "7. RENEWAL CONDITIONS",
                "Scholarships are automatically renewed annually for up to 8 undergraduate semesters provided",
                "the recipient maintains the minimum required cumulative GPA and continuous enrollment."
            }
        });

        createPdf("sample-data/org1_apex_university/Apex_Academic_Admissions_Policy.pdf", new String[][]{
            {
                "APEX UNIVERSITY - UNDERGRADUATE ADMISSIONS POLICY",
                "",
                "1. GENERAL ADMISSIONS REQUIREMENTS",
                "Apex University evaluates applicants based on high school academic performance, standardized",
                "test scores (optional), letters of recommendation, and personal statements.",
                "",
                "2. MINIMUM ACADEMIC CRITERIA",
                "- High School GPA: Minimum unweighted GPA of 3.0 on a 4.0 scale.",
                "- Core Curriculum: 4 years of English, 3 years of Mathematics (including Algebra II),",
                "  3 years of Natural Sciences, and 2 years of Social Studies.",
                "",
                "3. APPLICATION CYCLES & DEADLINES",
                "- Early Action (Non-Binding): November 1st.",
                "- Regular Decision: February 1st.",
                "- Decisions released rolling starting December 15th."
            },
            {
                "APEX UNIVERSITY - TRANSFER CREDIT & STANDARDIZED TEST GUIDELINES",
                "",
                "4. TRANSFER CREDIT EVALUATION",
                "- Maximum Transfer Credits: A maximum of 60 transferable semester credits may be accepted",
                "  from accredited community colleges or 4-year institutions.",
                "- Minimum Grade: Only courses with a grade of 'C' (2.0) or higher will receive credit.",
                "",
                "5. INTERNATIONAL APPLICANTS & LANGUAGE PROFICIENCY",
                "International applicants from non-English speaking institutions must demonstrate proficiency via:",
                "- TOEFL iBT: Minimum score of 85 (no section below 20).",
                "- IELTS Academic: Minimum overall band score of 6.5."
            }
        });

        // Org 2: Nexus Corp
        createPdf("sample-data/org2_nexus_corp/Nexus_Travel_Expense_Policy.pdf", new String[][]{
            {
                "NEXUS CORP - BUSINESS TRAVEL & REIMBURSEMENT POLICY",
                "",
                "1. PURPOSE AND SCOPE",
                "This policy defines travel, entertainment, and expense reimbursement standards for all employees.",
                "",
                "2. FLIGHT BOOKING & ADVANCE RESERVATION POLICY",
                "- Domestic Airfare: All domestic airline tickets must be booked at least 14 days in advance",
                "  through the corporate travel portal (Nexus Travel Hub).",
                "- International Airfare: International travel must be booked at least 21 days in advance.",
                "- Cabin Class: Standard Economy class is mandatory for flights under 6 hours duration.",
                "  Business Class is permitted only for non-stop flights exceeding 6 hours with prior VP approval.",
                "- Late Booking Penalty: Bookings made with less than 14 days lead time require explicit",
                "  written justification and Division VP exception approval."
            },
            {
                "NEXUS CORP - EXPENSE THRESHOLDS & APPROVAL HIERARCHY",
                "",
                "3. APPROVAL HIERARCHY AND THRESHOLDS",
                "All business expenses must be approved according to the following monetary tiers:",
                "",
                "- Tier 1 (Under $500):",
                "  Eligible for automatic department manager sign-off upon submission of itemized receipt.",
                "- Tier 2 ($500.01 to $2,500.00):",
                "  Requires formal electronic approval from the Employee's Direct Department Manager.",
                "- Tier 3 (Exceeding $2,500.00):",
                "  Requires dual approval: Direct Manager PLUS Division Vice President (VP) or CFO.",
                "",
                "4. EXPENSE SUBMISSION TIMELINE",
                "Expense reports must be submitted in the Concur portal within 30 calendar days of expense incurrence."
            },
            {
                "NEXUS CORP - MEAL ALLOWANCES & NON-REIMBURSABLE EXPENSES",
                "",
                "5. PER DIEM MEAL ALLOWANCES",
                "- Standard Domestic Per Diem: $75.00 per full travel day (Breakfast: $15, Lunch: $25, Dinner: $35).",
                "- High-Cost Tier 1 Metros (NYC, SF, London, Tokyo): $100.00 per full travel day.",
                "- Receipts: Itemized tax receipts are mandatory for all meal claims exceeding $25.00.",
                "",
                "6. NON-REIMBURSABLE ITEMS",
                "- Alcoholic beverages during individual travel.",
                "- Airline seat upgrades, airline club memberships, or priority boarding fees.",
                "- Parking fines, traffic violations, or hotel pay-per-view movies.",
                "- Personal grooming, clothing, or hotel laundry for trips under 5 days."
            }
        });

        createPdf("sample-data/org2_nexus_corp/Nexus_Remote_Work_Guidelines.pdf", new String[][]{
            {
                "NEXUS CORP - REMOTE WORK & HOME OFFICE GUIDELINES",
                "",
                "1. REMOTE WORK ELIGIBILITY & SCHEDULES",
                "Full-time employees in good standing are eligible for hybrid or full remote work arrangements",
                "with direct managerial concurrence.",
                "",
                "2. CORE WORKING HOURS",
                "All remote employees must be accessible on Slack and email during mandatory core hours:",
                "10:00 AM to 4:00 PM Eastern Standard Time (EST) Monday through Thursday.",
                "",
                "3. HOME OFFICE SETUP STIPEND",
                "- One-time Equipment Allowance: Up to $1,000.00 for ergonomic desk, monitor, or chair.",
                "- Expense Code: Submit under 'Remote Equipment Stipend' within first 90 days of remote status."
            },
            {
                "NEXUS CORP - INTERNET REIMBURSEMENT & DATA SECURITY",
                "",
                "4. MONTHLY INTERNET STIPEND",
                "- Nexus Corp provides a recurring monthly internet reimbursement of $60.00 to support high-speed",
                "  broadband connections required for remote work.",
                "",
                "5. INFORMATION SECURITY COMPLIANCE",
                "- Mandatory VPN: Corporate GlobalProtect VPN must be active at all times when handling client data.",
                "- Multi-Factor Authentication (MFA): Duo Security push verification required for all corporate logins.",
                "- Device Policy: Strictly prohibited to use personal laptops or unauthorized USB drives."
            }
        });

        System.out.println("All sample PDF files generated successfully!");
    }

    private static void createPdf(String outputPath, String[][] pagesContent) throws IOException {
        File file = new File(outputPath);
        file.getParentFile().mkdirs();

        try (PDDocument doc = new PDDocument()) {
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            for (String[] pageLines : pagesContent) {
                PDPage page = new PDPage();
                doc.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    float y = 740;
                    for (int i = 0; i < pageLines.length; i++) {
                        String line = pageLines[i];
                        cs.beginText();
                        if (i == 0 || line.startsWith("1.") || line.startsWith("2.") || line.startsWith("3.") ||
                            line.startsWith("4.") || line.startsWith("5.") || line.startsWith("6.") || line.startsWith("7.")) {
                            cs.setFont(fontBold, (i == 0) ? 13 : 11);
                        } else {
                            cs.setFont(fontRegular, 10);
                        }
                        cs.newLineAtOffset(50, y);
                        cs.showText(line);
                        cs.endText();
                        y -= 18;
                    }
                }
            }

            doc.save(file);
            System.out.println("Generated: " + outputPath + " (" + pagesContent.length + " pages)");
        }
    }
}
