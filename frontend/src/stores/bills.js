import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useBillsStore = defineStore('bills', () => {
  const bills = ref([])
  const dueSoon = ref([])
  const isLoading = ref(false)

  const fetchBills = async () => {
    isLoading.value = true
    try {
      const res = await axios.get('/bills')
      bills.value = res.data || []
    } finally {
      isLoading.value = false
    }
  }

  const fetchDueSoon = async (days = 7) => {
    const res = await axios.get(`/bills/due-soon?days=${days}`)
    dueSoon.value = res.data || []
  }

  const createBill = async (data) => {
    const res = await axios.post('/bills', data)
    await fetchBills()
    await fetchDueSoon()
    return res.data
  }

  const updateBill = async (id, data) => {
    const res = await axios.put(`/bills/${id}`, data)
    await fetchBills()
    await fetchDueSoon()
    return res.data
  }

  const deleteBill = async (id) => {
    await axios.delete(`/bills/${id}`)
    await fetchBills()
    await fetchDueSoon()
  }

  const markPaid = async (id, createTransaction = true) => {
    const res = await axios.post(`/bills/${id}/mark-paid`, { createTransaction })
    await fetchBills()
    await fetchDueSoon()
    return res.data
  }

  return { bills, dueSoon, isLoading, fetchBills, fetchDueSoon, createBill, updateBill, deleteBill, markPaid }
})
