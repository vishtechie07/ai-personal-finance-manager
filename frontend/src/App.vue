<template>
  <div id="app" class="min-h-screen bg-slate-100">
    <AppBootOverlay :show="authStore.isBooting" />
    <ToastContainer />
    <nav
      class="w-full sticky top-0 z-50 border-b border-slate-200/90 bg-white/95 backdrop-blur-md shadow-sm transition-all duration-300"
      style="min-height: 72px; max-height: 72px; height: 72px"
    >
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-full">
        <div class="flex justify-between items-center h-full">
          <div class="flex items-center">
            <router-link to="/" class="flex items-center space-x-3 group">
              <div
                class="w-10 h-10 bg-gradient-to-br from-primary-500 to-accent rounded-xl flex items-center justify-center shadow-lg shadow-primary-500/30 group-hover:shadow-primary-500/50 transition-all duration-300 group-hover:scale-105"
              >
                <svg
                  class="w-6 h-6 text-white"
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
              <span class="text-xl font-bold text-slate-900 tracking-tight"
                >Spend<span class="text-primary-600">Sense</span></span
              >
            </router-link>
          </div>

          <div class="flex items-center space-x-2">
            <template v-if="isAuthenticated">
              <nav class="hidden md:flex items-center space-x-1">
                <router-link
                  v-for="link in navLinks"
                  :key="link.to"
                  :to="link.to"
                  class="px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200"
                  :class="navLinkClass(link.to)"
                  >{{ link.label }}</router-link
                >
              </nav>
              <router-link
                to="/settings"
                class="hidden md:inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium transition-colors"
                :class="aiStatus.chipClass"
                title="AI settings"
              >
                {{ aiStatus.label }}
              </router-link>
              <div class="hidden md:block">
                <NotificationBell />
              </div>

              <!-- User Profile Dropdown / Actions -->
              <div
                class="flex items-center space-x-4 pl-4 ml-2 border-l border-slate-200"
              >
                <router-link
                  to="/settings"
                  class="text-slate-500 hover:text-primary-700 transition-colors"
                  title="Settings"
                >
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
                      d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"
                    ></path>
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                    ></path>
                  </svg>
                </router-link>

                <div
                  class="flex items-center space-x-3 bg-slate-50 py-1.5 px-3 rounded-full border border-slate-200"
                >
                  <div
                    class="w-8 h-8 bg-gradient-to-r from-primary-500 to-accent rounded-full flex items-center justify-center text-white font-bold text-xs shadow-inner"
                  >
                    {{ userInitials }}
                  </div>
                  <span class="text-sm font-medium text-slate-700">{{
                    username
                  }}</span>
                  <button
                    class="text-slate-500 hover:text-red-600 ml-2 transition-colors"
                    title="Logout"
                    @click="handleLogout"
                  >
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
                        d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                      ></path>
                    </svg>
                  </button>
                </div>
              </div>
            </template>

            <template v-else>
              <div class="flex items-center gap-3">
                <button
                  type="button"
                  class="btn-primary"
                  @click="showLoginModal = true"
                >
                  Login
                </button>
                <router-link to="/register" class="btn-secondary"
                  >Register</router-link
                >
              </div>
            </template>
          </div>
        </div>
      </div>
    </nav>

    <main
      class="w-full mx-auto px-4 sm:px-6 lg:px-8 py-0 pb-20 md:pb-0"
      style="margin: 0; padding-top: 80px"
    >
      <div class="max-w-7xl mx-auto">
        <SampleDataBanner v-if="isAuthenticated" />
        <router-view />
      </div>
    </main>

    <nav
      v-if="isAuthenticated"
      class="md:hidden fixed bottom-0 inset-x-0 z-40 border-t border-slate-200 bg-white/95 backdrop-blur-md safe-area-pb"
      aria-label="Mobile navigation"
    >
      <div class="grid grid-cols-5 h-14">
        <router-link
          v-for="tab in mobileTabs"
          :key="tab.to"
          :to="tab.to"
          class="flex flex-col items-center justify-center gap-0.5 text-[10px] font-medium transition-colors"
          :class="
            isActiveRoute(tab.to)
              ? 'text-primary-600'
              : 'text-slate-500 hover:text-primary-600'
          "
        >
          <span class="text-base" aria-hidden="true">{{ tab.icon }}</span>
          <span>{{ tab.label }}</span>
        </router-link>
      </div>
    </nav>

    <!-- Login Modal -->
    <div
      v-if="showLoginModal"
      class="fixed inset-0 bg-slate-900/40 backdrop-blur-sm overflow-y-auto h-full w-full z-50 flex items-center justify-center animate-fade-in"
    >
      <div
        class="relative mx-auto p-8 border border-slate-200 shadow-xl rounded-2xl bg-white w-full max-w-md transform transition-all duration-300 animate-slide-up"
      >
        <div class="text-center mb-8">
          <div
            class="w-16 h-16 bg-gradient-to-br from-primary-500 to-accent rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg shadow-primary-500/30"
          >
            <svg
              class="w-8 h-8 text-white"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
              ></path>
            </svg>
          </div>
          <h3 class="text-2xl font-bold text-slate-900">Welcome Back</h3>
          <p class="text-slate-600 mt-2">Sign in to SpendSense</p>
        </div>
        <div class="space-y-5">
          <GoogleSignInButton
            v-if="publicConfig?.googleSignInEnabled"
            :client-id="publicConfig.googleClientId"
            text="signin_with"
            @success="loginWithGoogle"
            @error="onGoogleLoginError"
          />

          <div v-if="publicConfig?.googleSignInEnabled" class="relative">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-slate-200" />
            </div>
            <div class="relative flex justify-center text-sm">
              <span class="px-2 bg-white text-slate-500">or username</span>
            </div>
          </div>

          <form class="space-y-5" @submit.prevent="login">
            <div>
              <label class="form-label">Username</label>
              <input
                v-model="loginForm.username"
                type="text"
                class="input-field"
                placeholder="Enter your username"
                required
                :disabled="authStore.isLoading"
              />
            </div>
            <div>
              <label class="form-label">Password</label>
              <input
                v-model="loginForm.password"
                type="password"
                class="input-field"
                placeholder="Enter your password"
                required
                :disabled="authStore.isLoading"
              />
            </div>
            <p v-if="loginError" class="text-sm text-red-600">
              {{ loginError }}
            </p>
            <div class="flex space-x-3 pt-2">
              <button
                type="button"
                class="btn-secondary flex-1"
                :disabled="authStore.isLoading"
                @click="showLoginModal = false"
              >
                Cancel
              </button>
              <button
                type="submit"
                class="btn-primary flex-1"
                :disabled="authStore.isLoading"
              >
                {{ authStore.isLoading ? "Logging in…" : "Login" }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useAuthStore } from "./stores/auth";
