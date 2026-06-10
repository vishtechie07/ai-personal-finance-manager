<template>
  <div class="dashboard-card p-6">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-semibold text-slate-900">Recent transactions</h3>
      <router-link
        to="/transactions"
        class="text-sm font-medium text-primary-600 hover:text-primary-700"
      >
        View all
      </router-link>
    </div>

    <div
      v-if="!transactions.length"
      class="text-center py-10 text-slate-500 text-sm"
    >
      No transactions for {{ monthLabel }}.
    </div>

    <div v-else class="overflow-x-auto -mx-2">
      <table class="min-w-full text-sm">
        <thead>
          <tr class="text-left text-xs uppercase tracking-wide text-slate-500 border-b border-slate-100">
            <th class="px-2 py-2 font-medium">Description</th>
            <th class="px-2 py-2 font-medium hidden sm:table-cell">Category</th>
            <th class="px-2 py-2 font-medium hidden md:table-cell">Date</th>
            <th class="px-2 py-2 font-medium text-right">Amount</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr
            v-for="tx in transactions"
            :key="tx.id"
            class="hover:bg-slate-50/80 transition-colors"
          >
            <td class="px-2 py-3 font-medium text-slate-900 max-w-[12rem] truncate">
              {{ tx.description }}
            </td>
            <td class="px-2 py-3 hidden sm:table-cell">
              <span class="category-pill">{{ formatCategoryLabel(tx.category) }}</span>
            </td>
            <td class="px-2 py-3 text-slate-500 hidden md:table-cell tabular-nums">
              {{ formatDate(tx.transactionDate) }}
            </td>
            <td
              class="px-2 py-3 text-right font-semibold tabular-nums"
              :class="tx.type === 'EXPENSE' ? 'text-danger-600' : 'text-success-600'"
            >
              {{ tx.type === "EXPENSE" ? "−" : "+" }}{{ formatMoney(tx.amount) }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import { formatCategoryLabel, formatMoney } from "../../utils/formatCategory";
import { format } from "date-fns";

export default {
  name: "DashboardRecentTransactions",
  props: {
    transactions: { type: Array, default: () => [] },
    monthLabel: { type: String, default: "" },
  },
  methods: {
    formatCategoryLabel,
    formatMoney,
    formatDate(date) {
      return format(new Date(date), "MMM d");
    },
  },
};
</script>
