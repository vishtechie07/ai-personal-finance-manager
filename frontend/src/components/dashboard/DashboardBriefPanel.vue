<template>
  <div class="dashboard-card p-5 border border-primary-100/80 bg-gradient-to-br from-white to-primary-50/40">
    <div class="flex items-start justify-between gap-3 mb-3">
      <div>
        <p class="text-xs font-semibold uppercase tracking-wide text-primary-600">
          AI insight
        </p>
        <h3 class="text-lg font-semibold text-slate-900">Monthly brief</h3>
      </div>
      <button
        type="button"
        class="btn-primary btn-sm shrink-0"
        :disabled="loading || !yearMonth"
        @click="$emit('generate')"
      >
        {{ loading ? "…" : briefBullets.length ? "Refresh" : "Generate" }}
      </button>
    </div>

    <LoadingSkeleton v-if="loading" :rows="3" />

    <template v-else-if="briefBullets.length || briefRecommendation">
      <ul class="space-y-2 text-sm text-slate-700">
        <li
          v-for="(b, i) in briefBullets.slice(0, 3)"
          :key="i"
          class="flex gap-2"
        >
          <span class="text-primary-500 shrink-0">•</span>
          <span>{{ b }}</span>
        </li>
      </ul>
      <p
        v-if="briefRecommendation"
        class="mt-3 text-sm text-primary-900 bg-white/80 rounded-lg px-3 py-2 border border-primary-100"
      >
        {{ briefRecommendation }}
      </p>
    </template>

    <p v-else class="text-sm text-slate-500">
      Get a concise summary of spending patterns and one actionable tip for
      {{ monthLabel }}.
    </p>
  </div>
</template>

<script setup>
import LoadingSkeleton from "../LoadingSkeleton.vue";

defineProps({
  briefBullets: { type: Array, default: () => [] },
  briefRecommendation: { type: String, default: "" },
  loading: { type: Boolean, default: false },
  yearMonth: { type: String, default: "" },
  monthLabel: { type: String, default: "" },
});

defineEmits(["generate"]);
</script>
