<template>
  <div class="space-y-6 pt-6 max-w-2xl mx-auto">
    <div>
      <h1 class="text-3xl font-bold text-slate-900">Create your account</h1>
      <p class="text-slate-600 mt-2">Join SpendSense — budgets, bills, and spending insights in minutes</p>
    </div>

    <div class="glass-card p-6">
      <form class="space-y-4" @submit.prevent="onRegister">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="form-label">Username</label>
            <input
              v-model="form.username"
              type="text"
              class="input-field"
              autocomplete="username"
              required
              minlength="3"
            />
          </div>

          <div>
            <label class="form-label">Email</label>
            <input
              v-model="form.email"
              type="email"
              class="input-field"
              autocomplete="email"
              required
            />
          </div>

          <div>
            <label class="form-label">First name (optional)</label>
            <input v-model="form.firstName" type="text" class="input-field" autocomplete="given-name" />
          </div>

          <div>
            <label class="form-label">Last name (optional)</label>
            <input v-model="form.lastName" type="text" class="input-field" autocomplete="family-name" />
          </div>

          <div class="md:col-span-2">
            <label class="form-label">Password</label>
            <input
              v-model="form.password"
              type="password"
              class="input-field"
              autocomplete="new-password"
              required
              minlength="8"
            />
            <p class="text-xs text-slate-500 mt-1">At least 8 characters</p>
          </div>
        </div>

        <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

        <div class="flex gap-3">
          <button type="submit" class="btn-primary flex-1" :disabled="authStore.isLoading">
            {{ authStore.isLoading ? 'Creating account…' : 'Create account' }}
          </button>
          <button type="button" class="btn-secondary" @click="router.push('/')">
            Cancel
          </button>
        </div>

        <p class="text-sm text-slate-600 text-center">
          Already have an account?
          <router-link to="/" class="text-primary-600 hover:text-primary-700 font-medium">Sign in</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useToast } from '../composables/useToast'
import { getApiErrorMessage } from '../utils/apiError'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

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
    await authStore.register({
      username: form.value.username.trim(),
      email: form.value.email.trim(),
      password: form.value.password,
      firstName: form.value.firstName?.trim() || null,
      lastName: form.value.lastName?.trim() || null
    })
    toast.success('Account created!')
    await router.push('/dashboard')
  } catch (e) {
    error.value = getApiErrorMessage(e)
    toast.error(error.value)
  }
}
</script>
