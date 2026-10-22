# eagle-bank-api [![Gradle Build](https://github.com/atilla8huno/eagle-bank-api/actions/workflows/gradle.yml/badge.svg)](https://github.com/atilla8huno/eagle-bank-api/actions/workflows/gradle.yml)

Spring Boot 4.1 / Java 21 implementation of the Eagle Bank REST service based on `openapi.yaml`.

## Prerequisites

- JDK 21+
- Gradle (wrapper included)

## Running Locally

Start the application:

```bash
./gradlew bootRun
```

Once started:
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Running Tests

The test suite follows a 1-to-1 unit and integration tests per class (`*Test`) plus Cucumber BDD acceptance suites running against in-memory H2 (`MODE=PostgreSQL`):

```bash
./gradlew test                  # unit tests
./gradlew integrationTest       # Spring / JPA / concurrency
./gradlew acceptanceTest        # Cucumber BDD features (25 live, 13 @wip)
./gradlew check                 # all of the above + JaCoCo report
```

## Design Notes

### Authentication, RBAC + ABAC & Specifications
- **OAuth2 Resource Server**: `POST /v1/auth/token` exchanges a `userId` (or email) and password for a signed HS256 JWT (`NimbusJwtEncoder` / `NimbusJwtDecoder`). When creating a user via `POST /v1/users`, `password` is optional so contract payloads without a password field remain valid.
- **Coarse-grained RBAC / OAuth2 Scopes**: `Permission` and `Permission.Role` define explicit scopes (`users:read`, `users:write`, `accounts:read`, `accounts:write`, `transactions:read`, `transactions:write`) enforced at the HTTP filter chain in `SecurityConfig`.
- **Fine-grained ABAC via Domain Specifications**: Resource ownership and domain invariants are evaluated via composable `Specification<T>` rules in `UserSpecifications` and `AccountSpecifications`, ensuring missing resources return `404 Not Found` while cross-customer access attempts return `403 Forbidden`.

### Concurrency, Idempotency & Ledger Consistency
- **Monetary values**: Handled via the immutable `Money` value object (`BigDecimal` with scale `2` and `RoundingMode.HALF_EVEN`).
- **Optimistic locking**: `BankAccount` uses `@Version` for optimistic concurrency control rather than `SELECT ... FOR UPDATE` row locks, avoiding JDBC connection pool starvation under high contention. Collisions on concurrent updates are retried with bounded jittered backoff in `TransactionService` (`PROPAGATION_REQUIRES_NEW` via `TransactionTemplate`).
- **Mandatory Idempotency with Foreign Key**: `POST /v1/accounts/{accountNumber}/transactions` resolves the `Idempotency-Key` header (or generates on the fly) and persists an `IdempotencyRecord`.
- **Append-only ledger**: `BankTransaction` and `IdempotencyRecord` are marked `@Immutable` with `updatable = false` columns so ledger records are never mutated after creation.
- **Request & log correlation**: `CorrelationIdFilter` propagates or generates `X-Correlation-Id` on every request and binds `correlationId` into SLF4J `MDC`.
- **Race condition test**: `ConcurrencyTransactionTest` fires 100 parallel virtual-thread withdrawals of £10.00 against a £500.00 account (asserting exactly 50 succeed, 50 fail with insufficient funds, and the final balance is £0.00) and verifies that retried requests with the same `Idempotency-Key` credit the account only once.
- **Jittered exponential backoff**: On an `@Version` optimistic-lock collision, `TransactionService` retries in a fresh transaction (`PROPAGATION_REQUIRES_NEW`) up to 15 times, sleeping `min(50ms, 2^min(attempt, 4) + random(1..7ms))` between attempts to desynchronize contending virtual threads and prevent retry storms.

### 4-Layer ATDD / BDD Architecture
Acceptance tests under `src/test/java/com/eaglebank/acceptance` and `src/test/resources/features` follow the four-layer Continuous Delivery acceptance testing architecture:
1. **Executable Specifications**: `user_management.feature`, `bank_account_management.feature`, and `account_transactions.feature` (25 live scenarios covering all implemented endpoints and 13 `@wip` scenarios documenting unimplemented `PATCH` / `DELETE` endpoints).
2. **Domain DSL**: `EagleBankDsl`, `UsersDsl`, `AccountsDsl`, `TransactionsDsl`, and `Params` (`DslContext` per-scenario aliasing for functional test isolation).
3. **Protocol Driver**: `EagleBankApiProtocolDriver` / `EagleBankApiProtocolDriverImpl` encapsulating HTTP transport, JWT authentication headers, and JSON serialization.
4. **System Under Test**: `EagleBankApiApplication` backed by in-memory H2 (`MODE=PostgreSQL`).
