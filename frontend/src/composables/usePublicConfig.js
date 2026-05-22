import { ref } from "vue";
import axios from "axios";

const config = ref(null);
let loadPromise = null;

export function usePublicConfig() {
  async function load() {
    if (config.value) return config.value;
    if (!loadPromise) {
      loadPromise = axios
        .get("/config/public")
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
