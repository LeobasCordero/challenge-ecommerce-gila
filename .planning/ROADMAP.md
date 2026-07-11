# Project Roadmap

## Milestone 1: Spec, Setup, and Core Database Layer (Phases 1-2)

### Progress

| Phase | Name | Status | Plans | Date |
|-------|------|--------|-------|------|
| 1 | API Contracts & Spec | Complete | 1 | 2026-07-11 |
| 2 | DB Schema, Migrations & Core APIs | Complete | 1 | 2026-07-11 |

---

## Milestone 2: Catalog UI, Shopping Cart, and Checkout Flow (Phases 3-4)

### Progress

| Phase | Name | Status | Plans | Date |
|-------|------|--------|-------|------|
| 3 | Responsive Catalog UI, Search & Translation | Planned | — | — |
| 4 | Redis Shopping Cart & Transactional Purchase | Planned | — | — |

---

## Milestone 3: Contract Verification, CI Pipelines, and Deployment (Phases 5-6)

### Progress

| Phase | Name | Status | Plans | Date |
|-------|------|--------|-------|------|
| 5 | Pact Testing & UAT Reset Tools | Planned | — | — |
| 6 | GitHub Actions CI & Docker Compose | Planned | — | — |

---

## Phase Details

### Phase 1: API Contracts & Spec
**Goal:** Establish the API design contract first, verifying compile-time interfaces and MockMvc integration tests.
**Requirements:** R1, R13
* [x] Define root `openapi.yaml` specification detailing authentication, product catalog, CSV import, Redis shopping cart, purchase checkout, and admin reset APIs.
* [x] Configure Maven `openapi-generator-maven-plugin` using Spring Boot 3 configurations (`useSpringBoot3 = true`).
* [x] Set up base MockMvc controller tests matching OpenAPI routes.

### Phase 2: DB Schema, Migrations & Core APIs
**Goal:** Build PostgreSQL schemas, security context, and transactional backend APIs (CRUD, Kafka-driven async CSV imports, and Kafka-driven audit logs).
**Requirements:** R2, R3, R5, R7, R15, R16, R20
* [x] Set up Flyway migrations in root `db/migrations/` and Java resources, generating initial `V1__init.sql` (for tables: `users`, `products`, `orders`, `order_items`, `audit_logs`).
* [x] Implement Spring Boot repositories (with Pessimistic Write locking for inventory updates) and core Product CRUD service.
* [x] Implement stateless Spring Security with JWT in-memory/JDBC user records for Admin vs Customer roles.
* [x] Implement Kafka event publisher and background consumer for asynchronous bulk CSV imports, handling prices (stripping `$`, mapping `"free"` to `0.0`), clamping stocks, and batch inserting rows.
* [x] Configure database auditing via Kafka listeners to log events to `audit_logs`.
* [x] Write JUnit 5 parameterized tests verifying XSS, SQL Injection strings, and CSV format anomalies.

### Phase 3: Responsive Catalog UI, Search & Translation
**Goal:** Create frontend catalog interface, support multi-language, and implement responsive viewports for mobile device browsers.
**Requirements:** R4, R6, R8, R17, R18, R24
* [ ] Initialize Angular app containing Sass 7-1 directory structure (abstracts, base, components, layout, pages, themes, vendors, main.scss).
* [ ] Generate Angular HttpClient API services from `openapi.yaml`.
* [ ] Implement dynamic dynamic English and Spanish translation resource files (runtime i18n).
* [ ] Create responsive catalog layout (media queries for mobile browsers, flexible search input, category filters).
* [ ] Create Admin views (product CRUD list, csv upload modal).

### Phase 4: Redis Shopping Cart & Transactional Purchase
**Goal:** Integrate Redis for fast cart updates and process transactional database checkouts.
**Requirements:** R9, R10, R11
* [ ] Configure Redis connection pool in Spring Boot and write repository to handle cart state.
* [ ] Implement checkout controller processing Redis cart data: locks Postgres records, validates inventory stock, decrements stock, inserts order/items, purges Redis cart, and returns fake payment success.
* [ ] Build Angular shopping cart sidebar, tracking items, quantity edits, stock limits, and buy-now flow.

### Phase 5: Pact Testing & UAT Reset Tools
**Goal:** Set up consumer-driven contract tests using Pact and manual reset APIs.
**Requirements:** R12, R14
* [ ] Implement `DELETE /api/orders/clear` endpoint to truncate order details and reset inventory stock, with UI reset trigger.
* [ ] Write Pact consumer tests in Angular generating pact JSON contract files.
* [ ] Write Pact provider tests in Spring Boot verifying the Angular JSON contracts against mock DB states.

### Phase 6: GitHub Actions CI & Docker Compose
**Goal:** Deploy the multi-container stack in Docker Compose and run checks in CI pipelines.
**Requirements:** R19, R21
* [ ] Write Dockerfiles for backend and frontend, and build `docker-compose.yml` (exposing postgres, redis, kafka + kraft, backend, frontend).
* [ ] Create `.github/workflows/ci.yml` to compile project, run PMD/Checkstyle checks, ESLint/Stylelint, JUnit 5 unit/parameterized tests, and Pact verification on merge.

---
*Last updated: 2026-07-10*
