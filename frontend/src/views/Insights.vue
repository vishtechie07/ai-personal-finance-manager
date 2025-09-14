<template>
  <div class="space-y-6 pt-6">
    <!-- Page Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Financial Insights</h1>
        <p class="text-gray-600 mt-2">Smart financial analysis and recommendations for {{ currentMonthDisplay }}</p>
      </div>
    </div>

    <!-- Month Selector -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <div class="flex items-center justify-between">
        <h3 class="text-lg font-semibold text-gray-900">Viewing Insights for {{ currentMonthDisplay }}</h3>
        <div class="flex items-center space-x-3">
          <button @click="previousMonth" class="p-2 hover:bg-gray-100 rounded-lg transition-colors">
            <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
          </button>
          <span class="text-lg font-medium text-gray-900 min-w-[120px] text-center">
            {{ currentMonthDisplay }}
          </span>
          <button @click="nextMonth" class="p-2 hover:bg-gray-100 rounded-lg transition-colors">
            <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
            </svg>
          </button>
          <button @click="goToCurrentMonth" class="text-sm text-blue-600 hover:text-blue-700 font-medium">
            Current Month
          </button>
        </div>
      </div>
    </div>

    <!-- Smart Insights Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <!-- Spending Pattern Analysis -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div class="flex items-center mb-4">
          <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
            <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
            </svg>
          </div>
          <h3 class="text-lg font-semibold text-gray-900 ml-3">Spending Patterns</h3>
        </div>
        <p class="text-gray-600 mb-4">{{ spendingPatternInsight }}</p>
        <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-3">
          <p class="text-yellow-800 text-sm font-medium">Recommendation</p>
          <p class="text-yellow-700 text-sm">{{ spendingPatternRecommendation }}</p>
        </div>
      </div>

      <!-- Smart Detection -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div class="flex items-center mb-4">
          <div class="w-10 h-10 bg-yellow-100 rounded-lg flex items-center justify-center">
            <svg class="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z"></path>
            </svg>
          </div>
          <h3 class="text-lg font-semibold text-gray-900 ml-3">Smart Detection</h3>
        </div>
        <p class="text-gray-600 mb-4">{{ anomalyInsight }}</p>
        <div class="bg-red-50 border border-red-200 rounded-lg p-3">
          <p class="text-red-800 text-sm font-medium">Alert</p>
          <p class="text-red-700 text-sm">{{ anomalyAlert }}</p>
        </div>
      </div>

      <!-- Savings Opportunity -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div class="flex items-center mb-4">
          <div class="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
            <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path>
            </svg>
          </div>
          <h3 class="text-lg font-semibold text-gray-900 ml-3">Savings Opportunity</h3>
        </div>
        <p class="text-gray-600 mb-4">{{ savingsInsight }}</p>
        <div class="bg-green-50 border border-green-200 rounded-lg p-3">
          <p class="text-green-800 text-sm font-medium">Tip</p>
          <p class="text-green-700 text-sm">{{ savingsTip }}</p>
        </div>
      </div>

      <!-- Investment Recommendation -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div class="flex items-center mb-4">
          <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
            <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path>
            </svg>
          </div>
          <h3 class="text-lg font-semibold text-gray-900 ml-3">Investment Insight</h3>
        </div>
        <p class="text-gray-600 mb-4">{{ investmentInsight }}</p>
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-3">
          <p class="text-blue-800 text-sm font-medium">Opportunity</p>
          <p class="text-blue-700 text-sm">{{ investmentOpportunity }}</p>
        </div>
      </div>

      <!-- Budget Optimization -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div class="flex items-center mb-4">
          <div class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
            <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
            </svg>
          </div>
          <h3 class="text-lg font-semibold text-gray-900 ml-3">Budget Optimization</h3>
        </div>
        <p class="text-gray-600 mb-4">{{ budgetOptimizationInsight }}</p>
        <div class="bg-purple-50 border border-purple-200 rounded-lg p-3">
          <p class="text-purple-800 text-sm font-medium">Suggestion</p>
          <p class="text-purple-700 text-sm">{{ budgetOptimizationSuggestion }}</p>
        </div>
      </div>

      <!-- Future Planning -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div class="flex items-center mb-4">
          <div class="w-10 h-10 bg-indigo-100 rounded-lg flex items-center justify-center">
            <svg class="w-6 h-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
            </svg>
          </div>
          <h3 class="text-lg font-semibold text-gray-900 ml-3">Future Planning</h3>
        </div>
        <p class="text-gray-600 mb-4">{{ futurePlanningInsight }}</p>
        <div class="bg-indigo-50 border border-indigo-200 rounded-lg p-3">
          <p class="text-indigo-800 text-sm font-medium">Goal</p>
          <p class="text-indigo-700 text-sm">{{ futurePlanningGoal }}</p>
        </div>
      </div>
    </div>

    <!-- Smart Status -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <div class="flex items-center mb-4">
        <div class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
          <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"></path>
          </svg>
        </div>
        <h3 class="text-lg font-semibold text-gray-900">Smart Analysis Status</h3>
      </div>
      <div class="flex items-center space-x-2 mb-3">
        <div class="w-3 h-3 bg-green-500 rounded-full"></div>
        <span class="text-sm text-gray-600">Smart Active</span>
      </div>
      <p class="text-gray-600 text-sm">
        Our smart system continuously analyzes your financial data for {{ currentMonthDisplay }} to provide personalized insights and recommendations.
      </p>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue'
