<template>
  <div class="w-full mx-auto pt-6">
    <!-- Success Notification -->
    <div
      v-if="showSuccessMessage"
      class="fixed top-20 right-4 bg-green-500 text-white px-6 py-3 rounded-lg shadow-lg z-50 transform transition-all duration-300 ease-in-out"
    >
      <div class="flex items-center space-x-2">
        <svg
          class="w-5 h-5"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
          ></path>
        </svg>
        <span>{{ successMessage }}</span>
      </div>
    </div>

    <!-- Page Header -->
    <div
      class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between mb-8"
    >
      <div class="min-w-0">
        <h1 class="text-3xl font-bold text-slate-900">Dashboard</h1>
        <p class="text-slate-600 mt-2">
          Welcome back, {{ username }}! Here's your financial overview.
        </p>
      </div>
      <div class="page-header-actions">
        <button type="button" class="btn-secondary" @click="refreshDashboard">
          <svg
            class="w-5 h-5 shrink-0"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
            ></path>
          </svg>
          Refresh
        </button>
        <button
          type="button"
          class="btn-primary"
          @click="showAddTransaction = true"
        >
          <svg
            class="w-5 h-5 shrink-0"
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
          Add Transaction
        </button>
        <button
          type="button"
          class="btn-secondary"
          @click="showAddBudget = true"
        >
          <svg
            class="w-5 h-5 shrink-0"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4"
            ></path>
          </svg>
          Create Budget
        </button>
      </div>
    </div>

    <!-- Month Selector -->
    <div class="glass-card p-6 mb-8">
      <div class="flex items-center justify-between">
        <h3 class="text-lg font-semibold text-slate-900">
          Viewing Data for {{ currentMonthDisplay }}
        </h3>
        <div class="flex items-center space-x-3">
          <button
            type="button"
            class="btn-icon"
            aria-label="Previous month"
            @click="previousMonth"
          >
            <svg
              class="w-5 h-5 text-slate-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M15 19l-7-7 7-7"
              ></path>
            </svg>
          </button>
          <span
            class="text-lg font-medium text-slate-900 min-w-[120px] text-center"
          >
            {{ currentMonthDisplay }}
          </span>
          <button
            type="button"
            class="btn-icon"
            aria-label="Next month"
            @click="nextMonth"
          >
            <svg
              class="w-5 h-5 text-slate-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 5l7 7-7 7"
              ></path>
            </svg>
          </button>
          <button type="button" class="btn-text" @click="goToCurrentMonth">
            Current Month
          </button>
        </div>
      </div>
    </div>

    <div class="glass-card p-6 mb-6">
      <h3 class="text-lg font-semibold text-slate-900 mb-4">
        Quick add transaction
      </h3>
      <form
        class="grid grid-cols-1 md:grid-cols-5 gap-3 items-end"
        @submit.prevent="quickAdd"
      >
        <input
          v-model="quickForm.description"
          class="input-field"
          placeholder="Description"
          required
        />
        <input
          v-model.number="quickForm.amount"
          type="number"
          step="0.01"
          class="input-field"
          placeholder="Amount"
          required
        />
        <select v-model="quickForm.type" class="input-field">
          <option value="EXPENSE">Expense</option>
          <option value="INCOME">Income</option>
        </select>
        <input
          v-model="quickForm.category"
          class="input-field"
          placeholder="Category"
        />
        <button type="submit" class="btn-primary" :disabled="quickSaving">
          {{ quickSaving ? "Adding…" : "Add" }}
        </button>
      </form>
    </div>

    <div v-if="dueSoonBills.length" class="glass-card p-6 mb-6">
      <div class="flex justify-between items-center mb-3">
        <h3 class="text-lg font-semibold text-slate-900">Bills due soon</h3>
        <router-link
          to="/bills"
          class="text-sm text-primary-600 hover:underline"
          >View all</router-link
        >
      </div>
      <ul class="space-y-2">
        <li
          v-for="b in dueSoonBills"
          :key="b.id"
          class="flex justify-between text-sm"
        >
          <span>{{ b.payeeName }} — due {{ b.dueDate }}</span>
          <span class="font-medium">${{ Number(b.amount).toFixed(2) }}</span>
        </li>
      </ul>
    </div>

    <div class="glass-card p-6 mb-6">
      <h3 class="text-lg font-semibold text-slate-900 mb-4">Budget health</h3>
      <div v-if="!budgetHealthItems.length" class="text-slate-500 text-sm">
        No budgets for this month
      </div>
      <div v-else class="space-y-4">
        <div v-for="item in budgetHealthItems" :key="item.id">
          <div class="flex justify-between text-sm mb-1">
            <router-link
              :to="item.link"
              class="font-medium text-slate-900 hover:text-primary-600"
              >{{ item.name }}</router-link
            >
            <span>{{ item.pct.toFixed(0) }}%</span>
          </div>
          <div class="h-2 bg-slate-200 rounded-full overflow-hidden">
            <div
              class="h-full rounded-full"
              :class="item.barClass"
              :style="{ width: Math.min(item.pct, 100) + '%' }"
            />
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="attentionBudgets.length"
      class="glass-card p-6 mb-6 border border-amber-200 bg-amber-50/50"
    >
      <h3 class="text-lg font-semibold text-amber-900 mb-3">Needs attention</h3>
      <ul class="space-y-2 text-sm text-amber-800">
        <li v-for="a in attentionBudgets" :key="a.id">
          <router-link :to="a.link" class="hover:underline">{{
            a.message
          }}</router-link>
        </li>
      </ul>
    </div>

    <!-- Financial Summary Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
      <div class="glass-card p-6">
        <div class="flex items-center">
          <div
            class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center"
          >
            <svg
              class="w-6 h-6 text-green-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"
              ></path>
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-slate-600">
              Current Month Balance
            </p>
            <p class="text-2xl font-bold text-slate-900">
              ${{ currentMonthBalance.toFixed(2) }}
            </p>
          </div>
        </div>
      </div>

      <div class="glass-card p-6">
        <div class="flex items-center">
          <div
            class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center"
          >
            <svg
              class="w-6 h-6 text-blue-600"
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
          <div class="ml-4">
            <p class="text-sm font-medium text-slate-600">
              Current Month Income
            </p>
            <p class="text-2xl font-bold text-green-600">
              ${{ currentMonthIncome.toFixed(2) }}
            </p>
          </div>
        </div>
      </div>

      <div class="glass-card p-6">
        <div class="flex items-center">
          <div
            class="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center"
          >
            <svg
              class="w-6 h-6 text-red-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M13 17h8m0 0V9m0 8l-8-8-4 4-6-6"
              ></path>
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-slate-600">
              Current Month Expenses
            </p>
            <p class="text-2xl font-bold text-red-600">
              ${{ currentMonthExpenses.toFixed(2) }}
            </p>
          </div>
        </div>
      </div>

      <div class="glass-card p-6">
        <div class="flex items-center">
          <div
            class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center"
          >
            <svg
              class="w-6 h-6 text-purple-600"
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
          <div class="ml-4">
            <p class="text-sm font-medium text-slate-600">
              Current Month Budgets
            </p>
            <p class="text-2xl font-bold text-purple-600">
              {{ currentMonthBudgets.length }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-4 w-full">
      <div class="glass-card p-6" style="height: 400px; overflow: hidden">
        <h3 class="text-lg font-semibold text-slate-900">
          Spending by Category - {{ currentMonthDisplay }}
        </h3>
        <div style="height: 300px; position: relative; border: 1px dashed #ccc">
          <canvas
            ref="categoryChart"
            style="
              max-height: 300px;
              width: 100% !important;
              height: 100% !important;
              display: block;
            "
          ></canvas>
          <div
            v-if="!categoryChart"
            class="absolute inset-0 flex items-center justify-center text-gray-400"
          >
            Chart loading...
          </div>
        </div>
      </div>

      <div class="glass-card p-6" style="height: 400px; overflow: hidden">
        <h3 class="text-lg font-semibold text-slate-900">
          Monthly Overview - Last 6 Months
        </h3>
        <div style="height: 300px; position: relative; border: 1px dashed #ccc">
          <canvas
            ref="monthlyChart"
            style="
              max-height: 300px;
              width: 100% !important;
              height: 100% !important;
              display: block;
            "
          ></canvas>
          <div
            v-if="!monthlyChart"
            class="absolute inset-0 flex items-center justify-center text-gray-400"
          >
            Chart loading...
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Transactions -->
    <div class="glass-card p-6 mb-4">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-semibold text-slate-900">
          Current Month Transactions
        </h3>
        <router-link
          to="/transactions"
          class="text-blue-600 hover:text-blue-700 text-sm font-medium"
        >
          View All
        </router-link>
      </div>
      <div class="space-y-3">
        <div
          v-if="currentMonthTransactions.length === 0"
          class="text-center py-8 text-gray-500"
        >
          <svg
            class="w-12 h-12 mx-auto mb-4 text-gray-300"
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
          <p>No transactions found for {{ currentMonthDisplay }}</p>
        </div>
        <div
          v-for="transaction in currentMonthTransactions.slice(0, 5)"
          v-else
          :key="transaction.id"
          class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
        >
          <div class="flex items-center space-x-3">
            <div
              class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center"
            >
              <svg
                class="w-4 h-4 text-blue-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z"
                ></path>
              </svg>
            </div>
            <div>
              <p class="font-medium text-slate-900">
                {{ transaction.description }}
              </p>
              <p class="text-sm text-gray-500">{{ transaction.category }}</p>
            </div>
          </div>
          <div class="flex items-center space-x-4">
            <div class="text-right">
              <p
                class="font-medium"
                :class="
                  transaction.type === 'EXPENSE'
                    ? 'text-red-600'
                    : 'text-green-600'
                "
              >
                {{ transaction.type === "EXPENSE" ? "-" : "+" }}${{
                  transaction.amount.toFixed(2)
                }}
              </p>
              <p class="text-sm text-gray-500">
                {{ formatDate(transaction.transactionDate) }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Budget Alerts -->
    <div class="glass-card p-6">
      <h3 class="text-lg font-semibold text-slate-900 mb-4">
        Budget Alerts for {{ currentMonthDisplay }}
      </h3>
      <div class="space-y-3">
        <div
          v-if="budgetAlerts.length === 0"
          class="text-center py-8 text-gray-500"
        >
          <svg
            class="w-12 h-12 mx-auto mb-4 text-gray-300"
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
          <p>No budget alerts for {{ currentMonthDisplay }}</p>
        </div>
        <div
          v-for="alert in budgetAlerts"
          v-else
          :key="alert.id"
          class="flex items-center p-3 border border-gray-200 rounded-lg"
        >
          <div
            class="w-8 h-8 rounded-full flex items-center justify-center mr-3"
            :class="alert.type === 'warning' ? 'bg-yellow-100' : 'bg-red-100'"
          >
            <svg
              class="w-4 h-4"
              :class="
                alert.type === 'warning' ? 'text-yellow-600' : 'text-red-600'
              "
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
          <div class="flex-1">
            <p class="font-medium text-slate-900">{{ alert.title }}</p>
            <p class="text-sm text-slate-600">{{ alert.message }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Transaction Modal -->
    <div
      v-if="showAddTransaction"
      class="fixed inset-0 bg-slate-900/40 backdrop-blur-sm overflow-y-auto h-full w-full z-50 flex items-center justify-center animate-fade-in"
    >
      <div
        class="relative mx-auto p-8 border border-slate-200 shadow-xl rounded-2xl bg-white w-full max-w-md transform transition-all duration-300 animate-slide-up"
      >
        <div class="mt-3">
          <h3 class="text-2xl font-bold text-slate-900 mb-6">
            Add New Transaction
          </h3>
          <form class="space-y-4" @submit.prevent="addTransaction">
            <div>
              <label class="form-label">Description</label>
              <input
                v-model="newTransaction.description"
                type="text"
                class="input-field"
                placeholder="E.g., Groceries"
                required
              />
            </div>
            <div>
              <label class="form-label">Amount</label>
              <input
                v-model="newTransaction.amount"
                type="number"
                step="0.01"
                class="input-field"
                placeholder="0.00"
                required
              />
            </div>
            <div>
              <label class="form-label">Type</label>
              <select v-model="newTransaction.type" class="input-field">
                <option value="EXPENSE">Expense</option>
                <option value="INCOME">Income</option>
              </select>
            </div>
            <div>
              <label class="form-label">Date</label>
              <input
                v-model="newTransaction.transactionDate"
                type="date"
                class="input-field"
                required
              />
            </div>
            <div>
              <label class="form-label">Category</label>
              <div class="relative">
                <input
                  v-model="newTransaction.category"
                  type="text"
                  class="input-field pr-20"
                  placeholder="Smart categorization will suggest category"
                  :class="
                    newTransaction.category &&
                    newTransaction.category !== 'Uncategorized'
                      ? 'border-green-500 bg-green-50'
                      : ''
                  "
                />
                <div class="absolute inset-y-0 right-0 flex items-center pr-3">
                  <div
                    v-if="
                      newTransaction.description &&
                      newTransaction.description.length > 3 &&
                      !newTransaction.category
                    "
                    class="flex items-center space-x-2"
                  >
                    <div
                      class="w-2 h-2 bg-primary-500 rounded-full animate-pulse"
                    ></div>
                    <span class="text-xs text-primary-400">Analyzing...</span>
                  </div>
                  <div
                    v-else-if="
                      newTransaction.category &&
                      newTransaction.category !== 'Uncategorized'
                    "
                    class="flex items-center space-x-2"
                  >
                    <svg
                      class="w-4 h-4 text-green-500"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                      ></path>
                    </svg>
                    <span class="text-xs text-green-500"
                      >Category suggested</span
                    >
                  </div>
                </div>
              </div>
              <div class="mt-2 flex items-center space-x-2">
                <button
                  v-if="
                    newTransaction.description &&
                    newTransaction.description.length > 3
                  "
                  type="button"
                  class="text-xs bg-primary-500/20 text-primary-400 px-2 py-1 rounded hover:bg-primary-500/30 transition-colors"
                  @click="suggestCategory(newTransaction.description)"
                >
                  🤖 Get Smart Suggestion
                </button>
                <p
                  v-if="
                    newTransaction.category &&
                    newTransaction.category !== 'Uncategorized'
                  "
                  class="text-xs text-green-500"
                >
                  Smart suggestion:
                  <span class="font-medium">{{ newTransaction.category }}</span>
                </p>
              </div>
            </div>

            <div class="flex space-x-3 pt-4">
              <button
                type="button"
                class="btn-secondary flex-1"
                @click="showAddTransaction = false"
              >
                Cancel
              </button>
              <button type="submit" class="btn-primary flex-1">
                Add Transaction
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Add Budget Modal -->
    <div
      v-if="showAddBudget"
      class="fixed inset-0 bg-slate-900/40 backdrop-blur-sm overflow-y-auto h-full w-full z-50 flex items-center justify-center animate-fade-in"
    >
      <div
        class="relative mx-auto p-8 border border-slate-200 shadow-xl rounded-2xl bg-white w-full max-w-md transform transition-all duration-300 animate-slide-up"
      >
        <div class="mt-3">
          <h3 class="text-2xl font-bold text-slate-900 mb-6">
            Create New Budget
          </h3>
          <form class="space-y-4" @submit.prevent="addBudget">
            <div>
              <label class="form-label">Budget Name</label>
              <input
                v-model="newBudget.name"
                type="text"
                class="input-field"
                placeholder="E.g., Monthly Groceries"
                required
              />
            </div>
            <div>
              <label class="form-label">Amount</label>
              <input
                v-model="newBudget.amount"
                type="number"
                step="0.01"
                class="input-field"
                placeholder="0.00"
                required
              />
            </div>
            <div>
              <label class="form-label">Start Month</label>
              <input
                v-model="newBudget.startMonth"
                type="month"
                class="input-field"
                required
              />
            </div>
            <div>
              <label class="form-label">Category</label>
              <div class="relative">
                <input
                  v-model="newBudget.category"
                  type="text"
                  :class="[
                    'input-field pr-20',
                    isBudgetAnalyzing
                      ? 'border-yellow-500 bg-yellow-50'
                      : isBudgetSmartSuggested
                        ? 'border-green-500 bg-green-50'
                        : '',
                  ]"
                  placeholder="Type to get smart suggestions..."
                  required
                />
                <div
                  v-if="isBudgetAnalyzing"
                  class="absolute right-3 top-3.5 text-yellow-500 text-xs font-medium"
                >
                  🤖 Smart analyzing...
                </div>
                <div
                  v-else-if="isBudgetSmartSuggested"
                  class="absolute right-3 top-3.5 text-green-500 text-xs font-medium"
                >
                  ✨ Smart suggested
                </div>
              </div>
              <div class="mt-2">
                <button
                  type="button"
                  class="text-xs text-primary-400 hover:text-primary-300 font-medium flex items-center"
                  @click="suggestBudgetCategory(newBudget.name)"
                >
                  🤖 Get Smart Suggestion
                </button>
              </div>
            </div>
            <div>
              <label class="form-label">Period</label>
              <select v-model="newBudget.period" class="input-field">
                <option value="WEEKLY">Weekly</option>
                <option value="MONTHLY">Monthly</option>
                <option value="QUARTERLY">Quarterly</option>
                <option value="YEARLY">Yearly</option>
              </select>
            </div>
            <div class="flex space-x-3 pt-4">
              <button
                type="button"
                class="btn-secondary flex-1"
                @click="showAddBudget = false"
              >
                Cancel
              </button>
              <button type="submit" class="btn-primary flex-1">
                Create Budget
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import { useAuthStore } from "../stores/auth";
import { useTransactionsStore } from "../stores/transactions";
import { useBudgetsStore } from "../stores/budgets";
import { Chart, registerables } from "chart.js";
import { format } from "date-fns";
import { useRouter } from "vue-router";
import {
  resolveCategory,
  TRANSACTION_KEYWORDS,
  BUDGET_KEYWORDS,
} from "../utils/categorySuggestion";
import { useBillsStore } from "../stores/bills";
import { useToast } from "../composables/useToast";
import { getApiErrorMessage } from "../utils/apiError";

Chart.register(...registerables);

export default {
  name: "Dashboard",
  setup() {
    const authStore = useAuthStore();
    const transactionsStore = useTransactionsStore();
    const budgetsStore = useBudgetsStore();
    const billsStore = useBillsStore();
    const router = useRouter();
    const toast = useToast();

    const quickForm = ref({
      description: "",
      amount: "",
      type: "EXPENSE",
      category: "OTHER",
    });
    const quickSaving = ref(false);
    const dueSoonBills = computed(() => billsStore.dueSoon);

    const budgetHealthItems = computed(() => {
      return (budgetsStore.budgetsByMonth || []).map((b) => {
        const pct =
          (parseFloat(b.spentAmount || 0) / parseFloat(b.amount || 1)) * 100;
        let barClass = "bg-green-500";
        if (pct >= 90 || parseFloat(b.remainingAmount || 0) < 0)
          barClass = "bg-red-500";
        else if (pct >= 70) barClass = "bg-amber-500";
        return {
          id: b.id,
          name: b.name,
          pct,
          barClass,
          link: { path: "/transactions", query: { category: b.category } },
        };
      });
    });

    const attentionBudgets = computed(() => {
      return budgetHealthItems.value
        .filter((i) => i.pct >= 90)
        .map((i) => ({
          id: i.id,
          link: i.link,
          message:
            i.pct >= 100
              ? `${i.name} is over budget`
              : `${i.name} has used ${i.pct.toFixed(0)}% of its budget`,
        }));
    });

    const quickAdd = async () => {
      quickSaving.value = true;
      try {
        await transactionsStore.addTransaction({
          ...quickForm.value,
          transactionDate: new Date().toISOString().split("T")[0],
        });
        quickForm.value = {
          description: "",
          amount: "",
          type: "EXPENSE",
          category: "OTHER",
        };
        toast.success("Transaction added");
        await budgetsStore.fetchBudgets();
      } catch (e) {
        toast.error(getApiErrorMessage(e));
      } finally {
        quickSaving.value = false;
      }
    };

    const showAddTransaction = ref(false);
    const showAddBudget = ref(false);
    const showSuccessMessage = ref(false);
    const successMessage = ref("");

    // Chart refs
    const categoryChart = ref(null);
    const monthlyChart = ref(null);

    // Chart instances to prevent multiple initializations
    let spendingChart = null;
    let monthlyChartInstance = null;

    const newTransaction = ref({
      description: "",
      amount: "",
      type: "EXPENSE",
      category: "",
      transactionDate: new Date().toISOString().split("T")[0], // Today's date as default
    });

    const newBudget = ref({
      name: "",
      amount: "",
      category: "",
      period: "MONTHLY",
      startMonth: new Date().toISOString().slice(0, 7), // Current month in YYYY-MM format
    });

    // Smart suggestion state
    const isSmartSuggested = ref(false);

    // Budget smart suggestion state
    const isBudgetSmartSuggested = ref(false);
    const budgetSuggestionTimeout = ref(null);

    // Use shared store data instead of local refs
    const totalBalance = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.totalBalance || 0;
    });
    const monthlyIncome = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.totalIncome || 0;
    });
    const monthlyExpenses = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.totalExpenses || 0;
    });
    const recentTransactions = computed(() => {
      if (!transactionsStore) return [];
      return transactionsStore.recentTransactions || [];
    });

    // Add missing computed properties
    const totalIncome = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.totalIncome || 0;
    });
    const totalExpenses = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.totalExpenses || 0;
    });
    const totalSavings = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.totalBalance || 0;
    });

    // Month-based data
    const currentMonthDisplay = computed(() => {
      const monthNames = [
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December",
      ];
      return `${monthNames[transactionsStore?.currentMonth || 0]} ${transactionsStore?.currentYear || new Date().getFullYear()}`;
    });

    const currentMonthIncome = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.currentMonthIncome || 0;
    });

    const currentMonthExpenses = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.currentMonthExpenses || 0;
    });

    const currentMonthBalance = computed(() => {
      if (!transactionsStore) return 0;
      return transactionsStore.currentMonthBalance || 0;
    });

    const currentMonthTransactions = computed(() => {
      if (!transactionsStore) return [];
      return transactionsStore.transactionsByMonth || [];
    });

    const currentMonthBudgets = computed(() => {
      if (!budgetsStore) return [];
      return budgetsStore.budgetsByMonth || [];
    });

    // Compute budget status from the budgets store
    const budgetStatus = computed(() => {
      if (!budgetsStore?.budgets) return "Loading...";
      if (budgetsStore.overBudgetBudgets.length > 0) {
        return "Over Budget";
      } else if (budgetsStore.nearLimitBudgets.length > 0) {
        return "Near Limit";
      } else {
        return "On Track";
      }
    });

    // Generate budget alerts from the budgets store
    const budgetAlerts = computed(() => {
      if (!budgetsStore?.budgets) return [];
      const alerts = [];

      // Check for over-budget budgets (month-filtered)
      budgetsStore.currentMonthOverBudgetBudgets.forEach((budget) => {
        alerts.push({
          id: `over-${budget.id}`,
          type: "danger",
          title: `${budget.name} Budget`,
          message: `You've exceeded your ${budget.name.toLowerCase()} budget by $${Math.abs(parseFloat(budget.remainingAmount || 0)).toFixed(2)}`,
        });
      });

      // Check for near-limit budgets (month-filtered)
      budgetsStore.currentMonthNearLimitBudgets.forEach((budget) => {
        const percentage =
          (parseFloat(budget.spentAmount || 0) /
            parseFloat(budget.amount || 1)) *
          100;
        alerts.push({
          id: `near-${budget.id}`,
          type: "warning",
          title: `${budget.name} Budget`,
          message: `You've used ${percentage.toFixed(1)}% of your ${budget.name.toLowerCase()} budget this month`,
        });
      });

      return alerts.slice(0, 3); // Limit to 3 most important alerts
    });

    const username = computed(() => {
      if (!authStore || !authStore.user) return "User";
      return authStore.user.username || "User";
    });

    const formatDate = (date) => {
      return format(new Date(date), "MMM dd");
    };

    const addTransaction = async () => {
      // Validate required fields
      if (
        !newTransaction.value.description ||
        !newTransaction.value.amount ||
        !newTransaction.value.category ||
        !newTransaction.value.transactionDate
      ) {
        successMessage.value =
          "Please fill in all required fields (Description, Amount, Date, and Category)";
        showSuccessMessage.value = true;
        setTimeout(() => {
          showSuccessMessage.value = false;
        }, 3000);
        return;
      }

      if (!transactionsStore) {
        successMessage.value =
          "Transactions store not available. Please refresh the page.";
        showSuccessMessage.value = true;
        setTimeout(() => {
          showSuccessMessage.value = false;
        }, 3000);
        return;
      }

      if (typeof transactionsStore.addTransaction !== "function") {
        successMessage.value =
          "Transactions store not properly initialized. Please refresh the page.";
        showSuccessMessage.value = true;
        setTimeout(() => {
          showSuccessMessage.value = false;
        }, 3000);
        return;
      }

      try {
        // Use the shared store to add the transaction
        const newTransactionData = await transactionsStore.addTransaction(
          newTransaction.value,
        );

        // Show success message
        successMessage.value = `${newTransactionData.type === "INCOME" ? "Income" : "Expense"} added successfully!`;
        showSuccessMessage.value = true;

        // Hide success message after 3 seconds
        setTimeout(() => {
          showSuccessMessage.value = false;
        }, 3000);

        showAddTransaction.value = false;
        newTransaction.value = {
          description: "",
          amount: "",
          type: "EXPENSE",
          category: "",
          transactionDate: new Date().toISOString().split("T")[0],
        };

        // Reinitialize charts to show new data
        setTimeout(() => {
          try {
            initCharts();
          } catch (error) {
            // Handle error silently
          }
        }, 100);
      } catch (error) {
        // Provide more specific error messages
        let errorMessage = "Please try again.";
        if (
          error.message &&
          error.message.includes("newTransaction is not defined")
        ) {
          errorMessage =
            "Transaction data is not properly formatted. Please refresh the page and try again.";
        } else if (error.message) {
          errorMessage = error.message;
        } else if (error.response?.status === 404) {
          errorMessage =
            "Backend service not available. Transaction added to local storage.";
        } else if (error.response?.status >= 500) {
          errorMessage = "Server error. Please try again later.";
        }

        successMessage.value = `Failed to add transaction: ${errorMessage}`;
        showSuccessMessage.value = true;
        setTimeout(() => {
          showSuccessMessage.value = false;
        }, 3000);
      }
    };

    const suggestCategory = async (description) => {
      const cat = await resolveCategory(description, TRANSACTION_KEYWORDS);
      if (cat) newTransaction.value.category = cat;
    };

    // Watch for description changes to trigger smart suggestion
    watch(
      () => newTransaction.value.description,
      (newDescription, _oldDescription) => {
        if (newDescription && newDescription.length >= 3) {
          // Debounce the suggestion to avoid too many calls
          setTimeout(() => {
            suggestCategory(newDescription);
          }, 300);
        }
        // Don't clear category when description is short - let user keep it
      },
    );

    const suggestBudgetCategory = async (budgetName) => {
      if (!budgetName || budgetName.trim().length < 3) return;
      isBudgetSmartSuggested.value = false;
      const cat = await resolveCategory(budgetName, BUDGET_KEYWORDS);
      if (cat) {
        newBudget.value.category = cat;
        isBudgetSmartSuggested.value = true;
      } else {
        newBudget.value.category = "Uncategorized";
      }
    };

    // Watch for budget name changes to trigger smart suggestions
    watch(
      () => newBudget.value.name,
      (newName) => {
        if (newName && newName.trim().length >= 3) {
          // Debounce the smart suggestion
          if (budgetSuggestionTimeout.value) {
            clearTimeout(budgetSuggestionTimeout.value);
          }
          budgetSuggestionTimeout.value = setTimeout(() => {
            suggestBudgetCategory(newName);
          }, 500);
        } else {
          isBudgetSmartSuggested.value = false;
        }
      },
    );

    const addBudget = async () => {
      if (!budgetsStore) {
        return;
      }

      try {
        // Convert month string to start date
        const [year, month] = newBudget.value.startMonth.split("-");
        const startDate = new Date(parseInt(year), parseInt(month) - 1, 1);

        const budgetData = {
          name: newBudget.value.name,
          amount: newBudget.value.amount,
          category: newBudget.value.category,
          period: newBudget.value.period,
          startDate: startDate.toISOString(),
        };

        // Use the shared store to add the budget
        const budget = await budgetsStore.addBudget(budgetData);

        // Show success message
        successMessage.value = `Budget "${budget.name}" created successfully!`;
        showSuccessMessage.value = true;

        // Hide success message after 3 seconds
        setTimeout(() => {
          showSuccessMessage.value = false;
        }, 3000);

        // Reset form and close modal
        showAddBudget.value = false;
        newBudget.value = {
          name: "",
          amount: "",
          category: "",
          period: "MONTHLY",
          startMonth: new Date().toISOString().slice(0, 7),
        };
        isBudgetSmartSuggested.value = false;

        // Reinitialize charts to show new data
        setTimeout(() => {
          try {
            initCharts();
          } catch (error) {
            console.error("Error reinitializing charts:", error);
          }
        }, 100);
      } catch (error) {
        console.error("Failed to create budget:", error);
        successMessage.value = "Failed to create budget. Please try again.";
        showSuccessMessage.value = true;
        setTimeout(() => {
          showSuccessMessage.value = false;
        }, 3000);
      }
    };

    // Chart initialization with proper cleanup
    const initCharts = () => {
      // Initialize spending by category chart
      if (
        categoryChart.value &&
        transactionsStore.transactionsByMonth.length > 0
      ) {
        const ctx = categoryChart.value.getContext("2d");

        // Destroy existing chart if it exists
        if (spendingChart) {
          spendingChart.destroy();
        }

        // Prepare data for the chart
        const categoryData = {};
        transactionsStore.transactionsByMonth
          .filter((t) => t.type === "EXPENSE")
          .forEach((t) => {
            if (categoryData[t.category]) {
              categoryData[t.category] += parseFloat(t.amount);
            } else {
              categoryData[t.category] = parseFloat(t.amount);
            }
          });

        const labels = Object.keys(categoryData);
        const data = Object.values(categoryData);

        // Create the chart
        spendingChart = new Chart(ctx, {
          type: "doughnut",
          data: {
            labels: labels,
            datasets: [
              {
                data: data,
                backgroundColor: [
                  "#3B82F6",
                  "#10B981",
                  "#F59E0B",
                  "#EF4444",
                  "#8B5CF6",
                  "#06B6D4",
                  "#84CC16",
                  "#F97316",
                  "#EC4899",
                  "#6B7280",
                ],
                borderWidth: 2,
                borderColor: "#e2e8f0",
              },
            ],
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                position: "bottom",
                labels: {
                  padding: 20,
                  usePointStyle: true,
                  color: "#475569",
                },
              },
              title: {
                display: true,
                text: `Spending by Category - ${currentMonthDisplay.value}`,
                color: "#0f172a",
                font: {
                  size: 16,
                  weight: "bold",
                },
              },
            },
          },
        });
      }

      // Initialize monthly overview chart
      if (monthlyChart.value && transactionsStore.transactions.length > 0) {
        const ctx = monthlyChart.value.getContext("2d");

        // Destroy existing chart if it exists
        if (monthlyChartInstance) {
          monthlyChartInstance.destroy();
        }

        // Prepare data for the last 6 months
        const months = [];
        const incomeData = [];
        const expenseData = [];

        const now = new Date();
        for (let i = 5; i >= 0; i--) {
          const month = new Date(now.getFullYear(), now.getMonth() - i, 1);
          months.push(
            month.toLocaleDateString("en-US", {
              month: "short",
              year: "2-digit",
            }),
          );

          // Calculate income and expenses for this month
          const monthStart = new Date(month.getFullYear(), month.getMonth(), 1);
          const monthEnd = new Date(
            month.getFullYear(),
            month.getMonth() + 1,
            0,
          );

          const monthTransactions = transactionsStore.transactions.filter(
            (t) => {
              const transactionDate = new Date(t.transactionDate);
              return (
                transactionDate >= monthStart && transactionDate <= monthEnd
              );
            },
          );

          const monthIncome = monthTransactions
            .filter((t) => t.type === "INCOME")
            .reduce((sum, t) => sum + parseFloat(t.amount), 0);

          const monthExpenses = monthTransactions
            .filter((t) => t.type === "EXPENSE")
            .reduce((sum, t) => sum + parseFloat(t.amount), 0);

          incomeData.push(monthIncome);
          expenseData.push(monthExpenses);
        }

        // Create the chart
        monthlyChartInstance = new Chart(ctx, {
          type: "line",
          data: {
            labels: months,
            datasets: [
              {
                label: "Income",
                data: incomeData,
                borderColor: "#10B981",
                backgroundColor: "rgba(16, 185, 129, 0.1)",
                borderWidth: 3,
                fill: true,
                tension: 0.4,
              },
              {
                label: "Expenses",
                data: expenseData,
                borderColor: "#EF4444",
                backgroundColor: "rgba(239, 68, 68, 0.1)",
                borderWidth: 3,
                fill: true,
                tension: 0.4,
              },
            ],
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                position: "top",
                labels: {
                  usePointStyle: true,
                  color: "#475569",
                },
              },
              title: {
                display: true,
                text: "Monthly Overview - Last 6 Months",
                color: "#0f172a",
                font: {
                  size: 16,
                  weight: "bold",
                },
              },
            },
            scales: {
              y: {
                beginAtZero: true,
                grid: {
                  color: "rgba(148, 163, 184, 0.25)",
                },
                ticks: {
                  color: "#64748b",
                  callback: function (value) {
                    return "$" + value.toLocaleString();
                  },
                },
              },
              x: {
                grid: {
                  color: "rgba(148, 163, 184, 0.15)",
                },
                ticks: {
                  color: "#64748b",
                },
              },
            },
          },
        });
      }
    };

    // Initialize charts when component mounts
    onMounted(async () => {
      try {
        // Ensure stores are properly initialized
        if (!transactionsStore) {
          return;
        }

        // Check if stores already have data
        // If stores are not initialized, fetch data
        if (!transactionsStore.isInitialized) {
          await transactionsStore.fetchTransactions();
        }

        if (!budgetsStore.isInitialized) {
          await budgetsStore.fetchBudgets();
        }
        await billsStore.fetchDueSoon();
      } catch (error) {
        console.error("Dashboard load error:", error);
      }

      // Add a small delay to ensure DOM is fully ready and data is loaded
      setTimeout(() => {
        try {
          // Double-check that chart refs are available
          if (categoryChart.value && monthlyChart.value) {
            initCharts();
          } else {
            // Retry after another delay
            setTimeout(() => {
              if (categoryChart.value && monthlyChart.value) {
                initCharts();
              }
            }, 500);
          }
        } catch (error) {
          // Handle error silently
        }
      }, 500);
    });

    // Cleanup charts when component unmounts
    onUnmounted(() => {
      if (budgetSuggestionTimeout.value) {
        clearTimeout(budgetSuggestionTimeout.value);
      }
      if (spendingChart) {
        spendingChart.destroy();
      }
      if (monthlyChartInstance) {
        monthlyChartInstance.destroy();
      }
    });

    // Refresh charts for current month
    const refreshChartsForCurrentMonth = () => {
      try {
        // Destroy existing charts first
        if (spendingChart) {
          spendingChart.destroy();
          spendingChart = null;
        }
        if (monthlyChartInstance) {
          monthlyChartInstance.destroy();
          monthlyChartInstance = null;
        }

        // Wait a bit and then reinitialize
        setTimeout(() => {
          initCharts();
        }, 100);
      } catch (error) {
        // Handle error silently
      }
    };

    // Add refresh mechanism
    const refreshDashboard = async () => {
      try {
        // Refresh both stores
        if (transactionsStore) {
          await transactionsStore.refreshData();
        }
        if (budgetsStore) {
          await budgetsStore.refreshData();
        }

        // Refresh charts
        refreshChartsForCurrentMonth();

        // Show success message
        successMessage.value = "Dashboard refreshed successfully!";
        showSuccessMessage.value = true;
        setTimeout(() => {
          showSuccessMessage.value = false;
        }, 3000);
      } catch (error) {
        // Handle error silently
      }
    };

    // Watch for route changes to refresh data
    watch(
      () => router.currentRoute.value.path,
      (newPath, oldPath) => {
        if (newPath === "/dashboard" && oldPath !== "/dashboard") {
          refreshDashboard();
        }
      },
    );

    // Watch for transactions data changes to refresh charts
    watch(
      () => transactionsStore?.transactions,
      (newTransactions, oldTransactions) => {
        if (
          newTransactions &&
          newTransactions.length !== oldTransactions?.length
        ) {
          refreshChartsForCurrentMonth();
        }
      },
    );

    // Watch for month changes to refresh charts
    watch(
      [
        () => transactionsStore?.currentMonth,
        () => transactionsStore?.currentYear,
      ],
      (newValues, oldValues) => {
        if (
          newValues &&
          (newValues[0] !== oldValues?.[0] || newValues[1] !== oldValues?.[1])
        ) {
          refreshChartsForCurrentMonth();
        }
      },
    );

    // Watch for budgets data changes to refresh charts
    watch(
      () => budgetsStore?.budgets,
      (newBudgets, oldBudgets) => {
        if (newBudgets && newBudgets.length !== oldBudgets?.length) {
          refreshChartsForCurrentMonth();
        }
      },
    );

    // Watch for month-based data changes to refresh charts
    watch(
      [
        () => transactionsStore?.transactionsByMonth,
        () => budgetsStore?.budgetsByMonth,
      ],
      (newValues, oldValues) => {
        if (
          newValues &&
          (newValues[0]?.length !== oldValues?.[0]?.length ||
            newValues[1]?.length !== oldValues?.[1]?.length)
        ) {
          refreshChartsForCurrentMonth();
        }
      },
    );

    // Month navigation
    const setCurrentMonth = (month, year) => {
      if (transactionsStore) {
        transactionsStore.setCurrentMonth(month, year);
      }
      if (budgetsStore) {
        budgetsStore.setCurrentMonth(month, year);
      }

      // Force chart update after month change
      setTimeout(() => {
        refreshChartsForCurrentMonth();
      }, 150);
    };

    const goToCurrentMonth = () => {
      if (transactionsStore) {
        transactionsStore.goToCurrentMonth();
      }
      if (budgetsStore) {
        budgetsStore.goToCurrentMonth();
      }

      // Force chart update after returning to current month
      setTimeout(() => {
        refreshChartsForCurrentMonth();
      }, 150);
    };

    const previousMonth = () => {
      if (transactionsStore) {
        transactionsStore.previousMonth();
      }
      if (budgetsStore) {
        budgetsStore.previousMonth();
      }

      // Force chart update after month navigation
      setTimeout(() => {
        refreshChartsForCurrentMonth();
      }, 150);
    };

    const nextMonth = () => {
      if (transactionsStore) {
        transactionsStore.nextMonth();
      }
      if (budgetsStore) {
        budgetsStore.nextMonth();
      }

      // Force chart update after month navigation
      setTimeout(() => {
        refreshChartsForCurrentMonth();
      }, 150);
    };

    return {
      transactionsStore,
      budgetsStore,
      authStore,
      showAddTransaction,
      showAddBudget,
      newTransaction,
      newBudget,
      totalBalance,
      monthlyIncome,
      monthlyExpenses,
      budgetStatus,
      totalIncome,
      totalExpenses,
      totalSavings,
      recentTransactions,
      budgetAlerts,
      username,
      formatDate,
      addTransaction,
      addBudget,
      categoryChart,
      monthlyChart,
      showSuccessMessage,
      successMessage,
      isSmartSuggested,
      isBudgetSmartSuggested,
      suggestBudgetCategory,
      refreshDashboard,
      refreshChartsForCurrentMonth,
      setCurrentMonth,
      goToCurrentMonth,
      previousMonth,
      nextMonth,
      currentMonthDisplay,
      currentMonthIncome,
      currentMonthExpenses,
      currentMonthBalance,
      currentMonthTransactions,
      currentMonthBudgets,
      quickForm,
      quickSaving,
      quickAdd,
      dueSoonBills,
      budgetHealthItems,
      attentionBudgets,
    };
  },
};
</script>
