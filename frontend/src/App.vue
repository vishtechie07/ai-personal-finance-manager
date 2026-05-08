<template>
  <div id="app" class="min-h-screen bg-dark-900">
    <nav class="glass-card !rounded-none !border-x-0 !border-t-0 !border-b border-white/10 w-full sticky top-0 z-50 transition-all duration-300" style="min-height: 72px; max-height: 72px; height: 72px;">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-full">
        <div class="flex justify-between items-center h-full">
          <div class="flex items-center">
            <router-link to="/" class="flex items-center space-x-3 group">
              <div class="w-10 h-10 bg-gradient-to-br from-primary-500 to-accent rounded-xl flex items-center justify-center shadow-lg shadow-primary-500/30 group-hover:shadow-primary-500/50 transition-all duration-300 group-hover:scale-105">
                <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"></path>
                </svg>
              </div>
              <span class="text-xl font-bold text-white tracking-tight">Finance<span class="text-primary-400">Flow</span></span>
            </router-link>
          </div>
          
          <div class="flex items-center space-x-2">
            <template v-if="isAuthenticated">
              <router-link to="/dashboard" class="text-slate-300 hover:text-white hover:bg-white/5 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200">Dashboard</router-link>
              <router-link to="/transactions" class="text-slate-300 hover:text-white hover:bg-white/5 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200">Transactions</router-link>
              <router-link to="/budgets" class="text-slate-300 hover:text-white hover:bg-white/5 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200">Budgets</router-link>
              <router-link to="/insights" class="text-slate-300 hover:text-white hover:bg-white/5 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200">Insights</router-link>
              
              <!-- User Profile Dropdown / Actions -->
              <div class="flex items-center space-x-4 pl-4 ml-2 border-l border-white/10">
                <router-link to="/settings" class="text-slate-400 hover:text-white transition-colors" title="Settings">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                </router-link>
                
                <div class="flex items-center space-x-3 bg-dark-800/50 py-1.5 px-3 rounded-full border border-white/5">
                  <div class="w-8 h-8 bg-gradient-to-r from-primary-500 to-accent rounded-full flex items-center justify-center text-white font-bold text-xs shadow-inner">
                    {{ userInitials }}
                  </div>
                  <span class="text-sm font-medium text-slate-200">{{ username }}</span>
                  <button @click="handleLogout" class="text-slate-400 hover:text-red-400 ml-2 transition-colors" title="Logout">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path></svg>
                  </button>
                </div>
              </div>
            </template>

            <template v-else>
              <button @click="showLoginModal = true" class="btn-primary">Login</button>
              <router-link to="/register" class="btn-secondary ml-3">Register</router-link>
            </template>
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
    <div v-if="showLoginModal" class="fixed inset-0 bg-dark-900/80 backdrop-blur-sm overflow-y-auto h-full w-full z-50 flex items-center justify-center animate-fade-in">
      <div class="relative mx-auto p-8 border border-white/10 shadow-2xl rounded-2xl bg-dark-800 w-full max-w-md transform transition-all duration-300 animate-slide-up">
        <div class="text-center mb-8">
          <div class="w-16 h-16 bg-gradient-to-br from-primary-500 to-accent rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg shadow-primary-500/30">
            <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
            </svg>
          </div>
          <h3 class="text-2xl font-bold text-white">Welcome Back</h3>
          <p class="text-slate-400 mt-2">Login to manage your finances</p>
        </div>
        <form @submit.prevent="login" class="space-y-5">
          <div>
            <label class="form-label">Username</label>
            <input v-model="loginForm.username" type="text" class="input-field" placeholder="Enter your username" required>
          </div>
          <div>
            <label class="form-label">Password</label>
            <input v-model="loginForm.password" type="password" class="input-field" placeholder="Enter your password" required>
          </div>
          <div class="flex space-x-3 pt-4">
            <button type="button" @click="showLoginModal = false" class="btn-secondary flex-1">Cancel</button>
            <button type="submit" class="btn-primary flex-1">Login</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from './stores/auth'
import { useRouter, useRoute } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

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
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    await router.push(redirect)
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
