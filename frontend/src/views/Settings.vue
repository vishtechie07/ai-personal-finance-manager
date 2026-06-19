<template>
  <div class="space-y-8 pt-6 max-w-4xl mx-auto">
    <div>
      <h1 class="text-3xl font-bold text-slate-900">Settings</h1>
      <p class="text-slate-600 mt-2">
        Account, preferences, category rules, and AI configuration.
      </p>
    </div>

    <div v-if="loading" class="glass-card p-6 text-slate-500">Loading…</div>

    <template v-else>
      <p
        v-if="message"
        class="rounded-xl bg-green-50 text-green-800 px-4 py-3 text-sm border border-green-200"
      >
        {{ message }}
      </p>
      <p
        v-if="error"
        class="rounded-xl bg-red-50 text-red-800 px-4 py-3 text-sm border border-red-200"
      >
        {{ error }}
      </p>

      <!-- Profile -->
      <section class="glass-card p-6 space-y-4">
        <h2 class="text-lg font-semibold text-slate-900">Profile</h2>
        <form class="grid grid-cols-1 md:grid-cols-2 gap-4" @submit.prevent="saveProfile">
          <div>
            <label class="form-label">First name</label>
            <input v-model="profile.firstName" class="input-field" />
          </div>
          <div>
            <label class="form-label">Last name</label>
            <input v-model="profile.lastName" class="input-field" />
          </div>
          <div>
            <label class="form-label">Email</label>
            <input v-model="profile.email" type="email" class="input-field" required />
          </div>
          <div>
            <label class="form-label">Username</label>
            <input :value="profile.username" class="input-field bg-slate-50" disabled />
          </div>
          <div class="md:col-span-2">
            <button type="submit" class="btn-primary" :disabled="saving">Save profile</button>
          </div>
        </form>

        <form
          v-if="!isGoogle"
          class="border-t border-slate-100 pt-4 grid grid-cols-1 md:grid-cols-2 gap-4"
          @submit.prevent="savePassword"
        >
          <h3 class="md:col-span-2 text-sm font-semibold text-slate-800">Change password</h3>
          <div>
            <label class="form-label">Current password</label>
            <input v-model="password.current" type="password" class="input-field" required />
          </div>
          <div>
            <label class="form-label">New password</label>
            <input v-model="password.next" type="password" class="input-field" minlength="8" required />
          </div>
          <div class="md:col-span-2">
            <button type="submit" class="btn-secondary" :disabled="saving">Update password</button>
          </div>
        </form>
        <p v-else class="text-sm text-slate-500 border-t border-slate-100 pt-4">
          Signed in with Google — password is managed by Google.
        </p>
      </section>

      <!-- Preferences -->
      <section class="glass-card p-6 space-y-4">
        <h2 class="text-lg font-semibold text-slate-900">Bill reminders</h2>
        <p class="text-sm text-slate-600">
          Email reminders when bills are due soon (requires server email configuration).
        </p>
        <label class="flex items-center gap-3">
          <input v-model="prefs.billRemindersEnabled" type="checkbox" class="rounded" />
          <span class="text-sm text-slate-700">Enable email bill reminders</span>
        </label>
        <div>
          <label class="form-label">Remind me days before due</label>
          <input
            v-model.number="prefs.billReminderDaysBefore"
            type="number"
            min="1"
            max="14"
            class="input-field max-w-[8rem]"
          />
        </div>
        <button type="button" class="btn-secondary" :disabled="saving" @click="savePreferences">
          Save preferences
        </button>
      </section>

      <!-- Category rules -->
      <section class="glass-card p-6 space-y-4">
        <div class="flex items-center justify-between gap-3">
          <div>
            <h2 class="text-lg font-semibold text-slate-900">Category rules</h2>
            <p class="text-sm text-slate-600 mt-1">
              Auto-categorize when a description contains your keyword (e.g. Uber → Transport).
            </p>
          </div>
          <button type="button" class="btn-primary btn-sm" @click="openRuleModal()">Add rule</button>
        </div>
        <div v-if="!rules.length" class="text-sm text-slate-500 py-4 text-center">
          No rules yet.
        </div>
        <ul v-else class="divide-y divide-slate-100">
          <li
            v-for="rule in rules"
            :key="rule.id"
            class="flex items-center justify-between py-3 gap-3"
          >
            <div>
              <p class="font-medium text-slate-900">
                "{{ rule.pattern }}" → {{ formatCategoryLabel(rule.category) }}
              </p>
              <p class="text-xs text-slate-500">Priority {{ rule.priority }}</p>
            </div>
            <button
              type="button"
              class="text-xs text-red-600 hover:underline"
              @click="removeRule(rule.id)"
            >
              Delete
            </button>
          </li>
        </ul>
      </section>

      <!-- AI -->
      <section class="glass-card p-6 space-y-4">
        <h2 class="text-lg font-semibold text-slate-900">AI (optional)</h2>
        <div
          v-if="aiStatus.platformTrialActive.value"
          class="rounded-lg border border-primary-200 bg-primary-50 px-3 py-2 text-sm text-primary-900"
        >
          <span class="font-medium">Hosted AI trial:</span>
          {{ formatTrialCountdown(aiStatus.secondsRemaining.value) }} remaining.
          Add your own key below to keep AI after the trial.
        </div>
        <div
          v-else-if="aiStatus.platformTrialExpired.value && !hasOpenAiApiKey"
          class="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-950"
        >
          Your {{ aiStatus.platformTrialMinutes.value }}-minute hosted AI trial has ended.
          Paste your OpenAI key below to continue using category suggest, receipt scan, and monthly briefs.
        </div>
        <p v-else class="text-sm text-slate-600">
          Your OpenAI key powers category suggestions, receipt scan, and monthly briefs.
        </p>
        <input
          v-model="apiKeyInput"
          type="password"
          autocomplete="off"
          placeholder="sk-…"
          class="input-field font-mono text-sm"
        />
        <div class="flex flex-wrap gap-3">
          <button
            type="button"
            class="btn-primary"
            :disabled="saving || !apiKeyInput.trim()"
            @click="saveKey"
          >
            Save API key
          </button>
          <button
            type="button"
            class="btn-secondary"
            :disabled="saving || !hasOpenAiApiKey"
            @click="removeKey"
          >
            Remove key
          </button>
        </div>
      </section>

      <!-- Danger zone -->
      <section class="glass-card p-6 border border-red-100 space-y-3">
        <h2 class="text-lg font-semibold text-red-800">Delete account</h2>
        <p class="text-sm text-slate-600">
          Permanently removes your account and all financial data.
        </p>
        <input
          v-if="!isGoogle"
          v-model="deletePassword"
          type="password"
          placeholder="Confirm password"
          class="input-field max-w-sm"
        />
        <button type="button" class="btn-danger" :disabled="saving" @click="deleteAccount">
          Delete my account
        </button>
      </section>
    </template>

    <div
      v-if="showRuleModal"
      class="fixed inset-0 bg-slate-900/40 z-50 flex items-center justify-center p-4"
    >
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md p-6 space-y-4">
        <h3 class="text-lg font-bold">New category rule</h3>
        <input v-model="ruleForm.pattern" class="input-field" placeholder="Keyword e.g. Netflix" />
        <select v-model="ruleForm.category" class="input-field">
          <option v-for="c in TRANSACTION_CATEGORIES" :key="c" :value="c">
            {{ formatCategoryLabel(c) }}
          </option>
        </select>
        <input v-model.number="ruleForm.priority" type="number" class="input-field" placeholder="Priority" />
        <div class="flex gap-3">
          <button type="button" class="btn-secondary flex-1" @click="showRuleModal = false">Cancel</button>
          <button type="button" class="btn-primary flex-1" @click="saveRule">Save</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import axios from "axios";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { getApiErrorMessage } from "../utils/apiError";
