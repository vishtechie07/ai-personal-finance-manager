<template>
  <div class="relative notification-bell-root">
    <button
      type="button"
      class="relative p-2 text-slate-500 hover:text-primary-700 rounded-lg hover:bg-slate-100"
      title="Notifications"
      @click.stop="toggle"
    >
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6 6 0 10-12 0v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
      </svg>
      <span
        v-if="store.unreadCount > 0"
        class="absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px] px-1 text-[10px] font-bold bg-red-500 text-white rounded-full flex items-center justify-center"
      >{{ store.unreadCount > 9 ? '9+' : store.unreadCount }}</span>
    </button>
    <div
      v-if="open"
      class="absolute right-0 mt-2 w-80 max-h-96 overflow-y-auto bg-white border border-slate-200 rounded-xl shadow-xl z-50"
      @click.stop
    >
      <div class="flex items-center justify-between px-4 py-3 border-b border-slate-100">
        <span class="font-semibold text-slate-900">Notifications</span>
        <button v-if="store.unreadCount" type="button" class="text-xs text-primary-600 hover:underline" @click="markAll">Mark all read</button>
      </div>
      <div v-if="!store.items.length" class="p-4 text-sm text-slate-500 text-center">No notifications</div>
      <ul v-else class="divide-y divide-slate-100">
        <li
          v-for="n in store.items"
          :key="n.id"
          class="px-4 py-3 text-sm cursor-pointer hover:bg-slate-50"
          :class="{ 'bg-primary-50/50': !n.read }"
          @click="onClick(n)"
        >
          <p class="font-medium text-slate-900">{{ n.title }}</p>
          <p class="text-slate-600 mt-0.5">{{ n.message }}</p>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useNotificationsStore } from '../stores/notifications'

const store = useNotificationsStore()
const open = ref(false)

const toggle = async () => {
  open.value = !open.value
  if (open.value) await store.fetchNotifications()
}

const markAll = async () => {
  await store.markAllRead()
}

const onClick = async (n) => {
  if (!n.read) await store.markRead(n.id)
}

const onDocClick = (e) => {
  if (!e.target.closest('.notification-bell-root')) open.value = false
}

onMounted(async () => {
  await store.fetchNotifications()
  document.addEventListener('click', onDocClick)
})

onUnmounted(() => document.removeEventListener('click', onDocClick))
</script>
