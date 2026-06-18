<template>
  <button
    type="button"
    class="md:hidden fixed bottom-20 right-4 z-40 w-14 h-14 rounded-full bg-gradient-to-br from-primary-500 to-accent text-white shadow-xl shadow-primary-500/40 flex items-center justify-center text-2xl font-light hover:scale-105 active:scale-95 transition-transform"
    aria-label="Add expense"
    @click="$emit('add')"
  >
    +
  </button>
</template>

<script setup>
defineEmits(["add"]);
</script>
