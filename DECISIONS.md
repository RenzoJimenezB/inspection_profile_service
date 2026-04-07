# Design Decisions

## Database Design

### Schema Overview

The database consists of 5 core tables: `users`, `profiles`, `credentials`,
`subscriptions`, and `refresh_tokens`.

### Key Design Decisions

**Password column VARCHAR(255)**.
BCrypt hashes are always 60 characters. VARCHAR(255) was chosen as a safe margin
for future algorithm flexibility without requiring a migration.

**Enum storage as VARCHAR**.
PostgreSQL native enum types were initially used but caused JDBC casting issues
with Hibernate. Converted to VARCHAR(50) via migration V8 while maintaining
application-level validation through Kotlin enums. A future improvement would
be implementing a custom Hibernate dialect for native PostgreSQL enum support.

**RefreshToken does not extend BaseEntity**.
Refresh tokens are immutable (created and deleted, never updated). `updatedAt`
has no semantic meaning here. The entity manages its own `id` and `createdAt`.

**Subscription belongs to User, not Profile**.
Feature access is a user-level concern per the requirements. One payment covers
all profiles under a user, similar to a streaming service account model.

**is_active on profiles is a soft delete flag**.
Not used for session context. Active profile tracking is handled by JWT claims
and Redis. Soft delete prevents access to deactivated profiles even within a
valid JWT window.

**Sequential BIGSERIAL IDs**.
Standard PostgreSQL auto-increment. Security concerns around guessable IDs are
mitigated by application-level ownership checks on all queries. UUID would be
more appropriate for a distributed system.

### Indexes

- `idx_users_email`: frequent login lookups by email
- `idx_profiles_user_id`: listing profiles by user
- `idx_credentials_profile_id`: listing credentials by profile
- `idx_credentials_status`: scheduler bulk queries by status
- `idx_credentials_expiry_date`: scheduler expiry date range queries
- `idx_subscriptions_user_id`: active subscription lookups
- `idx_refresh_tokens_token`: token validation on every refresh
- `idx_refresh_tokens_user_id`: cleanup on logout and profile switch

---

## Challenge Solutions

### Challenge A: Profile Context Switching

**Decision:** Issue a new JWT on profile switch and blacklist the old refresh
token in the database.

When a user switches profiles:

1. Verify the target profile belongs to the authenticated user
2. Delete all existing refresh tokens for that user
3. Issue a new JWT with the new `profileId` claim
4. Issue a new refresh token

**Trade-off:** The old access token remains valid for up to 15 minutes after
switching. This is acceptable for a property inspection platform where a brief
stale context window poses minimal business risk. A token blacklist using Redis
would eliminate this window but adds complexity not justified for this use case.

### Challenge B: Credential Expiration Grace Period

**Decision:** A VP profile is not downgraded if any credential is PENDING.

The scheduler query before downgrading:

```sql
SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
FROM Credential c
WHERE c.profile.id = :profileId
  AND c.status = 'PENDING'
```

If any credential is PENDING (renewal in progress), the downgrade is skipped
regardless of expired credentials. A renewal is a new credential submission. No explicit link between renewal and
expiring credential is needed.

### Challenge C: Feature Gating Race Condition

**Decision:** Reject inspection creation if limit is exceeded, even during
payment processing.

Redis atomic INCR is used for inspection counting:

1. Atomically increment the monthly counter
2. If counter exceeds tier limit, decrement and reject with 403
3. When Stripe webhook confirms upgrade, tier is updated in database
4. Next request naturally sees the new tier and succeeds

The 2-second window where a user is blocked while payment processes is
acceptable. Access is never granted based on unconfirmed payments.

---

## Architecture Decisions

### Credential Approval Flow

No reviewer role is defined in the requirements. A minimal admin role was
introduced with `PUT /api/v1/admin/credentials/{id}/status` to enable:

- Realistic testing of PENDING → APPROVED/REJECTED flow
- VP status promotion testing (requires 2 approved credentials)
- Profile downgrade testing via the scheduler

Credentials stay PENDING on submission. Auto-approval was rejected as it would
make Challenge B untestable.

### Profile Promotion is Event-Driven

Profile becomes VERIFIED_PROFESSIONAL immediately upon admin approval of the
second credential. Checked in `CredentialService.approveCredential()` after
status update. Downgrade is handled separately by the nightly scheduler.

### Scheduler Testability

`POST /api/v1/admin/jobs/credential-expiration` exposes a manual trigger for
the nightly scheduler. This allows testing downgrade logic without waiting for
2AM UTC execution.

### Nightly Scheduler Over Real-Time Expiration

Maximum 24-hour window between credential expiry and profile downgrade.
Acceptable for inspection platform use case. Real-time alternative would
require Redis keyspace notifications or Quartz job queue (future improvement).

### Multiple Profiles

Users are limited to 2 profiles (personal and company) based on the
personal ↔ company context requirement. Profile creation beyond
registration is handled via `POST /api/v1/profiles`. The switch endpoint
issues a new JWT with the target profile context.

### Login Returns First Active Profile

Default behavior on login. Users with multiple profiles switch context
immediately after login via `POST /api/v1/profiles/switch/{profileId}`.

### Admin User Seeding