import { useTransactionsStore } from '../stores/transactions'
import { useBudgetsStore } from '../stores/budgets'

export default {
  name: 'Insights',
  setup() {
    const transactionsStore = useTransactionsStore()
    const budgetsStore = useBudgetsStore()
    
    // Initialize month state from store or current date
    const currentMonth = ref(transactionsStore?.currentMonth || new Date().getMonth())
    const currentYear = ref(transactionsStore?.currentYear || new Date().getFullYear())
    
    const lastUpdated = ref(new Date().toLocaleDateString())

    // Computed properties for month filtering
    const currentMonthDisplay = computed(() => {
      const monthNames = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
      ]
      return `${monthNames[currentMonth.value]} ${currentYear.value}`
    })

    // Month navigation methods
    const previousMonth = () => {
      if (currentMonth.value === 0) {
        currentMonth.value = 11
        currentYear.value--
      } else {
        currentMonth.value--
      }
      
      // Sync with both stores
      if (transactionsStore) {
        transactionsStore.setCurrentMonth(currentMonth.value, currentYear.value)
      }
      if (budgetsStore) {
        budgetsStore.setCurrentMonth(currentMonth.value, currentYear.value)
      }
    }

    const nextMonth = () => {
      if (currentMonth.value === 11) {
        currentMonth.value = 0
        currentYear.value++
      } else {
        currentMonth.value++
      }
      
      // Sync with both stores
      if (transactionsStore) {
        transactionsStore.setCurrentMonth(currentMonth.value, currentYear.value)
      }
      if (budgetsStore) {
        budgetsStore.setCurrentMonth(currentMonth.value, currentYear.value)
      }
    }

    const goToCurrentMonth = () => {
      const now = new Date()
      currentMonth.value = now.getMonth()
      currentYear.value = now.getFullYear()
      
      // Sync with both stores
      if (transactionsStore) {
        transactionsStore.setCurrentMonth(currentMonth.value, currentYear.value)
      }
      if (budgetsStore) {
        budgetsStore.setCurrentMonth(currentMonth.value, currentYear.value)
      }
    }

    // Dynamic insights based on current month data
    const spendingPatternInsight = computed(() => {
      const currentMonthTransactions = transactionsStore?.transactionsByMonth || []
      const entertainmentSpending = currentMonthTransactions
        .filter(t => t.category === 'ENTERTAINMENT' && t.type === 'EXPENSE')
        .reduce((sum, t) => sum + parseFloat(t.amount), 0)
      
      if (entertainmentSpending > 200) {
        return `Your entertainment spending is $${entertainmentSpending.toFixed(2)} this month, which is above the recommended threshold.`
      } else {
        return `Your entertainment spending is $${entertainmentSpending.toFixed(2)} this month, which is within healthy limits.`
      }
    })

    const spendingPatternRecommendation = computed(() => {
      const currentMonthTransactions = transactionsStore?.transactionsByMonth || []
      const entertainmentSpending = currentMonthTransactions
        .filter(t => t.category === 'ENTERTAINMENT' && t.type === 'EXPENSE')
        .reduce((sum, t) => sum + parseFloat(t.amount), 0)
      
      if (entertainmentSpending > 200) {
        return `Consider setting a stricter budget for entertainment to maintain financial balance.`
      } else {
        return `Great job keeping entertainment spending under control!`
      }
    })

    const anomalyInsight = computed(() => {
      const currentMonthTransactions = transactionsStore?.transactionsByMonth || []
      const currentMonthBudgets = budgetsStore?.budgetsByMonth || []
      
      // Check for budget overruns
      const overBudgetCategories = currentMonthBudgets
        .filter(budget => budget.remainingAmount < 0)
        .map(budget => budget.category)
      
      if (overBudgetCategories.length > 0) {
        return `Budget overrun detected in ${overBudgetCategories.join(', ')} categories.`
      } else {
        return `All budgets are within limits this month.`
      }
    })

    const anomalyAlert = computed(() => {
      const currentMonthTransactions = transactionsStore?.transactionsByMonth || []
      const currentMonthBudgets = budgetsStore?.budgetsByMonth || []
      
      const overBudgetCategories = currentMonthBudgets
        .filter(budget => budget.remainingAmount < 0)
        .map(budget => budget.category)
      
      if (overBudgetCategories.length > 0) {
        return `Review your spending in ${overBudgetCategories.join(', ')} to get back on track.`
      } else {
        return `Keep up the good work staying within your budgets!`
      }
    })

    const savingsInsight = computed(() => {
      const currentMonthTransactions = transactionsStore?.transactionsByMonth || []
      const grocerySpending = currentMonthTransactions
        .filter(t => t.category === 'FOOD_GROCERIES' && t.type === 'EXPENSE')
        .reduce((sum, t) => sum + parseFloat(t.amount), 0)
      
      const potentialSavings = Math.round(grocerySpending * 0.15)
      return `Based on your spending patterns, you could save $${potentialSavings}/month by optimizing your grocery shopping.`
    })

    const savingsTip = computed(() => {
      return `Try meal planning and bulk buying to reduce grocery expenses.`
    })

    const investmentInsight = computed(() => {
      const currentMonthTransactions = transactionsStore?.transactionsByMonth || []
      const income = currentMonthTransactions
        .filter(t => t.type === 'INCOME')
        .reduce((sum, t) => sum + parseFloat(t.amount), 0)
      
      const expenses = currentMonthTransactions
        .filter(t => t.type === 'EXPENSE')
        .reduce((sum, t) => sum + parseFloat(t.amount), 0)
      
      const savings = income - expenses
      const investmentAmount = Math.max(0, Math.round(savings * 0.3))
      
      return `With your current savings rate of $${savings.toFixed(2)}/month, you could invest $${investmentAmount}/month for retirement.`
    })

    const investmentOpportunity = computed(() => {
      return `Consider setting up automatic transfers to an investment account.`
    })

    const budgetOptimizationInsight = computed(() => {
      const currentMonthTransactions = transactionsStore?.transactionsByMonth || []
      const transportationSpending = currentMonthTransactions
        .filter(t => t.category === 'TRANSPORTATION' && t.type === 'EXPENSE')
        .reduce((sum, t) => sum + parseFloat(t.amount), 0)
      
      const potentialSavings = Math.round(transportationSpending * 0.15)
      return `Your transportation spending is $${transportationSpending.toFixed(2)} this month. You could save $${potentialSavings} with carpooling options.`
    })

    const budgetOptimizationSuggestion = computed(() => {
      return `Explore public transport or carpooling to save on fuel costs.`
    })

    const futurePlanningInsight = computed(() => {
      const currentMonthTransactions = transactionsStore?.transactionsByMonth || []
      const monthlyExpenses = currentMonthTransactions
        .filter(t => t.type === 'EXPENSE')
        .reduce((sum, t) => sum + parseFloat(t.amount), 0)
      
      const emergencyFundTarget = Math.round(monthlyExpenses * 6)
      const monthlySavings = Math.round(emergencyFundTarget / 12)
      
      return `Based on current trends, you'll need $${emergencyFundTarget.toLocaleString()} for emergency fund in 12 months.`
    })

    const futurePlanningGoal = computed(() => {
      const currentMonthTransactions = transactionsStore?.transactionsByMonth || []
      const monthlyExpenses = currentMonthTransactions
        .filter(t => t.type === 'EXPENSE')
        .reduce((sum, t) => sum + parseFloat(t.amount), 0)
      
      const emergencyFundTarget = Math.round(monthlyExpenses * 6)
      const monthlySavings = Math.round(emergencyFundTarget / 12)
      
      return `Set aside $${monthlySavings.toLocaleString()}/month to reach your emergency fund target.`
    })

    // Watch for store month changes to sync local state
    watch([() => transactionsStore?.currentMonth, () => transactionsStore?.currentYear], (newValues) => {
      if (newValues && newValues[0] !== undefined && newValues[1] !== undefined) {
        currentMonth.value = newValues[0]
        currentYear.value = newValues[1]
      }
    }, { immediate: true })

    // Initialize data when component mounts
    onMounted(async () => {
      try {
        // Ensure store is properly initialized
        if (!transactionsStore) {
          return
        }
        
        // Initialize month state from store or current date
        if (transactionsStore.currentMonth !== undefined && transactionsStore.currentYear !== undefined) {
          currentMonth.value = transactionsStore.currentMonth
          currentYear.value = transactionsStore.currentYear
        } else if (budgetsStore?.currentMonth !== undefined && budgetsStore?.currentYear !== undefined) {
          currentMonth.value = budgetsStore.currentMonth
          currentYear.value = budgetsStore.currentYear
        } else {
          // Set to current month if neither store has month state
          const now = new Date()
          currentMonth.value = now.getMonth()
          currentYear.value = now.getFullYear()
        }
        
        // Update both stores with the current month state
        if (transactionsStore) {
          transactionsStore.setCurrentMonth(currentMonth.value, currentYear.value)
        }
        if (budgetsStore) {
          budgetsStore.setCurrentMonth(currentMonth.value, currentYear.value)
        }
        
        // If store is not initialized, fetch data
        if (!transactionsStore.isInitialized) {
          await transactionsStore.fetchTransactions()
        }
        if (!budgetsStore.isInitialized) {
          await budgetsStore.fetchBudgets()
        }
        
        // If still no data, initialize with demo data
        if (transactionsStore.transactions.length === 0) {
          transactionsStore.initializeWithDemoData()
        }
        if (budgetsStore.budgets.length === 0) {
          budgetsStore.initializeWithDemoData()
        }
      } catch (error) {
        // Ensure demo data is loaded if everything fails
        if (transactionsStore.transactions.length === 0) {
          transactionsStore.initializeWithDemoData()
        }
        if (budgetsStore.budgets.length === 0) {
          budgetsStore.initializeWithDemoData()
        }
      }
    })

    return {
      transactionsStore,
      budgetsStore,
      currentMonth,
      currentYear,
      currentMonthDisplay,
      lastUpdated,
      previousMonth,
      nextMonth,
      goToCurrentMonth,
      spendingPatternInsight,
      spendingPatternRecommendation,
      anomalyInsight,
      anomalyAlert,
      savingsInsight,
      savingsTip,
      investmentInsight,
      investmentOpportunity,
      budgetOptimizationInsight,
      budgetOptimizationSuggestion,
      futurePlanningInsight,
      futurePlanningGoal
    }
  }
}
</script>
