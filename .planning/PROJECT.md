# challenge-ecommerce-gila

## Vision
An enterprise-grade e-commerce application featuring CRUD operations, CSV product import with robust validation and sanitization, search capabilities, a complete shopping cart flow, stock management (with decrementing stock), and a simulated payment process. The system is designed using Spec-Driven Development (OpenAPI YAML first, MockMvc contract validation, Pact for contract testing) and is fully packageable in Docker container images.

## Core Value
A robust, contract-validated product catalog and shopping cart system that gracefully handles dirty/malicious data imports (handling XSS, SQL injection, formatted prices/stocks) and ensures strict transaction and stock integrity.

## Target Users
* **Challenge Evaluators / Reviewers:** Testing architectural choices, robustness, security, and developer practices.
* **End Users / Customers:** Searching and purchasing products with a seamless shopping cart experience.
* **Administrators:** Performing product CRUD and CSV imports.

## Technical Context
* **Backend:** Spring Boot (Java 17+)
* **Frontend:** Angular
* **Database:** PostgreSQL (local & Docker containerized)
* **API Spec & Contracts:** OpenAPI 3.0 YAML-first contract definition
* **Testing:** 
  * MockMvc contract validation (backend controller testing)
  * Pact (consumer-driven contract tests)
  * Parameterized tests (JUnit 5 `@ParameterizedTest` for input validation edge cases)
* **Containerization:** Docker & Docker Compose (multi-container setup for Postgres, Spring Boot backend, and Angular frontend)
* **Coding Conventions:** 
  * Comments are allowed only on method signatures (JavaDoc/TSDoc style).
  * No internal comments within method/function bodies.

## Requirements

### Validated
*(None yet — ship to validate)*

### Active
- [ ] CRUD APIs & UI for Products
- [ ] Product search API & UI (by name, category, etc.)
- [ ] CSV product import API (with validation for prices, integers, SQL injection, XSS, duplicates, and missing columns)
- [ ] Shopping cart implementation (Add, update quantity, remove, clear)
- [ ] Checkout/Purchase flow (Validate stock, decrement stock, record order in Postgres, fake payment authorization)
- [ ] Admin /api/orders/clear endpoint to truncate orders and reset system for testing
- [ ] OpenAPI specification file (`openapi.yaml`) driving the backend interfaces and frontend client generation
- [ ] Full Docker & Docker Compose setup to run Postgres, Backend, and Frontend locally

### Out of Scope
- [ ] Integration with a real payment gateway (Stripe, PayPal, etc.) — simulated/mock provider is sufficient.
- [ ] Full OAuth2 production identity server (e.g. Okta) — we will use a lightweight authentication mechanism for testing.
- [ ] Multi-currency or localized tax calculation.

## Key Decisions

| Decision | Source | Rationale | Outcome |
|----------|--------|-----------|---------|
| **YAML-First OpenAPI** | User | Core Spec-Driven Development (SDD) practice to ensure backend-frontend contract alignment. | Decided |
| **Pact for Contracts** | User | Industry standard for consumer-driven contract testing. | Decided |
| **MockMvc Contract Testing** | User | Ensures backend controller endpoints conform strictly to the defined contract interfaces. | Decided |
| **Stock is strictly Int** | User | Keeps stock representation numeric, parsing/cleaning any dirty string representation in the CSV. | Decided |
| **Decrement Stock & Persist Orders** | User | Standard transactional integrity for e-commerce purchases. | Decided |
| **Order Clear Endpoint** | User | Crucial utility for manual testing, allowing reviewers to reset the application state. | Decided |
| **Method Signature Comments Only** | User | Code challenge rule to verify code readability and self-documenting implementation. | Decided |
| **Lightweight Auth/Roles** | User / AI | User wants to consider authentication/roles. We need to decide on the best lightweight open-source options (detailed in the next conversation step). | Pending Choice |

---
*Last updated: 2026-07-10 after initialization*
