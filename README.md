# AI Knowledge & Decision Assistant (Multi-Tenant SaaS)

A production-grade, multi-tenant AI Knowledge & Decision Assistant SaaS MVP built with **Spring Boot 3 (Java 17)**, **React.js (Bootstrap 5)**, **Apache PDFBox**, **MySQL FULLTEXT Search**, and a **System 1 Deterministic Decision Engine** orchestrated with Generative LLMs.

---

## 🌟 Executive Summary & Core Objective

The primary architectural objective of this platform is to **demonstrate that Large Language Models (LLMs) and specialized System 1 decision models work together**, rather than treating every user question as a plain text-generation task.

```
Visitor Question
       │
       ▼
[Organization Token Validation & Identification]
       │
       ▼
[MySQL FULLTEXT Knowledge Retrieval (Tenant Scoped)]
       │
       ▼
[Question Understanding & NLP Decomposition]
       │
       ▼
[System 1 Deterministic Decision Engine (Parallel Execution)]
 ├─ Choice Model       ──> Policy Category / Award Tier
 ├─ Boolean Model      ──> Multi-condition Criteria Evaluation
 ├─ Score Model        ──> Quantitative Match & Confidence Rating (0-100)
 └─ Classification     ──> Question Intent & Out-of-Scope Detection
       │
       ▼
[Application Logic Decision Aggregator]
       │
       ▼
[LLM Grounded Answer Generation with Source Citations]
       │
       ▼
Answer + Precise Document & Page References ([Document.pdf — Page X])
```

---

## 🏗️ Architecture & Technology Stack

| Layer | Technology | Details |
|---|---|---|
| **Frontend** | React 18, Axios, Bootstrap 5, Bootstrap Icons | Responsive Admin Dashboard, Document Manager, Embed Code Generator, Visitor Chat Interface |
| **Backend** | Java 17, Spring Boot 3.2.5, Spring Data JPA | Stateless REST APIs, Asynchronous Parallel Decision Execution (`CompletableFuture`) |
| **Authentication** | Spring Security + JWT (HS256) | Role-based token auth for Admins; Stateless public token validation for Visitors |
| **Database** | MySQL 8.0 (InnoDB FULLTEXT Indexing) / H2 in-memory | Multi-tenant schema with document chunking and usage analytics logging |
| **PDF Extraction** | Apache PDFBox 3.0.2 | Text extraction, page number attribution, paragraph chunking |
| **Knowledge Retrieval**| MySQL FULLTEXT Search (`MATCH...AGAINST`) | Strict tenant-isolated vector/keyword queries with page tracking |
| **Decision Engine** | Java-based System 1 Deterministic Models | Choice, Boolean, Score, and Classification models evaluated concurrently |
| **LLM Layer** | Hybrid Orchestration (Ollama / OpenAI / Deterministic Engine)| Parameter decomposition and grounded answer synthesis |
| **API Docs** | Springdoc OpenAPI 3 / Swagger UI | Interactive API documentation at `/swagger-ui/index.html` |
| **Testing** | JUnit 5, Mockito, In-memory doubles | 15 automated test cases covering isolation, limits, and decision rules |
| **Deployment** | Docker & Docker Compose | Multi-container setup for MySQL, Spring Boot Backend, and Nginx Frontend |

---

## 🏛️ Multi-Tenant Architecture & Security Isolation

Multi-tenant isolation is enforced across all architectural boundaries:

```
+-----------------------------------------------------------------------------------+
| Multi-Tenant Storage & Access Isolation                                           |
+-----------------------------------------------------------------------------------+
|  Organization A (Apex University)      |  Organization B (Nexus Corp)             |
|  - Assistant Token: ast_apex_...       |  - Assistant Token: ast_nexus_...        |
|  - Documents: Apex_Scholarship.pdf     |  - Documents: Nexus_Travel_Policy.pdf    |
|  - Chunks: organization_id = 101       |  - Chunks: organization_id = 202         |
|  - Assistant A CANNOT see Org B docs   |  - Assistant B CANNOT see Org A docs     |
+-----------------------------------------------------------------------------------+
```

