<template>
  <div class="space-y-6 pt-6">
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Register</h1>
        <p class="text-gray-600 mt-2">Create your account to start tracking budgets</p>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <form @submit.prevent="onRegister" class="space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Username</label>
            <input
              v-model="form.username"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              required
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
            <input
              v-model="form.email"
              type="email"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              required
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">First Name (optional)</label>
            <input
              v-model="form.firstName"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Last Name (optional)</label>
            <input
              v-model="form.lastName"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-700 mb-2">Password</label>
            <input
              v-model="form.password"
              type="password"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              required
              minlength="6"
            />
          </div>
        </div>

        <div v-if="error" class="text-sm text-red-600">
          {{ error }}
        </div>

        <div class="flex space-x-3">
          <button
            type="submit"
            class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 flex-1"
          >
            Create Account
          </button>
          <button
            type="button"
            @click="router.push('/')"
            class="bg-gray-200 hover:bg-gray-300 text-gray-900 font-medium py-2 px-4 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()

    const form = ref({
      username: '',
      email: '',
      password: '',
      firstName: '',
      lastName: ''
    })

    const error = ref(null)

    const onRegister = async () => {
      error.value = null
      try {
        const payload = {
          username: form.value.username,
          email: form.value.email,
          password: form.value.password,
          firstName: form.value.firstName || null,
          lastName: form.value.lastName || null
        }

        await authStore.register(payload)
        router.push('/dashboard')
      } catch (e) {
        error.value =
          e?.response?.data?.error ||
          e?.message ||
          'Registration failed. Please try again.'
      }
    }

    return {
      router,
      form,
      error,
      onRegister
    }
  }
}
</script>

