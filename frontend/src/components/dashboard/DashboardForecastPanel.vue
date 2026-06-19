<template>
  <div class="dashboard-card p-6 space-y-6">
    <div>
      <p class="text-xs font-semibold uppercase tracking-wide text-primary-600">
        Projections
      </p>
      <h3 class="text-lg font-semibold text-slate-900 mt-1">
        Month outlook · {{ monthLabel }}
      </h3>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      <div class="rounded-xl border border-slate-200 bg-slate-50/80 p-4">
        <p class="text-sm font-medium text-slate-600">Month-end spend forecast</p>
        <p class="mt-1 text-2xl font-bold tabular-nums text-slate-900">
          {{ forecastExpense != null ? formatMoney(forecastExpense) : "—" }}
        </p>
        <p class="mt-2 text-xs text-slate-500">
          <template v-if="forecastExpense != null && currentExpenses > 0">
            Based on {{ formatMoney(currentExpenses) }} spent so far, projected to
            month end.
          </template>
          <template v-else>Add expenses this month to see a projection.</template>
        </p>
      </div>

      <div class="rounded-xl border border-slate-200 bg-slate-50/80 p-4">
        <p class="text-sm font-medium text-slate-600">Upcoming bills (30 days)</p>
        <p class="mt-1 text-2xl font-bold tabular-nums text-slate-900">
          {{ formatMoney(billsTotal) }}
        </p>
        <p class="mt-2 text-xs text-slate-500">
          {{ billTimeline.length }} unpaid bill{{
            billTimeline.length === 1 ? "" : "s"
          }}
          due soon
        </p>
      </div>
    </div>

    <section v-if="burnItems.length">
      <h4 class="text-sm font-medium text-slate-600 mb-3">Budget burn rate</h4>
      <ul class="space-y-2">
        <li
          v-for="item in burnItems"
          :key="item.id"
          class="text-sm rounded-lg px-3 py-2 border"
          :class="toneClass(item.tone)"
        >
          <span class="font-medium text-slate-900">{{ item.name }}</span>
          <span class="text-slate-600"> — {{ item.message }}</span>
        </li>
      </ul>
    </section>

    <section v-if="momDeltas.length">
      <h4 class="text-sm font-medium text-slate-600 mb-3">
        vs last month (categories)
      </h4>
      <ul class="space-y-2">
        <li
          v-for="row in momDeltas"
          :key="row.category"
          class="flex items-center justify-between gap-3 text-sm"
        >
          <span class="text-slate-800">{{ formatCategoryLabel(row.category) }}</span>
          <span
            class="tabular-nums font-medium shrink-0"
            :class="row.delta > 0 ? 'text-danger-600' : 'text-success-600'"
          >
            {{ row.delta > 0 ? "+" : "" }}{{ formatMoney(row.delta) }}
            <span v-if="row.pct != null" class="text-slate-500 font-normal text-xs">
              ({{ row.pct > 0 ? "+" : "" }}{{ row.pct.toFixed(0) }}%)
            </span>
          </span>
        </li>
      </ul>
    </section>

    <section v-if="billTimeline.length">
      <h4 class="text-sm font-medium text-slate-600 mb-3">Bill timeline</h4>
      <ul class="space-y-2">
        <li
          v-for="bill in billTimeline"
          :key="bill.id"
          class="grid grid-cols-[minmax(0,1fr)_4.5rem_5.5rem] items-center gap-x-3 text-sm"
        >
          <span class="text-slate-800 truncate min-w-0">{{ bill.payeeName }}</span>
          <span class="text-slate-500 tabular-nums whitespace-nowrap text-right">{{
            formatDue(bill.due)
          }}</span>
          <span class="tabular-nums font-medium text-right whitespace-nowrap">{{
            formatMoney(bill.amount)
          }}</span>
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { formatCategoryLabel, formatMoney } from "../../utils/formatCategory";

const props = defineProps({
  monthLabel: { type: String, default: "" },
  forecastExpense: { type: Number, default: null },
  currentExpenses: { type: Number, default: 0 },
  burnItems: { type: Array, default: () => [] },
  momDeltas: { type: Array, default: () => [] },
  billTimeline: { type: Array, default: () => [] },
});

const billsTotal = computed(() =>
  props.billTimeline.reduce((s, b) => s + parseFloat(b.amount || 0), 0),
);

function toneClass(tone) {
  if (tone === "danger") return "border-danger-100 bg-danger-50 text-danger-800";
  if (tone === "warning") return "border-warning-100 bg-warning-50 text-warning-800";
  if (tone === "positive") return "border-success-100 bg-success-50 text-success-800";
  return "border-slate-200 bg-white text-slate-700";
}

function formatDue(d) {
  if (!d) return "—";
  return d.toLocaleDateString(undefined, { month: "short", day: "numeric" });
}
</script>
