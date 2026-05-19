<script setup lang="ts">
import { onMounted, reactive, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import api from '../services/api'
import Navbar from '../components/Navbar.vue'

const auth = useAuthStore()

interface Tournament {
  torneosId: number
  nombre: string
  tipoFormato: string
  fechaInicio: string
}

interface FormData {
  nombre: string
  tipoFormato: string
  fechaInicio: string
}

const tournaments = reactive<Tournament[]>([])
const loading = reactive({ fetch: false, submit: false })
const feedback = reactive({
  type: '' as 'success' | 'error' | '',
  message: '',
})

const form = reactive<FormData>({
  nombre: '',
  tipoFormato: 'Round-Robin',
  fechaInicio: '',
})

const isAdmin = computed(() => auth.session?.usuario.rol === 'ADMIN')
const formatoOptions = ['Round-Robin', 'Elimination']

async function fetchTournaments() {
  loading.fetch = true
  try {
    const response = await api.get<{ data: Tournament[] }>('/torneos')
    tournaments.length = 0
    tournaments.push(...(response.data.data || []))
  } catch (error) {
    feedback.type = 'error'
    feedback.message = error instanceof Error ? error.message : 'Error al cargar torneos'
  } finally {
    loading.fetch = false
  }
}

async function submitForm() {
  feedback.type = ''
  feedback.message = ''

  if (!form.nombre.trim()) {
    feedback.type = 'error'
    feedback.message = 'Nombre es requerido'
    return
  }

  if (!form.fechaInicio) {
    feedback.type = 'error'
    feedback.message = 'Fecha de inicio es requerida'
    return
  }

  loading.submit = true
  try {
    const payload = {
      nombre: form.nombre.trim(),
      tipoFormato: form.tipoFormato,
      fechaInicio: form.fechaInicio,
    }

    await api.post('/torneos', payload)

    feedback.type = 'success'
    feedback.message = 'Torneo creado exitosamente'
    form.nombre = ''
    form.tipoFormato = 'Round-Robin'
    form.fechaInicio = ''

    await fetchTournaments()
  } catch (error) {
    feedback.type = 'error'
    feedback.message = error instanceof Error ? error.message : 'Error al crear torneo'
  } finally {
    loading.submit = false
  }
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Intl.DateTimeFormat('es-ES', { dateStyle: 'medium' }).format(new Date(date))
}

onMounted(() => {
  auth.hydrateSession()
  fetchTournaments()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />

    <main v-if="isAdmin" class="max-w-6xl mx-auto px-6 py-8">
      <!-- Create Tournament Form -->
      <section class="bg-white border border-gray-300 rounded-lg p-6 mb-8">
        <header class="mb-6">
          <h2 class="text-2xl font-semibold text-gray-900">Crear Nuevo Torneo</h2>
        </header>

        <form class="space-y-4" @submit.prevent="submitForm">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <label class="block space-y-1.5">
              <span class="text-sm font-medium text-gray-700">Nombre</span>
              <input v-model="form.nombre" type="text" placeholder="Ej: Campeonato Verano 2026"
                class="w-full px-3 py-2.5 border border-gray-300 rounded bg-white text-gray-900 focus:outline-2 focus:outline-blue-400 focus:outline-offset-1"
                required />
            </label>

            <label class="block space-y-1.5">
              <span class="text-sm font-medium text-gray-700">Tipo de Formato</span>
              <select v-model="form.tipoFormato"
                class="w-full px-3 py-2.5 border border-gray-300 rounded bg-white text-gray-900 focus:outline-2 focus:outline-blue-400 focus:outline-offset-1"
                required>
                <option v-for="formato in formatoOptions" :key="formato" :value="formato">
                  {{ formato }}
                </option>
              </select>
            </label>

            <label class="block space-y-1.5">
              <span class="text-sm font-medium text-gray-700">Fecha de Inicio</span>
              <input v-model="form.fechaInicio" type="date"
                class="w-full px-3 py-2.5 border border-gray-300 rounded bg-white text-gray-900 focus:outline-2 focus:outline-blue-400 focus:outline-offset-1"
                required />
            </label>
          </div>

          <div v-if="feedback.message" :class="{
            'px-4 py-3 rounded border bg-green-50 border-green-300 text-green-700': feedback.type === 'success',
            'px-4 py-3 rounded border bg-red-50 border-red-300 text-red-700': feedback.type === 'error'
          }" class="text-sm">
            {{ feedback.message }}
          </div>

          <button
            class="px-4 py-2.5 bg-blue-600 text-white border border-blue-600 rounded hover:bg-blue-700 hover:border-blue-700 disabled:opacity-60 cursor-pointer font-medium text-sm"
            type="submit" :disabled="loading.submit">
            <span v-if="loading.submit">Creando…</span>
            <span v-else>Crear Torneo</span>
          </button>
        </form>
      </section>

      <!-- Tournaments Table -->
      <section class="bg-white border border-gray-300 rounded-lg overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-300 bg-gray-50">
          <h3 class="text-xl font-semibold text-gray-900">Torneos Existentes</h3>
        </div>

        <div v-if="loading.fetch" class="px-6 py-8 text-center text-gray-600">
          Cargando torneos…
        </div>

        <div v-else-if="tournaments.length === 0" class="px-6 py-8 text-center text-gray-600">
          No hay torneos creados aún
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="bg-gray-50 border-b border-gray-300">
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">ID</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Nombre</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Tipo de Formato</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Fecha de Inicio</th>
                <th class="px-6 py-3 text-center text-sm font-semibold text-gray-900">Partidos</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="tournament in tournaments" :key="tournament.torneosId"
                class="border-b border-gray-200 hover:bg-gray-50 transition-colors">
                <td class="px-6 py-3 text-sm text-gray-900">{{ tournament.torneosId }}</td>
                <td class="px-6 py-3 text-sm text-gray-900">{{ tournament.nombre }}</td>
                <td class="px-6 py-3 text-sm text-gray-700">{{ tournament.tipoFormato }}</td>
                <td class="px-6 py-3 text-sm text-gray-700">{{ formatDate(tournament.fechaInicio) }}</td>
                <td class="px-6 py-3 text-center">
                  <router-link
                    :to="`/tournaments/${tournament.torneosId}/matches`"
                    class="text-sm text-blue-600 hover:text-blue-800 font-medium transition-colors"
                  >Ver partidos</router-link>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>

    <!-- Non-Admin: read-only tournament list -->
    <main v-else class="max-w-6xl mx-auto px-6 py-8">
      <section class="bg-white border border-gray-300 rounded-lg overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-300 bg-gray-50">
          <h3 class="text-xl font-semibold text-gray-900">Torneos</h3>
        </div>

        <div v-if="loading.fetch" class="px-6 py-8 text-center text-gray-600">
          Cargando torneos…
        </div>

        <div v-else-if="tournaments.length === 0" class="px-6 py-8 text-center text-gray-600">
          No hay torneos disponibles
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="bg-gray-50 border-b border-gray-300">
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Nombre</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Tipo de Formato</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Fecha de Inicio</th>
                <th class="px-6 py-3 text-center text-sm font-semibold text-gray-900">Partidos</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="tournament in tournaments" :key="tournament.torneosId"
                class="border-b border-gray-200 hover:bg-gray-50 transition-colors">
                <td class="px-6 py-3 text-sm text-gray-900">{{ tournament.nombre }}</td>
                <td class="px-6 py-3 text-sm text-gray-700">{{ tournament.tipoFormato }}</td>
                <td class="px-6 py-3 text-sm text-gray-700">{{ formatDate(tournament.fechaInicio) }}</td>
                <td class="px-6 py-3 text-center">
                  <router-link
                    :to="`/tournaments/${tournament.torneosId}/matches`"
                    class="text-sm text-blue-600 hover:text-blue-800 font-medium transition-colors"
                  >Ver partidos</router-link>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>
  </div>
</template>
