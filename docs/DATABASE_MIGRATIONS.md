# Database migrations (Flyway)

SpendSense uses **Flyway** on PostgreSQL (`render` and `railway` profiles). Schema changes are versioned under:

`backend/src/main/resources/db/migration/`

## Profiles

| Profile | Flyway | Hibernate `ddl-auto` |
|---------|--------|----------------------|
| `local` (H2) | **Off** | `create-drop` (dev convenience) |
| `render` / `railway` | **On** | `validate` |

## First deploy on an existing Render database

If the DB was created before Google sign-in, Flyway **V1** adds missing columns (`auth_provider`, `google_sub`, nullable `password`) idempotently. On startup you should see:

```text
Successfully applied 1 migration to schema "public"
```

Or, if the schema already existed without Flyway history:

```text
Successfully baselined schema with version: 0
Successfully applied 1 migration ...
```

## Adding a new migration

1. Add `V2__short_description.sql` (never edit applied migrations).
2. Use idempotent SQL where possible (`IF NOT EXISTS`, `ADD COLUMN IF NOT EXISTS`).
3. Deploy; Flyway runs before JPA starts.

## Local Docker Postgres

`docker compose` uses `SPRING_PROFILES_ACTIVE=render`, so Flyway runs against the Compose Postgres volume on startup.
