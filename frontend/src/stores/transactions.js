import { defineStore } from "pinia";
import { ref, computed } from "vue";
import axios from "axios";

export const useTransactionsStore = defineStore("transactions", () => {
  const transactions = ref([]);
  const isLoading = ref(false);
  const error = ref(null);
  const isInitialized = ref(false);
  const currentMonth = ref(new Date().getMonth());
  const currentYear = ref(new Date().getFullYear());
  const filters = ref({ search: "", category: "", type: "", sort: "dateDesc" });

  const recentTransactions = computed(() => transactions.value.slice(0, 5));

  const totalIncome = computed(() =>
    transactions.value
      .filter((t) => t.type === "INCOME")
      .reduce((sum, t) => sum + parseFloat(t.amount || 0), 0),
  );

  const totalExpenses = computed(() =>
    transactions.value
      .filter((t) => t.type === "EXPENSE")
      .reduce((sum, t) => sum + parseFloat(t.amount || 0), 0),
  );

  const totalBalance = computed(() => totalIncome.value - totalExpenses.value);

  const transactionsByMonth = computed(() => transactions.value);

  const currentMonthIncome = computed(() =>
    transactionsByMonth.value
      .filter((t) => t.type === "INCOME")
      .reduce((sum, t) => sum + parseFloat(t.amount || 0), 0),
  );

  const currentMonthExpenses = computed(() =>
    transactionsByMonth.value
      .filter((t) => t.type === "EXPENSE")
      .reduce((sum, t) => sum + parseFloat(t.amount || 0), 0),
  );

  const currentMonthBalance = computed(
    () => currentMonthIncome.value - currentMonthExpenses.value,
  );

  const yearMonthParam = () => {
    const y = currentYear.value;
    const m = String(currentMonth.value + 1).padStart(2, "0");
    return `${y}-${m}`;
  };

  const fetchTransactions = async () => {
    await fetchTransactionsForMonth();
  };

  const fetchTransactionsForMonth = async (override = {}) => {
    if (isLoading.value) return;
    isLoading.value = true;
    error.value = null;
    const f = { ...filters.value, ...override };
    try {
      const params = new URLSearchParams();
      if (f.search) params.set("search", f.search);
      if (f.category) params.set("category", f.category);
      if (f.type) params.set("type", f.type);
      if (f.sort) params.set("sort", f.sort);
      const qs = params.toString();
      const url = `/transactions/month/${yearMonthParam()}${qs ? `?${qs}` : ""}`;
      const response = await axios.get(url);
      transactions.value = Array.isArray(response.data) ? response.data : [];
      isInitialized.value = true;
    } catch (err) {
      error.value = err.message;
      transactions.value = [];
    } finally {
      isLoading.value = false;
    }
  };

  const addTransaction = async (transactionData) => {
    isLoading.value = true;
    error.value = null;
    try {
      const transactionToAdd = {
        description: transactionData.description,
        amount: transactionData.amount,
        type: transactionData.type,
        category: transactionData.category,
        transactionDate: transactionData.transactionDate
          ? new Date(transactionData.transactionDate).toISOString()
          : new Date().toISOString(),
      };
      const response = await axios.post("/transactions", transactionToAdd);
      if (response.data) {
        await fetchTransactionsForMonth();
        return response.data;
      }
      throw new Error("No response data from backend");
    } catch (err) {
      error.value = err.message;
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const updateTransaction = async (id, transactionData) => {
    const payload = {
      description: transactionData.description,
      amount: transactionData.amount,
      type: transactionData.type,
      category: transactionData.category,
      transactionDate: new Date(transactionData.transactionDate).toISOString(),
    };
    const response = await axios.put(`/transactions/${id}`, payload);
    await fetchTransactionsForMonth();
    return response.data;
  };

  const deleteTransaction = async (id) => {
    await axios.delete(`/transactions/${id}`);
    await fetchTransactionsForMonth();
  };

  const setFilters = (partial) => {
    filters.value = { ...filters.value, ...partial };
  };

  const setCurrentMonth = (month, year) => {
    currentMonth.value = month;
    currentYear.value = year;
  };

  const goToCurrentMonth = () => {
    const now = new Date();
    currentMonth.value = now.getMonth();
    currentYear.value = now.getFullYear();
  };

  const previousMonth = () => {
    if (currentMonth.value === 0) {
      currentMonth.value = 11;
      currentYear.value--;
    } else {
      currentMonth.value--;
    }
  };

  const nextMonth = () => {
    if (currentMonth.value === 11) {
      currentMonth.value = 0;
      currentYear.value++;
    } else {
      currentMonth.value++;
    }
  };

  const refreshData = async () => {
    isInitialized.value = false;
    await fetchTransactionsForMonth();
  };

  return {
    transactions,
    isLoading,
    error,
    isInitialized,
    currentMonth,
    currentYear,
    filters,
    recentTransactions,
    totalIncome,
    totalExpenses,
    totalBalance,
    transactionsByMonth,
    currentMonthIncome,
    currentMonthExpenses,
    currentMonthBalance,
    fetchTransactions,
    fetchTransactionsForMonth,
    addTransaction,
    updateTransaction,
    deleteTransaction,
    setFilters,
    refreshData,
    setCurrentMonth,
    goToCurrentMonth,
    previousMonth,
    nextMonth,
  };
});