import { useRouter, useRoute } from "vue-router";
import { getApiErrorMessage } from "./utils/apiError";
import { useToast } from "./composables/useToast";
import { useAiStatus } from "./composables/useAiStatus";
import ToastContainer from "./components/ToastContainer.vue";
import NotificationBell from "./components/NotificationBell.vue";
import GoogleSignInButton from "./components/GoogleSignInButton.vue";
import AppBootOverlay from "./components/AppBootOverlay.vue";
import SampleDataBanner from "./components/SampleDataBanner.vue";
import { usePublicConfig } from "./composables/usePublicConfig";

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();

const showLoginModal = ref(false);
const showUserMenu = ref(false);
const loginError = ref(null);
const toast = useToast();
const { config: publicConfig, load: loadPublicConfig } = usePublicConfig();
const aiStatus = useAiStatus();

const navLinks = [
  { to: "/dashboard", label: "Dashboard" },
  { to: "/transactions", label: "Transactions" },
  { to: "/budgets", label: "Budgets" },
  { to: "/bills", label: "Bills" },
  { to: "/insights", label: "Insights" },
];

const mobileTabs = [
  { to: "/dashboard", label: "Home", icon: "⌂" },
  { to: "/transactions", label: "Txns", icon: "↕" },
  { to: "/budgets", label: "Budget", icon: "◎" },
  { to: "/bills", label: "Bills", icon: "▤" },
  { to: "/insights", label: "Insights", icon: "◈" },
];

const isActiveRoute = (path) => {
  if (path === "/dashboard") {
    return route.path === "/dashboard";
  }
  return route.path === path || route.path.startsWith(`${path}/`);
};

const navLinkClass = (path) =>
  isActiveRoute(path)
    ? "text-primary-700 bg-primary-50 font-semibold"
    : "text-slate-600 hover:text-primary-700 hover:bg-slate-100";

const loginForm = ref({
  username: "",
  password: "",
});

const isAuthenticated = computed(() => authStore.isAuthenticated);
const username = computed(() => authStore.user?.username || "User");
const userInitials = computed(() => {
  if (!authStore.user?.username) return "U";
  return authStore.user.username.substring(0, 2).toUpperCase();
});

const loginWithGoogle = async (credential) => {
  loginError.value = null;
  try {
    await authStore.loginWithGoogle(credential);
    showLoginModal.value = false;
    toast.success("Welcome back!");
    const redirect =
      typeof route.query.redirect === "string"
        ? route.query.redirect
        : "/dashboard";
    await router.push(redirect);
  } catch (error) {
    loginError.value = getApiErrorMessage(error, { auth: "google" });
    toast.error(loginError.value);
  }
};

const onGoogleLoginError = (e) => {
  loginError.value = e?.message || "Google sign-in failed";
  toast.error(loginError.value);
};

const login = async () => {
  loginError.value = null;
  try {
    await authStore.login(loginForm.value.username, loginForm.value.password);
    showLoginModal.value = false;
    loginForm.value = { username: "", password: "" };
    toast.success("Welcome back!");
    const redirect =
      typeof route.query.redirect === "string"
        ? route.query.redirect
        : "/dashboard";
    await router.push(redirect);
  } catch (error) {
    loginError.value = getApiErrorMessage(error, { auth: "login" });
    toast.error(loginError.value);
  }
};

const handleLogout = () => {
  aiStatus.reset();
  authStore.logout();
  showUserMenu.value = false;
  router.push("/"); // Navigate to home page after logout
};

// Check if user is already authenticated on app start
watch(
  () => authStore.isAuthenticated,
  (authed) => {
    if (authed) aiStatus.refresh();
    else aiStatus.reset();
  },
  { immediate: true },
);

onMounted(() => {
  loadPublicConfig();
  if (authStore.token) {
    authStore.checkAuth({ coldStart: true });
  }
});
</script>

<style scoped>
.form-label {
  @apply block text-sm font-medium text-gray-700 mb-2;
}

.input-field {
  @apply w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent;
}
</style>
