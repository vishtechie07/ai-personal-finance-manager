<template>
  <div class="max-w-4xl mx-auto py-8">
    <div class="bg-white shadow rounded-lg">
      <div class="px-6 py-4 border-b border-gray-200">
        <h1 class="text-2xl font-bold text-gray-900">Settings</h1>
        <p class="text-gray-600 mt-1">Optional: add your own OpenAI key, or use SpendSense hosted AI when enabled.</p>
      </div>

      <div class="px-6 py-6 space-y-6">
        <div v-if="loading" class="text-gray-600">Loading…</div>

        <template v-else>
          <div v-if="message" class="rounded-md bg-green-50 text-green-800 px-4 py-3 text-sm">{{ message }}</div>
          <div v-if="error" class="rounded-md bg-red-50 text-red-800 px-4 py-3 text-sm">{{ error }}</div>

          <div v-if="platformAiEnabled && !hasOpenAiApiKey" class="rounded-md bg-primary-50 text-primary-900 px-4 py-3 text-sm">
            SpendSense AI is enabled. Category suggestions and receipt scan work without a personal OpenAI key.
          </div>

          <div>
            <p class="text-sm text-gray-700 mb-2">
              Your OpenAI API key:
              <span v-if="hasOpenAiApiKey" class="text-green-700 font-medium ml-1">saved</span>
              <span v-else-if="platformAiEnabled" class="text-primary-700 font-medium ml-1">using SpendSense AI</span>
              <span v-else class="text-amber-700 font-medium ml-1">not set — keyword suggestions only</span>
            </p>
            <label class="block text-sm font-medium text-gray-700 mb-2">Secret key</label>
            <input
              v-model="apiKeyInput"
              type="password"
              autocomplete="off"
              placeholder="sk-…"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 font-mono text-sm"
            >
            <p class="text-xs text-gray-500 mt-2">
              Keys are encrypted with the server’s settings encryption secret. Only you can use your key for suggestions after login.
            </p>
          </div>

          <div class="flex flex-wrap gap-3">
            <button
              type="button"
              :disabled="saving || !apiKeyInput.trim()"
              class="btn-primary"
              @click="saveKey"
            >
              {{ saving ? 'Saving…' : 'Save API key' }}
            </button>
            <button
              type="button"
              :disabled="saving || !hasOpenAiApiKey"
              class="btn-secondary"
              @click="removeKey"
            >
              Remove key
            </button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const loading = ref(true)
const saving = ref(false)
const hasOpenAiApiKey = ref(false)
const platformAiEnabled = ref(false)
const apiKeyInput = ref('')
const message = ref('')
const error = ref('')

async function load() {
  loading.value = true
  message.value = ''
  error.value = ''
  try {
    const { data } = await axios.get('/settings')
    hasOpenAiApiKey.value = !!data?.hasOpenAiApiKey
    platformAiEnabled.value = !!data?.platformAiEnabled
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to load settings.'
  } finally {
    loading.value = false
  }
}

async function saveKey() {
  saving.value = true
  message.value = ''
  error.value = ''
  try {
    await axios.put('/settings/openai-api-key', { apiKey: apiKeyInput.value.trim() })
    hasOpenAiApiKey.value = true
    apiKeyInput.value = ''
    message.value = 'API key saved.'
  } catch (e) {
    const body = e.response?.data
    if (body?.errors && typeof body.errors === 'object') {
      const first = Object.values(body.errors)[0]
      error.value = Array.isArray(first) ? first[0] : String(first)
    } else {
      error.value = body?.message || e.message || 'Could not save key.'
    }
  } finally {
    saving.value = false
  }
}

async function removeKey() {
  if (!confirm('Remove your saved OpenAI API key?')) return
  saving.value = true
  message.value = ''
  error.value = ''
  try {
    await axios.delete('/settings/openai-api-key')
    hasOpenAiApiKey.value = false
    apiKeyInput.value = ''
    message.value = 'API key removed.'
  } catch (e) {
    error.value = e.response?.data?.message || 'Could not remove key.'
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
