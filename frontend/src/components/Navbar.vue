<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

const userDisplayName = computed(() => auth.session?.usuario.nombre || 'Usuario')
const userEmail = computed(() => auth.session?.usuario.correo || '')

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <nav class="bg-white border-b border-gray-300 px-6 py-4">
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-lg font-semibold text-gray-900">Sports Tournament Manager</h1>
      </div>
      <div class="flex items-center gap-4">
        <div class="text-right">
          <p class="text-sm font-medium text-gray-900">{{ userDisplayName }}</p>
          <p class="text-xs text-gray-600">{{ userEmail }}</p>
        </div>
        <button @click="handleLogout"
          class="px-4 py-2 bg-gray-100 text-gray-900 border border-gray-300 rounded hover:bg-gray-200 font-medium text-sm cursor-pointer">
          Cerrar sesión
        </button>
      </div>
    </div>
  </nav>
</template>
