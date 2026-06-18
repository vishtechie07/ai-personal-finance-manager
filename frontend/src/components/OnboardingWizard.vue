<template>
  <div
    v-if="visible"
    class="fixed inset-0 z-[60] bg-slate-900/50 backdrop-blur-sm flex items-center justify-center p-4"
  >
    <div
      class="bg-white rounded-2xl shadow-2xl border border-slate-200 w-full max-w-lg p-6 space-y-5 animate-slide-up"
    >
      <div>
        <p class="text-xs font-semibold uppercase tracking-wide text-primary-600">
          Welcome to SpendSense
        </p>
        <h2 class="text-2xl font-bold text-slate-900 mt-1">Quick setup</h2>
        <p class="text-sm text-slate-600 mt-2">
          Three steps to make the app yours — takes about a minute.
        </p>
      </div>

      <ol class="space-y-3">
        <li
          v-for="(step, idx) in steps"
          :key="step.id"
          class="flex gap-3 p-3 rounded-xl border"
          :class="
            idx === current
              ? 'border-primary-200 bg-primary-50/60'
              : 'border-slate-200 bg-slate-50/50'
          "
        >
          <span
            class="w-7 h-7 shrink-0 rounded-full flex items-center justify-center text-sm font-bold"
            :class="
              idx < current
                ? 'bg-success-500 text-white'
                : idx === current
                  ? 'bg-primary-600 text-white'
                  : 'bg-slate-200 text-slate-600'
            "
          >
            {{ idx < current ? "✓" : idx + 1 }}
          </span>
          <div>
            <p class="font-semibold text-slate-900">{{ step.title }}</p>
            <p class="text-sm text-slate-600">{{ step.body }}</p>
          </div>
        </li>
      </ol>

      <div class="flex gap-3 pt-1">
        <button type="button" class="btn-secondary flex-1" @click="skip">
          Skip
        </button>
        <button type="button" class="btn-primary flex-1" @click="next">
          {{ current >= steps.length - 1 ? "Finish" : "Next" }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const props = defineProps({
  show: { type: Boolean, default: false },
});
const emit = defineEmits(["done"]);

const router = useRouter();
const authStore = useAuthStore();
const current = ref(0);

const steps = [
  {
    id: "tx",
    title: "Log your first expense",
    body: "Add a real transaction or import CSV from your bank export.",
  },
  {
    id: "budget",
    title: "Set a monthly budget",
    body: "Pick one category — groceries or dining is a great start.",
  },
  {
    id: "bill",
    title: "Track a recurring bill",
    body: "Use Detect recurring or add rent, utilities, or subscriptions.",
  },
];

const storageKey = computed(() =>
  authStore.user?.id ? `pfm_onboarding_${authStore.user.id}` : null,
);

const visible = computed(() => props.show);

onMounted(() => {
  if (storageKey.value && localStorage.getItem(storageKey.value) === "done") {
    emit("done");
  }
});

function skip() {
  finish();
}

function next() {
  const step = steps[current.value];
  if (step.id === "tx") router.push("/transactions");
  if (step.id === "budget") router.push("/budgets");
  if (step.id === "bill") router.push("/bills");

  if (current.value >= steps.length - 1) {
    finish();
    return;
  }
  current.value++;
}

function finish() {
  if (storageKey.value) localStorage.setItem(storageKey.value, "done");
  emit("done");
}
</script>
