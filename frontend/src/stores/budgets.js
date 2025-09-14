import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

export const useBudgetsStore = defineStore('budgets', () => {
  // State
  const budgets = ref([])
  const isLoading = ref(false)
  const error = ref(null)
  const isInitialized = ref(false)
  const currentMonth = ref(new Date().getMonth())
  const currentYear = ref(new Date().getFullYear())

  // Getters
  const totalBudgetAmount = computed(() => {
    return budgets.value.reduce((sum, budget) => sum + parseFloat(budget.amount || 0), 0)
  })

  const totalSpentAmount = computed(() => {
    return budgets.value.reduce((sum, budget) => sum + parseFloat(budget.spentAmount || 0), 0)
  })

  const totalRemainingAmount = computed(() => {
    return totalBudgetAmount.value - totalSpentAmount.value
  })

  const overBudgetBudgets = computed(() => {
    return budgets.value.filter(budget => {
      const remaining = parseFloat(budget.remainingAmount || 0)
      return remaining < 0
    })
  })

  const nearLimitBudgets = computed(() => {
    return budgets.value.filter(budget => {
      const percentage = (parseFloat(budget.spentAmount || 0) / parseFloat(budget.amount || 1)) * 100
      return percentage >= 80 && percentage < 100
    })
  })

  // Month-filtered versions for current month alerts
  const currentMonthOverBudgetBudgets = computed(() => {
    return budgetsByMonth.value.filter(budget => {
      const remaining = parseFloat(budget.remainingAmount || 0)
      return remaining < 0
    })
  })

  const currentMonthNearLimitBudgets = computed(() => {
    return budgetsByMonth.value.filter(budget => {
      const percentage = (parseFloat(budget.spentAmount || 0) / parseFloat(budget.amount || 1)) * 100
      return percentage >= 80 && percentage < 100
    })
  })

  // Month-based filtering
  const budgetsByMonth = computed(() => {
    return budgets.value.filter(budget => {
      const budgetDate = new Date(budget.startDate)
      return budgetDate.getMonth() === currentMonth.value && 
             budgetDate.getFullYear() === currentYear.value
    })
  })

  const currentMonthBudgetAmount = computed(() => {
    return budgetsByMonth.value.reduce((sum, budget) => sum + parseFloat(budget.amount || 0), 0)
  })

  const currentMonthSpentAmount = computed(() => {
    return budgetsByMonth.value.reduce((sum, budget) => sum + parseFloat(budget.spentAmount || 0), 0)
  })

  const currentMonthRemainingAmount = computed(() => {
    return currentMonthBudgetAmount.value - currentMonthSpentAmount.value
  })

  // Actions
  const fetchBudgets = async () => {
    if (isLoading.value) return // Prevent multiple simultaneous requests
    
    isLoading.value = true
    error.value = null
    try {
      const response = await axios.get('/api/budgets')
      
      if (response.data && Array.isArray(response.data)) {
        budgets.value = response.data
        isInitialized.value = true
      } else {
        budgets.value = []
      }
    } catch (err) {
      error.value = err.message
      console.error('Failed to fetch budgets:', err)
      
      // If backend is not available, initialize with demo data
      if (!isInitialized.value) {
        initializeWithDemoData()
      }
    } finally {
      isLoading.value = false
    }
  }

  const addBudget = async (budgetData) => {
    isLoading.value = true
    error.value = null
    try {
      const budgetToAdd = {
        name: budgetData.name,
        amount: budgetData.amount,
        category: budgetData.category,
        period: budgetData.period || 'MONTHLY',
        startDate: budgetData.startDate ? new Date(budgetData.startDate).toISOString() : new Date().toISOString(),
        endDate: null, // Will be set by backend
        spentAmount: 0,
        remainingAmount: budgetData.amount,
        isActive: true
      }
      
      try {
        const response = await axios.post('/api/budgets', budgetToAdd)
        
        if (response.data) {
          budgets.value.unshift(response.data)
          return response.data
        } else {
          throw new Error('No response data from backend')
        }
      } catch (axiosError) {
        // If it's a network error (backend not available), add to local state
        if (axiosError.code === 'ERR_NETWORK' || axiosError.message.includes('Network Error')) {
          console.log('Backend not available, adding budget to local state')
          const localBudget = {
            id: Date.now(),
            name: budgetData.name,
            amount: budgetData.amount,
            category: budgetData.category,
            period: budgetData.period || 'MONTHLY',
            startDate: budgetData.startDate ? new Date(budgetData.startDate).toISOString() : new Date().toISOString(),
            endDate: null,
            spentAmount: 0,
            remainingAmount: budgetData.amount,
            isActive: true
          }
          budgets.value.unshift(localBudget)
          return localBudget
        }
        throw axiosError
      }
    } catch (err) {
      error.value = err.message
      console.error('Failed to add budget:', err)
      
      // If backend fails, add to local state for demo mode
      const localBudget = {
        id: Date.now(),
        name: budgetData.name,
        amount: budgetData.amount,
        category: budgetData.category,
        period: budgetData.period || 'MONTHLY',
        startDate: budgetData.startDate ? new Date(budgetData.startDate).toISOString() : new Date().toISOString(),
        endDate: null,
        spentAmount: 0,
        remainingAmount: budgetData.amount,
        isActive: true
      }
      budgets.value.unshift(localBudget)
      return localBudget
    } finally {
      isLoading.value = false
    }
  }

  // Month navigation
  const setCurrentMonth = (month, year) => {
    currentMonth.value = month
    currentYear.value = year
  }

  const goToCurrentMonth = () => {
    const now = new Date()
    currentMonth.value = now.getMonth()
    currentYear.value = now.getFullYear()
  }

  const previousMonth = () => {
    if (currentMonth.value === 0) {
      currentMonth.value = 11
      currentYear.value--
    } else {
      currentMonth.value--
    }
  }

  const nextMonth = () => {
    if (currentMonth.value === 11) {
      currentMonth.value = 0
      currentYear.value++
    } else {
      currentMonth.value++
    }
  }

  // Initialize with demo data if no backend connection
  const initializeWithDemoData = () => {
    if (budgets.value.length === 0) {
      const now = new Date()
      const currentMonth = now.getMonth()
      
      budgets.value = [
        {
          id: 1,
          name: 'Groceries Budget',
          amount: 500.00,
          category: 'FOOD_GROCERIES',
          period: 'MONTHLY',
          startDate: new Date(now.getFullYear(), currentMonth, 1).toISOString(),
          endDate: new Date(now.getFullYear(), currentMonth + 1, 1).toISOString(),
          spentAmount: 320.50,
          remainingAmount: 179.50,
          isActive: true
        },
        {
          id: 2,
          name: 'Entertainment Budget',
          amount: 200.00,
          category: 'ENTERTAINMENT',
          period: 'MONTHLY',
          startDate: new Date(now.getFullYear(), currentMonth, 1).toISOString(),
          endDate: new Date(now.getFullYear(), currentMonth + 1, 1).toISOString(),
          spentAmount: 150.00,
          remainingAmount: 50.00,
          isActive: true
        },
        {
          id: 3,
          name: 'Transportation Budget',
          amount: 300.00,
          category: 'TRANSPORTATION',
          period: 'MONTHLY',
          startDate: new Date(now.getFullYear(), currentMonth, 1).toISOString(),
          endDate: new Date(now.getFullYear(), currentMonth + 1, 1).toISOString(),
          spentAmount: 280.00,
          remainingAmount: 20.00,
          isActive: true
        },
        {
          id: 4,
          name: 'Housing Budget',
          amount: 1200.00,
          category: 'HOUSING_RENT',
          period: 'MONTHLY',
          startDate: new Date(now.getFullYear(), currentMonth - 1, 1).toISOString(),
          endDate: new Date(now.getFullYear(), currentMonth, 1).toISOString(),
          spentAmount: 1200.00,
          remainingAmount: 0.00,
          isActive: true
        },
        {
          id: 5,
          name: 'Healthcare Budget',
          amount: 150.00,
          category: 'HEALTHCARE',
          period: 'MONTHLY',
          startDate: new Date(now.getFullYear(), currentMonth - 2, 1).toISOString(),
          endDate: new Date(now.getFullYear(), currentMonth - 1, 1).toISOString(),
          spentAmount: 120.00,
          remainingAmount: 30.00,
          isActive: true
        }
      ]
      isInitialized.value = true
    }
  }

  // Force refresh data
  const refreshData = async () => {
    isInitialized.value = false
    await fetchBudgets()
  }

  return {
    // State
    budgets,
    isLoading,
    error,
    isInitialized,
    currentMonth,
    currentYear,
    
    // Getters
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
    
    // Actions
    fetchBudgets,
    addBudget,
    initializeWithDemoData,
    refreshData,
    setCurrentMonth,
    goToCurrentMonth,
    previousMonth,
    nextMonth
  }
})
