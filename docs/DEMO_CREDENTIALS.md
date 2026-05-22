# SpendSense — trial / seed account

On first startup (empty database), the API can create a **seed user** with sample transactions and budgets (current month plus the previous two).

**Render / production:** seeding is **off by default** (`APP_SEED_DEMO_ENABLED=false`). Set `APP_SEED_DEMO_ENABLED=true` only for controlled demos.

| Field | Value |
|-------|--------|
| **Username** | `spendsense` |
| **Password** | `TrySpend2026!` |
| **Email** | `trial@spendsense.app` |

Use these for local Docker, Render smoke tests, and demos. **Change or disable this account on any public production deployment** you do not control.

## Where it is defined

Constants live in `DemoSeedService` (`DEMO_USERNAME`, `DEMO_PASSWORD`, etc.). Startup seeding runs in `ApplicationSeedRunner`.

## Existing database (old `demo` user)

If you already ran the app with the previous `demo` / `Demo1234` account:

1. **Local (`local` profile):** consolidation is on by default — delete the old DB or set `APP_SEED_CONSOLIDATE_LEGACY_USERS=true` once, restart, then sign in with the credentials above.
2. **Docker Compose:** `docker compose down -v` (wipes Postgres volume), then `docker compose up --build`.
3. **Render:** set `APP_SEED_CONSOLIDATE_LEGACY_USERS=true` once, redeploy, then remove the variable.

## New registrations

Users who register via the UI get their own account and the same style of sample data; they do **not** use these credentials.
