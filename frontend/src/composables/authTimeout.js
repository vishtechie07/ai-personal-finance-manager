import axios from "axios";

/** Render free tier cold starts can exceed the default 10s axios timeout. */
export const AUTH_COLD_START_TIMEOUT_MS = 90_000;
export const DEFAULT_API_TIMEOUT_MS = 10_000;

export function authRequestOptions(options = {}) {
  const coldStart = options.coldStart !== false;
  return {
    coldStart,
    timeout: coldStart ? AUTH_COLD_START_TIMEOUT_MS : DEFAULT_API_TIMEOUT_MS,
  };
}

/** Fire-and-forget health ping to wake a sleeping host before signup/login. */
export function warmupServer() {
  const base = (import.meta.env.VITE_API_URL || "/api").replace(/\/$/, "");
  axios
    .get(`${base}/actuator/health`, { timeout: AUTH_COLD_START_TIMEOUT_MS })
    .catch(() => {});
}
