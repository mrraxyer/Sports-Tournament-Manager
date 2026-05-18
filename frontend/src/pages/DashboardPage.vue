<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import Navbar from '../components/Navbar.vue'

const auth = useAuthStore()

const userDisplayName = computed(() => auth.session?.usuario.nombre || 'Usuario autenticado')
const userEmail = computed(() => auth.session?.usuario.correo || '')
const userRole = computed(() => auth.session?.usuario.rol || 'Sin rol asignado')
const tokenPreview = computed(() => {
  const token = auth.session?.accessToken ?? ''
  if (!token) return ''
  return token.length > 18 ? `${token.slice(0, 10)}…${token.slice(-6)}` : token
})
const expiresAtLabel = computed(() => {
  const expiresAt = auth.session?.expiresAt
  if (!expiresAt) return ''
  return new Intl.DateTimeFormat('es-ES', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(expiresAt))
})

onMounted(() => {
  auth.hydrateSession()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />

    <main class="max-w-2xl mx-auto px-6 py-8">
      <section class="bg-white border border-gray-300 rounded-lg p-6">
        <header class="mb-6">
          <h1 class="text-2xl font-semibold text-gray-900">Dashboard</h1>
        </header>

        <div class="space-y-4">
          <div class="px-4 py-3 border border-gray-300 rounded bg-gray-50">
            <span class="text-sm text-gray-600 block">Usuario</span>
            <strong class="block mt-2 text-gray-900">{{ userDisplayName }}</strong>
            <small class="text-sm text-gray-600 block mt-1">{{ userEmail }}</small>
          </div>

          <div class="px-4 py-3 border border-gray-300 rounded bg-gray-50">
            <span class="text-sm text-gray-600 block">Rol</span>
            <strong class="block mt-2 text-gray-900">{{ userRole }}</strong>
          </div>

          <div class="px-4 py-3 border border-gray-300 rounded bg-gray-50">
            <span class="text-sm text-gray-600 block">Expira</span>
            <strong class="block mt-2 text-gray-900">{{ expiresAtLabel }}</strong>
          </div>

          <div class="px-4 py-3 border border-gray-300 rounded bg-gray-50">
            <span class="text-sm text-gray-600 block">Token</span>
            <code class="text-sm text-gray-600 block mt-2 break-all font-mono">{{ tokenPreview }}</code>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>
