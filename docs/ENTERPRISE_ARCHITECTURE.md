# Enterprise architecture notes

This codebase is a modular monolith. That is deliberate: review, moderation,
identity and reporting share consistency boundaries, while package and service
boundaries leave room to extract components only when scale justifies it.

## Business workflows

### Review moderation

`PENDING -> APPROVED | REJECTED`

- New reviews always enter `PENDING`.
- Rejection requires a moderation reason.
- An approved review can later become `REJECTED` when a report is upheld.
- Repeating the same moderation command is idempotent.
- `@Version` and the client's `expectedVersion` prevent lost updates between
  concurrent moderators.
- The legacy `is_approved` column remains a compatibility read model while
  `moderation_status` is the source of workflow meaning.
- Decisions record moderator, time and reason and also write an audit event.

### Report resolution

`PENDING -> DISMISSED | ACTIONED`

- Only verified students can report a published review.
- A student cannot report their own review or create duplicate pending reports.
- Dismissing a report preserves it with a resolution note.
- Upholding a report atomically rejects the review and marks the report
  `ACTIONED`; neither record is hard-deleted by the normal workflow.
- Resolution also uses optimistic locking and audit events.

## Security model

- Access tokens are short-lived signed JWTs sent only in the Authorization
  header. Cookie fallback was removed so stateless CSRF assumptions remain true.
- Refresh tokens rotate under a pessimistic database lock. Only SHA-256
  fingerprints are stored, and reuse of a rotated token revokes the active
  session as a replay response.
- Password changes, password resets and account locks revoke all refresh tokens.
- Demo accounts are disabled by default and cannot be used in production unless
  `APP_SEED_ENABLED=true` is set explicitly.
- CORS allows only configured origins, a bounded method/header set, and exposes
  the correlation ID header.
- Only Actuator health/info are public. Other Actuator endpoints require an
  administrator; Swagger is disabled under the production profile.

## Reliability and operations

- Flyway migrations V8-V10 upgrade existing installations without discarding
  review/report history.
- Every HTTP response receives `X-Correlation-Id`; the same value appears in API
  envelopes and structured console logs.
- Generic 500 responses never expose internal exception messages.
- The production frontend proxies `/api/v1` to the backend, avoiding a runtime
  dependency on browser-visible `localhost` URLs.
- Containers have health checks, dependency health gates and a non-root backend
  runtime. JVM memory sizing follows container limits.
- CI runs backend tests, frontend lint/build, architecture acceptance checks and
  Compose validation for every pull request.

## Required production secrets

Copy `.env.example` to `.env` only for local Compose use. For shared deployments,
inject at least `DB_PASSWORD`, `JWT_SECRET`, and `REVIEW_SECRET_KEY` from a secret
manager. Do not reuse a secret between purposes. Mail credentials are optional
only when OTP email delivery is replaced or disabled.

## Compatibility endpoints

The old approve/reject/delete endpoints remain temporarily for existing clients.
New clients should use:

- `PATCH /api/v1/admin/reviews/{id}/moderation`
- `PATCH /api/v1/admin/reports/{id}/resolution`
- `POST /api/v1/auth/logout`

Remove the compatibility endpoints only after API usage telemetry confirms that
no supported client still calls them.
