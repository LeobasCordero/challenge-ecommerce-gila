# Phase 2: DB Schema, Migrations & Core APIs — Research

## Implementation Approach
The goals of Phase 2 are to implement the database schema, security layer, core catalog CRUD services, transactional checkouts, Kafka-driven CSV product imports, and Kafka-driven audit logging. To ensure code maintainability, clean design, and adhere to the strict limit of keeping all classes below 600 lines, the components are decomposed into modular, single-responsibility elements.

1. **Database Schema & Flyway:** Use versioned Flyway SQL scripts placed inside `src/main/resources/db/migration` to sequentially build database tables (`users`, `roles`, `user_roles`, `products`, `orders`, `order_items`, `audit_logs`). 
2. **Spring Security & JWT:** Implement token-based stateless authentication. A custom `UserDetailsService` fetches users from PostgreSQL. JWT parsing and checking is done in a lightweight filter.
3. **Core CRUD & Checkout:** Set up standard Spring Data JPA repositories. To handle concurrent stock decrement safely, `ProductRepository` will use `PESSIMISTIC_WRITE` locks on product updates during checkout. To separate cart concerns before Redis integration in Phase 4, checkout logic resolves the user's cart from a modular `CartService` (which uses a temporary thread-safe in-memory map in Phase 2).
4. **Kafka CSV Import Pipeline:** CSV upload triggers a multipart file save to `temp-imports/` and issues a UUID task ID. A request event is published to Kafka (`product-import-request`). A sequential listener consumes the event, invokes a modular `ProductCsvParser`, cleans/sanitizes data via `ProductRowSanitizer`, and writes products to the database using `ProductImportProcessor`. Status is published to `product-import-status` and cached.
5. **Asynchronous Audit Logging:** An `AuditLogService` publishes audit records (actions like checkout, CRUD, logins) to a Kafka topic. A Kafka listener (`AuditLogConsumer`) processes these events and saves them to the database.

## Libraries & Tools

| Library | Purpose | Why | Confidence | Source |
|---------|---------|-----|-----------|--------|
| `spring-boot-starter-data-jpa` | Database persistence (JPA / Hibernate) | Official starter for Spring Data JPA and repository patterns | HIGH | [Spring Data JPA Docs](https://spring.io/projects/spring-data-jpa) |
| `org.postgresql:postgresql` | PostgreSQL database connection driver | Standard runtime database driver for PostgreSQL | HIGH | [Postgres JDBC Driver](https://jdbc.postgresql.org/) |
| `org.flywaydb:flyway-core` | Database schema migrations | Enables sequential version-controlled SQL script migrations | HIGH | [Flyway Documentation](https://flywaydb.org/) |
| `spring-boot-starter-security` | API authentication & role protection | Standard Spring Boot security framework | HIGH | [Spring Security Docs](https://spring.io/projects/spring-security) |
| `io.jsonwebtoken:jjwt-api` / `jjwt-impl` / `jjwt-jackson` | JWT creation, parsing and signature validation | Standard lightweight library for HS256 tokens | HIGH | [JJWT GitHub Repository](https://github.com/jwtk/jjwt) |
| `org.springframework.kafka:spring-kafka` | Event messaging for async tasks | Integrates Spring Boot with Apache Kafka topic producers/consumers | HIGH | [Spring Kafka Docs](https://spring.io/projects/spring-kafka) |
| `com.opencsv:opencsv` | CSV data parsing | Efficient parsing of CSV rows with custom mapping support | HIGH | [OpenCSV Documentation](https://opencsv.sourceforge.net/) |

## Patterns to Follow
- **Single-Responsibility Decompositions (Lines < 600):** Instead of monolithic classes, split complex tasks:
  - **CSV Imports:** Separate files for `ProductCsvParser`, `ProductRowSanitizer`, `ProductImportProcessor`, and `ProductImportConsumer`.
  - **Security:** Separate files for `SecurityConfig`, `JwtAuthenticationFilter`, `JwtTokenProvider`, and `CustomUserDetailsService`.
- **High-Precision Money Representation:** Always map the SQL `DECIMAL(12, 4)` columns to `BigDecimal` in Java entities and service calculations. DTO prices should be safely cast at controller boundaries.
- **Pessimistic Locking on Checkout:** Retrieve products using `@Lock(LockModeType.PESSIMISTIC_WRITE)` to block concurrent transactions and prevent stock overdraws.
- **Method-Signature Comments Only:** Comments must reside *only* on method signatures (JavaDoc/TSDoc style). Do not put any comments inside method bodies.

## Pitfalls to Avoid
- **Dirty Price/Stock String Inputs:** CSV file rows may contain `$`, spaces, or `"free"`. Row sanitizers must strip `$`, replace `"free"` with `0.0`, clamp stock variables, and log warning messages in the import task's warning list rather than crashing.
- **Cross-Site Scripting (XSS) & SQL Injection:** Sanitize CSV strings using `HtmlUtils.htmlEscape` before saving. Strictly use Spring Data JPA parameterized queries to neutralize SQL Injection.
- **Thread Exhaustion on Bulk Kafka Imports:** Process import requests sequentially by assigning 1 partition to the import request topic and running a single-threaded consumer.
- **Security Mocking in Tests:** Ensure `ContractValidationTest` utilizes `@MockBean` or custom helper security context mock configs to bypass Spring Security filter chains during contract checks.

## Key References
- [Spring Security JWT Custom Filter Guide](https://spring.io/guides/topicals/spring-security-architecture)
- [Spring Data JPA Locking Patterns](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html#jpa.query-methods.locking)
- [Spring Kafka Official Documentation](https://docs.spring.io/spring-kafka/reference/)
- [OpenCSV Mapping Configuration Guide](https://opencsv.sourceforge.net/#mapping_by_position)

## Unverified Claims
None. All listed libraries and patterns are industry standards and fully supported in the target Spring Boot 3 + PostgreSQL environment.

---
*Researched: 2026-07-11*