import { useAiStatus } from "../composables/useAiStatus";
import { formatTrialCountdown } from "../utils/aiSourceMessage";
import {
  TRANSACTION_CATEGORIES,
  formatCategoryLabel,
} from "../composables/useCategories";

const authStore = useAuthStore();
const router = useRouter();
const aiStatus = useAiStatus();

const loading = ref(true);
const saving = ref(false);
const message = ref("");
const error = ref("");
const hasOpenAiApiKey = ref(false);
const apiKeyInput = ref("");
const rules = ref([]);
const showRuleModal = ref(false);
const deletePassword = ref("");

const profile = ref({ firstName: "", lastName: "", email: "", username: "" });
const password = ref({ current: "", next: "" });
const prefs = ref({ billRemindersEnabled: true, billReminderDaysBefore: 3 });
const ruleForm = ref({ pattern: "", category: "OTHER", priority: 0 });

const isGoogle = computed(() => authStore.user?.authProvider === "GOOGLE");

async function load() {
  loading.value = true;
  error.value = "";
  try {
    const [settingsRes, rulesRes] = await Promise.all([
      axios.get("/settings"),
      axios.get("/category-rules"),
    ]);
    hasOpenAiApiKey.value = !!settingsRes.data?.hasOpenAiApiKey;
    const p = settingsRes.data?.profile || authStore.user || {};
    profile.value = {
      firstName: p.firstName || "",
      lastName: p.lastName || "",
      email: p.email || "",
      username: p.username || "",
    };
    prefs.value = {
      billRemindersEnabled: p.billRemindersEnabled !== false,
      billReminderDaysBefore: p.billReminderDaysBefore ?? 3,
    };
    rules.value = rulesRes.data || [];
    aiStatus.applySettings(settingsRes.data);
  } catch (e) {
    error.value = getApiErrorMessage(e);
  } finally {
    loading.value = false;
  }
}