Seeded via Flyway migration V4 rather than application layer to guarantee
it runs exactly once without requiring startup validation logic.

Default credentials: admin@inspectpro.com / Admin123!

---

## Subscription Design

### expires_at Nullable

BASIC tier subscriptions never expire (`expires_at` is NULL). ENHANCED and
PROFESSIONAL tiers have `expires_at` set to 30 days when Stripe webhook
confirms payment. A production implementation would use the exact expiration
date provided by Stripe in the webhook payload.

### Stripe Webhook Only Handles Upgrades

Webhook processes ENHANCED and PROFESSIONAL upgrades only. Downgrade to BASIC
happens via the nightly scheduler when `expires_at` passes.

### Subscription History Preserved

Every tier change creates a new subscription record and deactivates the old
one. Provides full audit trail of subscription changes. Known trade-off:
accumulates inactive records over time. Archival strategy would be needed
in production.

---

## Security Decisions

### JWT + Redis Hybrid

- Access tokens: 15 minutes, stateless JWT
- Refresh tokens: 7 days, stored in database for invalidation
- Refresh token rotation: each refresh consumes the old token and issues new ones

### Rate Limiting Fails Open

If Redis is unavailable, requests are allowed through rather than blocking
all users. Availability prioritized over strict rate limiting during outages.

### Dual Admin Authorization

Admin endpoints protected both at SecurityConfig request matcher level and
`@PreAuthorize` annotation level. Defense in depth approach.

### EnumType.STRING over ORDINAL

Prevents data corruption if enum values are reordered or new values inserted.

---

## API Design

### Consistent Error Responses

All errors return a consistent `ErrorResponse` structure:

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "Descriptive message"
}
```

Spring Security handles 401/403, GlobalExceptionHandler handles all MVC errors.

---

## Bonus Enhancements

### OpenAPI Documentation + Postman Collection

Both Swagger UI and a Postman collection are provided. Swagger UI enables
interactive browser-based testing directly from the documentation page at
`/swagger-ui.html`. The Postman collection provides a structured testing
workflow with pre-configured requests, automated token capture via test
scripts, and a documented end-to-end testing sequence.

### Basic CI Pipeline

GitHub Actions pipeline runs on every push to main and feature branches.
PostgreSQL and Redis services are spun up in the CI environment so the
Spring Boot context load test runs against real dependencies, providing
genuine confidence the application starts correctly. Pipeline: build → test → docker build.

### Rate Limiting

Redis-based rate limiting of 100 requests per minute per authenticated user.
Implemented as a servlet filter running after JWT authentication. Uses atomic
Redis INCR with a 60-second TTL window. If Redis is unavailable, requests
are allowed through (fail open) to prioritize availability.
Exceeding the limit returns 429 Too Many Requests. Refresh token rotation
is also implemented. Each refresh call consumes the old token and issues
a new one. Attempting to reuse an old refresh token returns 400.

### Structured Logging

JSON-formatted logs using Logstash Logback Encoder. Each request is assigned
a unique correlation ID via `X-Correlation-ID` header, automatically included
in all log entries for that request. If the client provides a correlation ID
in the request header, it is reused, enabling distributed request tracing.
Logs include timestamp, level, logger name, thread, and correlation ID.

---

## Trade-offs

- **Token blacklisting:** Redis-based blacklist would eliminate the 15-minute
  stale JWT window after profile switching, at the cost of added complexity
- **Nightly scheduler:** Maximum 24-hour window between credential expiry and
  profile downgrade. Acceptable for this use case but real-time would require
  Redis keyspace notifications or Quartz
- **Shared token service:** Refresh token logic is duplicated between
  AuthService and ProfileService. Should be extracted to a shared TokenService
- **Enum storage:** VARCHAR chosen over PostgreSQL native enums due to
  Hibernate JDBC casting issues. Native enum support would require a custom dialect
- **Subscription records:** Inactive subscriptions accumulate over time.
  An archival strategy would be needed in production

## Future Improvements

- **UUID primary keys:** Better for distributed systems, prevents ID enumeration
- **Real-time credential expiration:** Redis keyspace notifications or Quartz
  for instant downgrade on expiry
- **Archive strategy:** For inactive subscriptions and soft-deleted profiles
- **Refresh token rotation verification:** Can be tested by calling
  POST /api/v1/auth/refresh twice with the same token. Second call returns 400
- **Rate limiting:** Set to 100 requests per minute per user. Exceeding
  this returns 429 Too Many Requests
- **Reactive stack:** WebFlux + R2DBC was suggested as a bonus implementation.
  Spring MVC was chosen for its maturity, simpler debugging, and faster
  development within the time constraints. A migration to WebFlux + R2DBC would
  provide non-blocking I/O and better scalability under high concurrency,
  replacing JPA with R2DBC repositories and adapting all service layers to
  return Mono/Flux types.

---

## Assumptions

1. No reviewer role defined. Admin endpoint introduced for credential approval
2. Credential renewal = new submission, no explicit link to expiring credential
3. Maximum 2 profiles per user based on personal ↔ company context
4. 30-day subscription expiration for paid tiers (real Stripe provides exact date)
5. Feature access determined by subscription tier, not profile type
6. BASIC tier never expires (expires_at = null)