import { defineStore } from "pinia";
import { ref, computed } from "vue";
import axios from "axios";

export const useBudgetsStore = defineStore("budgets", () => {
  const budgets = ref([]);
  const isLoading = ref(false);
  const error = ref(null);
  const isInitialized = ref(false);
  const currentMonth = ref(new Date().getMonth());
  const currentYear = ref(new Date().getFullYear());

  const totalBudgetAmount = computed(() => {
    return budgets.value.reduce(
      (sum, budget) => sum + parseFloat(budget.amount || 0),
      0,
    );
  });

  const totalSpentAmount = computed(() => {
    return budgets.value.reduce(
      (sum, budget) => sum + parseFloat(budget.spentAmount || 0),
      0,
    );
  });

  const totalRemainingAmount = computed(() => {
    return totalBudgetAmount.value - totalSpentAmount.value;
  });

  const overBudgetBudgets = computed(() => {
    return budgets.value.filter((budget) => {
      const remaining = parseFloat(budget.remainingAmount || 0);
      return remaining < 0;
    });
  });

  const nearLimitBudgets = computed(() => {
    return budgets.value.filter((budget) => {
      const percentage =
        (parseFloat(budget.spentAmount || 0) / parseFloat(budget.amount || 1)) *
        100;
      return percentage >= 70 && percentage < 90;
    });
  });

  const currentMonthOverBudgetBudgets = computed(() => {
    return budgetsByMonth.value.filter((budget) => {
      const remaining = parseFloat(budget.remainingAmount || 0);
      return remaining < 0;
    });
  });

  const currentMonthNearLimitBudgets = computed(() => {
    return budgetsByMonth.value.filter((budget) => {
      const percentage =
        (parseFloat(budget.spentAmount || 0) / parseFloat(budget.amount || 1)) *
        100;
      return percentage >= 70 && percentage < 90;
    });
  });

  const budgetsByMonth = computed(() => {
    return budgets.value.filter((budget) => {
      const budgetDate = new Date(budget.startDate);
      return (
        budgetDate.getMonth() === currentMonth.value &&
        budgetDate.getFullYear() === currentYear.value
      );
    });
  });

  const currentMonthBudgetAmount = computed(() => {
    return budgetsByMonth.value.reduce(
      (sum, budget) => sum + parseFloat(budget.amount || 0),
      0,
    );
  });

  const currentMonthSpentAmount = computed(() => {
    return budgetsByMonth.value.reduce(
      (sum, budget) => sum + parseFloat(budget.spentAmount || 0),
      0,
    );
  });

  const currentMonthRemainingAmount = computed(() => {
    return currentMonthBudgetAmount.value - currentMonthSpentAmount.value;
  });

  const fetchBudgets = async () => {
    if (isLoading.value) return;

    isLoading.value = true;
    error.value = null;
    try {
      const response = await axios.get("/budgets");

      if (response.data && Array.isArray(response.data)) {
        budgets.value = response.data;
        isInitialized.value = true;
      } else {
        budgets.value = [];
      }
    } catch (err) {
      error.value = err.message;
      console.error("Failed to fetch budgets:", err);
      budgets.value = [];
    } finally {
      isLoading.value = false;
    }
  };

  const addBudget = async (budgetData) => {
    isLoading.value = true;
    error.value = null;
    try {
      const budgetToAdd = {
        name: budgetData.name,
        amount: budgetData.amount,
        category: budgetData.category,
        period: budgetData.period || "MONTHLY",
        startDate: budgetData.startDate
          ? new Date(budgetData.startDate).toISOString()
          : new Date().toISOString(),
        endDate: null,
        spentAmount: 0,
        remainingAmount: budgetData.amount,
        isActive: true,
      };

      const response = await axios.post("/budgets", budgetToAdd);

      if (response.data) {
        budgets.value.unshift(response.data);
        return response.data;
      }
      throw new Error("No response data from backend");
    } catch (err) {
      error.value = err.message;
      console.error("Failed to add budget:", err);
      throw err;
    } finally {
      isLoading.value = false;
    }
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

  const updateBudget = async (id, budgetData) => {
    const response = await axios.put(`/budgets/${id}`, budgetData);
    const idx = budgets.value.findIndex((b) => b.id === id);
    if (idx !== -1) budgets.value[idx] = response.data;
    return response.data;
  };

  const deleteBudget = async (id) => {
    await axios.delete(`/budgets/${id}`);
    budgets.value = budgets.value.filter((b) => b.id !== id);
  };

  const refreshData = async () => {
    isInitialized.value = false;
    await fetchBudgets();
  };

  return {
    budgets,
    isLoading,
    error,
    isInitialized,
    currentMonth,
    currentYear,
    totalBudgetAmount,
    totalSpentAmount,
    totalRemainingAmount,
    overBudgetBudgets,
    nearLimitBudgets,
    currentMonthOverBudgetBudgets,
    currentMonthNearLimitBudgets,
    budgetsByMonth,
    currentMonthBudgetAmount,
    currentMonthSpentAmount,
    currentMonthRemainingAmount,
    fetchBudgets,
    addBudget,
    updateBudget,
    deleteBudget,
    refreshData,
    setCurrentMonth,
    goToCurrentMonth,
    previousMonth,
    nextMonth,
  };
});
