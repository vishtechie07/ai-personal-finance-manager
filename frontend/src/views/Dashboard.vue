<template>
  <div class="w-full mx-auto pt-2 sm:pt-6 max-w-7xl">
    <DashboardHeader
      :username="username"
      :month-label="currentMonthDisplay"
      :loading="dashboardLoading"
      @previous-month="previousMonth"
      @next-month="nextMonth"
      @current-month="goToCurrentMonth"
      @add-transaction="showAddTransaction = true"
      @add-budget="showAddBudget = true"
      @refresh="refreshDashboard"
    />

    <div
      class="dashboard-card p-6 mb-6 animate-slide-up bg-gradient-to-br from-white to-primary-50/30 border-primary-100/60"
    >
      <div
        class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between"
      >
        <div>
          <p class="text-sm font-medium text-slate-600">Net balance</p>
          <p
            class="mt-1 text-3xl sm:text-4xl font-bold tabular-nums"
            :class="
              currentMonthBalance >= 0 ? 'text-slate-900' : 'text-danger-600'
            "
          >
            {{ formatMoney(currentMonthBalance) }}
          </p>
          <p class="mt-2 text-sm text-slate-600">
            {{ budgetStatus }} · {{ currentMonthDisplay }}
          </p>
        </div>
        <div class="flex flex-wrap gap-2">
          <span
            v-if="attentionBudgets.length"
            class="category-pill bg-warning-50 text-warning-700 border border-warning-100"
          >
            {{ attentionBudgets.length }} budget alert{{
              attentionBudgets.length === 1 ? "" : "s"
            }}
          </span>
          <span
            v-if="dueSoonBills.length"
            class="category-pill bg-primary-50 text-primary-700 border border-primary-100"
          >
            {{ dueSoonBills.length }} bill{{
              dueSoonBills.length === 1 ? "" : "s"
            }}
            due soon
          </span>
        </div>
      </div>
    </div>

    <DashboardKpiGrid :items="kpiItems" :loading="dashboardLoading" />

    <div class="grid grid-cols-1 xl:grid-cols-3 gap-6 mb-6">
      <div class="xl:col-span-2 grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="dashboard-card p-6 h-[22rem] flex flex-col">
          <h3 class="text-lg font-semibold text-slate-900 mb-4">
            Spending by category
          </h3>
          <div class="relative flex-1 min-h-0">
            <canvas
              v-show="hasCategoryChartData"
              ref="categoryChart"
              class="absolute inset-0 w-full h-full"
            />
            <div
              v-if="!hasCategoryChartData"
              class="absolute inset-0 flex flex-col items-center justify-center text-center text-slate-500 px-4"
            >
              <p class="font-medium text-slate-700">No spending this month</p>
              <p class="text-sm mt-1">
                Add an expense or pick another month to see the chart.
              </p>
            </div>
          </div>
        </div>

        <div class="dashboard-card p-6 h-[22rem] flex flex-col">
          <h3 class="text-lg font-semibold text-slate-900 mb-4">
            Six-month trend
          </h3>
          <div class="relative flex-1 min-h-0">
            <canvas
              v-show="hasMonthlyChartData"
              ref="monthlyChart"
              class="absolute inset-0 w-full h-full"
            />
            <div
              v-if="!hasMonthlyChartData"
              class="absolute inset-0 flex flex-col items-center justify-center text-center text-slate-500 px-4"
            >
              <p class="font-medium text-slate-700">Not enough history yet</p>
              <p class="text-sm mt-1">
                Add transactions across a few months to see trends.
              </p>
            </div>
          </div>
        </div>
      </div>

      <DashboardTodayPanel
        :budget-health-items="budgetHealthItems"
        :due-soon-bills="dueSoonBills"
        :attention-budgets="attentionBudgets"
        :alerts="budgetAlerts"
      />
    </div>

    <DashboardRecentTransactions
      class="mt-6"
      :transactions="currentMonthTransactions.slice(0, 8)"
      :month-label="currentMonthDisplay"
    />

    <!-- Add Transaction Modal -->
    <div
      v-if="showAddTransaction"
      class="fixed inset-0 bg-slate-900/40 backdrop-blur-sm overflow-y-auto h-full w-full z-50 flex items-center justify-center animate-fade-in"
    >
      <div
        class="relative mx-auto p-8 border border-slate-200 shadow-xl rounded-2xl bg-white w-full max-w-md transform transition-all duration-300 animate-slide-up"
      >
        <div class="mt-3">
          <h3 class="text-2xl font-bold text-slate-900 mb-6">
            Add New Transaction
          </h3>
          <form class="space-y-4" @submit.prevent="addTransaction">
            <div>
              <label class="form-label">Description</label>
              <input
                v-model="newTransaction.description"
                type="text"
                class="input-field"
                placeholder="E.g., Groceries"
                required
              />
            </div>
            <div>
              <label class="form-label">Amount</label>
              <input
                v-model="newTransaction.amount"
                type="number"
                step="0.01"
                class="input-field"
                placeholder="0.00"
                required
              />
            </div>
            <div>
              <label class="form-label">Type</label>
              <select v-model="newTransaction.type" class="input-field">
                <option value="EXPENSE">Expense</option>
                <option value="INCOME">Income</option>
              </select>
            </div>
            <div>
              <label class="form-label">Date</label>
              <input
                v-model="newTransaction.transactionDate"
                type="date"
                class="input-field"
                required
              />
            </div>
            <div>
              <label class="form-label">Category</label>
              <div class="relative">
                <input
                  v-model="newTransaction.category"
                  type="text"
                  class="input-field pr-20"
                  placeholder="Smart categorization will suggest category"
                  :class="
                    newTransaction.category &&
                    newTransaction.category !== 'Uncategorized'
                      ? 'border-green-500 bg-green-50'
                      : ''
                  "
                />
                <div class="absolute inset-y-0 right-0 flex items-center pr-3">
                  <div
                    v-if="
                      newTransaction.description &&
                      newTransaction.description.length > 3 &&
                      !newTransaction.category
                    "
                    class="flex items-center space-x-2"
                  >
                    <div
                      class="w-2 h-2 bg-primary-500 rounded-full animate-pulse"
                    ></div>
                    <span class="text-xs text-primary-400">Analyzing...</span>
                  </div>
                  <div
                    v-else-if="
                      newTransaction.category &&
                      newTransaction.category !== 'Uncategorized'
                    "
                    class="flex items-center space-x-2"
                  >
                    <svg
                      class="w-4 h-4 text-green-500"
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
                    <span class="text-xs text-green-500"
                      >Category suggested</span
                    >
                  </div>
                </div>
              </div>
              <div class="mt-2 flex items-center space-x-2">
                <button
                  v-if="
                    newTransaction.description &&
                    newTransaction.description.length > 3
                  "
                  type="button"
                  class="text-xs bg-primary-500/20 text-primary-400 px-2 py-1 rounded hover:bg-primary-500/30 transition-colors"
                  @click="suggestCategory(newTransaction.description)"
                >
                  🤖 Get Smart Suggestion
                </button>
                <p
                  v-if="
                    newTransaction.category &&
                    newTransaction.category !== 'Uncategorized'
                  "
                  class="text-xs text-green-500"
                >
                  Smart suggestion:
                  <span class="font-medium">{{ newTransaction.category }}</span>
                </p>
              </div>
            </div>

            <div class="flex space-x-3 pt-4">
              <button
                type="button"
                class="btn-secondary flex-1"
                @click="showAddTransaction = false"
              >
                Cancel
              </button>
              <button type="submit" class="btn-primary flex-1">
                Add Transaction
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Add Budget Modal -->
    <div
      v-if="showAddBudget"
      class="fixed inset-0 bg-slate-900/40 backdrop-blur-sm overflow-y-auto h-full w-full z-50 flex items-center justify-center animate-fade-in"
    >
      <div
        class="relative mx-auto p-8 border border-slate-200 shadow-xl rounded-2xl bg-white w-full max-w-md transform transition-all duration-300 animate-slide-up"
      >
        <div class="mt-3">
          <h3 class="text-2xl font-bold text-slate-900 mb-6">
            Create New Budget
          </h3>
          <form class="space-y-4" @submit.prevent="addBudget">
            <div>
              <label class="form-label">Budget Name</label>
              <input
                v-model="newBudget.name"
                type="text"
                class="input-field"
                placeholder="E.g., Monthly Groceries"
                required
              />
            </div>
            <div>
              <label class="form-label">Amount</label>
              <input
                v-model="newBudget.amount"
                type="number"
                step="0.01"
                class="input-field"
                placeholder="0.00"
                required
              />
            </div>
            <div>
              <label class="form-label">Start Month</label>
              <input
                v-model="newBudget.startMonth"
                type="month"
                class="input-field"
                required
              />
            </div>
            <div>
              <label class="form-label">Category</label>
              <div class="relative">
                <input
                  v-model="newBudget.category"
                  type="text"
                  :class="[
                    'input-field pr-20',
                    isBudgetAnalyzing
                      ? 'border-yellow-500 bg-yellow-50'
                      : isBudgetSmartSuggested
                        ? 'border-green-500 bg-green-50'
                        : '',
                  ]"
                  placeholder="Type to get smart suggestions..."
                  required
                />
                <div
                  v-if="isBudgetAnalyzing"
                  class="absolute right-3 top-3.5 text-yellow-500 text-xs font-medium"
                >
                  🤖 Smart analyzing...
                </div>
                <div
                  v-else-if="isBudgetSmartSuggested"
                  class="absolute right-3 top-3.5 text-green-500 text-xs font-medium"
                >
                  ✨ Smart suggested
                </div>
              </div>
              <div class="mt-2">
                <button
                  type="button"
                  class="text-xs text-primary-400 hover:text-primary-300 font-medium flex items-center"
                  @click="suggestBudgetCategory(newBudget.name)"
                >
                  🤖 Get Smart Suggestion
                </button>
              </div>
            </div>
            <div>
              <label class="form-label">Period</label>
              <select v-model="newBudget.period" class="input-field">
                <option value="WEEKLY">Weekly</option>
                <option value="MONTHLY">Monthly</option>
                <option value="QUARTERLY">Quarterly</option>
                <option value="YEARLY">Yearly</option>
              </select>
            </div>
            <div class="flex space-x-3 pt-4">
              <button
                type="button"
                class="btn-secondary flex-1"
                @click="showAddBudget = false"
              >
                Cancel
              </button>
              <button type="submit" class="btn-primary flex-1">
                Create Budget
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import axios from "axios";
import { useAuthStore } from "../stores/auth";
import { useTransactionsStore } from "../stores/transactions";
import { useBudgetsStore } from "../stores/budgets";
import { Chart, registerables } from "chart.js";
import { format } from "date-fns";
import { useRouter } from "vue-router";
import {
  resolveCategory,
  TRANSACTION_KEYWORDS,
  BUDGET_KEYWORDS,
} from "../utils/categorySuggestion";
import { useBillsStore } from "../stores/bills";
import { useToast } from "../composables/useToast";
import { getApiErrorMessage } from "../utils/apiError";
import { formatMoney } from "../utils/formatCategory";
import DashboardHeader from "../components/dashboard/DashboardHeader.vue";
import DashboardKpiGrid from "../components/dashboard/DashboardKpiGrid.vue";
import DashboardTodayPanel from "../components/dashboard/DashboardTodayPanel.vue";
import DashboardRecentTransactions from "../components/dashboard/DashboardRecentTransactions.vue";

