# Deploy SpendSense on Render

SpendSense ships as a **single Docker image**: Nginx on port **80** serves the Vue UI and proxies `/api/*` to Spring Boot on **8081**.

## 1. Push code to GitHub

Ensure the latest commit is on the branch you will connect to Render.

## 2. Create PostgreSQL

1. [Render Dashboard](https://dashboard.render.com) → **New** → **PostgreSQL**
2. Name: e.g. `pfm-db`, region same as your web service
3. After creation, open the database and note **Internal** connection values:
   - Host, Port, Database, Username, Password

## 3. Create Web Service (Docker)

1. **New** → **Web Service**
2. Connect your GitHub repo
3. **Runtime**: Docker
4. **Dockerfile path**: `./Dockerfile` (repo root)
5. **Instance type**: Free (spins down after ~15 min idle; cold start ~30–60 s)

### Port

| Setting | Value |
|--------|--------|
| **Port** | `80` |

Render must hit Nginx inside the container, not Spring directly.

### Health check (recommended)

| Setting | Value |
|--------|--------|
| **Health Check Path** | `/api/actuator/health` |

## 4. Environment variables

Add these on the Web Service → **Environment**:

| Variable | Required | Example / notes |
|----------|----------|-----------------|
| `SPRING_PROFILES_ACTIVE` | Yes | `render` |
| `JWT_SECRET` | Yes | At least **32** random characters (`openssl rand -base64 32`) |
| `DATABASE_HOST` | Yes | From Postgres **Internal** host |
| `DATABASE_PORT` | Yes | Usually `5432` |
| `DATABASE_NAME` | Yes | Database name from Render |
| `DATABASE_USERNAME` | Yes | From Render |
| `DATABASE_PASSWORD` | Yes | From Render |
| `CORS_ALLOWED_ORIGIN_PATTERNS` | No | Default: `https://*.onrender.com` |
| `SETTINGS_ENCRYPTION_KEY` | No | Defaults to `JWT_SECRET`; use a separate value for extra isolation |
| `APP_SEED_CONSOLIDATE_LEGACY_USERS` | No | Set `true` **once** to wipe legacy users and recreate the seed account (see [DEMO_CREDENTIALS.md](DEMO_CREDENTIALS.md)) |
| `OPENAI_API_KEY` | No | Platform OpenAI key (secret). Only used when `APP_PLATFORM_AI_ENABLED=true`. |
| `APP_PLATFORM_AI_ENABLED` | No | Default `false` — enable shared platform AI only for controlled demos |
| `OPENAI_MODEL` | No | Default `gpt-4o-mini` |
| `APP_MAX_NEW_ACCOUNTS_PER_IP_PER_DAY` | No | Default `5` — limits register + Google new accounts per IP |
| `GOOGLE_CLIENT_ID` | No | Web OAuth client ID for **Sign in with Google** ([GOOGLE_SIGNIN.md](GOOGLE_SIGNIN.md)) |
| `APP_SEED_DEMO_ENABLED` | No | Default `false` on Render — set `true` only for shared demo account |
| `APP_REGISTRATION_ENABLED` | No | Default `true` — set `false` to allow Google-only sign-up |

**Alternative (JDBC URL):** use profile `railway` instead and set `DATABASE_URL=jdbc:postgresql://HOST:5432/DBNAME` plus `DATABASE_USERNAME` / `DATABASE_PASSWORD`.

## 5. Link database (optional)

On the Web Service → **Environment** → **Add from database** and map host, port, database, user, password to the variable names above.

Or use the repo **`render.yaml`** blueprint (New → Blueprint) to provision DB + web service together.

## 6. Deploy and verify

1. **Create Web Service** / **Deploy**
2. Open `https://<your-service>.onrender.com`
3. Sign in: **`spendsense`** / **`TrySpend2026!`** (seeded on first startup — [DEMO_CREDENTIALS.md](DEMO_CREDENTIALS.md))
4. API smoke test: `GET https://<host>/api/actuator/health` → `{"status":"UP"}`

## 7. OpenAI / AI features

**Production default:** platform AI is **off** (`APP_PLATFORM_AI_ENABLED=false`). Users add their own key in **Settings** (BYOK, encrypted per user).

**Controlled demo:** set `OPENAI_API_KEY` and `APP_PLATFORM_AI_ENABLED=true`. Per-user quotas, 24h account age gate, and trial user `spendsense` cannot use platform AI. See **[docs/OPENAI.md](OPENAI.md)**.

**Without any key:** core app works (transactions, budgets, bills); category UI falls back to keyword matching.

Details: **[docs/OPENAI.md](OPENAI.md)**.

## Local Docker test (before Render)

```bash
cp .env.example .env
# Edit .env: set JWT_SECRET (32+ chars) and POSTGRES_PASSWORD if desired
docker compose up --build
```

Open http://localhost:8080 and log in as `spendsense` / `TrySpend2026!` ([DEMO_CREDENTIALS.md](DEMO_CREDENTIALS.md)).

`docker-compose.yml` sets `CORS_ALLOWED_ORIGIN_PATTERNS` for localhost — required because the `render` profile otherwise only allows `https://*.onrender.com`.

### Receipt uploads

Receipt files are stored on disk at `APP_STORAGE_PATH` (default `/app/uploads`). Render’s default disk is **ephemeral** — files are lost on redeploy. For production receipts, attach a **persistent disk** on Render or migrate to object storage (S3/R2).

## Troubleshooting

| Symptom | Fix |
|--------|-----|
| 502 / blank after deploy | Check logs; ensure port **80** and health path `/api/actuator/health` |
| DB connection errors | Use **Internal** host (not External URL); verify `SPRING_PROFILES_ACTIVE=render` |
| CORS errors from browser | Set `CORS_ALLOWED_ORIGIN_PATTERNS` to your exact Render URL or `https://*.onrender.com` |
| Login works locally but not on Render | Confirm `JWT_SECRET` is set and unchanged between deploys |
| Cold start timeout on free tier | Wait up to 60 s on first request after idle; upgrade instance to avoid sleep |
