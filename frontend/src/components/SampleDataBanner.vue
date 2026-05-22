<template>
  <div
    v-if="visible"
    class="mb-4 rounded-xl border border-primary-200 bg-primary-50 px-4 py-3 text-sm text-primary-900"
    role="status"
  >
    <div
      class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between"
    >
      <p>
        <span class="font-semibold">Sample data loaded.</span>
        We added demo transactions and budgets so you can explore SpendSense.
        Clear them when you are ready to track your own finances.
      </p>
      <div class="flex shrink-0 gap-2">
        <button
          type="button"
          class="btn-secondary text-xs py-1.5 px-3"
          :disabled="clearing"
          @click="dismiss"
        >
          Dismiss
        </button>
        <button
          type="button"
          class="btn-primary text-xs py-1.5 px-3"
          :disabled="clearing"
          @click="clear"
        >
          {{ clearing ? "Clearing…" : "Clear sample data" }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue";
import axios from "axios";
import { useAuthStore } from "../stores/auth";
import { useTransactionsStore } from "../stores/transactions";
import { useBudgetsStore } from "../stores/budgets";
import { useBillsStore } from "../stores/bills";
import {
  dismissSampleBanner,
  isSampleBannerDismissed,
} from "../composables/useOnboarding";
import { getApiErrorMessage } from "../utils/apiError";
import { useToast } from "../composables/useToast";

const authStore = useAuthStore();
const transactionsStore = useTransactionsStore();
const budgetsStore = useBudgetsStore();
const billsStore = useBillsStore();
const toast = useToast();

const clearing = ref(false);
const dismissed = ref(false);

const visible = computed(() => {
  if (!authStore.isAuthenticated || dismissed.value) return false;
  const uid = authStore.user?.id;
  if (uid && isSampleBannerDismissed(uid)) return false;
  return (transactionsStore.transactions?.length ?? 0) > 0;
});

function dismiss() {
  const uid = authStore.user?.id;
  if (uid) dismissSampleBanner(uid);
  dismissed.value = true;
}

async function clear() {
  clearing.value = true;
  try {
    await axios.delete("/auth/me/sample-data");
    await Promise.all([
      transactionsStore.fetchTransactions(),
      budgetsStore.fetchBudgets(),
      billsStore.fetchBills(),
    ]);
    dismiss();
    toast.success("Sample data cleared. Add your first transaction!");
  } catch (e) {
    toast.error(getApiErrorMessage(e));
  } finally {
    clearing.value = false;
  }
}

watch(
  () => authStore.user?.id,
  () => {
    dismissed.value = false;
  },
);

onMounted(async () => {
  if (authStore.isAuthenticated && !transactionsStore.transactions?.length) {
    try {
      await transactionsStore.fetchTransactions();
    } catch {
      /* banner stays hidden until data loads */
    }
  }
});
</script>
