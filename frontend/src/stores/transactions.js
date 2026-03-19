import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

export const useTransactionsStore = defineStore('transactions', () => {
  // State
  const transactions = ref([])
  const isLoading = ref(false)
  const error = ref(null)
  const isInitialized = ref(false)
  const currentMonth = ref(new Date().getMonth())
  const currentYear = ref(new Date().getFullYear())

  // Getters
  const recentTransactions = computed(() => {
    return transactions.value.slice(0, 5) // Get latest 5 transactions
  })

  const totalIncome = computed(() => {
    return transactions.value
      .filter(t => t.type === 'INCOME')
      .reduce((sum, t) => sum + parseFloat(t.amount || 0), 0)
  })

  const totalExpenses = computed(() => {
    return transactions.value
      .filter(t => t.type === 'EXPENSE')
      .reduce((sum, t) => sum + parseFloat(t.amount || 0), 0)
  })

  const totalBalance = computed(() => {
    const income = totalIncome.value
    const expenses = totalExpenses.value
    const balance = income - expenses
    return balance
  })

  // Month-based filtering
  const transactionsByMonth = computed(() => {
    return transactions.value.filter(transaction => {
      const transactionDate = new Date(transaction.transactionDate)
      return transactionDate.getMonth() === currentMonth.value && 
             transactionDate.getFullYear() === currentYear.value
    })
  })

  const currentMonthIncome = computed(() => {
    return transactionsByMonth.value
      .filter(t => t.type === 'INCOME')
      .reduce((sum, t) => sum + parseFloat(t.amount || 0), 0)
  })

  const currentMonthExpenses = computed(() => {
    return transactionsByMonth.value
      .filter(t => t.type === 'EXPENSE')
      .reduce((sum, t) => sum + parseFloat(t.amount || 0), 0)
  })

  const currentMonthBalance = computed(() => {
    return currentMonthIncome.value - currentMonthExpenses.value
  })

  // Actions
  const fetchTransactions = async () => {
    if (isLoading.value) return // Prevent multiple simultaneous requests
    
    isLoading.value = true
    error.value = null
    try {
      const response = await axios.get('/transactions')
      
      if (response.data && Array.isArray(response.data)) {
        transactions.value = response.data
        isInitialized.value = true
      } else {
        transactions.value = []
      }
    } catch (err) {
      error.value = err.message
      console.error('Failed to fetch transactions:', err)
      
      // If backend is not available, initialize with demo data
      if (!isInitialized.value) {
        initializeWithDemoData()
      }
    } finally {
      isLoading.value = false
    }
  }

  const addTransaction = async (transactionData) => {
    isLoading.value = true
    error.value = null
    try {
      const transactionToAdd = {
        description: transactionData.description,
        amount: transactionData.amount,
        type: transactionData.type,
        category: transactionData.category,
        transactionDate: transactionData.transactionDate ? new Date(transactionData.transactionDate).toISOString() : new Date().toISOString()
      }
      
      try {
        const response = await axios.post('/transactions', transactionToAdd)
        
        if (response.data) {
          transactions.value.unshift(response.data)
          return response.data
        } else {
          throw new Error('No response data from backend')
        }
      } catch (axiosError) {
        // If it's a network error (backend not available), add to local state
        if (axiosError.code === 'ERR_NETWORK' || axiosError.message.includes('Network Error')) {
          console.log('Backend not available, adding transaction to local state')
          const localTransaction = {
            id: Date.now(),
            description: transactionData.description,
            amount: transactionData.amount,
            type: transactionData.type,
            category: transactionData.category,
            transactionDate: transactionData.transactionDate ? new Date(transactionData.transactionDate).toISOString() : new Date().toISOString()
          }
          transactions.value.unshift(localTransaction)
          return localTransaction
        }
        throw axiosError
      }
    } catch (err) {
      error.value = err.message
      console.error('Failed to add transaction:', err)
      
      // If backend fails, add to local state for demo mode
      const localTransaction = {
        id: Date.now(),
        description: transactionData.description,
        amount: transactionData.amount,
        type: transactionData.type,
        category: transactionData.category,
        transactionDate: transactionData.transactionDate ? new Date(transactionData.transactionDate).toISOString() : new Date().toISOString()
      }
      transactions.value.unshift(localTransaction)
      return localTransaction
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
    if (transactions.value.length === 0) {
      const now = new Date()
      const currentMonth = now.getMonth()
      
      // Create demo data with transactions from different months
      transactions.value = [
        {
          id: 1,
          description: 'Grocery Shopping',
          amount: 85.50,
          type: 'EXPENSE',
          category: 'FOOD_GROCERIES',
          transactionDate: new Date(now.getFullYear(), currentMonth, 15).toISOString()
        },
        {
          id: 2,
          description: 'Salary Deposit',
          amount: 3200.00,
          type: 'INCOME',
          category: 'SALARY',
          transactionDate: new Date(now.getFullYear(), currentMonth, 1).toISOString()
        },
        {
          id: 3,
          description: 'Gas Station',
          amount: 45.00,
          type: 'EXPENSE',
          category: 'TRANSPORTATION',
          transactionDate: new Date(now.getFullYear(), currentMonth, 20).toISOString()
        },
        {
          id: 4,
          description: 'Restaurant Dinner',
          amount: 65.00,
          type: 'EXPENSE',
          category: 'FOOD_RESTAURANT',
          transactionDate: new Date(now.getFullYear(), currentMonth - 1, 10).toISOString()
        },
        {
          id: 5,
          description: 'Freelance Project',
          amount: 800.00,
          type: 'INCOME',
          category: 'FREELANCE',
          transactionDate: new Date(now.getFullYear(), currentMonth - 1, 25).toISOString()
        },
        {
          id: 6,
          description: 'Movie Tickets',
          amount: 32.00,
          type: 'EXPENSE',
          category: 'ENTERTAINMENT',
          transactionDate: new Date(now.getFullYear(), currentMonth - 2, 5).toISOString()
        },
        {
          id: 7,
          description: 'Online Shopping',
          amount: 120.00,
          type: 'EXPENSE',
          category: 'SHOPPING',
          transactionDate: new Date(now.getFullYear(), currentMonth - 2, 18).toISOString()
        },
        {
          id: 8,
          description: 'Investment Dividend',
          amount: 150.00,
          type: 'INCOME',
          category: 'INVESTMENTS',
          transactionDate: new Date(now.getFullYear(), currentMonth - 3, 1).toISOString()
        },
        {
          id: 9,
          description: 'Utility Bills',
          amount: 95.00,
          type: 'EXPENSE',
          category: 'HOUSING_UTILITIES',
          transactionDate: new Date(now.getFullYear(), currentMonth - 3, 15).toISOString()
        },
        {
          id: 10,
          description: 'Gym Membership',
          amount: 45.00,
          type: 'EXPENSE',
          category: 'HEALTHCARE',
          transactionDate: new Date(now.getFullYear(), currentMonth - 4, 1).toISOString()
        }
      ]
      isInitialized.value = true
    }
  }

  // Force refresh data
  const refreshData = async () => {
    isInitialized.value = false
    await fetchTransactions()
  }

  return {
    // State
    transactions,
    isLoading,
    error,
    isInitialized,
    currentMonth,
    currentYear,
    
    // Getters
    recentTransactions,
    totalIncome,
    totalExpenses,
    totalBalance,
    transactionsByMonth,
    currentMonthIncome,
    currentMonthExpenses,
    currentMonthBalance,
    
    // Actions
    fetchTransactions,
    addTransaction,
    initializeWithDemoData,
    refreshData,
    setCurrentMonth,
    goToCurrentMonth,
    previousMonth,
    nextMonth
  }
})
