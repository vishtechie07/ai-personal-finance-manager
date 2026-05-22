<template>
  <div
    v-if="enabled"
    ref="buttonHost"
    class="flex justify-center w-full min-h-[44px]"
  />
</template>

<script setup>
import { ref, onMounted, watch } from "vue";

const props = defineProps({
  clientId: { type: String, required: true },
  text: { type: String, default: "continue_with" },
});

const emit = defineEmits(["success", "error"]);

const enabled = ref(!!props.clientId);
const buttonHost = ref(null);
let scriptLoaded = false;

function loadScript() {
  if (scriptLoaded || document.getElementById("google-gsi")) {
    scriptLoaded = true;
    return Promise.resolve();
  }
  return new Promise((resolve, reject) => {
    const s = document.createElement("script");
    s.id = "google-gsi";
    s.src = "https://accounts.google.com/gsi/client";
    s.async = true;
    s.defer = true;
    s.onload = () => {
      scriptLoaded = true;
      resolve();
    };
    s.onerror = () => reject(new Error("Failed to load Google sign-in"));
    document.head.appendChild(s);
  });
}

function renderButton() {
  if (!buttonHost.value || !window.google?.accounts?.id || !props.clientId)
    return;
  buttonHost.value.innerHTML = "";
  window.google.accounts.id.initialize({
    client_id: props.clientId,
    callback: (response) => {
      if (response?.credential) {
        emit("success", response.credential);
      } else {
        emit("error", new Error("No credential returned"));
      }
    },
  });
  window.google.accounts.id.renderButton(buttonHost.value, {
    type: "standard",
    theme: "outline",
    size: "large",
    text: props.text,
    width: 320,
  });
}

onMounted(async () => {
  if (!props.clientId) return;
  try {
    await loadScript();
    renderButton();
  } catch (e) {
    emit("error", e);
  }
});

watch(
  () => props.clientId,
  async (id) => {
    enabled.value = !!id;
    if (id) {
      await loadScript();
      renderButton();
    }
  },
);
</script>
