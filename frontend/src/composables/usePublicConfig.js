import { ref } from "vue";
import axios from "axios";
import { AUTH_COLD_START_TIMEOUT_MS } from "./authTimeout";

const config = ref(null);
let loadPromise = null;

export function usePublicConfig() {
  async function load() {
    if (config.value) return config.value;
    if (!loadPromise) {
      loadPromise = axios
        .get("/config/public", { timeout: AUTH_COLD_START_TIMEOUT_MS })
        .then((res) => {
          config.value = res.data;
          return config.value;
        })
        .catch(() => {
          config.value = {
            googleSignInEnabled: false,
            googleClientId: "",
            registrationEnabled: true,
          };
          return config.value;
        });
    }
    return loadPromise;
  }

  return { config, load };
}
