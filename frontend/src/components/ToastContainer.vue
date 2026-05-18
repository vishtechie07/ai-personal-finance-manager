<template>
  <div class="fixed top-20 right-4 z-[100] flex flex-col gap-2 max-w-sm w-full pointer-events-none">
    <div
      v-for="toast in toasts"
      :key="toast.id"
      class="pointer-events-auto px-4 py-3 rounded-lg shadow-lg text-sm font-medium flex items-start justify-between gap-3"
      :class="toastClass(toast.type)"
    >
      <span>{{ toast.message }}</span>
      <button type="button" class="opacity-70 hover:opacity-100 shrink-0" @click="dismiss(toast.id)">×</button>
    </div>
  </div>
</template>

<script setup>
import { useToast } from '../composables/useToast'

const { toasts, dismiss } = useToast()

function toastClass(type) {
  switch (type) {
    case 'success':
      return 'bg-green-600 text-white'
    case 'error':
      return 'bg-red-600 text-white'
    default:
      return 'bg-slate-800 text-white'
  }
}
</script>
