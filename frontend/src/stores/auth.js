import { defineStore } from "pinia";
import { ref, computed } from "vue";
import axios from "axios";

axios.defaults.timeout = 10000;
axios.defaults.baseURL = import.meta.env.VITE_API_URL || "/api";

export const useAuthStore = defineStore("auth", () => {
  const user = ref(null);
  const token = ref(localStorage.getItem("token") || null);
  const isLoading = ref(false);

  const isAuthenticated = computed(() => !!token.value);

  if (token.value) {
    axios.defaults.headers.common["Authorization"] = `Bearer ${token.value}`;
  }

  const login = async (username, password) => {
    isLoading.value = true;
    try {
      const response = await axios.post("/auth/login", {
        username,
        password,
      });

      const { token: authToken, user: userData } = response.data;

      token.value = authToken;
      user.value = userData;

      localStorage.setItem("token", authToken);
      axios.defaults.headers.common["Authorization"] = `Bearer ${authToken}`;

      return userData;
    } finally {
      isLoading.value = false;
    }
  };

  const logout = () => {
    user.value = null;
    token.value = null;
    localStorage.removeItem("token");
    delete axios.defaults.headers.common["Authorization"];
  };

  const checkAuth = async () => {
    if (!token.value) return false;

    try {
      const response = await axios.get("/auth/me");
      user.value = response.data;
      return true;
    } catch {
      logout();
      return false;
    }
  };

  const loginWithGoogle = async (credential) => {
    isLoading.value = true;
    try {
      const response = await axios.post("/auth/google", { credential });
      const { token: authToken, user: userData } = response.data;
      token.value = authToken;
      user.value = userData;
      localStorage.setItem("token", authToken);
      axios.defaults.headers.common["Authorization"] = `Bearer ${authToken}`;
      return userData;
    } finally {
      isLoading.value = false;
    }
  };

  const register = async (userData) => {
    isLoading.value = true;
    try {
      const response = await axios.post("/auth/register", userData);
      const { token: authToken, user: newUser } = response.data;

      token.value = authToken;
      user.value = newUser;

      localStorage.setItem("token", authToken);
      axios.defaults.headers.common["Authorization"] = `Bearer ${authToken}`;

      return newUser;
    } finally {
      isLoading.value = false;
    }
  };

  return {
    user,
    token,
    isLoading,
    isAuthenticated,
    login,
    loginWithGoogle,
    logout,
    checkAuth,
    register,
  };
});
