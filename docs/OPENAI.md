# SpendSense — OpenAI configuration

## How API keys are resolved

1. **User key** (Settings → secret key) — encrypted in the database, highest priority.
2. **Platform key** (`OPENAI_API_KEY` environment variable) — optional, for hosted trials on Render.
3. **Neither** — AI endpoints return `no_api_key`; the frontend uses keyword-based category fallback.

The platform key is **never** exposed to the browser or returned in API responses.

## Environment variables

| Variable | Description |
|----------|-------------|
| `OPENAI_API_KEY` | Platform secret key (e.g. set in Render dashboard) |
| `OPENAI_MODEL` | Model id, default `gpt-4o-mini` |

## Local development

Add to `.env` (see `.env.example`):

```env
OPENAI_API_KEY=sk-your-key-here
```

Then `docker compose up --build` or run the backend with `SPRING_PROFILES_ACTIVE=local`.

## Render

1. Web Service → **Environment** → add `OPENAI_API_KEY` as a **secret**.
2. Redeploy. New users can use AI features without configuring Settings.
3. Monitor usage and cost in the [OpenAI dashboard](https://platform.openai.com/usage).

## Cost and abuse

- Prefer `gpt-4o-mini` for category and receipt tasks.
- Consider signup rate limits and OpenAI usage alerts on public demos.
- Users with their own keys bill to their OpenAI account, not yours.
