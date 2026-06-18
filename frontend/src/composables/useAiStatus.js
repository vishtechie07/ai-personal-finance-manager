import { ref, computed, onUnmounted } from "vue";
import axios from "axios";
import { trialSecondsRemaining } from "../utils/aiSourceMessage";

const hasOpenAiApiKey = ref(false);
const platformAiEnabled = ref(false);
const platformTrialConfigured = ref(false);
const platformTrialActive = ref(false);
const platformTrialExpired = ref(false);
const platformTrialMinutes = ref(0);
const platformTrialEndsAt = ref(null);
const loaded = ref(false);
const tick = ref(0);

let loadPromise = null;
let tickInterval = null;

function ensureTickInterval() {
  if (tickInterval) return;
  tickInterval = setInterval(() => {
    tick.value += 1;
    if (
      platformTrialActive.value &&
      trialSecondsRemaining(platformTrialEndsAt.value) === 0
    ) {
      platformTrialActive.value = false;
      platformTrialExpired.value = true;
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
    return trialSecondsRemaining(platformTrialEndsAt.value);
  });

  const aiAvailable = computed(
    () => hasOpenAiApiKey.value || platformTrialActive.value || (platformAiEnabled.value && !platformTrialConfigured.value),
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

  const showTrialBanner = computed(
    () =>
      loaded.value &&
      !hasOpenAiApiKey.value &&
      platformTrialConfigured.value &&
      (platformTrialActive.value || platformTrialExpired.value),
  );

  function applySettings(data) {
    hasOpenAiApiKey.value = !!data?.hasOpenAiApiKey;
    platformAiEnabled.value = !!data?.platformAiEnabled;
    platformTrialConfigured.value = !!data?.platformTrialConfigured;
    platformTrialActive.value = !!data?.platformTrialActive;
    platformTrialExpired.value = !!data?.platformTrialExpired;
    platformTrialMinutes.value = Number(data?.platformTrialMinutes) || 0;
    platformTrialEndsAt.value = data?.platformTrialEndsAt || null;
    loaded.value = true;
    if (platformTrialActive.value) {
      ensureTickInterval();
    } else {
      clearTickInterval();
    }
  }

  async function refresh() {
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
    loaded.value = false;
    clearTickInterval();
  }

  onUnmounted(() => {
    /* shared singleton — interval cleared on reset */
  });

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
