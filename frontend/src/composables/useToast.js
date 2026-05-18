import { ref } from 'vue'

const toasts = ref([])
let nextId = 0

export function useToast() {
  const show = (message, type = 'info', durationMs = 4000) => {
    const id = ++nextId
    toasts.value.push({ id, message, type })
    setTimeout(() => dismiss(id), durationMs)
  }

  const dismiss = (id) => {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }

  return {
    toasts,
    success: (message) => show(message, 'success'),
    error: (message) => show(message, 'error', 6000),
    info: (message) => show(message, 'info'),
    dismiss
  }
}
