<template>
  <div
    class="sticky top-0 z-20 -mx-4 px-4 py-4 mb-6 bg-slate-100/95 backdrop-blur-sm border-b border-slate-200/80 sm:-mx-6 sm:px-6 lg:static lg:mx-0 lg:px-0 lg:py-0 lg:mb-8 lg:bg-transparent lg:border-0 lg:backdrop-blur-none"
  >
    <div
      class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between"
    >
      <div class="min-w-0">
        <h1 class="text-2xl sm:text-3xl font-bold text-slate-900">Dashboard</h1>
        <p class="text-slate-600 mt-1 text-sm sm:text-base">
          Welcome back, {{ username }}. Here's {{ monthLabel }} at a glance.
        </p>
      </div>

      <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between lg:justify-end">
        <div
          class="inline-flex items-center gap-1 rounded-xl border border-slate-200 bg-white px-2 py-1 shadow-sm self-start sm:self-auto"
        >
          <button
            type="button"
            class="btn-icon !w-9 !h-9"
            aria-label="Previous month"
            @click="$emit('previous-month')"
          >
            <svg
              class="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M15 19l-7-7 7-7"
              />
            </svg>
          </button>
          <span
            class="text-sm font-semibold text-slate-900 min-w-[8.5rem] text-center tabular-nums"
          >
            {{ monthLabel }}
          </span>
          <button
            type="button"
            class="btn-icon !w-9 !h-9"
            aria-label="Next month"
            @click="$emit('next-month')"
          >
            <svg
              class="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 5l7 7-7 7"
              />
            </svg>
          </button>
          <button
            type="button"
            class="btn-text !text-xs !px-2"
            @click="$emit('current-month')"
          >
            Today
          </button>
        </div>

        <div class="page-header-actions">
          <button type="button" class="btn-secondary btn-sm" @click="$emit('refresh')">
            Refresh
          </button>
          <button
            type="button"
            class="btn-secondary btn-sm hidden sm:inline-flex"
            @click="$emit('add-budget')"
          >
            Budget
          </button>
          <button type="button" class="btn-primary btn-sm" @click="$emit('add-transaction')">
            Add transaction
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "DashboardHeader",
  props: {
    username: { type: String, default: "User" },
    monthLabel: { type: String, required: true },
    loading: { type: Boolean, default: false },
  },
  emits: [
    "previous-month",
    "next-month",
    "current-month",
    "add-transaction",
    "add-budget",
    "refresh",
  ],
};
</script>
