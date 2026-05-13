# Admin Module

Endpoints (all under `/api/admin`)

- `GET /api/admin/statistics` - dashboard stats (cached in Redis 5 minutes)
- `GET /api/admin/users` - list users (pagination, sorting, keyword, role, verified, facultyId)
- `PUT /api/admin/users/{id}/lock` - lock account
- `PUT /api/admin/users/{id}/unlock` - unlock account
- `PUT /api/admin/users/{id}/verify` - force verify
- `PUT /api/admin/users/{id}/reset-password` - reset password
- `PUT /api/admin/users/{id}/role` - change role (SUPER_ADMIN only)

Security

- All endpoints under `/api/admin/**` require `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`.

Database

- Migration `V2__admin_module.sql` adds `toxic_keywords` and adds indexes and soft-delete columns.

Caching

- Statistics endpoint uses Redis caching (key: `admin:statistics`, TTL 5 minutes configured via application properties).

Notes

- This is a scaffolded, production-oriented admin module. Some complex queries (projections, top N lecturers) are left as placeholders and should be implemented with custom JPQL/SQL for performance.
