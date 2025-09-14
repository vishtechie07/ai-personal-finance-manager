<template>
  <div class="space-y-6 pt-6">
    <!-- Page Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Transactions</h1>
        <p class="text-gray-600 mt-2">Manage your financial transactions</p>
      </div>
      <button @click="showAddTransaction = true" class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2">
        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
        </svg>
        Add Transaction
      </button>
    </div>

    <!-- Month Selector -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
      <div class="flex items-center justify-between">
        <h3 class="text-lg font-semibold text-gray-900">Filter by Month</h3>
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

    <!-- Transactions List -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-semibold text-gray-900">Transactions for {{ currentMonthDisplay }}</h3>
        <div class="text-sm text-gray-500">
          {{ filteredTransactions.length }} transaction{{ filteredTransactions.length !== 1 ? 's' : '' }}
        </div>
      </div>
      <div class="space-y-3">
        <div v-for="transaction in filteredTransactions" :key="transaction.id" 
             class="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
          <div class="flex items-center space-x-3">
            <div class="w-8 h-8 rounded-full flex items-center justify-center" 
                 :class="transaction.type === 'EXPENSE' ? 'bg-red-100' : 'bg-green-100'">
              <svg class="w-4 h-4" :class="transaction.type === 'EXPENSE' ? 'text-red-600' : 'text-green-600'" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z"></path>
              </svg>
            </div>
            <div>
              <p class="font-medium text-gray-900">{{ transaction.description }}</p>
              <p class="text-sm text-gray-500">{{ transaction.category }}</p>
            </div>
          </div>
          <div class="text-right">
            <p class="font-medium text-gray-900" :class="transaction.type === 'EXPENSE' ? 'text-red-600' : 'text-green-600'">
              {{ transaction.type === 'EXPENSE' ? '-' : '+' }}${{ transaction.amount.toFixed(2) }}
            </p>
            <p class="text-sm text-gray-500">{{ formatDate(transaction.transactionDate) }}</p>
          </div>
        </div>
        
        <div v-if="filteredTransactions.length === 0" class="text-center py-8 text-gray-500">
          <svg class="w-12 h-12 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
          </svg>
          <p>No transactions found for {{ currentMonthDisplay }}</p>
        </div>
      </div>
    </div>

    <!-- Add Transaction Modal -->
    <div v-if="showAddTransaction" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
      <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
        <div class="mt-3">
          <h3 class="text-lg font-medium text-gray-900 mb-4">Add New Transaction</h3>
          <form @submit.prevent="addTransaction" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Description</label>
              <input v-model="newTransaction.description" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent" required>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Amount</label>
              <input v-model="newTransaction.amount" type="number" step="0.01" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent" required>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Type</label>
              <select v-model="newTransaction.type" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                <option value="EXPENSE">Expense</option>
                <option value="INCOME">Income</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Date</label>
              <input v-model="newTransaction.transactionDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent" required>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Category</label>
              <div class="relative">
                <input 
                  v-model="newTransaction.category" 
                  type="text" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent pr-20" 
                  placeholder="Smart categorization will suggest category"
                  :class="newTransaction.category && newTransaction.category !== 'Uncategorized' ? 'border-green-300 bg-green-50' : ''"
                >
                <div class="absolute inset-y-0 right-0 flex items-center pr-3">
                  <div v-if="newTransaction.description && newTransaction.description.length >= 3 && !newTransaction.category" class="flex items-center space-x-2">
                    <div class="w-2 h-2 bg-blue-500 rounded-full animate-pulse"></div>
                    <span class="text-xs text-blue-600">Analyzing...</span>
                  </div>
                  <div v-else-if="newTransaction.category && newTransaction.category !== 'Uncategorized'" class="flex items-center space-x-2">
                    <svg class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                    <span class="text-xs text-green-600">Category suggested</span>
                  </div>
                </div>
              </div>
              <div class="mt-2 flex items-center space-x-2">
                <button 
                  v-if="newTransaction.description && newTransaction.description.length >= 3" 
                  @click="suggestCategory(newTransaction.description)" 
                  type="button"
                  class="text-xs bg-blue-100 text-blue-600 px-2 py-1 rounded hover:bg-blue-200 transition-colors"
                >
                  🤖 Get Smart Suggestion
                </button>
                <p v-if="newTransaction.category && newTransaction.category !== 'Uncategorized'" class="text-xs text-green-600">
                  Smart suggestion: <span class="font-medium">{{ newTransaction.category }}</span>
                </p>
              </div>
            </div>

            <div class="flex space-x-3">
              <button type="submit" class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 flex-1">Add Transaction</button>
              <button type="button" @click="showAddTransaction = false" class="bg-gray-200 hover:bg-gray-300 text-gray-900 font-medium py-2 px-4 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2">Cancel</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue'
import { useTransactionsStore } from '../stores/transactions'
import { useBudgetsStore } from '../stores/budgets'