1. **Database-Level Isolation**: Every document and text chunk contains a mandatory `organization_id` column. All retrieval queries strictly bind `WHERE organization_id = :orgId`.
2. **Assistant Token Separation**: Public visitor requests pass an opaque `assistantToken` (e.g. `ast_...`). The backend validates this token server-side and scopes knowledge retrieval strictly to the token owner.
3. **No Credential Leakage**: The assistant token allows asking questions only; it grants zero access to administrative dashboards, document deletion, or database internals.
4. **Administrative Protection**: All management endpoints (`/api/v1/documents/**`, `/api/v1/assistant/**`, `/api/v1/dashboard/**`) require a signed JWT bearer token.

---

## 🧠 System 1 Decision Engine (Section 6, 7 & 8)

Rather than passing raw text into an LLM prompt to let the model guess policy rules, our pipeline decomposes questions and runs **deterministic, mathematical, and rule-based Java models** in parallel:

### 1. Choice Decision Model
* **Purpose**: Selects the policy category or tier applicable to the student or employee.
* **Example**: Maps GPA to award tiers:
  - $\text{GPA} \ge 3.80 \longrightarrow$ `TIER_1_PRESIDENTIAL_MERIT` (\$12,000/yr)
  - $3.50 \le \text{GPA} < 3.80 \longrightarrow$ `TIER_2_DEANS_EXCELLENCE` (\$8,000/yr)
  - $3.20 \le \text{GPA} < 3.50 \longrightarrow$ `TIER_3_ACHIEVEMENT` (\$4,000/yr)

### 2. Boolean Decision Model (Multi-Condition Evaluation)
* **Purpose**: Evaluates independent logical conditions:
  - $\text{Condition 1 (GPA): } \text{GPA} \ge 3.50 \longrightarrow \text{PASS}$
  - $\text{Condition 2 (Standing): } \text{Year} \in \{\text{Sophomore}, \text{Junior}, \text{Senior}\} \longrightarrow \text{PASS}$
  - $\text{Condition 3 (Award Stacking): } \text{Athletic Scholarship} + \text{Merit} \le 100\% \text{ Tuition} \longrightarrow \text{PASS (CONDITIONAL)}$
  - $\text{Condition 4 (Disciplinary): } \text{onProbation} == \text{false} \longrightarrow \text{PASS}$

### 3. Score Decision Model
* **Purpose**: Computes an objective confidence score ($0 - 100$ points) based on weighted factors:
  $$\text{Score} = \text{GPA Factor (up to 50 pts)} + \text{Academic Standing (up to 30 pts)} + \text{Co-curricular Factor (up to 20 pts)}$$
  - $\text{Score} \ge 85 \longrightarrow \text{HIGH\_CONFIDENCE\_ELIGIBILITY}$
  - $70 \le \text{Score} < 85 \longrightarrow \text{MODERATE\_CONFIDENCE\_ELIGIBILITY}$

### 4. Classification Decision Model
* **Purpose**: Categorizes query intent into `DIRECT_INFO`, `POLICY_ELIGIBILITY`, `PROCEDURAL`, `COMPARISON`, or `OUT_OF_SCOPE`.

### 5. Parallel Execution
Independent decision models run concurrently using Java's `CompletableFuture.supplyAsync()` across a dedicated daemon thread pool, returning combined decision summaries in under **10 milliseconds**.

---

## 📋 Mandatory Limits & Validation (Section 3)

| Rule | Limit | Enforcement & Error Handling |
|---|---|---|
| **Documents per Organization** | Max 10 PDFs | Validated in `DocumentService.java`. Throws `DocumentLimitExceededException` (HTTP 400). |
| **Pages per PDF** | Max 20 pages | Validated via PDFBox `document.getNumberOfPages()`. Throws `InvalidDocumentException` (HTTP 400). |
| **File Size per PDF** | Max 10 MB | Validated on client and server (`file.getSize() > 10MB`). Throws `DocumentLimitExceededException` (HTTP 400). |
| **File Format** | PDF only (`.pdf`) | Validates extension and PDF magic bytes (`%PDF-`). Throws `InvalidDocumentException` (HTTP 400). |