async function saveProfile() {
  saving.value = true;
  message.value = "";
  error.value = "";
  try {
    const { data } = await axios.patch("/auth/me", {
      firstName: profile.value.firstName,
      lastName: profile.value.lastName,
      email: profile.value.email,
    });
    authStore.user = { ...authStore.user, ...data };
    message.value = "Profile saved.";
  } catch (e) {
    error.value = getApiErrorMessage(e);
  } finally {
    saving.value = false;
  }
}

async function savePassword() {
  saving.value = true;
  try {
    await axios.post("/auth/me/change-password", {
      currentPassword: password.value.current,
      newPassword: password.value.next,
    });
    password.value = { current: "", next: "" };
    message.value = "Password updated.";
  } catch (e) {
    error.value = getApiErrorMessage(e);
  } finally {
    saving.value = false;
  }
}

async function savePreferences() {
  saving.value = true;
  try {
    await axios.put("/settings/preferences", prefs.value);
    message.value = "Preferences saved.";
  } catch (e) {
    error.value = getApiErrorMessage(e);
  } finally {
    saving.value = false;
  }
}

async function saveKey() {
  saving.value = true;
  try {
    const { data } = await axios.put("/settings/openai-api-key", {
      apiKey: apiKeyInput.value.trim(),
    });
    hasOpenAiApiKey.value = true;
    apiKeyInput.value = "";
    message.value = "API key saved.";
    aiStatus.applySettings(data);
  } catch (e) {
    error.value = getApiErrorMessage(e);
  } finally {
    saving.value = false;
  }
}

async function removeKey() {
  if (!confirm("Remove your saved OpenAI API key?")) return;
  saving.value = true;
  try {
    const { data } = await axios.delete("/settings/openai-api-key");
    hasOpenAiApiKey.value = false;
    message.value = "API key removed.";
    aiStatus.applySettings(data);
  } catch (e) {
    error.value = getApiErrorMessage(e);
  } finally {
    saving.value = false;
  }
}

function openRuleModal() {
  ruleForm.value = { pattern: "", category: "OTHER", priority: 0 };
  showRuleModal.value = true;
}

async function saveRule() {
  saving.value = true;
  try {
    await axios.post("/category-rules", ruleForm.value);
    showRuleModal.value = false;
    await load();
    message.value = "Rule added.";
  } catch (e) {
    error.value = getApiErrorMessage(e);
  } finally {
    saving.value = false;
  }
}

async function removeRule(id) {
  if (!confirm("Delete this rule?")) return;
  await axios.delete(`/category-rules/${id}`);
  rules.value = rules.value.filter((r) => r.id !== id);
}

async function deleteAccount() {
  if (!confirm("Delete your account permanently? This cannot be undone.")) return;
  saving.value = true;
  try {
    await axios.delete("/auth/me", {
      data: isGoogle.value ? {} : { password: deletePassword.value },
    });
    authStore.logout();
    router.push("/");
  } catch (e) {
    error.value = getApiErrorMessage(e);
  } finally {
    saving.value = false;
  }
}

onMounted(load);
</script>
