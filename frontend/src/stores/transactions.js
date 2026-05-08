import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

export const useTransactionsStore = defineStore('transactions', () => {
  const transactions = ref([])
  const isLoading = ref(false)
  const error = ref(null)
  const isInitialized = ref(false)
  const currentMonth = ref(new Date().getMonth())
  const currentYear = ref(new Date().getFullYear())

  const recentTransactions = computed(() => {
    return transactions.value.slice(0, 5)
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

  const totalBalance = computed(() => totalIncome.value - totalExpenses.value)

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

  const fetchTransactions = async () => {
    if (isLoading.value) return

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
      transactions.value = []
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
        transactionDate: transactionData.transactionDate
          ? new Date(transactionData.transactionDate).toISOString()
          : new Date().toISOString()
      }

      const response = await axios.post('/transactions', transactionToAdd)

      if (response.data) {
        transactions.value.unshift(response.data)
        return response.data
      }
      throw new Error('No response data from backend')
    } catch (err) {
      error.value = err.message
      console.error('Failed to add transaction:', err)
      throw err
    } finally {
      isLoading.value = false
    }
  }

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

  const refreshData = async () => {
    isInitialized.value = false
    await fetchTransactions()
  }

  return {
    transactions,
    isLoading,
    error,
    isInitialized,
    currentMonth,
    currentYear,
    recentTransactions,
    totalIncome,
    totalExpenses,
    totalBalance,
    transactionsByMonth,
    currentMonthIncome,
    currentMonthExpenses,
    currentMonthBalance,
    fetchTransactions,
    addTransaction,
    refreshData,
    setCurrentMonth,
    goToCurrentMonth,
    previousMonth,
    nextMonth
  }
})
