<template>
  <div class="space-y-6 pt-6">
    <div
      class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between"
    >
      <div class="min-w-0">
        <h1 class="text-3xl font-bold text-slate-900">Transactions</h1>
        <p class="text-slate-600 mt-2">Manage your financial transactions</p>
      </div>
      <button type="button" class="btn-primary shrink-0" @click="openModal()">
        <svg
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          aria-hidden="true"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 6v6m0 0v6m0-6h6m-6 0H6"
          ></path>
        </svg>
        <span>Add Transaction</span>
      </button>
    </div>

    <div class="glass-card p-4 space-y-4">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <h3 class="text-lg font-semibold text-slate-900">
          {{ currentMonthDisplay }}
        </h3>
        <div class="flex items-center gap-2">
          <button
            type="button"
            class="btn-icon"
            aria-label="Previous month"
            @click="navMonth(-1)"
          >
            ‹
          </button>
          <button type="button" class="btn-text" @click="goToCurrentMonth">
            Current
          </button>
          <button
            type="button"
            class="btn-icon"
            aria-label="Next month"
            @click="navMonth(1)"
          >
            ›
          </button>
        </div>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-4 gap-3">
        <input
          v-model="localFilters.search"
          type="search"
          placeholder="Search description…"
          class="input-field"
          @input="debouncedFetch"
        />
        <select
          v-model="localFilters.category"
          class="input-field"
          @change="applyFilters"
        >
          <option value="">All categories</option>
          <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
        </select>
        <select
          v-model="localFilters.type"
          class="input-field"
          @change="applyFilters"
        >
          <option value="">All types</option>
          <option value="EXPENSE">Expense</option>
          <option value="INCOME">Income</option>
        </select>
        <select
          v-model="localFilters.sort"
          class="input-field"
          @change="applyFilters"
        >
          <option value="dateDesc">Newest first</option>
          <option value="dateAsc">Oldest first</option>
          <option value="amountDesc">Amount high–low</option>
          <option value="amountAsc">Amount low–high</option>
        </select>
      </div>
    </div>

    <div class="glass-card p-6">
      <p class="text-sm text-slate-500 mb-4">
        {{ transactions.length }} transaction(s)
      </p>
      <div class="space-y-3">
        <div
          v-for="transaction in transactions"
          :key="transaction.id"
          class="flex items-center justify-between p-3 bg-slate-50 rounded-lg border border-slate-100"
        >
          <div>
            <p class="font-medium text-slate-900">
              {{ transaction.description }}
            </p>
            <p class="text-sm text-slate-500">{{ transaction.category }}</p>
          </div>
          <div class="flex items-center gap-4">
            <div class="text-right">
              <p
                class="font-medium"
                :class="
                  transaction.type === 'EXPENSE'
                    ? 'text-red-600'
                    : 'text-green-600'
                "
              >
                {{ transaction.type === "EXPENSE" ? "-" : "+" }}${{
                  Number(transaction.amount).toFixed(2)
                }}
              </p>
              <p class="text-sm text-slate-500">
                {{ formatDate(transaction.transactionDate) }}
              </p>
            </div>
            <div class="flex gap-2">
              <button
                class="text-xs text-primary-600 hover:underline"
                @click="openModal(transaction)"
              >
                Edit
              </button>
              <button
                class="text-xs text-red-600 hover:underline"
                @click="remove(transaction.id)"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
        <div
          v-if="!transactions.length"
          class="text-center py-8 text-slate-500"
        >
          No transactions found
        </div>
      </div>
    </div>

    <div
      v-if="showModal"
      class="fixed inset-0 bg-slate-900/40 z-50 flex items-center justify-center p-4 overflow-y-auto"
    >
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6 my-8">
        <h3 class="text-lg font-bold mb-4">
          {{ editingId ? "Edit" : "Add" }} Transaction
        </h3>
        <form class="space-y-4" @submit.prevent="save">
          <input
            v-model="form.description"
            class="input-field w-full"
            placeholder="Description"
            required
          />
          <input
            v-model.number="form.amount"
            type="number"
            step="0.01"
            class="input-field w-full"
            placeholder="Amount"
            required
          />
          <select v-model="form.type" class="input-field w-full">
            <option value="EXPENSE">Expense</option>
            <option value="INCOME">Income</option>
          </select>
          <input
            v-model="form.transactionDate"
            type="date"
            class="input-field w-full"
            required
          />
          <input
            v-model="form.category"
            class="input-field w-full"
            placeholder="Category"
            required
          />
          <div v-if="editingId" class="space-y-2 border-t pt-3">
            <label class="text-sm font-medium text-slate-700">Receipt</label>
            <input
              type="file"
              accept="image/*,application/pdf"
              @change="onFile"
            />
            <button
              v-if="receiptFile"
              type="button"
              class="text-sm text-primary-600"
              @click="scanReceipt"
            >
              Scan receipt (AI)
            </button>
            <a
              v-if="hasReceipt"
              :href="receiptUrl"
              target="_blank"
              class="text-sm text-primary-600 block"
              >Download receipt</a
            >
          </div>
          <div class="flex gap-2">
            <button
              type="button"
              class="btn-secondary flex-1"
              @click="showModal = false"
            >
              Cancel
            </button>
            <button type="submit" class="btn-primary flex-1" :disabled="saving">
              {{ saving ? "Saving…" : "Save" }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import axios from "axios";
import { useTransactionsStore } from "../stores/transactions";
import { useBudgetsStore } from "../stores/budgets";
import { useToast } from "../composables/useToast";
import { getApiErrorMessage } from "../utils/apiError";
import {
  resolveCategory,
  TRANSACTION_KEYWORDS,
} from "../utils/categorySuggestion";

const transactionsStore = useTransactionsStore();
const budgetsStore = useBudgetsStore();
const route = useRoute();
const toast = useToast();

const categories = [
  "FOOD_GROCERIES",
  "FOOD_RESTAURANT",
  "TRANSPORTATION",
  "HOUSING_RENT",
  "HOUSING_UTILITIES",
  "HOUSING_MAINTENANCE",
  "HEALTHCARE",
  "ENTERTAINMENT",
  "SHOPPING",
  "EDUCATION",
  "TRAVEL",
  "INSURANCE",
  "INVESTMENTS",
  "SALARY",
  "FREELANCE",
  "OTHER",
];

const showModal = ref(false);
const editingId = ref(null);
const saving = ref(false);
const receiptFile = ref(null);
const hasReceipt = ref(false);
const receiptUrl = ref("");
let debounceTimer = null;

const localFilters = ref({
  search: "",
  category: "",
  type: "",
  sort: "dateDesc",
});
const form = ref({
  description: "",
  amount: "",
  type: "EXPENSE",
  category: "",
  transactionDate: new Date().toISOString().split("T")[0],
});

const transactions = computed(() => transactionsStore.transactions);
const currentMonthDisplay = computed(() => {
  const names = [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
  ];
  return `${names[transactionsStore.currentMonth]} ${transactionsStore.currentYear}`;
});

const formatDate = (d) => new Date(d).toLocaleDateString();

const applyFilters = () => {
  transactionsStore.setFilters({ ...localFilters.value });
  transactionsStore.fetchTransactionsForMonth();
};

const debouncedFetch = () => {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(applyFilters, 300);
};

const navMonth = (delta) => {
  if (delta < 0) transactionsStore.previousMonth();
  else transactionsStore.nextMonth();
  budgetsStore.setCurrentMonth(
    transactionsStore.currentMonth,
    transactionsStore.currentYear,
  );
  applyFilters();
};

const goToCurrentMonth = () => {
  transactionsStore.goToCurrentMonth();
  budgetsStore.goToCurrentMonth();
  applyFilters();
};

const openModal = async (tx = null) => {
  editingId.value = tx?.id ?? null;
  receiptFile.value = null;
  hasReceipt.value = false;
  if (tx) {
    form.value = {
      description: tx.description,
      amount: tx.amount,
      type: tx.type,
      category: tx.category,
      transactionDate:
        tx.transactionDate?.split("T")[0] ||
        new Date().toISOString().split("T")[0],
    };
    try {
      await axios.get(`/transactions/${tx.id}/receipt/meta`);
      hasReceipt.value = true;
      receiptUrl.value = `/api/transactions/${tx.id}/receipt`;
    } catch {
      hasReceipt.value = false;
    }
  } else {
    form.value = {
      description: "",
      amount: "",
      type: "EXPENSE",
      category: "",
      transactionDate: new Date().toISOString().split("T")[0],
    };
  }
  showModal.value = true;
};

const onFile = (e) => {
  receiptFile.value = e.target.files?.[0] || null;
};

const scanReceipt = async () => {
  if (!receiptFile.value) return;
  const fd = new FormData();
  fd.append("file", receiptFile.value);
  try {
    const res = await axios.post("/ai/extract-receipt", fd);
    if (res.data?.description) form.value.description = res.data.description;
    if (res.data?.amount) form.value.amount = res.data.amount;
    if (res.data?.date) form.value.transactionDate = res.data.date;
    toast.success("Receipt scanned — review and save");
  } catch (e) {
    toast.error(getApiErrorMessage(e));
  }
};

const uploadReceipt = async (txId) => {
  if (!receiptFile.value) return;
  const fd = new FormData();
  fd.append("file", receiptFile.value);
  await axios.post(`/transactions/${txId}/receipt`, fd);
};

const save = async () => {
  saving.value = true;
  try {
    if (!form.value.category && form.value.description) {
      const cat = await resolveCategory(
        form.value.description,
        TRANSACTION_KEYWORDS,
      );
      if (cat) form.value.category = cat;
    }
    let tx;
    if (editingId.value) {
      tx = await transactionsStore.updateTransaction(
        editingId.value,
        form.value,
      );
      toast.success("Transaction updated");
    } else {
      tx = await transactionsStore.addTransaction(form.value);
      toast.success("Transaction added");
    }
    if (receiptFile.value && tx?.id) await uploadReceipt(tx.id);
    showModal.value = false;
    await budgetsStore.fetchBudgets();
  } catch (e) {
    toast.error(getApiErrorMessage(e));
  } finally {
    saving.value = false;
  }
};

const remove = async (id) => {
  if (!confirm("Delete this transaction?")) return;
  try {
    await transactionsStore.deleteTransaction(id);
    toast.success("Transaction deleted");
    await budgetsStore.fetchBudgets();
  } catch (e) {
    toast.error(getApiErrorMessage(e));
  }
};

watch(
  () => route.query.category,
  (cat) => {
    if (cat) {
      localFilters.value.category = String(cat);
      applyFilters();
    }
  },
  { immediate: true },
);

onMounted(async () => {
  if (route.query.category)
    localFilters.value.category = String(route.query.category);
  transactionsStore.setFilters(localFilters.value);
  await transactionsStore.fetchTransactionsForMonth();
  if (!budgetsStore.isInitialized) await budgetsStore.fetchBudgets();
});
</script>