---

## 🚀 Quick Start & Installation

### Prerequisites
* **Java 17 JDK** (or Eclipse Temurin 17)
* **Node.js 18+** & npm
* **MySQL 8.0+** (or use Docker Compose)
* **Maven 3.9+** (bundled in `tools/maven`)

### 1. Run with Docker Compose (Recommended)

```bash
# Start MySQL, Backend, and Frontend containers
docker-compose up -d --build
```
* Frontend Dashboard: `http://localhost:3000`
* Backend API & Swagger: `http://localhost:8080/swagger-ui/index.html`

---

### 2. Run Locally from Source

#### Step A: Start the Backend (Spring Boot 3)
```bash
cd backend

# Option 1: Run with in-memory H2 profile (Instant zero-setup startup)
..\tools\maven\bin\mvn.cmd spring-boot:run -Dspring-boot.run.profiles=h2

# Option 2: Run with MySQL (ensure MySQL running on localhost:3306)
..\tools\maven\bin\mvn.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```
The backend starts on `http://localhost:8080`.
OpenAPI documentation is live at `http://localhost:8080/swagger-ui/index.html`.

#### Step B: Start the Frontend (React.js)
```bash
cd frontend
npm start
```
The frontend starts on `http://localhost:3000`.

---

## 🧪 Automated Test Suite (JUnit 5 & Mockito)

Run the full suite of unit and integration tests:

```bash
cd backend
..\tools\maven\bin\mvn.cmd test
```

### Test Coverage Highlights:
* `MultiTenantIsolationTest`: Verifies that Tenant A cannot list or delete Tenant B's documents, and Assistant Token A strictly rejects answering questions about Tenant B's policies.
* `DecisionEngineTest`: Verifies Choice model tier selection, Boolean multi-condition evaluation, Score confidence calculation, and parallel execution.
* `DocumentValidationTest`: Verifies mandatory document limits (max 10 documents, max 20 pages, max 10 MB, and non-PDF rejection).
* `QuestionPipelineTest`: Verifies end-to-end question processing, citation formatting (`Document.pdf — Page X`), and out-of-scope response generation.

---

## 📂 Sample Test Data & Questions (Section 18)

Sample PDF files are located in `sample-data/`:

| Organization | Sample PDF Documents | Focus Area |
|---|---|---|
| **Org 1: Apex University** | `Apex_Scholarship_Rules_2024.pdf` (3 pages)<br>`Apex_Academic_Admissions_Policy.pdf` (2 pages) | Academic Merit Scholarships, GPA Thresholds, Stacking with Athletic Awards |
| **Org 2: Nexus Corp** | `Nexus_Travel_Expense_Policy.pdf` (3 pages)<br>`Nexus_Remote_Work_Guidelines.pdf` (2 pages) | Flight Booking Lead Times, Manager Approval Tiers, Remote Equipment Allowances |

### 10+ Test Questions Covering All Question Types:

