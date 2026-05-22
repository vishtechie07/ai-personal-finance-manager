<template>
  <div class="space-y-6 pt-6 max-w-4xl mx-auto">
    <div>
      <h1 class="text-3xl font-bold text-slate-900">Settings</h1>
      <p class="text-slate-600 mt-2">
        Optional: add your own OpenAI key, or use SpendSense hosted AI when
        enabled.
      </p>
    </div>

    <div class="glass-card p-6 space-y-6">
      <div v-if="loading" class="text-slate-600">Loading…</div>

      <template v-else>
        <div
          v-if="message"
          class="rounded-lg bg-green-50 text-green-800 px-4 py-3 text-sm border border-green-200"
        >
          {{ message }}
        </div>
        <div
          v-if="error"
          class="rounded-lg bg-red-50 text-red-800 px-4 py-3 text-sm border border-red-200"
        >
          {{ error }}
        </div>

        <div
          v-if="platformAiEnabled && !hasOpenAiApiKey"
          class="rounded-lg bg-primary-50 text-primary-900 px-4 py-3 text-sm border border-primary-200"
        >
          SpendSense AI is enabled. Category suggestions and receipt scan work
          without a personal OpenAI key.
        </div>

        <div>
          <p class="text-sm text-slate-700 mb-2">
            Your OpenAI API key:
            <span
              v-if="hasOpenAiApiKey"
              class="text-green-700 font-medium ml-1"
              >saved</span
            >
            <span
              v-else-if="platformAiEnabled"
              class="text-primary-700 font-medium ml-1"
              >using SpendSense AI</span
            >
            <span v-else class="text-amber-700 font-medium ml-1"
              >not set — keyword suggestions only</span
            >
          </p>
          <label class="form-label">Secret key</label>
          <input
            v-model="apiKeyInput"
            type="password"
            autocomplete="off"
            placeholder="sk-…"
            class="input-field font-mono text-sm"
          />
          <p class="text-xs text-slate-500 mt-2">
            Keys are encrypted on the server. Only you can use your key for
            suggestions after login.
          </p>
        </div>

        <div class="flex flex-wrap gap-3">
          <button
            type="button"
            :disabled="saving || !apiKeyInput.trim()"
            class="btn-primary"
            @click="saveKey"
          >
            {{ saving ? "Saving…" : "Save API key" }}
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
</template>

<script setup>
import { ref, onMounted } from "vue";
import axios from "axios";
import { getApiErrorMessage } from "../utils/apiError";
import { useAiStatus } from "../composables/useAiStatus";

const loading = ref(true);
const saving = ref(false);
const hasOpenAiApiKey = ref(false);
const platformAiEnabled = ref(false);
const apiKeyInput = ref("");
const message = ref("");
const error = ref("");
const aiStatus = useAiStatus();

async function load() {
  loading.value = true;
  message.value = "";
  error.value = "";
  try {
    const { data } = await axios.get("/settings");
    hasOpenAiApiKey.value = !!data?.hasOpenAiApiKey;
    platformAiEnabled.value = !!data?.platformAiEnabled;
    await aiStatus.refresh();
  } catch (e) {
    error.value = getApiErrorMessage(e);
  } finally {
    loading.value = false;
  }
}

async function saveKey() {
  saving.value = true;
  message.value = "";
  error.value = "";
  try {
    await axios.put("/settings/openai-api-key", {
      apiKey: apiKeyInput.value.trim(),
    });
    hasOpenAiApiKey.value = true;
    apiKeyInput.value = "";
    message.value = "API key saved.";
    await aiStatus.refresh();
  } catch (e) {
    const body = e.response?.data;
    if (body?.errors && typeof body.errors === "object") {
      const first = Object.values(body.errors)[0];
      error.value = Array.isArray(first) ? first[0] : String(first);
    } else {
      error.value = getApiErrorMessage(e);
    }
  } finally {
    saving.value = false;
  }
}

async function removeKey() {
  if (!confirm("Remove your saved OpenAI API key?")) return;
  saving.value = true;
  message.value = "";
  error.value = "";
  try {
    await axios.delete("/settings/openai-api-key");
    hasOpenAiApiKey.value = false;
    apiKeyInput.value = "";
    message.value = "API key removed.";
    await aiStatus.refresh();
  } catch (e) {
    error.value = getApiErrorMessage(e);
  } finally {
    saving.value = false;
  }
}

onMounted(load);
</script>
