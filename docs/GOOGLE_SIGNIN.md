# Google sign-in

SpendSense uses **Google Identity Services** (GIS) in the browser. The frontend loads your Client ID from `GET /api/config/public` (from `GOOGLE_CLIENT_ID` on the server).

## Flow

1. User clicks **Sign in with Google** on login or register.
2. Google returns a one-time **ID token** (JWT).
3. Frontend sends it to `POST /api/auth/google` with body `{ "credential": "<token>" }`.
4. Backend verifies the token with Google (audience = your Client ID).
5. Backend creates or links the user, then returns the same **SpendSense JWT** as password login.

## Environment variables

| Variable | Required | Description |
|----------|----------|-------------|
| `GOOGLE_CLIENT_ID` | Yes (for Google login) | Web OAuth client ID from Google Cloud Console |

Add **Authorized JavaScript origins** in Google Console:

- `https://<your-app>.onrender.com`
- `http://localhost:3000` and `http://localhost:8080` for local dev

## Security notes

- Google-only users have no password stored (`authProvider=GOOGLE`).
- Email must be **verified** by Google before sign-in is accepted.
- Auth and AI routes are **rate-limited** per IP (see `app.auth.rate-limit-*` in `application.yml`).
- On public Render, **trial seed user is off by default** (`APP_SEED_DEMO_ENABLED=false`).

## Disable email registration

Set `APP_REGISTRATION_ENABLED=false` on Render. Users can still sign in with Google.
