<template>
  <div
    v-if="show"
    class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/30 backdrop-blur-sm"
    role="alert"
    aria-live="polite"
  >
    <div
      class="mx-4 max-w-sm rounded-2xl bg-white px-6 py-8 shadow-xl text-center"
    >
      <div
        class="mx-auto mb-4 h-10 w-10 animate-spin rounded-full border-2 border-primary-500 border-t-transparent"
      />
      <p class="font-semibold text-slate-900">Connecting to SpendSense</p>
      <p class="mt-2 text-sm text-slate-600">
        First load after idle can take up to a minute on hosted environments.
      </p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  show: { type: Boolean, default: false },
});
</script>
