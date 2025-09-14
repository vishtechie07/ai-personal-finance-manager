<template>
  <div id="app" class="min-h-screen bg-gray-50">
    <nav class="bg-white shadow-sm border-b border-gray-200 w-full" style="min-height: 64px; max-height: 64px; height: 64px; overflow: hidden;">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <router-link to="/" class="flex items-center space-x-3">
              <div class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
                <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"></path>
                </svg>
              </div>
              <span class="text-xl font-bold text-gray-900">Personal Finance Manager</span>
            </router-link>
          </div>
          
          <div class="flex items-center space-x-4" style="position: relative; z-index: 100; pointer-events: auto; background: rgba(255,255,255,0.9); padding: 8px; border-radius: 8px;">
            <router-link to="/dashboard" class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200 cursor-pointer border border-transparent hover:border-blue-200" style="pointer-events: auto; z-index: 10; background: white; padding: 8px 12px; border-radius: 6px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);">Dashboard</router-link>
            <router-link to="/transactions" class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200 cursor-pointer border border-transparent hover:border-blue-200" style="pointer-events: auto; z-index: 10; background: white; padding: 8px 12px; border-radius: 6px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);">Transactions</router-link>
            <router-link to="/budgets" class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200 cursor-pointer border border-transparent hover:border-blue-200" style="pointer-events: auto; z-index: 10; background: white; padding: 8px 12px; border-radius: 6px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);">Budgets</router-link>
            <router-link to="/insights" class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200 cursor-pointer border border-transparent hover:border-blue-200" style="pointer-events: auto; z-index: 10; background: white; padding: 8px 12px; border-radius: 6px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);">Financial Insights</router-link>

            <button v-if="!isAuthenticated" @click="showLoginModal = true" class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2">Login</button>
            
            <!-- User info and logout when authenticated -->
            <div v-else class="flex items-center space-x-3">
              <div class="flex items-center space-x-2">
                <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                  <span class="text-blue-600 font-medium">{{ userInitials }}</span>
                </div>
                <span class="text-sm font-medium text-gray-700">{{ username }}</span>
              </div>
              <button @click="handleLogout" class="bg-red-600 hover:bg-red-700 text-white font-medium py-2 px-3 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 text-sm">Logout</button>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <main class="w-full mx-auto px-4 sm:px-6 lg:px-8 py-0" style="margin: 0; padding-top: 80px; padding-bottom: 0;">
      <div class="max-w-7xl mx-auto">
        <router-view />
      </div>
    </main>

    <!-- Login Modal -->
    <div v-if="showLoginModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
      <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
        <div class="mt-3">
          <h3 class="text-lg font-medium text-gray-900 mb-4">Login to Personal Finance Manager</h3>
          <form @submit.prevent="login" class="space-y-4">
            <div>
              <label class="form-label">Username</label>
              <input v-model="loginForm.username" type="text" class="input-field" required>
            </div>
            <div>
              <label class="form-label">Password</label>
              <input v-model="loginForm.password" type="password" class="input-field" required>
            </div>
            <div class="flex space-x-3">
              <button type="submit" class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 flex-1">Login</button>
              <button type="button" @click="showLoginModal = false" class="bg-gray-200 hover:bg-gray-300 text-gray-900 font-medium py-2 px-4 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2">Cancel</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from './stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const showLoginModal = ref(false)
const showUserMenu = ref(false)

const loginForm = ref({
  username: '',
  password: ''
})

const isAuthenticated = computed(() => authStore.isAuthenticated)
const username = computed(() => authStore.user?.username || 'User')
const userInitials = computed(() => {
  if (!authStore.user?.username) return 'U'
  return authStore.user.username.substring(0, 2).toUpperCase()
})

const login = async () => {
  try {
    await authStore.login(loginForm.value.username, loginForm.value.password)
    showLoginModal.value = false
    loginForm.value = { username: '', password: '' }
  } catch (error) {
    console.error('Login failed:', error)
  }
}

const handleLogout = () => {
  authStore.logout()
  showUserMenu.value = false
  router.push('/') // Navigate to home page after logout
}

// Check if user is already authenticated on app start
onMounted(() => {
  authStore.checkAuth()
})
</script>

<style scoped>
.form-label {
  @apply block text-sm font-medium text-gray-700 mb-2;
}

.input-field {
  @apply w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent;
}
</style>
