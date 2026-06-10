<template>
  <div class="dashboard-card p-6 h-full">
    <h3 class="text-lg font-semibold text-slate-900 mb-4">This month</h3>

    <section class="mb-6">
      <div class="flex items-center justify-between mb-3">
        <h4 class="text-sm font-medium text-slate-600">Budget health</h4>
        <router-link
          to="/budgets"
          class="text-xs font-medium text-primary-600 hover:text-primary-700"
        >
          Manage
        </router-link>
      </div>
      <p v-if="!budgetHealthItems.length" class="text-sm text-slate-500">
        No budgets for this month.
      </p>
      <div v-else class="space-y-3">
        <div v-for="item in budgetHealthItems.slice(0, 4)" :key="item.id">
          <div class="flex justify-between text-sm mb-1">
            <router-link
              :to="item.link"
              class="font-medium text-slate-900 hover:text-primary-600 truncate pr-2"
            >
              {{ item.name }}
            </router-link>
            <span class="tabular-nums text-slate-600 shrink-0">
              {{ item.pct.toFixed(0) }}%
            </span>
          </div>
          <div class="h-1.5 bg-slate-200 rounded-full overflow-hidden">
            <div
              class="h-full rounded-full transition-all duration-500"
              :class="item.barClass"
              :style="{ width: Math.min(item.pct, 100) + '%' }"
            />
          </div>
        </div>
      </div>
    </section>

    <section v-if="dueSoonBills.length" class="mb-6 pt-6 border-t border-slate-100">
      <div class="flex items-center justify-between mb-3">
        <h4 class="text-sm font-medium text-slate-600">Bills due soon</h4>
        <router-link
          to="/bills"
          class="text-xs font-medium text-primary-600 hover:text-primary-700"
        >
          View all
        </router-link>
      </div>
      <ul class="space-y-2">
        <li
          v-for="bill in dueSoonBills.slice(0, 4)"
          :key="bill.id"
          class="flex items-center justify-between gap-3 text-sm"
        >
          <span class="text-slate-800 truncate">{{ bill.payeeName }}</span>
          <span class="shrink-0 tabular-nums font-medium text-slate-900">
            {{ formatMoney(bill.amount) }}
          </span>
        </li>
      </ul>
    </section>

    <section
      v-if="alerts.length || attentionBudgets.length"
      class="pt-6 border-t border-slate-100"
    >
      <h4 class="text-sm font-medium text-slate-600 mb-3">Needs attention</h4>
      <ul class="space-y-2">
        <li
          v-for="item in attentionBudgets"
          :key="'att-' + item.id"
          class="text-sm text-warning-700 bg-warning-50 border border-warning-100 rounded-lg px-3 py-2"
        >
          <router-link :to="item.link" class="hover:underline">
            {{ item.message }}
          </router-link>
        </li>
        <li
          v-for="alert in alerts"
          :key="alert.id"
          class="text-sm rounded-lg px-3 py-2 border"
          :class="
            alert.type === 'danger'
              ? 'text-danger-700 bg-danger-50 border-danger-100'
              : 'text-warning-700 bg-warning-50 border-warning-100'
          "
        >
          <span class="font-medium">{{ alert.title }}</span>
          <span class="text-slate-600"> — {{ alert.message }}</span>
        </li>
      </ul>
    </section>

    <p
      v-if="
        !budgetHealthItems.length &&
        !dueSoonBills.length &&
        !alerts.length &&
        !attentionBudgets.length
      "
      class="text-sm text-slate-500"
    >
      You're all caught up for this month.
    </p>
  </div>
</template>

<script>
import { formatMoney } from "../../utils/formatCategory";

export default {
  name: "DashboardTodayPanel",
  props: {
    budgetHealthItems: { type: Array, default: () => [] },
    dueSoonBills: { type: Array, default: () => [] },
    attentionBudgets: { type: Array, default: () => [] },
    alerts: { type: Array, default: () => [] },
  },
  methods: { formatMoney },
};
</script>