const CHART_PALETTE = [
  "#4f46e5",
  "#06b6d4",
  "#16a34a",
  "#d97706",
  "#dc2626",
  "#7c3aed",
  "#0891b2",
  "#65a30d",
  "#ea580c",
  "#64748b",
];

Chart.register(...registerables);

export default {
  name: "Dashboard",
  components: {
    DashboardHeader,
    DashboardKpiGrid,
    DashboardTodayPanel,
    DashboardRecentTransactions,
  },
  setup() {
    const authStore = useAuthStore();
    const transactionsStore = useTransactionsStore();
    const budgetsStore = useBudgetsStore();
    const billsStore = useBillsStore();
    const router = useRouter();
    const toast = useToast();
    const priorMonthStats = ref({ income: 0, expenses: 0, balance: 0 });

    const dueSoonBills = computed(() => billsStore.dueSoon);

    const budgetHealthItems = computed(() => {
      return (budgetsStore.budgetsByMonth || []).map((b) => {
        const pct =
          (parseFloat(b.spentAmount || 0) / parseFloat(b.amount || 1)) * 100;
        let barClass = "bg-green-500";
        if (pct >= 90 || parseFloat(b.remainingAmount || 0) < 0)
          barClass = "bg-red-500";
        else if (pct >= 70) barClass = "bg-amber-500";
        return {
          id: b.id,
          name: b.name,
          pct,
          barClass,
          link: { path: "/transactions", query: { category: b.category } },
        };
      });
    });

    const attentionBudgets = computed(() => {
      return budgetHealthItems.value
        .filter((i) => i.pct >= 90)
        .map((i) => ({
          id: i.id,
          link: i.link,
          message:
            i.pct >= 100
              ? `${i.name} is over budget`
              : `${i.name} has used ${i.pct.toFixed(0)}% of its budget`,
        }));
    });

    const showAddTransaction = ref(false);
    const showAddBudget = ref(false);

    // Chart refs
    const categoryChart = ref(null);
    const monthlyChart = ref(null);

    // Chart instances to prevent multiple initializations
    let spendingChart = null;
    let monthlyChartInstance = null;

    const newTransaction = ref({
      description: "",
      amount: "",
      type: "EXPENSE",
      category: "",
      transactionDate: new Date().toISOString().split("T")[0], // Today's date as default
    });

    const newBudget = ref({
      name: "",
      amount: "",
      category: "",
      period: "MONTHLY",
      startMonth: new Date().toISOString().slice(0, 7), // Current month in YYYY-MM format
    });

    // Smart suggestion state
    const isSmartSuggested = ref(false);

    // Budget smart suggestion state
    const isBudgetSmartSuggested = ref(false);
    const budgetSuggestionTimeout = ref(null);

    // Use shared store data instead of local refs
    const totalBalance = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.totalBalance || 0;
    });
    const monthlyIncome = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.totalIncome || 0;
    });
    const monthlyExpenses = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.totalExpenses || 0;
    });
    const recentTransactions = computed(() => {
      if (!transactionsStore) return [];
      return transactionsStore.recentTransactions || [];
    });

    // Add missing computed properties
    const totalIncome = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.totalIncome || 0;
    });
    const totalExpenses = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.totalExpenses || 0;
    });
    const totalSavings = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.totalBalance || 0;
    });

    // Month-based data
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
      return `${monthNames[transactionsStore?.currentMonth || 0]} ${transactionsStore?.currentYear || new Date().getFullYear()}`;
    });

    const currentMonthIncome = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.currentMonthIncome || 0;
    });

    const currentMonthExpenses = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.currentMonthExpenses || 0;
    });

    const currentMonthBalance = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.currentMonthBalance || 0;
    });

    const currentMonthTransactions = computed(() => {
      if (!transactionsStore) return [];
      return transactionsStore.transactionsByMonth || [];
    });

    const hasCategoryChartData = computed(() =>
      currentMonthTransactions.value.some((t) => t.type === "EXPENSE"),
    );

    const hasMonthlyChartData = computed(() =>
      (transactionsStore?.transactions || []).some(
        (t) => t.type === "EXPENSE" || t.type === "INCOME",
      ),
    );

    const currentMonthBudgets = computed(() => {
      if (!budgetsStore) return [];
      return budgetsStore.budgetsByMonth || [];
    });

    const dashboardLoading = computed(
      () =>
        !transactionsStore.isInitialized && transactionsStore.isLoading,
    );

    const budgetUtilization = computed(() => {
      const budgets = currentMonthBudgets.value;
      if (!budgets.length) return null;
      const totalBudget = budgets.reduce(
        (sum, b) => sum + parseFloat(b.amount || 0),
        0,
      );
      if (totalBudget <= 0) return null;
      const totalSpent = budgets.reduce(
        (sum, b) => sum + parseFloat(b.spentAmount || 0),
        0,
      );
      return (totalSpent / totalBudget) * 100;
    });

    const percentDelta = (current, prior) => {
      if (!prior || prior === 0) return null;
      return ((current - prior) / Math.abs(prior)) * 100;
    };

    const kpiItems = computed(() => [
      {
        key: "balance",
        label: "Net balance",
        value: formatMoney(currentMonthBalance.value),
        tone: currentMonthBalance.value >= 0 ? "neutral" : "negative",
        delta: percentDelta(
          currentMonthBalance.value,
          priorMonthStats.value.balance,
        ),
      },
      {
        key: "income",
        label: "Income",
        value: formatMoney(currentMonthIncome.value),
        tone: "positive",
        delta: percentDelta(
          currentMonthIncome.value,
          priorMonthStats.value.income,
        ),
      },
      {
        key: "expenses",
        label: "Expenses",
        value: formatMoney(currentMonthExpenses.value),
        tone: "negative",
        delta: percentDelta(
          currentMonthExpenses.value,
          priorMonthStats.value.expenses,
        ),
      },
      {
        key: "budgets",
        label: "Budget used",
        value:
          budgetUtilization.value === null
            ? "—"
            : `${budgetUtilization.value.toFixed(0)}%`,
        tone: "accent",
        hint:
          budgetUtilization.value === null
            ? "No budgets this month"
            : `${currentMonthBudgets.value.length} active budget${
                currentMonthBudgets.value.length === 1 ? "" : "s"
              }`,
        delta: null,
      },
    ]);

    const loadPriorMonthStats = async () => {
      let month = transactionsStore.currentMonth - 1;
      let year = transactionsStore.currentYear;
      if (month < 0) {
        month = 11;
        year -= 1;
      }
      const ym = `${year}-${String(month + 1).padStart(2, "0")}`;
      try {
        const { data } = await axios.get(`/transactions/month/${ym}`);
        const txs = Array.isArray(data) ? data : [];
        const income = txs
          .filter((t) => t.type === "INCOME")
          .reduce((sum, t) => sum + parseFloat(t.amount || 0), 0);
        const expenses = txs
          .filter((t) => t.type === "EXPENSE")
          .reduce((sum, t) => sum + parseFloat(t.amount || 0), 0);
        priorMonthStats.value = { income, expenses, balance: income - expenses };
      } catch {
        priorMonthStats.value = { income: 0, expenses: 0, balance: 0 };
      }
    };

    // Compute budget status from the budgets store
    const budgetStatus = computed(() => {
      if (!budgetsStore?.budgets) return "Loading...";
      if (budgetsStore.overBudgetBudgets.length > 0) {
        return "Over Budget";
      } else if (budgetsStore.nearLimitBudgets.length > 0) {
        return "Near Limit";
      } else {
        return "On Track";
      }
    });

    // Generate budget alerts from the budgets store
    const budgetAlerts = computed(() => {
      if (!budgetsStore?.budgets) return [];
      const alerts = [];

      // Check for over-budget budgets (month-filtered)
      budgetsStore.currentMonthOverBudgetBudgets.forEach((budget) => {
        alerts.push({
          id: `over-${budget.id}`,
          type: "danger",
          title: `${budget.name} Budget`,
          message: `You've exceeded your ${budget.name.toLowerCase()} budget by $${Math.abs(parseFloat(budget.remainingAmount || 0)).toFixed(2)}`,
        });
      });

      // Check for near-limit budgets (month-filtered)
      budgetsStore.currentMonthNearLimitBudgets.forEach((budget) => {
        const percentage =
          (parseFloat(budget.spentAmount || 0) /
            parseFloat(budget.amount || 1)) *
          100;
        alerts.push({
          id: `near-${budget.id}`,
          type: "warning",
          title: `${budget.name} Budget`,
          message: `You've used ${percentage.toFixed(1)}% of your ${budget.name.toLowerCase()} budget this month`,
        });
      });

      return alerts.slice(0, 3); // Limit to 3 most important alerts
    });

    const username = computed(() => {
      if (!authStore || !authStore.user) return "User";
      return authStore.user.username || "User";
    });

    const formatDate = (date) => {
      return format(new Date(date), "MMM dd");
    };

    const addTransaction = async () => {
      if (
        !newTransaction.value.description ||
        !newTransaction.value.amount ||
        !newTransaction.value.category ||
        !newTransaction.value.transactionDate
      ) {
        toast.error("Fill in description, amount, date, and category.");
        return;
      }

      if (
        !transactionsStore ||
        typeof transactionsStore.addTransaction !== "function"
      ) {
        toast.error("Transactions unavailable. Refresh and try again.");
        return;
      }

      try {
        const newTransactionData = await transactionsStore.addTransaction(
          newTransaction.value,
        );

        toast.success(
          `${newTransactionData.type === "INCOME" ? "Income" : "Expense"} added`,
        );

        showAddTransaction.value = false;
        newTransaction.value = {
          description: "",
          amount: "",
          type: "EXPENSE",
          category: "",
          transactionDate: new Date().toISOString().split("T")[0],
        };

        await loadPriorMonthStats();
        setTimeout(() => {
          try {
            initCharts();
          } catch {
            /* ignore chart errors */
          }
        }, 100);
      } catch (error) {
        toast.error(getApiErrorMessage(error));
      }
    };

    const suggestCategory = async (description) => {
      const cat = await resolveCategory(description, TRANSACTION_KEYWORDS);
      if (cat) newTransaction.value.category = cat;
    };

    const suggestBudgetCategory = async (budgetName) => {
      if (!budgetName || budgetName.trim().length < 3) return;
      isBudgetSmartSuggested.value = false;
      const cat = await resolveCategory(budgetName, BUDGET_KEYWORDS);
      if (cat) {
        newBudget.value.category = cat;
        isBudgetSmartSuggested.value = true;
      } else {
        newBudget.value.category = "Uncategorized";
      }
    };

    const addBudget = async () => {
      if (!budgetsStore) {
        return;
      }

      try {
        // Convert month string to start date
        const [year, month] = newBudget.value.startMonth.split("-");
        const startDate = new Date(parseInt(year), parseInt(month) - 1, 1);

        const budgetData = {
          name: newBudget.value.name,
          amount: newBudget.value.amount,
          category: newBudget.value.category,
          period: newBudget.value.period,
          startDate: startDate.toISOString(),
        };

        // Use the shared store to add the budget
        const budget = await budgetsStore.addBudget(budgetData);

        toast.success(`Budget "${budget.name}" created`);

        showAddBudget.value = false;
        newBudget.value = {
          name: "",
          amount: "",
          category: "",
          period: "MONTHLY",
          startMonth: new Date().toISOString().slice(0, 7),
        };
        isBudgetSmartSuggested.value = false;

        setTimeout(() => {
          try {
            initCharts();
          } catch {
            /* ignore chart errors */
          }
        }, 100);
      } catch (error) {
        toast.error(getApiErrorMessage(error));
      }
    };

    // Chart initialization with proper cleanup
    const initCharts = () => {
      // Initialize spending by category chart
      if (
        categoryChart.value &&
        transactionsStore.transactionsByMonth.length > 0
      ) {
        const ctx = categoryChart.value.getContext("2d");

        // Destroy existing chart if it exists
        if (spendingChart) {
          spendingChart.destroy();
        }

        // Prepare data for the chart
        const categoryData = {};
        transactionsStore.transactionsByMonth
          .filter((t) => t.type === "EXPENSE")
          .forEach((t) => {
            if (categoryData[t.category]) {
              categoryData[t.category] += parseFloat(t.amount);
            } else {
              categoryData[t.category] = parseFloat(t.amount);
            }
          });

        const labels = Object.keys(categoryData);
        const data = Object.values(categoryData);

        // Create the chart
        spendingChart = new Chart(ctx, {
          type: "doughnut",
          data: {
            labels: labels,
            datasets: [
              {
                data: data,
                backgroundColor: CHART_PALETTE,
                borderWidth: 2,
                borderColor: "#f8fafc",
              },
            ],
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                position: "bottom",
                labels: {
                  padding: 20,
                  usePointStyle: true,
                  color: "#475569",
                },
              },
              title: {
                display: true,
                text: `Spending by Category - ${currentMonthDisplay.value}`,
                color: "#0f172a",
                font: {
                  size: 16,
                  weight: "bold",
                },
              },
            },
          },
        });
      }

      // Initialize monthly overview chart
      if (monthlyChart.value && transactionsStore.transactions.length > 0) {
        const ctx = monthlyChart.value.getContext("2d");

        // Destroy existing chart if it exists
        if (monthlyChartInstance) {
          monthlyChartInstance.destroy();
        }

        // Prepare data for the last 6 months
        const months = [];
        const incomeData = [];
        const expenseData = [];

        const now = new Date();
        for (let i = 5; i >= 0; i--) {
          const month = new Date(now.getFullYear(), now.getMonth() - i, 1);
          months.push(
            month.toLocaleDateString("en-US", {
              month: "short",
              year: "2-digit",
            }),
          );

          // Calculate income and expenses for this month
          const monthStart = new Date(month.getFullYear(), month.getMonth(), 1);
          const monthEnd = new Date(
            month.getFullYear(),
            month.getMonth() + 1,
            0,
          );

          const monthTransactions = transactionsStore.transactions.filter(
            (t) => {
              const transactionDate = new Date(t.transactionDate);
              return (
                transactionDate >= monthStart && transactionDate <= monthEnd
              );
            },
          );

          const monthIncome = monthTransactions
            .filter((t) => t.type === "INCOME")
            .reduce((sum, t) => sum + parseFloat(t.amount), 0);

          const monthExpenses = monthTransactions
            .filter((t) => t.type === "EXPENSE")
            .reduce((sum, t) => sum + parseFloat(t.amount), 0);

          incomeData.push(monthIncome);
          expenseData.push(monthExpenses);
        }

        // Create the chart
        monthlyChartInstance = new Chart(ctx, {
          type: "line",
          data: {
            labels: months,
            datasets: [
              {
                label: "Income",
                data: incomeData,
                borderColor: "#16a34a",
                backgroundColor: "rgba(22, 163, 74, 0.12)",
                borderWidth: 3,
                fill: true,
                tension: 0.4,
              },
              {
                label: "Expenses",
                data: expenseData,
                borderColor: "#dc2626",
                backgroundColor: "rgba(220, 38, 38, 0.12)",
                borderWidth: 3,
                fill: true,
                tension: 0.4,
              },
            ],
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                position: "top",
                labels: {
                  usePointStyle: true,
                  color: "#475569",
                },
              },
              title: {
                display: true,
                text: "Monthly Overview - Last 6 Months",
                color: "#0f172a",
                font: {
                  size: 16,
                  weight: "bold",
                },
              },
            },
            scales: {
              y: {
                beginAtZero: true,
                grid: {
                  color: "rgba(148, 163, 184, 0.25)",
                },
                ticks: {
                  color: "#64748b",
                  callback: function (value) {
                    return "$" + value.toLocaleString();
                  },
                },
              },
              x: {
                grid: {
                  color: "rgba(148, 163, 184, 0.15)",
                },
                ticks: {
                  color: "#64748b",
                },
              },
            },
          },
        });
      }
    };

    // Initialize charts when component mounts
    onMounted(async () => {
      try {
        // Ensure stores are properly initialized
        if (!transactionsStore) {
          return;
        }

        // Check if stores already have data
        // If stores are not initialized, fetch data
        if (!transactionsStore.isInitialized) {
          await transactionsStore.fetchTransactions();
        }

        if (!budgetsStore.isInitialized) {
          await budgetsStore.fetchBudgets();
        }
        await billsStore.fetchDueSoon();
        await loadPriorMonthStats();
      } catch (error) {
        console.error("Dashboard load error:", error);
      }

      // Add a small delay to ensure DOM is fully ready and data is loaded
      setTimeout(() => {
        try {
          // Double-check that chart refs are available
          if (categoryChart.value && monthlyChart.value) {
            initCharts();
          } else {
            // Retry after another delay
            setTimeout(() => {
              if (categoryChart.value && monthlyChart.value) {
                initCharts();
              }
            }, 500);
          }
        } catch (error) {
          // Handle error silently
        }
      }, 500);
    });

    // Cleanup charts when component unmounts
    onUnmounted(() => {
      if (budgetSuggestionTimeout.value) {
        clearTimeout(budgetSuggestionTimeout.value);
      }
      if (spendingChart) {
        spendingChart.destroy();
      }
      if (monthlyChartInstance) {
        monthlyChartInstance.destroy();
      }
    });

    // Refresh charts for current month
    const refreshChartsForCurrentMonth = () => {
      try {
        // Destroy existing charts first
        if (spendingChart) {
          spendingChart.destroy();
          spendingChart = null;
        }
        if (monthlyChartInstance) {
          monthlyChartInstance.destroy();
          monthlyChartInstance = null;
        }

        // Wait a bit and then reinitialize
        setTimeout(() => {
          initCharts();
        }, 100);
      } catch (error) {
        // Handle error silently
      }
    };

    // Add refresh mechanism
    const refreshDashboard = async () => {
      try {
        // Refresh both stores
        if (transactionsStore) {
          await transactionsStore.refreshData();
        }
        if (budgetsStore) {
          await budgetsStore.refreshData();
        }

        // Refresh charts
        refreshChartsForCurrentMonth();
        toast.success("Dashboard refreshed");
      } catch (error) {
        toast.error(getApiErrorMessage(error));
      }
    };

    // Watch for route changes to refresh data
    watch(
      () => router.currentRoute.value.path,
      (newPath, oldPath) => {
        if (newPath === "/dashboard" && oldPath !== "/dashboard") {
          refreshDashboard();
        }
      },
    );

    // Watch for transactions data changes to refresh charts
    watch(
      () => transactionsStore?.transactions,
      (newTransactions, oldTransactions) => {
        if (
          newTransactions &&
          newTransactions.length !== oldTransactions?.length
        ) {
          refreshChartsForCurrentMonth();
        }
      },
    );

    // Watch for month changes to refresh charts
    watch(
      [
        () => transactionsStore?.currentMonth,
        () => transactionsStore?.currentYear,
      ],
      (newValues, oldValues) => {
        if (
          newValues &&
          (newValues[0] !== oldValues?.[0] || newValues[1] !== oldValues?.[1])
        ) {
          loadPriorMonthStats();
          refreshChartsForCurrentMonth();
        }
      },
    );

    // Watch for budgets data changes to refresh charts
    watch(
      () => budgetsStore?.budgets,
      (newBudgets, oldBudgets) => {
        if (newBudgets && newBudgets.length !== oldBudgets?.length) {
          refreshChartsForCurrentMonth();
        }
      },
    );

    // Watch for month-based data changes to refresh charts
    watch(
      [
        () => transactionsStore?.transactionsByMonth,
        () => budgetsStore?.budgetsByMonth,
      ],
      (newValues, oldValues) => {
        if (
          newValues &&
          (newValues[0]?.length !== oldValues?.[0]?.length ||
            newValues[1]?.length !== oldValues?.[1]?.length)
        ) {
          refreshChartsForCurrentMonth();
        }
      },
    );

    // Month navigation
    const setCurrentMonth = (month, year) => {
      if (transactionsStore) {
        transactionsStore.setCurrentMonth(month, year);
      }
      if (budgetsStore) {
        budgetsStore.setCurrentMonth(month, year);
      }

      // Force chart update after month change
      setTimeout(() => {
        refreshChartsForCurrentMonth();
      }, 150);
    };

    const goToCurrentMonth = () => {
      if (transactionsStore) {
        transactionsStore.goToCurrentMonth();
      }
      if (budgetsStore) {
        budgetsStore.goToCurrentMonth();
      }

      // Force chart update after returning to current month
      setTimeout(() => {
        refreshChartsForCurrentMonth();
      }, 150);
    };

    const previousMonth = () => {
      if (transactionsStore) {
        transactionsStore.previousMonth();
      }
      if (budgetsStore) {
        budgetsStore.previousMonth();
      }

      // Force chart update after month navigation
      setTimeout(() => {
        refreshChartsForCurrentMonth();
      }, 150);
    };

    const nextMonth = () => {
      if (transactionsStore) {
        transactionsStore.nextMonth();
      }
      if (budgetsStore) {
        budgetsStore.nextMonth();
      }

      // Force chart update after month navigation
      setTimeout(() => {
        refreshChartsForCurrentMonth();
      }, 150);
    };

    return {
      showAddTransaction,
      showAddBudget,
      newTransaction,
      newBudget,
      budgetStatus,
      budgetAlerts,
      username,
      formatDate,
      formatMoney,
      addTransaction,
      addBudget,
      categoryChart,
      monthlyChart,
      hasCategoryChartData,
      hasMonthlyChartData,
      isBudgetSmartSuggested,
      suggestCategory,
      suggestBudgetCategory,
      refreshDashboard,
      goToCurrentMonth,
      previousMonth,
      nextMonth,
      currentMonthDisplay,
      currentMonthBalance,
      currentMonthTransactions,
      kpiItems,
      dashboardLoading,
      dueSoonBills,
      budgetHealthItems,
      attentionBudgets,
    };
  },
};
</script>
