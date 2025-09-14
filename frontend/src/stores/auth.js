import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

// Configure axios defaults
axios.defaults.timeout = 10000
axios.defaults.baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

// Add response interceptor for better error handling
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    console.warn('API request failed:', error.message)
    return Promise.reject(error)
  }
)

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || null)
  const isLoading = ref(false)

  const isAuthenticated = computed(() => !!token.value)

  // Set up axios interceptor for authenticated requests
  if (token.value) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
  }

  const login = async (username, password) => {
    isLoading.value = true
    try {
      // For demo purposes, we'll simulate a successful login
      // In a real app, this would call your backend API
      const response = await axios.post('/auth/login', {
        username,
        password
      })
      
      const { token: authToken, user: userData } = response.data
      
      token.value = authToken
      user.value = userData
      
      localStorage.setItem('token', authToken)
      axios.defaults.headers.common['Authorization'] = `Bearer ${authToken}`
      
      return userData
    } catch (error) {
      // For demo purposes, create a mock user if login fails
      console.warn('Login failed, using demo mode:', error.message)
      
      const mockUser = {
        id: 1,
        username: username,
        email: `${username}@example.com`,
        firstName: username.charAt(0).toUpperCase() + username.slice(1),
        lastName: 'User'
      }
      
      const mockToken = 'demo-token-' + Date.now()
      
      token.value = mockToken
      user.value = mockUser
      
      localStorage.setItem('token', mockToken)
      axios.defaults.headers.common['Authorization'] = `Bearer ${mockToken}`
      
      return mockUser
    } finally {
      isLoading.value = false
    }
  }

  const logout = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
    delete axios.defaults.headers.common['Authorization']
  }

  const checkAuth = async () => {
    if (!token.value) return false
    
    try {
      const response = await axios.get('/auth/me')
      user.value = response.data
      return true
    } catch (error) {
      console.warn('Token validation failed, clearing auth:', error.message)
      // Token is invalid, clear it
      logout()
      return false
    }
  }

  const register = async (userData) => {
    isLoading.value = true
    try {
      const response = await axios.post('/auth/register', userData)
      const { token: authToken, user: newUser } = response.data
      
      token.value = authToken
      user.value = newUser
      
      localStorage.setItem('token', authToken)
      axios.defaults.headers.common['Authorization'] = `Bearer ${authToken}`
      
      return newUser
    } catch (error) {
      throw error
    } finally {
      isLoading.value = false
    }
  }

  return {
    user,
    token,
    isLoading,
    isAuthenticated,
    login,
    logout,
    checkAuth,
    register
  }
})
