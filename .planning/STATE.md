# Project State

## Current Position
**Phase:** 3 — Responsive Catalog UI, Search & Translation
**Status:** Planning complete, ready for execution
**Last activity:** 2026-07-11 — Phase 3 planning complete

## Key Decisions

| Decision | Phase | Source | Rationale |
|----------|-------|--------|-----------|
| **YAML-First OpenAPI** | Init | User | Ensures backend-frontend API contract alignment. |
| **Pact for Contract Testing** | Init | User | Industry standard for consumer-driven contract testing. |
| **MockMvc Backend Tests** | Init | User | Validates controller endpoints against contracts. |
| **Stock is strictly Int** | Init | User | Eliminates dirty string inputs from stock data. |
| **Pessimistic Writes on Postgres**| Init | User / AI | Blocks concurrent checkouts from overdrawing inventory stock. |
| **Flyway Database Migrations** | Init | User | Sequential database versioning files (similar to python). |
| **Redis Shopping Cart Cache** | Init | User | Fast session caching to prevent PostgreSQL write inflation. |
| **Kafka Asynchronous Import** | Init | User | Offloads bulk CSV parsing, preventing HTTP timeouts. |
| **Asynchronous Audit Logging** | Init | User / AI | Publishes audit messages via Kafka to decouple logs database writes. |
| **Sass 7-1 styling pattern** | Init | User | Standardized frontend styling directory organization. |
| **Multi-Language (EN/ES)** | Init | User | Localization supporting runtime language selection. |
| **GitHub Actions Pipeline** | Init | User | Automates lints (Checkstyle/PMD/ESLint) and testing suites. |
| **API Path Versioning (/api/v1/)** | Phase 1 | User | API version prefixes standardizing routes. |
| **Implicit Cart Resolution** | Phase 1 | User | Resolves cart owner context implicitly from JWT instead of exposing IDs in paths. |
| **Dynamic DTO Generation** | Phase 1 | User | OpenAPI generator builds interface classes dynamically in target folder without checking into git. |
| **Standard Maven Migration Location** | Phase 2 | User | Place migration SQL scripts inside standard Maven resources (`src/main/resources/db/migration`). |
| **High Precision Product Price** | Phase 2 | User | Represent price as `DECIMAL(12, 4)` in DB to avoid precision loss. |
| **Stock App-level Locks only** | Phase 2 | User | Skip DB check constraint on stock and rely on application check and Pessimistic Write locks. |
| **Detailed Audit Logging Schema** | Phase 2 | User | Store UUID, timestamp, username, action type, status, and metadata JSONB in audit logs. |
| **Database-backed user storage** | Phase 2 | User | Users, roles, and user_roles persisted in PG and queried via custom UserDetailsService. |
| **JWT Secrets in YAML** | Phase 2 | User | Maintain token config in application.yml with HS256 and 24h lifespan. |
| **BCrypt Password Encoder** | Phase 2 | User | Standard BCrypt encoder for user password hashing. |
| **Role-based Endpoints** | Phase 2 | User | Public login/GET, Admin-only modification/import/clear, and Customer checkout. |
| **Double topic Kafka CSV Import** | Phase 2 | User | Separate request and status Kafka topics with 1 partition to guarantee sequential execution. |
| **Sequential CSV Consumer** | Phase 2 | User | Process CSV rows sequentially in a single thread to avoid database connection exhaustion. |
| **Skip & Log Import Sanitization** | Phase 2 | User | Strip $, map free to 0.0, escape HTML, clamp stocks, and skip/warn invalid rows on import. |
| **Ignore Import Duplicates** | Phase 2 | User | Skip duplicate records during product import without overwriting DB. |
| **Direct Kafka Audit log injection** | Phase 2 | User | Directly inject and use AuditLogService Kafka template in service layer. |
| **Detailed Checkout Audit Metadata** | Phase 2 | User | Store item list, failure reasons, and IP/Agent details inside JSONB for checkouts. |
| **Parameterized Input Security Tests** | Phase 2 | User | Parameterized tests verifying XSS and SQL Injection payloads are blocked/sanitized. |

### Blockers/Concerns
None.

---
*Last updated: 2026-07-11*

