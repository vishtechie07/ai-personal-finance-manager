<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
    <StatCard
      v-for="item in items"
      :key="item.key"
      :label="item.label"
      :value="item.value"
      :tone="item.tone"
      :hint="item.hint"
      :delta="item.delta"
      :loading="loading"
    />
  </div>
</template>

<script>
import StatCard from "./StatCard.vue";

export default {
  name: "DashboardKpiGrid",
  components: { StatCard },
  props: {
    items: { type: Array, default: () => [] },
    loading: { type: Boolean, default: false },
  },
};
</script>
