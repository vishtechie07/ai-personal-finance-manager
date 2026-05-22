import { ref, computed } from "vue";
import axios from "axios";

const hasOpenAiApiKey = ref(false);
const platformAiEnabled = ref(false);
const loaded = ref(false);
let loadPromise = null;

export function useAiStatus() {
  const aiAvailable = computed(
    () => hasOpenAiApiKey.value || platformAiEnabled.value,
  );

  const label = computed(() => {
    if (!loaded.value) return "AI…";
    if (hasOpenAiApiKey.value) return "AI: Your key";
    if (platformAiEnabled.value) return "AI: SpendSense";
    return "AI: Keywords only";
  });

  const chipClass = computed(() => {
    if (!loaded.value) return "bg-slate-100 text-slate-600";
    if (aiAvailable.value) return "bg-primary-50 text-primary-800";
    return "bg-amber-50 text-amber-800";
  });

  async function refresh() {
    if (!loadPromise) {
      loadPromise = axios
        .get("/settings")
        .then(({ data }) => {
          hasOpenAiApiKey.value = !!data?.hasOpenAiApiKey;
          platformAiEnabled.value = !!data?.platformAiEnabled;
          loaded.value = true;
        })
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
    loaded.value = false;
  }

  return { label, chipClass, aiAvailable, loaded, refresh, reset };
}
