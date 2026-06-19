import { ref, computed } from "vue";
import axios from "axios";
import { trialSecondsRemaining } from "../utils/aiSourceMessage";

const hasOpenAiApiKey = ref(false);
const platformAiEnabled = ref(false);
const platformTrialConfigured = ref(false);
const platformTrialActive = ref(false);
const platformTrialExpired = ref(false);
const platformTrialMinutes = ref(0);
const platformTrialEndsAt = ref(null);
const serverTrialSeconds = ref(null);
const trialSyncedAt = ref(0);
const loaded = ref(false);
const tick = ref(0);

let loadPromise = null;
let tickInterval = null;

function ensureTickInterval(refreshFn) {
  if (tickInterval) return;
  tickInterval = setInterval(() => {
    tick.value += 1;
    if (!platformTrialActive.value) return;
    const left = trialSecondsRemaining(
      platformTrialEndsAt.value,
      serverTrialSeconds.value != null
        ? Math.max(
            0,
            serverTrialSeconds.value -
              Math.floor((Date.now() - trialSyncedAt.value) / 1000),
          )
        : null,
    );
    if (left === 0) {
      refreshFn();
    }
  }, 1000);
}

function clearTickInterval() {
  if (tickInterval) {
    clearInterval(tickInterval);
    tickInterval = null;
  }
}

export function useAiStatus() {
  const secondsRemaining = computed(() => {
    tick.value;
    if (!platformTrialActive.value) return null;
    if (serverTrialSeconds.value != null) {
      return Math.max(
        0,
        serverTrialSeconds.value -
          Math.floor((Date.now() - trialSyncedAt.value) / 1000),
      );
    }
    return trialSecondsRemaining(platformTrialEndsAt.value);
  });

  const aiAvailable = computed(
    () =>
      hasOpenAiApiKey.value ||
      platformTrialActive.value ||
      (platformAiEnabled.value && !platformTrialConfigured.value),
  );

  const label = computed(() => {
    if (!loaded.value) return "AI…";
    if (hasOpenAiApiKey.value) return "AI: Your key";
    if (platformTrialActive.value && secondsRemaining.value != null) {
      const m = Math.floor(secondsRemaining.value / 60);
      const s = secondsRemaining.value % 60;
      return `AI trial ${m}:${String(s).padStart(2, "0")}`;
    }
    if (platformTrialExpired.value) return "AI trial ended";
    if (platformAiEnabled.value) return "AI: SpendSense";
    return "AI: Keywords only";
  });

  const chipClass = computed(() => {
    if (!loaded.value) return "bg-slate-100 text-slate-600";
    if (hasOpenAiApiKey.value || platformTrialActive.value) {
      return "bg-primary-50 text-primary-800";
    }
    if (platformTrialExpired.value) return "bg-amber-50 text-amber-900";
    if (aiAvailable.value) return "bg-primary-50 text-primary-800";
    return "bg-amber-50 text-amber-800";
  });

  const showTrialBanner = computed(() => {
    if (!loaded.value || hasOpenAiApiKey.value) return false;
    if (!platformTrialConfigured.value) return false;
    return platformTrialActive.value || platformTrialExpired.value;
  });

  function applySettings(data) {
    hasOpenAiApiKey.value = !!data?.hasOpenAiApiKey;
    platformAiEnabled.value = !!data?.platformAiEnabled;
    platformTrialConfigured.value = !!data?.platformTrialConfigured;
    platformTrialActive.value = !!data?.platformTrialActive;
    platformTrialExpired.value = !!data?.platformTrialExpired;
    platformTrialMinutes.value = Number(data?.platformTrialMinutes) || 0;
    platformTrialEndsAt.value = data?.platformTrialEndsAt || null;
    const secs = data?.platformTrialSecondsRemaining;
    serverTrialSeconds.value =
      secs != null && secs !== "" ? Number(secs) : null;
    trialSyncedAt.value = Date.now();
    loaded.value = true;
    if (platformTrialActive.value) {
      ensureTickInterval(refresh);
    } else {
      clearTickInterval();
    }
  }

  async function refresh(options = {}) {
    const force = options.force === true;
    if (force) {
      if (loadPromise) {
        try {
          await loadPromise;
        } catch {
          /* ignore */
        }
      }
      loadPromise = null;
    }
    if (!loadPromise) {
      loadPromise = axios
        .get("/settings")
        .then(({ data }) => applySettings(data))
        .catch(() => {
          loaded.value = true;
        })
        .finally(() => {
          loadPromise = null;
        });
    }
    await loadPromise;
  }

  function reset() {
    hasOpenAiApiKey.value = false;
    platformAiEnabled.value = false;
    platformTrialConfigured.value = false;
    platformTrialActive.value = false;
    platformTrialExpired.value = false;
    platformTrialMinutes.value = 0;
    platformTrialEndsAt.value = null;
    serverTrialSeconds.value = null;
    trialSyncedAt.value = 0;
    loaded.value = false;
    clearTickInterval();
  }

  return {
    label,
    chipClass,
    aiAvailable,
    loaded,
    hasOpenAiApiKey,
    platformTrialActive,
    platformTrialExpired,
    platformTrialMinutes,
    platformTrialConfigured,
    secondsRemaining,
    showTrialBanner,
    refresh,
    reset,
    applySettings,
  };
}
