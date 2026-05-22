import { defineStore } from "pinia";
import { ref } from "vue";
import axios from "axios";

export const useBillsStore = defineStore("bills", () => {
  const bills = ref([]);
  const dueSoon = ref([]);
  const isLoading = ref(false);

  const fetchBills = async () => {
    isLoading.value = true;
    try {
      const res = await axios.get("/bills");
      bills.value = Array.isArray(res.data) ? res.data : [];
    } catch (err) {
      console.warn("Failed to load bills", err);
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const fetchDueSoon = async (days = 7) => {
    try {
      const res = await axios.get(`/bills/due-soon?days=${days}`);
      dueSoon.value = Array.isArray(res.data) ? res.data : [];
    } catch (err) {
      console.warn("Failed to load due-soon bills", err);
      dueSoon.value = [];
    }
  };

  const refreshLists = async () => {
    await Promise.all([fetchBills(), fetchDueSoon()]);
  };

  const createBill = async (data) => {
    const res = await axios.post("/bills", data);
    await refreshLists();
    return res.data;
  };

  const updateBill = async (id, data) => {
    const res = await axios.put(`/bills/${id}`, data);
    await refreshLists();
    return res.data;
  };

  const deleteBill = async (id) => {
    await axios.delete(`/bills/${id}`);
    bills.value = bills.value.filter((b) => b.id !== id);
    dueSoon.value = dueSoon.value.filter((b) => b.id !== id);
    try {
      await refreshLists();
    } catch {
      /* mutation succeeded; list will sync on next visit */
    }
  };

  const markPaid = async (id, createTransaction = true) => {
    const res = await axios.post(`/bills/${id}/mark-paid`, {
      createTransaction,
    });
    if (res.data) {
      const idx = bills.value.findIndex((b) => b.id === id);
      if (idx >= 0) {
        bills.value[idx] = res.data;
      }
    }
    try {
      await refreshLists();
    } catch {
      /* keep optimistic row from mark-paid response */
    }
    return res.data;
  };

  return {
    bills,
    dueSoon,
    isLoading,
    fetchBills,
    fetchDueSoon,
    createBill,
    updateBill,
    deleteBill,
    markPaid,
  };
});
