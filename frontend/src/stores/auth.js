import { defineStore } from "pinia";
import { ref, computed } from "vue";
import axios from "axios";
import { markSampleBannerPending } from "../composables/useOnboarding";
import {
  authRequestOptions,
  DEFAULT_API_TIMEOUT_MS,
} from "../composables/authTimeout";

axios.defaults.timeout = DEFAULT_API_TIMEOUT_MS;
axios.defaults.baseURL = import.meta.env.VITE_API_URL || "/api";

export const useAuthStore = defineStore("auth", () => {
  const user = ref(null);
  const token = ref(localStorage.getItem("token") || null);
  const isLoading = ref(false);
  const isBooting = ref(false);

  const isAuthenticated = computed(() => !!token.value);

  function afterAuthSuccess(userData) {
    if (userData?.id) markSampleBannerPending(userData.id);
  }

  if (token.value) {
    axios.defaults.headers.common["Authorization"] = `Bearer ${token.value}`;
  }

  const login = async (username, password, options = {}) => {
    const { timeout, coldStart } = authRequestOptions(options);
    isLoading.value = true;
    isBooting.value = coldStart;
    try {
      const response = await axios.post(
        "/auth/login",
        { username, password },
        { timeout },
      );

      const { token: authToken, user: userData } = response.data;

      token.value = authToken;
      user.value = userData;
      afterAuthSuccess(userData);

      localStorage.setItem("token", authToken);
      axios.defaults.headers.common["Authorization"] = `Bearer ${authToken}`;

      return userData;
    } finally {
      isLoading.value = false;
      isBooting.value = false;
    }
  };

  const logout = () => {
    user.value = null;
    token.value = null;
    localStorage.removeItem("token");
    delete axios.defaults.headers.common["Authorization"];
  };

  const checkAuth = async (options = {}) => {
    if (!token.value) return false;

    const { timeout, coldStart } = authRequestOptions(options);
    isBooting.value = coldStart;
    try {
      const response = await axios.get("/auth/me", { timeout });
      user.value = response.data;
      return true;
    } catch {
      logout();
      return false;
    } finally {
      isBooting.value = false;
    }
  };

  const loginWithGoogle = async (credential, options = {}) => {
    const { timeout, coldStart } = authRequestOptions(options);
    isLoading.value = true;
    isBooting.value = coldStart;
    try {
      const response = await axios.post(
        "/auth/google",
        { credential },
        { timeout },
      );
      const { token: authToken, user: userData } = response.data;
      token.value = authToken;
      user.value = userData;
      afterAuthSuccess(userData);
      localStorage.setItem("token", authToken);
      axios.defaults.headers.common["Authorization"] = `Bearer ${authToken}`;
      return userData;
    } finally {
      isLoading.value = false;
      isBooting.value = false;
    }
  };

  const register = async (userData, options = {}) => {
    const { timeout, coldStart } = authRequestOptions(options);
    isLoading.value = true;
    isBooting.value = coldStart;
    try {
      const response = await axios.post("/auth/register", userData, {
        timeout,
      });
      const { token: authToken, user: newUser } = response.data;

      token.value = authToken;
      user.value = newUser;
      afterAuthSuccess(newUser);

      localStorage.setItem("token", authToken);
      axios.defaults.headers.common["Authorization"] = `Bearer ${authToken}`;

      return newUser;
    } finally {
      isLoading.value = false;
      isBooting.value = false;
    }
  };

  return {
    user,
    token,
    isLoading,
    isBooting,
    isAuthenticated,
    login,
    loginWithGoogle,
    logout,
    checkAuth,
    register,
  };
});
