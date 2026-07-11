# Plan 2-01: DB Schema, Migrations, Core APIs & Kafka Integrations — Summary

**Executed:** 2026-07-11
**Status:** Complete
**Commits:** 5

## What Was Built
Successfully built the core backend, database schema, security configuration, event consumers, and test suites for Phase 2:
1. **Flyway Migrations & JPA Entities:** Implemented postgres migrations for `users`, `roles`, `user_roles`, `products`, `orders`, `order_items`, and `audit_logs` tables. Added corresponding JPA models and Spring Data repository interfaces.
2. **Spring Security with JWT:** Formulated stateless security filters parsing JWT tokens, adapting user credentials from database queries using a custom `UserDetailsService`, and enforcing strict access rules.
3. **Core CRUD & Pessimistic Lock Checkouts:** Implemented catalog CRUD operations and transactional checkouts. Products are locked via `@Lock(LockModeType.PESSIMISTIC_WRITE)` during decrement calculations to ensure stock consistency.
4. **Kafka CSV Import Pipeline:** Developed asynchronous sequential product uploads using Kafka topics (`product-import-request`, `product-import-status` with 1 partition) and OpenCSV. Sanitized price prefixes, clamped stock, escaped HTML to block XSS, and filtered out duplicate product names.
5. **Asynchronous Audit Logging:** Configured an asynchronous logging flow. Actions like login, catalog CRUD, CSV import start/stop, and checkout completions/failures are published to Kafka and persisted to `audit_logs`.

## Files Created/Modified

| File | Action | Description |
|------|--------|-------------|
| [pom.xml](file:///c:/Users/leocg/Documents/GitHub/challenge-ecommerce-gila/pom.xml) | Modified | Added JPA, PostgreSQL, Flyway, Spring Security, JWT, OpenCSV, and Spring Kafka dependencies. Configured Surefire plugin argLine for Java 23 Mockito support. |
| [application.yml](file:///c:/Users/leocg/Documents/GitHub/challenge-ecommerce-gila/src/main/resources/application.yml) | Created | Configured datasource, Flyway location, Kafka consumer/producer, and JWT properties. |
| [V1__init.sql](file:///c:/Users/leocg/Documents/GitHub/challenge-ecommerce-gila/src/main/resources/db/migration/V1__init.sql) | Created | Flyway SQL script creating all database tables and seeding roles and default user accounts. Includes `initial_stock` column for stock reset operations. |
| Model Entities (`User.java`, `Role.java`, `Product.java`, `Order.java`, `OrderItem.java`, `AuditLog.java`) | Created | JPA entity models mapping database schemas. |
| Repository Interfaces | Created | Spring Data JPA interfaces for database entities. `ProductRepository` declares transactional `findWithLockById` with pessimistic lock support. |
| Security Helper files | Created | Security configurations, JWT filters, custom UserDetails wrapper, and database-backed UserDetailsService. |
| Product Mapper and Services | Created | Formulated catalog services, temporary thread-safe in-memory cart, and lock-protected checkout service implementing stock reset logic. |
| CSV Parser, Sanitizer, and Consumer | Created | OpenCSV parser, text/XSS/price sanitizer, database writer, and Kafka topic consumer. |
| Audit Logger Consumer and Service | Created | Kafka publisher and database writer consumer. |
| REST Controllers | Modified | Updated AuthController, ProductController, CartController, and OrderController to delegate logic to services. |
| [SecurityParameterizedTest.java](file:///c:/Users/leocg/Documents/GitHub/challenge-ecommerce-gila/src/test/java/com/gila/ecommerce/service/SecurityParameterizedTest.java) | Created | JUnit 5 test suite validating HTML XSS escaping, SQL injection strings safety, stock clamping, and price parsing. |
| [ContractValidationTest.java](file:///c:/Users/leocg/Documents/GitHub/challenge-ecommerce-gila/src/test/java/com/gila/ecommerce/controller/ContractValidationTest.java) | Modified | Configured `@MockBean` controller dependencies and `@WithMockUser` to operate with security settings. |

## Verification Results
- [x] Compilation — passed (actual output: `mvn clean compile` successful)
- [x] MockMvc Contract Validation Tests — passed (6 tests green in `ContractValidationTest.java`)
- [x] Parameterized Security & Input Sanitization Tests — passed (14 parameterized test cases green in `SecurityParameterizedTest.java`)
- [x] Full test suite run — passed (actual output: 20 tests run, 0 failures, 0 errors, 0 skipped)

## Notable Decisions
- Added an `initial_stock` column to `products` to support the order clear reset operation, ensuring that stocks are accurately restored to their original created/imported levels when clearing history.
- Structured the CSV import process into highly focused modular classes (`ProductCsvParser`, `ProductRowSanitizer`, `ProductImportProcessor`, `ProductImportConsumer`) to strictly enforce class sizes below 600 lines. All added classes are under 150 lines.

## Issues Encountered
- **Mockito/ByteBuddy Java 23 Compatibility:** The default Mockito version used by Spring Boot 3.2.5 could not mock/instrument class hierarchies (like `JwtTokenProvider`) because of Java 23's structure. Resolved this by configuring `maven-surefire-plugin` in `pom.xml` with `<argLine>-Dnet.bytebuddy.experimental=true</argLine>`.
- **Security Context in MockMvc Tests:** Adding SecurityContextHolder references in controller classes broke base MockMvc tests because they lacked authentication context. Resolved by adding `@WithMockUser(username = "customer")` at the test class level.

---
*Executed: 2026-07-11*
