/** User-facing copy for AI endpoint `source` values. */
export function aiSourceMessage(source) {
  switch (source) {
    case "platform_trial_expired":
      return "Your free AI trial has ended. Add your OpenAI key in Settings to keep using AI features.";
    case "no_api_key":
      return "Add your OpenAI key in Settings to use AI features.";
    case "rate_limited":
      return "AI rate limit reached. Try again later or add your own OpenAI key in Settings.";
    case "demo_not_allowed":
      return "Shared demo account cannot use hosted AI. Add your own OpenAI key in Settings.";
    case "account_too_new":
      return "Hosted AI is not available for very new accounts yet.";
    case "platform_disabled":
      return "Hosted AI is not enabled on this server.";
    default:
      return null;
  }
}

export function formatTrialCountdown(secondsRemaining) {
  if (secondsRemaining == null || secondsRemaining <= 0) return "0:00";
  const m = Math.floor(secondsRemaining / 60);
  const s = secondsRemaining % 60;
  return `${m}:${String(s).padStart(2, "0")}`;
}

export function parseTrialEndsAt(endsAt) {
  if (!endsAt) return null;
  const normalized = endsAt.includes("T") ? endsAt : endsAt.replace(" ", "T");
  const ms = Date.parse(normalized);
  return Number.isNaN(ms) ? null : ms;
}

export function trialSecondsRemaining(endsAt) {
  const endMs = parseTrialEndsAt(endsAt);
  if (endMs == null) return null;
  return Math.max(0, Math.ceil((endMs - Date.now()) / 1000));
}
