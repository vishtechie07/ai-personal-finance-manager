<template>
  <div class="space-y-6 pt-6">
    <!-- Page Header -->
    <div
      class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between"
    >
      <div class="min-w-0">
        <h1 class="text-3xl font-bold text-slate-900">Budgets</h1>
        <p class="text-slate-600 mt-2">Manage your spending budgets</p>
      </div>
      <button
        type="button"
        class="btn-primary shrink-0"
        @click="showAddBudget = true"
      >
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
            d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4"
          ></path>
        </svg>
        <span>Create Budget</span>
      </button>
    </div>

    <!-- Month Selector -->
    <div class="glass-card p-4">
      <div class="flex items-center justify-between">
        <h3 class="text-lg font-semibold text-slate-900">Filter by Month</h3>
        <div class="flex items-center space-x-3">
          <button
            type="button"
            class="btn-icon"
            aria-label="Previous month"
            @click="previousMonth"
          >
            <svg
              class="w-5 h-5 text-slate-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M15 19l-7-7 7-7"
              ></path>
            </svg>
          </button>
          <span
            class="text-lg font-medium text-slate-900 min-w-[120px] text-center"
          >
            {{ currentMonthDisplay }}
          </span>
          <button
            type="button"
            class="btn-icon"
            aria-label="Next month"
            @click="nextMonth"
          >
            <svg
              class="w-5 h-5 text-slate-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 5l7 7-7 7"
              ></path>
            </svg>
          </button>
          <button type="button" class="btn-text" @click="goToCurrentMonth">
            Current Month
          </button>
        </div>
      </div>
    </div>

    <!-- Budgets Grid -->
    <div class="glass-card p-6">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-semibold text-slate-900">
          Budgets for {{ currentMonthDisplay }}
        </h3>
        <div class="text-sm text-slate-500">
          {{ filteredBudgets.length }} budget{{
            filteredBudgets.length !== 1 ? "s" : ""
          }}
        </div>
      </div>

      <div
        v-if="filteredBudgets.length === 0"
        class="text-center py-8 text-slate-500"
      >
        <svg
          class="w-12 h-12 mx-auto mb-4 text-gray-300"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
          ></path>
        </svg>
        <p>No budgets found for {{ currentMonthDisplay }}</p>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="budget in filteredBudgets"
          :key="budget.id"
          class="bg-slate-50 rounded-xl border border-slate-200 p-6 shadow-sm"
        >
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-slate-900">
              {{ budget.name }}
            </h3>
            <div class="flex items-center space-x-2">
              <span
                class="px-2 py-1 text-xs font-medium rounded-full"
                :class="getStatusClass(budget)"
              >
                {{ getStatusText(budget) }}
              </span>
            </div>
          </div>

          <div class="space-y-3">
            <div class="flex justify-between text-sm">
              <span class="text-slate-600">Amount:</span>
              <span class="font-medium">${{ budget.amount.toFixed(2) }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-slate-600">Spent:</span>
              <span class="font-medium"
                >${{ budget.spentAmount.toFixed(2) }}</span
              >
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-slate-600">Remaining:</span>
              <span
                class="font-medium"
                :class="
                  budget.remainingAmount < 0 ? 'text-red-600' : 'text-green-600'
                "
              >
                ${{ budget.remainingAmount.toFixed(2) }}
              </span>
            </div>

            <!-- Progress Bar -->
            <div class="w-full bg-gray-200 rounded-full h-2">
              <div
                class="h-2 rounded-full transition-all duration-300"
                :class="getProgressBarClass(budget)"
                :style="{ width: getProgressPercentage(budget) + '%' }"
              ></div>
            </div>

            <div class="text-xs text-slate-500 text-center">
              {{ getProgressPercentage(budget).toFixed(1) }}% used
            </div>
          </div>

          <div class="mt-4 pt-4 border-t border-gray-200">
            <div class="flex justify-between text-xs text-slate-500">
              <span>{{ formatCategoryLabel(budget.category) }}</span>
              <span>{{ budget.period }}</span>
            </div>
            <div class="mt-4 flex gap-2">
              <button type="button" class="btn-secondary btn-sm flex-1" @click="openEditBudget(budget)">
                Edit
              </button>
              <button type="button" class="text-xs text-red-600 hover:underline py-2" @click="removeBudget(budget.id)">
                Delete
              </button>
            </div>
            <div class="text-xs text-gray-400 mt-1">
              {{ formatDate(budget.startDate) }} -
              {{ formatDate(budget.endDate) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Budget Modal -->
    <div
      v-if="showAddBudget"
      class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50"
    >
      <div
        class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white"
      >
        <div class="mt-3">
          <h3 class="text-lg font-medium text-slate-900 mb-4">
            Create New Budget
          </h3>
          <form class="space-y-4" @submit.prevent="addBudget">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Budget Name</label
              >
              <input
                v-model="newBudget.name"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Amount</label
              >
              <input
                v-model="newBudget.amount"
                type="number"
                step="0.01"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Start Month</label
              >
              <input
                v-model="newBudget.startMonth"
                type="month"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Category</label
              >
              <div class="relative">
                <input
                  v-model="newBudget.category"
                  type="text"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent pr-20"
                  placeholder="Smart categorization will suggest category"
                  :class="
                    newBudget.category && newBudget.category !== 'Uncategorized'
                      ? 'border-green-300 bg-green-50'
                      : ''
                  "
                />
                <div class="absolute inset-y-0 right-0 flex items-center pr-3">
                  <div
                    v-if="
                      newBudget.name &&
                      newBudget.name.length >= 3 &&
                      !newBudget.category
                    "
                    class="flex items-center space-x-2"
                  >
                    <div
                      class="w-2 h-2 bg-blue-500 rounded-full animate-pulse"
                    ></div>
                    <span class="text-xs text-blue-600">Analyzing...</span>
                  </div>
                  <div
                    v-else-if="
                      newBudget.category &&
                      newBudget.category !== 'Uncategorized'
                    "
                    class="flex items-center space-x-2"
                  >
                    <svg
                      class="w-4 h-4 text-green-600"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                      ></path>
                    </svg>
                    <span class="text-xs text-green-600"
                      >Category suggested</span
                    >
                  </div>
                </div>
              </div>
              <div class="mt-2 flex items-center space-x-2">
                <button
                  v-if="newBudget.name && newBudget.name.length >= 3"
                  type="button"
                  class="text-xs bg-blue-100 text-blue-600 px-2 py-1 rounded hover:bg-blue-200 transition-colors"
                  @click="suggestCategory(newBudget.name)"
                >
                  🤖 Get Smart Suggestion
                </button>
                <p
                  v-if="
                    newBudget.category && newBudget.category !== 'Uncategorized'
                  "
                  class="text-xs text-green-600"
                >
                  Smart suggestion:
                  <span class="font-medium">{{ newBudget.category }}</span>
                </p>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Period</label
              >
              <select
                v-model="newBudget.period"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="MONTHLY">Monthly</option>
                <option value="WEEKLY">Weekly</option>
                <option value="YEARLY">Yearly</option>
              </select>
            </div>
            <div class="flex space-x-3">
              <button type="submit" class="btn-primary flex-1">
                Create Budget
              </button>
              <button
                type="button"
                class="btn-secondary flex-1"
                @click="showAddBudget = false"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from "vue";
import { useBudgetsStore } from "../stores/budgets";
import { useTransactionsStore } from "../stores/transactions";
import { formatCategoryLabel } from "../composables/useCategories";
import { useToast } from "../composables/useToast";

export default {
  name: "Budgets",
  setup() {
    const budgetsStore = useBudgetsStore();
    const transactionsStore = useTransactionsStore();
    const toast = useToast();
    const showAddBudget = ref(false);
    const editingBudgetId = ref(null);

    // Initialize month state from store or current date
    const currentMonth = ref(
      budgetsStore?.currentMonth ||
        transactionsStore?.currentMonth ||
        new Date().getMonth(),
    );
    const currentYear = ref(
      budgetsStore?.currentYear ||
        transactionsStore?.currentYear ||
        new Date().getFullYear(),
    );

    const newBudget = ref({
      name: "",
      amount: "",
      category: "",
      period: "MONTHLY",
      startMonth: new Date().toISOString().slice(0, 7), // Current month in YYYY-MM format
    });

    // Use shared store data
    const budgets = computed(() => budgetsStore.budgets);

    // Computed properties for month filtering
    const currentMonthDisplay = computed(() => {
      const monthNames = [
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
      return `${monthNames[currentMonth.value]} ${currentYear.value}`;
    });

    const filteredBudgets = computed(() => {
      return budgets.value.filter((budget) => {
        const budgetDate = new Date(budget.startDate);
        return (
          budgetDate.getMonth() === currentMonth.value &&
          budgetDate.getFullYear() === currentYear.value
        );
      });
    });

    const formatDate = (date) => {
      if (!date) return "N/A";
      return new Date(date).toLocaleDateString();
    };

    // Month navigation methods
    const previousMonth = () => {
      if (currentMonth.value === 0) {
        currentMonth.value = 11;
        currentYear.value--;
      } else {
        currentMonth.value--;
      }

      // Sync with both stores
      if (budgetsStore) {
        budgetsStore.setCurrentMonth(currentMonth.value, currentYear.value);
      }
      if (transactionsStore) {
        transactionsStore.setCurrentMonth(
          currentMonth.value,
          currentYear.value,
        );
      }
    };

    const nextMonth = () => {
      if (currentMonth.value === 11) {
        currentMonth.value = 0;
        currentYear.value++;
      } else {
        currentMonth.value++;
      }

      // Sync with both stores
      if (budgetsStore) {
        budgetsStore.setCurrentMonth(currentMonth.value, currentYear.value);
      }
      if (transactionsStore) {
        transactionsStore.setCurrentMonth(
          currentMonth.value,
          currentYear.value,
        );
      }
    };

    const goToCurrentMonth = () => {
      const now = new Date();
      currentMonth.value = now.getMonth();
      currentYear.value = now.getFullYear();

      // Sync with both stores
      if (budgetsStore) {
        budgetsStore.setCurrentMonth(currentMonth.value, currentYear.value);
      }
      if (transactionsStore) {
        transactionsStore.setCurrentMonth(
          currentMonth.value,
          currentYear.value,
        );
      }
    };

    // Smart Category Suggestion Function
    const suggestCategory = async (name) => {
      if (!name || name.trim().length < 3) return;

      // Simple smart logic based on keywords in name
      const nameLower = name.toLowerCase();

      const categoryKeywords = {
        FOOD_GROCERIES: [
          "food",
          "groceries",
          "supermarket",
          "walmart",
          "target",
          "costco",
        ],
        FOOD_RESTAURANT: [
          "dining",
          "restaurant",
          "meal",
          "lunch",
          "dinner",
          "breakfast",
          "cafe",
          "coffee",
          "snack",
        ],
        TRANSPORTATION: [
          "transport",
          "gas",
          "fuel",
          "car",
          "uber",
          "lyft",
          "taxi",
          "bus",
          "train",
          "subway",
          "parking",
          "maintenance",
          "insurance",
        ],
        HOUSING_RENT: ["rent", "lease"],
        HOUSING_UTILITIES: [
          "utilities",
          "electricity",
          "water",
          "gas",
          "internet",
          "wifi",
        ],
        HOUSING_MAINTENANCE: ["maintenance", "repair", "furniture"],
        ENTERTAINMENT: [
          "entertainment",
          "movie",
          "game",
          "concert",
          "show",
          "theater",
          "sport",
          "hobby",
          "gym",
          "fitness",
          "streaming",
        ],
        SHOPPING: [
          "shopping",
          "clothes",
          "electronics",
          "books",
          "gift",
          "fashion",
          "accessories",
          "beauty",
          "cosmetics",
        ],
        HEALTHCARE: [
          "health",
          "medical",
          "doctor",
          "dentist",
          "pharmacy",
          "medicine",
          "insurance",
          "therapy",
          "wellness",
        ],
        EDUCATION: [
          "education",
          "school",
          "course",
          "training",
          "book",
          "tuition",
          "student",
          "learning",
        ],
        TRAVEL: [
          "travel",
          "vacation",
          "hotel",
          "flight",
          "trip",
          "tourism",
          "lodging",
          "airfare",
        ],
      };

      // Find the best matching category
      let bestMatch = "Uncategorized";
      let bestScore = 0;

      for (const [category, keywords] of Object.entries(categoryKeywords)) {
        let score = 0;
        for (const keyword of keywords) {
          if (nameLower.includes(keyword)) {
            score += 1;
          }
        }

        if (score > bestScore) {
          bestScore = score;
          bestMatch = category;
        }
      }

      // Only update if we found a meaningful match
      if (bestScore > 0 && bestMatch !== "Uncategorized") {
        newBudget.value.category = bestMatch;
      }
    };

    // Watch for store month changes to sync local state
    watch(
      [
        () => budgetsStore?.currentMonth,
        () => budgetsStore?.currentYear,
        () => transactionsStore?.currentMonth,
        () => transactionsStore?.currentYear,
      ],
      (newValues) => {
        if (
          newValues &&
          newValues[0] !== undefined &&
          newValues[1] !== undefined
        ) {
          // Use the first store that has valid month data
          const validMonth =
            newValues[0] !== undefined ? newValues[0] : newValues[2];
          const validYear =
            newValues[1] !== undefined ? newValues[1] : newValues[3];

          if (validMonth !== undefined && validYear !== undefined) {
            currentMonth.value = validMonth;
            currentYear.value = validYear;
          }
        }
      },
      { immediate: true },
    );

    const addBudget = async () => {
      if (!budgetsStore) {
        return;
      }

      try {
        const [year, month] = newBudget.value.startMonth.split("-");
        const startDate = new Date(parseInt(year), parseInt(month) - 1, 1);

        const budgetData = {
          name: newBudget.value.name,
          amount: newBudget.value.amount,
          category: newBudget.value.category,
          period: newBudget.value.period,
          startDate: startDate.toISOString(),
        };

        if (editingBudgetId.value) {
          await budgetsStore.updateBudget(editingBudgetId.value, budgetData);
          toast.success("Budget updated");
        } else {
          await budgetsStore.addBudget(budgetData);
          toast.success("Budget created");
        }

        showAddBudget.value = false;
        editingBudgetId.value = null;
        newBudget.value = {
          name: "",
          amount: "",
          category: "",
          period: "MONTHLY",
          startMonth: new Date().toISOString().slice(0, 7),
        };
      } catch (error) {
        toast.error("Failed to save budget");
      }
    };

    const openEditBudget = (budget) => {
      editingBudgetId.value = budget.id;
      const d = new Date(budget.startDate);
      newBudget.value = {
        name: budget.name,
        amount: budget.amount,
        category: budget.category,
        period: budget.period || "MONTHLY",
        startMonth: `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`,
      };
      showAddBudget.value = true;
    };

    const removeBudget = async (id) => {
      if (!confirm("Delete this budget?")) return;
      try {
        await budgetsStore.deleteBudget(id);
        toast.success("Budget deleted");
      } catch {
        toast.error("Failed to delete budget");
      }
    };

    // Helper functions for budget display
    const getStatusClass = (budget) => {
      if (budget.remainingAmount < 0) return "bg-red-100 text-red-800";
      if (budget.remainingAmount < budget.amount * 0.2)
        return "bg-yellow-100 text-yellow-800";
      return "bg-green-100 text-green-800";
    };

    const getStatusText = (budget) => {
      if (budget.remainingAmount < 0) return "Over Budget";
      if (budget.remainingAmount < budget.amount * 0.2) return "Warning";
      return "On Track";
    };

    const getProgressBarClass = (budget) => {
      if (budget.remainingAmount < 0) return "bg-red-500";
      if (budget.remainingAmount < budget.amount * 0.2) return "bg-yellow-500";
      return "bg-green-500";
    };

    const getProgressPercentage = (budget) => {
      return (budget.spentAmount / budget.amount) * 100;
    };

    // Initialize data when component mounts
    onMounted(async () => {
      try {
        // Ensure store is properly initialized
        if (!budgetsStore) {
          return;
        }

        // Initialize month state from store or current date
        if (
          budgetsStore.currentMonth !== undefined &&
          budgetsStore.currentYear !== undefined
        ) {
          currentMonth.value = budgetsStore.currentMonth;
          currentYear.value = budgetsStore.currentYear;
        } else if (
          transactionsStore?.currentMonth !== undefined &&
          transactionsStore?.currentYear !== undefined
        ) {
          currentMonth.value = transactionsStore.currentMonth;
          currentYear.value = transactionsStore.currentYear;
        } else {
          // Set to current month if neither store has month state
          const now = new Date();
          currentMonth.value = now.getMonth();
          currentYear.value = now.getFullYear();
        }

        // Update both stores with the current month state
        if (budgetsStore) {
          budgetsStore.setCurrentMonth(currentMonth.value, currentYear.value);
        }
        if (transactionsStore) {
          transactionsStore.setCurrentMonth(
            currentMonth.value,
            currentYear.value,
          );
        }

        // If store is not initialized, fetch data
        if (!budgetsStore.isInitialized) {
          await budgetsStore.fetchBudgets();
        }
      } catch (error) {
        console.error("Budgets load error:", error);
      }
    });

    return {
      budgetsStore,
      transactionsStore,
      showAddBudget,
      newBudget,
      budgets,
      currentMonth,
      currentYear,
      currentMonthDisplay,
      filteredBudgets,
      formatDate,
      addBudget,
      openEditBudget,
      removeBudget,
      formatCategoryLabel,
      suggestCategory,
      previousMonth,
      nextMonth,
      goToCurrentMonth,
      getStatusClass,
      getStatusText,
      getProgressBarClass,
      getProgressPercentage,
    };
  },
};
</script>
