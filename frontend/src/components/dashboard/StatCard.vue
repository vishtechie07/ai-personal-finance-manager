<template>
  <div class="dashboard-card p-5">
    <div v-if="loading" class="animate-pulse space-y-3">
      <div class="h-3 w-24 bg-slate-200 rounded" />
      <div class="h-8 w-32 bg-slate-200 rounded" />
    </div>
    <template v-else>
      <p class="text-sm font-medium text-slate-600">{{ label }}</p>
      <p
        class="mt-1 text-2xl font-bold tabular-nums"
        :class="valueClass"
      >
        {{ value }}
      </p>
      <p v-if="hint" class="mt-1 text-xs text-slate-500">{{ hint }}</p>
      <p
        v-if="delta !== null && delta !== undefined"
        class="mt-2 text-xs font-medium tabular-nums"
        :class="deltaClass"
      >
        {{ deltaLabel }}
      </p>
    </template>
  </div>
</template>

<script>
export default {
  name: "StatCard",
  props: {
    label: { type: String, required: true },
    value: { type: String, required: true },
    tone: {
      type: String,
      default: "neutral",
      validator: (v) =>
        ["neutral", "positive", "negative", "accent"].includes(v),
    },
    hint: { type: String, default: "" },
    delta: { type: Number, default: null },
    loading: { type: Boolean, default: false },
  },
  computed: {
    valueClass() {
      if (this.tone === "positive") return "text-success-600";
      if (this.tone === "negative") return "text-danger-600";
      if (this.tone === "accent") return "text-primary-600";
      return "text-slate-900";
    },
    deltaClass() {
      if (this.delta > 0) return "text-success-600";
      if (this.delta < 0) return "text-danger-600";
      return "text-slate-500";
    },
    deltaLabel() {
      const sign = this.delta > 0 ? "+" : "";
      return `${sign}${this.delta.toFixed(1)}% vs last month`;
    },
  },
};
</script>
