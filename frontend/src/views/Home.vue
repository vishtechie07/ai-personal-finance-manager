<template>
  <div class="min-h-screen">
    <!-- Hero Section -->
    <section class="bg-gradient-to-br from-primary-50 to-primary-100 py-20">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <h1 class="text-4xl font-bold text-gray-900 mb-6">
          Smart-Powered
          <span class="text-blue-600">Personal Finance Management</span>
        </h1>
        <p class="text-xl text-gray-600 mb-8 max-w-3xl mx-auto">
          Our smart technology learns your spending patterns and provides personalized insights
        </p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center items-center">
          <button @click="getStarted" class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-4 px-8 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 text-lg">
            Get Started Free
          </button>
          <button @click="learnMore" class="bg-gray-200 hover:bg-gray-300 text-gray-900 font-medium py-4 px-8 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 text-lg">
            Learn More
          </button>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="py-20 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
            Smart Features for Smarter Finances
          </h2>
          <p class="text-xl text-gray-600 max-w-2xl mx-auto">
            Our smart technology learns your spending patterns and provides personalized insights
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 text-center">
            <div class="w-16 h-16 bg-blue-100 rounded-lg flex items-center justify-center mx-auto mb-4">
              <svg class="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">Smart Categorization</h3>
            <p class="text-gray-600">Automatically categorize your transactions with intelligent pattern recognition.</p>
          </div>
          
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 text-center">
            <div class="w-16 h-16 bg-yellow-100 rounded-lg flex items-center justify-center mx-auto mb-4">
              <svg class="w-8 h-8 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z"></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">Anomaly Detection</h3>
            <p class="text-gray-600">Detect unusual spending patterns and potential fraud with smart analysis.</p>
          </div>
          
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 text-center">
            <div class="w-16 h-16 bg-green-100 rounded-lg flex items-center justify-center mx-auto mb-4">
              <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">Predictive Insights</h3>
            <p class="text-gray-600">Get forecasts of future spending and personalized financial recommendations.</p>
          </div>
          
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 text-center">
            <div class="w-16 h-16 bg-blue-100 rounded-lg flex items-center justify-center mx-auto mb-4">
              <svg class="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">Smart Budgeting</h3>
            <p class="text-gray-600">Create and monitor budgets with smart spending predictions and alerts.</p>
          </div>
          
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 text-center">
            <div class="w-16 h-16 bg-blue-100 rounded-lg flex items-center justify-center mx-auto mb-4">
              <svg class="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">Beautiful Charts</h3>
            <p class="text-gray-600">Visualize your financial data with interactive charts and graphs.</p>
          </div>
          
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 text-center">
            <div class="w-16 h-16 bg-green-100 rounded-lg flex items-center justify-center mx-auto mb-4">
              <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">Bank-Level Security</h3>
            <p class="text-gray-600">Your data is protected with enterprise-grade security and encryption.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="py-20 bg-gray-900">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <h2 class="text-3xl md:text-4xl font-bold text-white mb-4">
          Ready to Transform Your Finances?
        </h2>
        <p class="text-xl text-gray-300 mb-8 max-w-2xl mx-auto">
          Join thousands of users who are already saving money and making better financial decisions
        </p>
        <button @click="getStarted" class="bg-white hover:bg-gray-100 text-gray-900 font-medium py-4 px-8 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 text-lg">
          Start Your Free Trial
        </button>
      </div>
    </section>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

export default {
  name: 'Home',
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()

    const getStarted = () => {
      if (authStore.isAuthenticated) {
        router.push('/dashboard')
      } else {
        router.push('/register')
      }
    }

    const learnMore = () => {
      // Scroll to features section or show more info
      document.querySelector('#features')?.scrollIntoView({ behavior: 'smooth' })
    }

    return {
      getStarted,
      learnMore
    }
  }
}
</script>
