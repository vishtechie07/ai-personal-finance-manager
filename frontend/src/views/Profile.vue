<template>
  <div class="max-w-4xl mx-auto py-8">
    <div class="bg-white shadow rounded-lg">
      <!-- Profile Header -->
      <div class="px-6 py-4 border-b border-gray-200">
        <h1 class="text-2xl font-bold text-gray-900">Profile Settings</h1>
        <p class="text-gray-600 mt-1">Manage your account information and preferences</p>
      </div>

      <!-- Profile Information -->
      <div class="px-6 py-6">
        <div class="flex items-center space-x-6 mb-8">
          <div class="w-20 h-20 bg-blue-100 rounded-full flex items-center justify-center">
            <span class="text-2xl font-bold text-blue-600">{{ userInitials }}</span>
          </div>
          <div>
            <h2 class="text-xl font-semibold text-gray-900">{{ user?.firstName }} {{ user?.lastName }}</h2>
            <p class="text-gray-600">{{ user?.email }}</p>
            <p class="text-sm text-gray-500">Member since {{ memberSince }}</p>
          </div>
        </div>

        <!-- Profile Form -->
        <form @submit.prevent="updateProfile" class="space-y-6">
          <h3 class="text-lg font-medium text-gray-900">Personal Information</h3>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">First Name</label>
              <input 
                v-model="profileForm.firstName" 
                type="text" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                required
              >
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Last Name</label>
              <input 
                v-model="profileForm.lastName" 
                type="text" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                required
              >
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Username</label>
              <input 
                v-model="profileForm.username" 
                type="text" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                required
                readonly
              >
              <p class="text-sm text-gray-500 mt-1">Username cannot be changed</p>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
              <input 
                v-model="profileForm.email" 
                type="email" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                required
              >
            </div>
          </div>

          <div class="flex justify-end">
            <button 
              type="submit" 
              class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-6 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            >
              Update Profile
            </button>
          </div>
        </form>

        <!-- Change Password -->
        <div class="border-t border-gray-200 pt-8 mt-8">
          <form @submit.prevent="changePassword" class="space-y-6">
            <h3 class="text-lg font-medium text-gray-900">Change Password</h3>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Current Password</label>
                <input 
                  v-model="passwordForm.currentPassword" 
                  type="password" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  required
                >
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">New Password</label>
                <input 
                  v-model="passwordForm.newPassword" 
                  type="password" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  required
                >
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Confirm New Password</label>
                <input 
                  v-model="passwordForm.confirmPassword" 
                  type="password" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  required
                >
              </div>
            </div>

            <div class="flex justify-end">
              <button 
                type="submit" 
                class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-6 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              >
                Change Password
              </button>
            </div>
          </form>
        </div>

        <!-- Account Actions -->
        <div class="border-t border-gray-200 pt-8 mt-8">
          <h3 class="text-lg font-medium text-gray-900 mb-4">Account Actions</h3>
          
          <div class="flex space-x-4">
            <button 
              @click="showDeleteConfirm = true" 
              class="bg-red-600 hover:bg-red-700 text-white font-medium py-2 px-6 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
            >
              Delete Account
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Success Message -->
    <div v-if="showSuccessMessage" class="fixed top-4 right-4 bg-green-500 text-white px-6 py-3 rounded-lg shadow-lg z-50">
      {{ successMessage }}
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteConfirm" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
      <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
        <div class="mt-3 text-center">
          <h3 class="text-lg font-medium text-gray-900 mb-4">Delete Account</h3>
          <p class="text-gray-600 mb-6">Are you sure you want to delete your account? This action cannot be undone.</p>
          
          <div class="flex space-x-3">
            <button 
              @click="deleteAccount" 
              class="bg-red-600 hover:bg-red-700 text-white font-medium py-2 px-6 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
            >
              Yes, Delete
            </button>
            <button 
              @click="showDeleteConfirm = false" 
              class="bg-gray-300 hover:bg-gray-400 text-gray-900 font-medium py-2 px-6 rounded-lg transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
            >
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'

export default {
  name: 'Profile',
  setup() {
    const authStore = useAuthStore()
    const showDeleteConfirm = ref(false)
    const showSuccessMessage = ref(false)
    const successMessage = ref('')
    
    const profileForm = ref({
      firstName: '',
      lastName: '',
      username: '',
      email: ''
    })
    
    const passwordForm = ref({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    const user = computed(() => authStore.user)
    
    const userInitials = computed(() => {
      if (!user.value?.firstName || !user.value?.lastName) return 'U'
      return (user.value.firstName.charAt(0) + user.value.lastName.charAt(0)).toUpperCase()
    })
    
    const memberSince = computed(() => {
      if (!user.value?.createdAt) return 'Unknown'
      return new Date(user.value.createdAt).toLocaleDateString()
    })

    const initializeProfileForm = () => {
      if (user.value) {
        profileForm.value = {
          firstName: user.value.firstName || '',
          lastName: user.value.lastName || '',
          username: user.value.username || '',
          email: user.value.email || ''
        }
      }
    }

    const updateProfile = async () => {
      try {
        // For now, just show success message since we removed user management
        successMessage.value = 'Profile updated successfully! (Demo mode)'
        showSuccessMessage.value = true
        setTimeout(() => { showSuccessMessage.value = false }, 3000)
      } catch (error) {
        successMessage.value = `Failed to update profile: ${error.message || error}`
        showSuccessMessage.value = true
        setTimeout(() => { showSuccessMessage.value = false }, 5000)
      }
    }

    const changePassword = async () => {
      if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        successMessage.value = 'New passwords do not match!'
        showSuccessMessage.value = true
        setTimeout(() => { showSuccessMessage.value = false }, 3000)
        return
      }

      try {
        // For now, just show success message since we removed user management
        successMessage.value = 'Password changed successfully! (Demo mode)'
        showSuccessMessage.value = true
        passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
        setTimeout(() => { showSuccessMessage.value = false }, 3000)
      } catch (error) {
        successMessage.value = `Failed to change password: ${error.message || error}`
        showSuccessMessage.value = true
        setTimeout(() => { showSuccessMessage.value = false }, 5000)
      }
    }

    const deleteAccount = () => {
      authStore.logout()
      showDeleteConfirm.value = false
    }

    onMounted(() => {
      initializeProfileForm()
    })

    return {
      user,
      userInitials,
      memberSince,
      profileForm,
      passwordForm,
      showDeleteConfirm,
      showSuccessMessage,
      successMessage,
      updateProfile,
      changePassword,
      deleteAccount
    }
  }
}
</script>