export default {
  name: 'Transactions',
  setup() {
    const transactionsStore = useTransactionsStore()
    const budgetsStore = useBudgetsStore()
    const showAddTransaction = ref(false)
    
    // Initialize month state from store or current date
    const currentMonth = ref(transactionsStore?.currentMonth || new Date().getMonth())
    const currentYear = ref(transactionsStore?.currentYear || new Date().getFullYear())
    
    const newTransaction = ref({
      description: '',
      amount: '',
      type: 'EXPENSE',
      category: '',
      transactionDate: new Date().toISOString().split('T')[0] // Today's date as default
    })
    
    // Use shared store data
    const transactions = computed(() => transactionsStore.transactions)

    // Computed properties for month filtering
    const currentMonthDisplay = computed(() => {
      const monthNames = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
      ]
      return `${monthNames[currentMonth.value]} ${currentYear.value}`
    })

    const filteredTransactions = computed(() => {
      return transactions.value.filter(transaction => {
        const transactionDate = new Date(transaction.transactionDate)
        return transactionDate.getMonth() === currentMonth.value && 
               transactionDate.getFullYear() === currentYear.value
      })
    })

    const formatDate = (date) => {
      return new Date(date).toLocaleDateString()
    }

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

    // Smart Category Suggestion Function
    const suggestCategory = async (description) => {
      if (!description || description.trim().length < 3) return
      
      // Simple smart logic based on keywords in description
      const descriptionLower = description.toLowerCase()
      
      const categoryKeywords = {
        'FOOD_GROCERIES': ['food', 'grocery', 'supermarket', 'walmart', 'target', 'costco', 'safeway', 'kroger'],
        'FOOD_RESTAURANT': ['restaurant', 'dining', 'meal', 'lunch', 'dinner', 'breakfast', 'coffee', 'snack', 'pizza', 'burger', 'sushi', 'share', 'doordash', 'uber eats'],
        'TRANSPORTATION': ['gas', 'fuel', 'uber', 'lyft', 'taxi', 'bus', 'train', 'subway', 'parking', 'toll', 'car', 'vehicle', 'transport'],
        'HOUSING_RENT': ['rent', 'lease'],
        'HOUSING_UTILITIES': ['utilities', 'electricity', 'water', 'gas', 'internet', 'cable', 'wifi'],
        'HOUSING_MAINTENANCE': ['maintenance', 'repair', 'home', 'house', 'plumbing', 'electrical'],
        'ENTERTAINMENT': ['movie', 'theater', 'concert', 'show', 'game', 'ticket', 'amusement', 'park', 'museum', 'zoo', 'entertainment', 'netflix', 'spotify'],
        'SHOPPING': ['clothes', 'shoes', 'accessories', 'electronics', 'books', 'gifts', 'shopping', 'store', 'mall', 'online', 'amazon'],
        'HEALTHCARE': ['doctor', 'dentist', 'pharmacy', 'medicine', 'medical', 'health', 'insurance', 'hospital', 'clinic', 'therapy'],
        'SALARY': ['salary', 'wage', 'income', 'payment', 'deposit', 'bonus', 'commission', 'overtime', 'paycheck'],
        'INVESTMENTS': ['dividend', 'interest', 'investment', 'stock', 'bond', 'fund', 'portfolio', 'return', 'profit']
      }
      
      // Find the best matching category
      let bestMatch = 'Uncategorized'
      let bestScore = 0
      
      for (const [category, keywords] of Object.entries(categoryKeywords)) {
        let score = 0
        for (const keyword of keywords) {
          if (descriptionLower.includes(keyword)) {
            score += 1
          }
        }
        if (score > bestScore) {
          bestScore = score
          bestMatch = category
        }
      }
      
      // Only update if we found a meaningful match
      if (bestScore > 0 && bestMatch !== 'Uncategorized') {
        newTransaction.value.category = bestMatch
      }
      
    }

    // Watch for description changes to trigger smart suggestion
    watch(() => newTransaction.value.description, (newDescription) => {
      if (newDescription && newDescription.length >= 3) {
        // Debounce the suggestion to avoid too many calls
        setTimeout(() => {
          suggestCategory(newDescription)
        }, 300)
      }
      // Don't clear category when description is short - let user keep it
    })

    // Watch for store month changes to sync local state
    watch([() => transactionsStore?.currentMonth, () => transactionsStore?.currentYear], (newValues) => {
      if (newValues && newValues[0] !== undefined && newValues[1] !== undefined) {
        currentMonth.value = newValues[0]
        currentYear.value = newValues[1]
      }
    }, { immediate: true })

    const addTransaction = async () => {
      // Validate required fields
      if (!newTransaction.value.description || !newTransaction.value.amount || !newTransaction.value.category || !newTransaction.value.transactionDate) {
        alert('Please fill in all required fields (Description, Amount, Date, and Category)')
        return
      }
      
      if (!transactionsStore) {
        alert('Transactions store not available. Please refresh the page.')
        return
      }
      
      try {
        // Use the shared store to add the transaction
        const newTransactionData = await transactionsStore.addTransaction(newTransaction.value)
        
        showAddTransaction.value = false
        newTransaction.value = { 
          description: '', 
          amount: '', 
          type: 'EXPENSE', 
          category: '',
          transactionDate: new Date().toISOString().split('T')[0]
        }
      } catch (error) {
        alert(`Failed to add transaction: ${error.message || 'Please try again.'}`)
      }
    }

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
        } else if (budgetsStore.currentMonth !== undefined && budgetsStore.currentYear !== undefined) {
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
        
        // If still no data, initialize with demo data
        if (transactionsStore.transactions.length === 0) {
          transactionsStore.initializeWithDemoData()
        }
      } catch (error) {
        // Ensure demo data is loaded if everything fails
        if (transactionsStore.transactions.length === 0) {
          transactionsStore.initializeWithDemoData()
        }
      }
    })

    return {
      transactionsStore,
      budgetsStore,
      showAddTransaction,
      newTransaction,
      transactions,
      currentMonth,
      currentYear,
      currentMonthDisplay,
      filteredTransactions,
      formatDate,
      addTransaction,
      suggestCategory,
      previousMonth,
      nextMonth,
      goToCurrentMonth
    }
  }
}
</script>
