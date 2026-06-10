# SpendSense — trial / seed account

On first startup (empty database), the API can create a **seed user** with sample transactions and budgets (current month plus the previous two).

**Render / production:** seeding is **off by default** (`APP_SEED_DEMO_ENABLED=false`). Set `APP_SEED_DEMO_ENABLED=true` only for controlled demos.

**Docker Compose (local):** copy `.env.example` to `.env` and set `APP_SEED_DEMO_ENABLED=true` so the trial account and three months of sample data are created on startup. Compose passes this variable through to the app container.

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
2. **Docker Compose:** copy `.env.example` to `.env`, set `APP_SEED_DEMO_ENABLED=true`, then `docker compose up --build`. To wipe Postgres entirely: `docker compose down -v`, then start again.
3. **Render:** set `APP_SEED_CONSOLIDATE_LEGACY_USERS=true` once, redeploy, then remove the variable.

## Stale sample month

Demo data is seeded for the **current month and the previous two**. If the calendar month rolled over since the last seed, the dashboard can look empty until you clear sample data (banner **Clear sample data** or `DELETE /api/auth/me/sample-data`) and restart with `APP_SEED_DEMO_ENABLED=true`.

## New registrations

Users who register via the UI get their own account and the same style of sample data; they do **not** use these credentials.
