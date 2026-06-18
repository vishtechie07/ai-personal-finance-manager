<template>
  <div class="animate-pulse space-y-3" aria-hidden="true">
    <div
      v-for="i in rows"
      :key="i"
      class="h-4 rounded-lg bg-gradient-to-r from-slate-100 via-slate-200/80 to-slate-100"
      :style="{ width: widths[i % widths.length] }"
    />
  </div>
</template>

<script setup>
defineProps({
  rows: { type: Number, default: 4 },
});

const widths = ["100%", "92%", "78%", "88%", "65%"];
</script>
