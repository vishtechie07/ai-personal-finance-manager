import { defineStore } from "pinia";
import { ref, computed } from "vue";

/** Shared month context across dashboard, transactions, budgets, insights. */
export const useFinanceMonthStore = defineStore("financeMonth", () => {
  const month = ref(new Date().getMonth());
  const year = ref(new Date().getFullYear());

  const yearMonth = computed(() => {
    const m = String(month.value + 1).padStart(2, "0");
    return `${year.value}-${m}`;
  });

  const label = computed(() => {
    const names = [
      "January", "February", "March", "April", "May", "June",
      "July", "August", "September", "October", "November", "December",
    ];
    return `${names[month.value]} ${year.value}`;
  });

  function set(m, y) {
    month.value = m;
    year.value = y;
  }

  function goToCurrent() {
    const now = new Date();
    month.value = now.getMonth();
    year.value = now.getFullYear();
  }

  function previous() {
    if (month.value === 0) {
      month.value = 11;
      year.value--;
    } else {
      month.value--;
    }
  }

  function next() {
    if (month.value === 11) {
      month.value = 0;
      year.value++;
    } else {
      month.value++;
    }
  }

  return { month, year, yearMonth, label, set, goToCurrent, previous, next };
});
