<template>
  <div class="min-h-screen">
    <!-- Hero Section -->
    <section class="bg-gradient-to-br from-primary-50 to-primary-100 py-20">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <h1 class="text-4xl font-bold text-slate-900 mb-6">
          Smarter spending with
          <span class="text-primary-600">SpendSense</span>
        </h1>
        <p class="text-xl text-slate-600 mb-8 max-w-3xl mx-auto">
          Track budgets and transactions, spot patterns, and get AI-powered
          category suggestions
        </p>
        <div
          class="flex flex-col sm:flex-row gap-4 justify-center items-center"
        >
          <button
            type="button"
            class="btn-primary text-base py-3.5 px-8 min-h-[3rem]"
            @click="getStarted"
          >
            Get Started Free
          </button>
          <button
            type="button"
            class="btn-secondary text-base py-3.5 px-8 min-h-[3rem]"
            @click="learnMore"
          >
            Learn More
          </button>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section id="features" class="py-20 bg-slate-100">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-bold text-slate-900 mb-4">
            What you can do with SpendSense
          </h2>
          <p class="text-xl text-slate-600 max-w-2xl mx-auto">
            Practical tools to log spending, set budgets, and see where your money goes.
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 text-center"
          >
            <div
              class="w-16 h-16 bg-blue-100 rounded-lg flex items-center justify-center mx-auto mb-4"
            >
              <svg
                class="w-8 h-8 text-blue-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
                ></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-slate-900 mb-2">
              Smart Categorization
            </h3>
            <p class="text-slate-600">
              Keyword rules plus optional AI suggestions — no bank login required.
            </p>
          </div>

          <div
            class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 text-center"
          >
            <div
              class="w-16 h-16 bg-yellow-100 rounded-lg flex items-center justify-center mx-auto mb-4"
            >
              <svg
                class="w-8 h-8 text-yellow-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z"
                ></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-slate-900 mb-2">
              Spending alerts
            </h3>
            <p class="text-slate-600">
              Budget warnings, duplicate charge detection, and bill due reminders.
            </p>
          </div>

          <div
            class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 text-center"
          >
            <div
              class="w-16 h-16 bg-green-100 rounded-lg flex items-center justify-center mx-auto mb-4"
            >
              <svg
                class="w-8 h-8 text-green-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"
                ></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-slate-900 mb-2">
              Monthly insights
            </h3>
            <p class="text-slate-600">
              Charts, budget health, and an optional AI monthly brief from your data.
            </p>
          </div>

          <div
            class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 text-center"
          >
            <div
              class="w-16 h-16 bg-blue-100 rounded-lg flex items-center justify-center mx-auto mb-4"
            >
              <svg
                class="w-8 h-8 text-blue-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
                ></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-slate-900 mb-2">
              Budget tracking
            </h3>
            <p class="text-slate-600">
              Set monthly limits per category and see burn rate and alerts as you spend.
            </p>
          </div>

          <div
            class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 text-center"
          >
            <div
              class="w-16 h-16 bg-blue-100 rounded-lg flex items-center justify-center mx-auto mb-4"
            >
              <svg
                class="w-8 h-8 text-blue-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                ></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-slate-900 mb-2">
              Clear charts
            </h3>
            <p class="text-slate-600">
              Category breakdowns and six-month trends from the transactions you enter.
            </p>
          </div>

          <div
            class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 text-center"
          >
            <div
              class="w-16 h-16 bg-green-100 rounded-lg flex items-center justify-center mx-auto mb-4"
            >
              <svg
                class="w-8 h-8 text-green-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
                ></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-slate-900 mb-2">
              Secure sign-in
            </h3>
            <p class="text-slate-600">
              Your account is protected with encrypted passwords and standard web security practices.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="py-20 bg-gradient-to-br from-primary-600 to-indigo-700">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <h2 class="text-3xl md:text-4xl font-bold text-white mb-4">
          Start tracking on your terms
        </h2>
        <p class="text-xl text-primary-100 mb-8 max-w-2xl mx-auto">
          SpendSense is free to try — create an account, add a few transactions, and see if it fits how you manage money.
        </p>
        <button
          type="button"
          class="btn-secondary text-base py-3.5 px-8 min-h-[3rem] shadow-lg"
          @click="getStarted"
        >
          Create free account
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const authStore = useAuthStore();

const getStarted = () => {
  if (authStore.isAuthenticated) {
    router.push("/dashboard");
  } else {
    router.push("/register");
  }
};

const learnMore = () => {
  document.querySelector("#features")?.scrollIntoView({ behavior: "smooth" });
};
</script>
