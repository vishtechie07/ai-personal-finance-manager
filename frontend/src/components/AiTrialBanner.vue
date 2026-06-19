<template>
  <div
    v-if="showTrialBanner"
    class="mb-4 rounded-xl border px-4 py-3 text-sm"
    :class="bannerClass"
    role="status"
  >
    <div
      class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between"
    >
      <p>
        <span class="font-semibold">{{ headline }}</span>
        {{ body }}
      </p>
      <router-link
        to="/settings"
        class="btn-primary text-xs py-1.5 px-3 shrink-0 text-center"
      >
        {{ cta }}
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useAiStatus } from "../composables/useAiStatus";
import { formatTrialCountdown } from "../utils/aiSourceMessage";

const {
  showTrialBanner,
  platformTrialActive,
  platformTrialMinutes,
  secondsRemaining,
} = useAiStatus();

const headline = computed(() => {
  if (platformTrialActive.value) return "Free AI trial active.";
  return "Free AI trial ended.";
});

const body = computed(() => {
  const mins = platformTrialMinutes.value;
  if (platformTrialActive.value) {
    const left = formatTrialCountdown(secondsRemaining.value);
    return `You have ${left} left to try category suggestions, receipt scan, and monthly briefs on SpendSense’s hosted AI. Add your own OpenAI key in Settings anytime to keep going after the trial.`;
  }
  return `Your ${mins}-minute hosted AI trial is over. Add your OpenAI API key in Settings to continue using AI features (billed to your OpenAI account). Keyword categorization still works without a key.`;
});

const cta = computed(() =>
  platformTrialActive.value ? "Add your key" : "Add OpenAI key",
);

const bannerClass = computed(() =>
  platformTrialActive.value
    ? "border-primary-200 bg-primary-50 text-primary-900"
    : "border-amber-200 bg-amber-50 text-amber-950",
);
</script>