| ID | Organization | Type | Question | Expected Pipeline Behavior |
|---|---|---|---|---|
| 1 | Apex University | **Direct Information** | *"What is the priority application deadline for fall term merit scholarships?"* | Returns July 1st annually with reference `[Apex_Scholarship_Rules_2024.pdf — Page 3]`. |
| 2 | Nexus Corp | **Direct Information** | *"What is the monthly internet reimbursement allowance for remote employees?"* | Returns \$60.00/month with reference `[Nexus_Remote_Work_Guidelines.pdf — Page 2]`. |
| 3 | Apex University | **Policy** | *"Can academic merit awards be combined with athletic or sports scholarships?"* | Grounded in `Page 2`. Explains NCAA Section IV rule permitting stacking up to 100% tuition. |
| 4 | Nexus Corp | **Policy** | *"What is the corporate policy regarding economy class versus business class flights?"* | Grounded in `Page 1`. Explains Economy mandatory under 6 hrs; Business permitted over 6 hrs with VP approval. |
| 5 | Apex University | **Procedural** | *"What are the step-by-step procedures to apply for an institutional scholarship?"* | Details Steps 1 to 4 from `Page 3`. |
| 6 | Nexus Corp | **Procedural** | *"Within how many calendar days must an employee submit travel expenses?"* | Grounded in `Page 2`. Explains 30 calendar days limit in Concur. |
| 7 | Apex University | **Multi-Condition Decision** | *"I am a 3rd year student with a 3.85 GPA and an existing athletic scholarship. Am I eligible for the Presidential Merit Award?"* | **Triggers System 1 Engine**: Decomposes GPA 3.85, Junior year, athletic award. Choice model assigns Tier 1 (\$12,000); Boolean model evaluates GPA $\ge 3.8$ (PASS), Standing (PASS), NCAA Stacking (PASS CONDITIONAL); Score model returns 95/100 HIGH CONFIDENCE. |
| 8 | Nexus Corp | **Multi-Condition Decision** | *"I have an expense for a flight of \$1,200 booked 18 days in advance. Is this compliant and who must approve it?"* | **Triggers System 1 Engine**: Evaluates lead time $\ge 14$ days (PASS), maps \$1,200 to Tier 2 (Manager Approval Required), Score 90/100. |
| 9 | Apex University | **Comparison** | *"How does the Presidential Merit Award compare to the Dean's Excellence Scholarship in GPA requirements and award amounts?"* | Compares Tier 1 (\$12k, GPA $\ge 3.8$) vs Tier 2 (\$8k, GPA 3.50-3.79). |
| 10| Nexus Corp | **Comparison** | *"What is the difference between standard domestic per diem meal allowance versus high-cost metro areas?"* | Compares standard (\$75/day) vs high-cost metros (\$100/day). |
| 11| Apex University | **Out-of-Scope** | *"What is the recipe for chocolate chip cookies?"* | Classified as `OUT_OF_SCOPE`. System confirms KB does not contain external recipes and does not invent facts. |
| 12| Apex University | **Tenant Isolation** | *"What is the advance booking rule for corporate domestic flights?"* | Asked to Apex Assistant about Nexus Corp policy. Confirms Apex KB has no record of corporate flight policies. |

---

## 🌐 Embed Mechanisms (Section 3 & 15)

### Option 1: Responsive iframe Embed
```html
<iframe
  src="http://localhost:3000/chat/<ASSISTANT_TOKEN>"
  width="100%"
  height="600"
  frameborder="0"
  style="border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.1);">
</iframe>
```

### Option 2: Floating Chat Bubble Widget (Script Tag)
```html
<script
  src="http://localhost:3000/embed.js"
  data-assistant-token="<ASSISTANT_TOKEN>">
</script>
```

To see both embed options live, open `embed-demo.html` in your web browser.

---

## 📮 Postman Collection

Import `postman/AI_Assistant_Collection.postman_collection.json` into Postman to execute end-to-end API workflows with automated token passing.

---

## 🔒 Major API Endpoints

### Authentication & Account
* `POST /api/v1/auth/register`: Create organization and admin account
* `POST /api/v1/auth/login`: Authenticate and receive JWT token
* `GET /api/v1/auth/me`: Get current admin details

### Knowledge Base Management (Authenticated)
* `POST /api/v1/documents/upload`: Upload PDF (Max 10 docs, 20 pages, 10MB)
* `GET /api/v1/documents`: List uploaded documents for organization
* `DELETE /api/v1/documents/{id}`: Delete document and all associated chunks

### Assistant & Analytics (Authenticated)
* `GET /api/v1/assistant`: Get assistant URL, token, and embed snippets
* `POST /api/v1/assistant/regenerate-token`: Regenerate assistant security token
* `GET /api/v1/dashboard/stats`: Retrieve usage counts and knowledge base metrics
* `GET /api/v1/dashboard/recent-questions`: Retrieve recent question logs and decision traces

### Public Visitor Assistant (No Auth Required)
* `POST /api/v1/public/ask`: Ask natural-language question using assistant token
* `GET /api/v1/public/assistant-info/{token}`: Retrieve assistant metadata and organization name
