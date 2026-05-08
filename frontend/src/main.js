import { createApp } from 'vue'
import { createPinia } from 'pinia'
import axios from 'axios'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import './assets/main.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

axios.interceptors.response.use(
  (response) => response,
  (error) => {
    console.warn('API request failed:', error.message)
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      if (router.currentRoute.value?.meta?.requiresAuth) {
        router.push('/')
      }
    }
    return Promise.reject(error)
  }
)

app.mount('#app')

console.log('App mounted successfully')
