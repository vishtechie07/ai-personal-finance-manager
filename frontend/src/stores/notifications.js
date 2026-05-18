import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useNotificationsStore = defineStore('notifications', () => {
  const items = ref([])
  const unreadCount = ref(0)
  const isLoading = ref(false)

  const fetchNotifications = async () => {
    isLoading.value = true
    try {
      const [listRes, countRes] = await Promise.all([
        axios.get('/notifications'),
        axios.get('/notifications/unread-count')
      ])
      items.value = listRes.data || []
      unreadCount.value = countRes.data?.count ?? 0
    } finally {
      isLoading.value = false
    }
  }

  const sync = async () => {
    await axios.post('/notifications/sync')
    await fetchNotifications()
  }

  const markRead = async (id) => {
    await axios.patch(`/notifications/${id}/read`)
    await fetchNotifications()
  }

  const markAllRead = async () => {
    await axios.post('/notifications/read-all')
    await fetchNotifications()
  }

  return { items, unreadCount, isLoading, fetchNotifications, sync, markRead, markAllRead }
})
