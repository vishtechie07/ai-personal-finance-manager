# SpendSense — OpenAI configuration

## How API keys are resolved

1. **User key** (Settings → secret key) — encrypted in the database, highest priority.
2. **Platform key** (`OPENAI_API_KEY` + `APP_PLATFORM_AI_ENABLED=true`) — optional hosted trials.
3. **Neither** — AI endpoints return `no_api_key`; the frontend uses keyword-based category fallback.

The platform key is **never** exposed to the browser or returned in API responses.

## Platform AI safety (production defaults)

On Render (`application-render.yml` / `render.yaml`):

| Control | Default | Env var |
|---------|---------|---------|
| Platform AI off | `false` | `APP_PLATFORM_AI_ENABLED` |
| New account wait | 24 hours | `APP_PLATFORM_AI_MIN_ACCOUNT_AGE_HOURS` |
| Trial user blocked | on | (code: `spendsense` cannot use platform AI) |
| Category quota / hour | 25 (platform), 60 (user key) | `APP_AI_PLATFORM_CATEGORY_QUOTA`, `APP_AI_USER_CATEGORY_QUOTA` |
| Receipt quota / hour | 5 (platform), 15 (user key) | `APP_AI_PLATFORM_RECEIPT_QUOTA`, `APP_AI_USER_RECEIPT_QUOTA` |
| Monthly brief quota / hour | 6 (platform), 12 (user key) | `APP_AI_PLATFORM_MONTHLY_BRIEF_QUOTA`, `APP_AI_USER_MONTHLY_BRIEF_QUOTA` |
| Max description length | 200 chars | `APP_AI_MAX_DESCRIPTION_LENGTH` |

Denied responses use `source` values: `rate_limited`, `platform_disabled`, `demo_not_allowed`, `account_too_new`, `no_api_key`.

**Recommended for public Render:** leave `APP_PLATFORM_AI_ENABLED=false` and omit `OPENAI_API_KEY`, or only enable platform AI for short controlled demos with OpenAI budget alerts.

## Signup abuse controls

| Control | Default (Render) | Env var |
|---------|------------------|---------|
| Register attempts / IP / hour | 5 | `APP_REGISTER_ATTEMPTS_PER_WINDOW` |
| New accounts / IP / day | 5 | `APP_MAX_NEW_ACCOUNTS_PER_IP_PER_DAY` |
| Reserved usernames | `spendsense`, `admin`, … | (code) |
| Disposable email domains | blocked | (code) |

## Environment variables

| Variable | Description |
|----------|-------------|
| `OPENAI_API_KEY` | Platform secret key (Render secret) |
| `OPENAI_MODEL` | Model id, default `gpt-4o-mini` |
| `APP_PLATFORM_AI_ENABLED` | Allow platform key fallback |
| `SETTINGS_ENCRYPTION_KEY` | Encrypts per-user API keys at rest |

## Local development

`local` profile enables platform AI when a key is set (`APP_PLATFORM_AI_ENABLED=true` by default in `application-local.yml`), with no account-age gate and higher quotas.

```env
OPENAI_API_KEY=sk-your-key-here
APP_PLATFORM_AI_ENABLED=true
```

## Render

1. Web Service → **Environment** → add `OPENAI_API_KEY` as a **secret** (optional).
2. Set `APP_PLATFORM_AI_ENABLED=true` only if you want shared platform AI.
3. Monitor usage in the [OpenAI dashboard](https://platform.openai.com/usage).

Users with their own keys bill to their OpenAI account and use separate (higher) per-user quotas.
