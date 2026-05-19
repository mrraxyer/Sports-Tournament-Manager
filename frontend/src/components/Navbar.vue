<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

const userDisplayName = computed(() => auth.session?.usuario.nombre || 'Usuario')
const userEmail = computed(() => auth.session?.usuario.correo || '')
const isAdmin = computed(() => auth.session?.usuario.rol?.toUpperCase() === 'ADMIN')

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <nav class="bg-white border-b border-gray-300 px-6 py-4">
    <div class="flex justify-between items-center">
      <div class="flex items-center gap-6">
        <h1 class="text-lg font-semibold text-gray-900">Sports Tournament Manager</h1>
        <div v-if="auth.isAuthenticated" class="flex gap-4">
          <router-link v-if="isAdmin" to="/panel"
            class="text-sm text-gray-700 hover:text-gray-900 font-medium transition-colors">
            Panel
          </router-link>
          <router-link to="/torneos"
            class="text-sm text-gray-700 hover:text-gray-900 font-medium transition-colors">
            Torneos
          </router-link>
          <router-link v-if="isAdmin" to="/gestion-equipos"
            class="text-sm text-gray-700 hover:text-gray-900 font-medium transition-colors">
            Equipos
          </router-link>
          <router-link v-if="isAdmin" to="/administrador/partidos"
            class="text-sm text-gray-700 hover:text-gray-900 font-medium transition-colors">
            Partidos
          </router-link>
          <router-link v-if="isAdmin" to="/administrador/usuarios"
            class="text-sm text-gray-700 hover:text-gray-900 font-medium transition-colors">
            Usuarios
          </router-link>
          <router-link v-if="!isAdmin" to="/capitan/jugadores"
            class="text-sm text-gray-700 hover:text-gray-900 font-medium transition-colors">
            Mis Jugadores
          </router-link>
        </div>
      </div>
      <div class="flex items-center gap-4">
        <template v-if="auth.isAuthenticated">
          <div class="text-right">
            <p class="text-sm font-medium text-gray-900">{{ userDisplayName }}</p>
            <p class="text-xs text-gray-600">{{ userEmail }}</p>
          </div>
          <button @click="handleLogout"
            class="px-4 py-2 bg-gray-100 text-gray-900 border border-gray-300 rounded hover:bg-gray-200 font-medium text-sm cursor-pointer">
            Cerrar sesión
          </button>
        </template>
        <template v-else>
          <button @click="router.push('/login')"
            class="px-4 py-2 bg-blue-600 text-white border border-blue-600 rounded hover:bg-blue-700 font-medium text-sm cursor-pointer transition-colors">
            Iniciar sesión
          </button>
        </template>
      </div>
    </div>
  </nav>
</template>
