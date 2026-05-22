<template>
  <div class="space-y-6 pt-6">
    <div
      class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between"
    >
      <div class="min-w-0">
        <h1 class="text-3xl font-bold text-slate-900">Bills</h1>
        <p class="text-slate-600 mt-2">
          Track upcoming payments and mark bills paid
        </p>
      </div>
      <button type="button" class="btn-primary shrink-0" @click="openCreate">
        <svg
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          aria-hidden="true"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 6v6m0 0v6m0-6h6m-6 0H6"
          ></path>
        </svg>
        <span>Add Bill</span>
      </button>
    </div>

    <div class="glass-card p-6">
      <div v-if="billsStore.isLoading" class="text-slate-500 text-center py-8">
        Loading…
      </div>
      <div
        v-else-if="!billsStore.bills.length"
        class="text-center py-8 text-slate-500"
      >
        No bills yet
      </div>
      <div v-else class="space-y-3">
        <div
          v-for="bill in billsStore.bills"
          :key="bill.id"
          class="flex items-center justify-between p-4 bg-slate-50 rounded-lg border border-slate-100"
        >
          <div>
            <p class="font-medium text-slate-900">{{ bill.payeeName }}</p>
            <p class="text-sm text-slate-500">
              Due {{ formatDate(bill.dueDate) }} · ${{
                Number(bill.amount).toFixed(2)
              }}
            </p>
          </div>
          <div class="flex items-center gap-2">
            <span
              v-if="bill.paid"
              class="text-xs font-medium text-green-700 bg-green-100 px-2 py-1 rounded"
              >Paid</span
            >
            <span
              v-else
              class="text-xs font-medium text-amber-700 bg-amber-100 px-2 py-1 rounded"
              >Unpaid</span
            >
            <button
              v-if="!bill.paid"
              type="button"
              class="btn-primary btn-sm"
              @click="pay(bill.id)"
            >
              Mark paid
            </button>
            <button
              class="text-sm text-slate-500 hover:text-red-600"
              @click="remove(bill.id)"
            >
              Delete
            </button>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="showModal"
      class="fixed inset-0 bg-slate-900/40 z-50 flex items-center justify-center p-4"
    >
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md p-6">
        <h3 class="text-xl font-bold text-slate-900 mb-4">
          {{ editingId ? "Edit Bill" : "New Bill" }}
        </h3>
        <form class="space-y-4" @submit.prevent="save">
          <div>
            <label class="form-label">Payee</label>
            <input
              v-model="form.payeeName"
              class="input-field w-full"
              required
            />
          </div>
          <div>
            <label class="form-label">Amount</label>
            <input
              v-model.number="form.amount"
              type="number"
              step="0.01"
              min="0.01"
              class="input-field w-full"
              required
            />
          </div>
          <div>
            <label class="form-label">Due date</label>
            <input
              v-model="form.dueDate"
              type="date"
              class="input-field w-full"
              required
            />
          </div>
          <div class="flex gap-3 pt-2">
            <button
              type="button"
              class="btn-secondary flex-1"
              @click="showModal = false"
            >
              Cancel
            </button>
            <button type="submit" class="btn-primary flex-1" :disabled="saving">
              {{ saving ? "Saving…" : "Save" }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useBillsStore } from "../stores/bills";
import { useToast } from "../composables/useToast";
import { getApiErrorMessage } from "../utils/apiError";

const billsStore = useBillsStore();
const toast = useToast();
const showModal = ref(false);
const editingId = ref(null);
const saving = ref(false);
const form = ref({ payeeName: "", amount: "", dueDate: "" });

const formatDate = (d) => new Date(d + "T00:00:00").toLocaleDateString();

const openCreate = () => {
  editingId.value = null;
  form.value = { payeeName: "", amount: "", dueDate: "" };
  showModal.value = true;
};

const save = async () => {
  saving.value = true;
  try {
    const payload = { ...form.value, paid: false };
    if (editingId.value) {
      await billsStore.updateBill(editingId.value, payload);
      toast.success("Bill updated");
    } else {
      await billsStore.createBill(payload);
      toast.success("Bill created");
    }
    showModal.value = false;
  } catch (e) {
    toast.error(getApiErrorMessage(e));
  } finally {
    saving.value = false;
  }
};

const pay = async (id) => {
  try {
    await billsStore.markPaid(id, true);
    toast.success("Bill marked paid");
  } catch (e) {
    toast.error(getApiErrorMessage(e));
  }
};

const remove = async (id) => {
  if (!confirm("Delete this bill?")) return;
  try {
    await billsStore.deleteBill(id);
    toast.success("Bill deleted");
  } catch (e) {
    toast.error(getApiErrorMessage(e));
  }
};

onMounted(() => billsStore.fetchBills());
</script>
