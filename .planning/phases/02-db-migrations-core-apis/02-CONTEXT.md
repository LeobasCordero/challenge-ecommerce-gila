# Phase 2: DB Schema, Migrations & Core APIs - Context

**Gathered:** 2026-07-11
**Status:** Ready for planning

## Phase Boundary
Build PostgreSQL database schemas using Flyway migrations, set up Spring Security with JWT authentication/authorization, implement core Product CRUD APIs and transactional checkouts (using pessimistic write locks), configure sequential Kafka-driven CSV product imports with field sanitization and duplicate ignoring, and configure Kafka-driven database audit logging alongside parameterized security tests.

## Implementation Decisions

### Database Schema & Flyway
- **DECIDED:** Migration files will reside in the standard Maven resources directory ([src/main/resources/db/migration](file:///c:/Users/leocg/Documents/GitHub/challenge-ecommerce-gila/src/main/resources/db/migration)).
- **DECIDED:** Product price column will be represented using `DECIMAL(12, 4)`.
- **DECIDED:** No database-level check constraint for stock; validation is handled in application logic under a database pessimistic write lock (`PESSIMISTIC_WRITE`).
- **DECIDED:** Audit logs table schema structure will contain `id` (UUID), `timestamp` (TIMESTAMPTZ), `username` (VARCHAR), `action_type` (VARCHAR), `status` (VARCHAR), and `details` (JSONB).

### Spring Security & JWT
- **DECIDED:** User accounts and roles will be persisted in database tables (`users`, `roles`, `user_roles`) via Flyway migrations and loaded with a custom `UserDetailsService`.
- **DECIDED:** JWT signature key will be set in `application.yml` (using HS256) with a 24-hour expiration token duration.
- **DECIDED:** BCrypt Password Encoder will hash stored user credentials.
- **DECIDED:** Endpoint rules:
  - **Public:** `POST /api/v1/auth/login`, `GET /api/v1/products/**` (includes catalog search).
  - **Admin only:** Product modifications (POST, PUT, DELETE), CSV Import (`POST /api/v1/products/import`), and Order Truncation (`DELETE /api/v1/orders/clear`).
  - **Customer & Admin:** Transactional checkout (`POST /api/v1/orders`).

### Kafka CSV Import Pipeline
- **DECIDED:** Use two Kafka topics: `product-import-request` and `product-import-status` (1 partition to guarantee sequential execution per session).
- **DECIDED:** Perform file reading and database persistence sequentially on a single background thread to minimize connection pool pressure and lock contention.
- **DECIDED:** Partial success (Skip & Log) sanitization strategy: Sanitize prices (removing `$`, converting `"free"` to `0.0`), strip/escape XSS inputs, clamp stock values, and skip invalid rows with descriptive warning logs.
- **DECIDED:** Ignore duplicate product names or identifiers during CSV imports, keeping existing records.

### Asynchronous Audit Logging & Testing
- **DECIDED:** Directly inject a custom `AuditLogService` wrapping the Kafka Template to send logging messages directly from service layer contexts.
- **DECIDED:** Include structured checkout details in the JSONB metadata field (ordered items details, failure reason if unsuccessful, client IP/agent).
- **DECIDED:** Implement JUnit 5 `@ParameterizedTest` verifying that security vectors (XSS, SQL injection scripts, anomalies) are correctly neutralized.
- **DECIDED:** Rely on the MockMvc contract test suite to guarantee controller outputs match the generated interfaces.

## AI Discretion
- Standard naming conventions for classes, services, DTOs, and event payloads are at the AI's discretion, following clean code and SOLID standards.

---
*Phase: 02-db-migrations-core-apis*
*Context gathered: 2026-07-11*
