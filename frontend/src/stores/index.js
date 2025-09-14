import { useTransactionsStore } from './transactions'
import { useBudgetsStore } from './budgets'

// Global store synchronization
export const syncStores = async () => {
  console.log('Syncing all stores...')
  
  const transactionsStore = useTransactionsStore()
  const budgetsStore = useBudgetsStore()
  
  try {
    // Fetch data for all stores in parallel
    await Promise.all([
      transactionsStore.fetchTransactions(),
      budgetsStore.fetchBudgets()
    ])
    
    console.log('All stores synced successfully')
    return true
  } catch (error) {
    console.error('Error syncing stores:', error)
    return false
  }
}

// Initialize all stores with demo data if needed
export const initializeStores = async () => {
  console.log('Initializing all stores...')
  
  const transactionsStore = useTransactionsStore()
  const budgetsStore = useBudgetsStore()
  
  try {
    // Check if stores need initialization
    if (!transactionsStore.isInitialized && transactionsStore.transactions.length === 0) {
      console.log('Initializing transactions store with demo data')
      transactionsStore.initializeWithDemoData()
    }
    
    if (!budgetsStore.isInitialized && budgetsStore.budgets.length === 0) {
      console.log('Initializing budgets store with demo data')
      budgetsStore.initializeWithDemoData()
    }
    
    console.log('All stores initialized')
    return true
  } catch (error) {
    console.error('Error initializing stores:', error)
    return false
  }
}

// Refresh all stores
export const refreshAllStores = async () => {
  console.log('Refreshing all stores...')
  
  const transactionsStore = useTransactionsStore()
  const budgetsStore = useBudgetsStore()
  
  try {
    await Promise.all([
      transactionsStore.refreshData(),
      budgetsStore.refreshData()
    ])
    
    console.log('All stores refreshed successfully')
    return true
  } catch (error) {
    console.error('Error refreshing stores:', error)
    return false
  }
}
