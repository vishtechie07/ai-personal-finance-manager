import { useTransactionsStore } from './transactions'
import { useBudgetsStore } from './budgets'

export const syncStores = async () => {
  const transactionsStore = useTransactionsStore()
  const budgetsStore = useBudgetsStore()

  try {
    await Promise.all([
      transactionsStore.fetchTransactions(),
      budgetsStore.fetchBudgets()
    ])
    return true
  } catch (error) {
    console.error('Error syncing stores:', error)
    return false
  }
}

export const initializeStores = async () => {
  return syncStores()
}

export const refreshAllStores = async () => {
  const transactionsStore = useTransactionsStore()
  const budgetsStore = useBudgetsStore()

  try {
    await Promise.all([
      transactionsStore.refreshData(),
      budgetsStore.refreshData()
    ])
    return true
  } catch (error) {
    console.error('Error refreshing stores:', error)
    return false
  }
}
