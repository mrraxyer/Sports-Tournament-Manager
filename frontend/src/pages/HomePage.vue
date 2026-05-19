<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Navbar from '../components/Navbar.vue'
import api from '../services/api'

interface Torneo {
  torneosId: number
  nombre: string
  tipoFormato: string
  estado: string
  fechaInicio: string
}

const router = useRouter()
const auth = useAuthStore()

const torneos = reactive<Torneo[]>([])
const loading = ref(false)
const error = ref('')
const search = ref('')
const filterEstado = ref<'todos' | 'ACTIVO' | 'FINALIZADO'>('todos')

async function fetchTorneos() {
  loading.value = true
  error.value = ''
  try {
    const res = await api.get<{ data: Torneo[] }>('/torneos')
    torneos.length = 0
    torneos.push(...(res.data.data ?? []))
  } catch {
    error.value = 'No se pudieron cargar los torneos.'
  } finally {
    loading.value = false
  }
}

function filteredTorneos() {
  const isAdmin = auth.session?.usuario?.rol === 'ADMIN'
  return torneos.filter(t => {
    // Never show BORRADOR tournaments publicly unless the user is an admin
    if ((t.estado ?? '').toUpperCase() === 'BORRADOR' && !isAdmin) return false
    const matchesSearch = t.nombre.toLowerCase().includes(search.value.toLowerCase())
    const matchesEstado = filterEstado.value === 'todos' || t.estado?.toUpperCase() === filterEstado.value
    return matchesSearch && matchesEstado
  })
}

function formatDate(d: string) {
  if (!d) return '-'
  return new Intl.DateTimeFormat('es-ES', { dateStyle: 'medium' }).format(new Date(d))
}

function estadoBadgeClass(estado: string) {
  const e = estado?.toUpperCase()
  if (e === 'ACTIVO') return 'bg-green-100 text-green-700 border border-green-200'
  if (e === 'FINALIZADO') return 'bg-gray-100 text-gray-600 border border-gray-200'
  return 'bg-yellow-50 text-yellow-700 border border-yellow-200'
}

onMounted(() => {
  auth.hydrateSession()
  fetchTorneos()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />

    <main class="max-w-5xl mx-auto px-6 py-10 space-y-6">

      <!-- Header -->
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 class="text-2xl font-semibold text-gray-900">Torneos</h1>
          <p class="text-sm text-gray-500 mt-0.5">Consulta los resultados y brackets de cada torneo</p>
        </div>
      </div>

      <!-- Filters -->
      <div class="flex flex-wrap gap-3">
        <input v-model="search" type="text" placeholder="Buscar torneo…"
          class="px-3 py-2 text-sm border border-gray-300 rounded bg-white text-gray-900 focus:outline-2 focus:outline-blue-400 focus:outline-offset-1 min-w-56" />

        <div class="flex rounded border border-gray-300 overflow-hidden text-sm">
          <button v-for="opt in [
            { key: 'todos', label: 'Todos' },
            { key: 'ACTIVO', label: 'Activos' },
            { key: 'FINALIZADO', label: 'Finalizados' },
          ]" :key="opt.key" @click="filterEstado = opt.key as typeof filterEstado" class="px-3 py-2 transition-colors"
            :class="filterEstado === opt.key
              ? 'bg-blue-600 text-white border-blue-600'
              : 'bg-white text-gray-600 hover:bg-gray-50'">
            {{ opt.label }}
          </button>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="py-16 text-center text-gray-500 text-sm">
        Cargando torneos…
      </div>

      <!-- Error -->
      <div v-else-if="error" class="px-4 py-3 rounded border bg-red-50 border-red-200 text-red-700 text-sm">
        {{ error }}
      </div>

      <!-- Empty -->
      <div v-else-if="filteredTorneos().length === 0" class="py-16 text-center text-gray-500 text-sm">
        No hay torneos que coincidan con los filtros aplicados.
      </div>

      <!-- Tournament list -->
      <div v-else class="bg-white border border-gray-300 rounded overflow-hidden">
        <table class="w-full text-sm">
          <thead>
            <tr class="bg-gray-50 border-b border-gray-300">
              <th class="px-6 py-3 text-left font-semibold text-gray-900">Nombre</th>
              <th class="px-6 py-3 text-left font-semibold text-gray-900">Formato</th>
              <th class="px-6 py-3 text-left font-semibold text-gray-900">Fecha inicio</th>
              <th class="px-6 py-3 text-left font-semibold text-gray-900">Estado</th>
              <th class="px-6 py-3 text-right font-semibold text-gray-900"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in filteredTorneos()" :key="t.torneosId"
              class="border-b border-gray-200 hover:bg-gray-50 transition-colors cursor-pointer"
              @click="router.push(`/tournaments/${t.torneosId}/matches`)">
              <td class="px-6 py-3 font-medium text-gray-900">{{ t.nombre }}</td>
              <td class="px-6 py-3 text-gray-600">{{ t.tipoFormato ?? '-' }}</td>
              <td class="px-6 py-3 text-gray-600">{{ formatDate(t.fechaInicio) }}</td>
              <td class="px-6 py-3">
                <span class="px-2 py-0.5 text-xs rounded font-medium" :class="estadoBadgeClass(t.estado)">
                  {{ t.estado ?? '-' }}
                </span>
              </td>
              <td class="px-6 py-3 text-right">
                <button class="text-blue-600 hover:text-blue-800 text-sm font-medium transition-colors"
                  @click.stop="router.push(`/tournaments/${t.torneosId}/matches`)">
                  Ver detalles →
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

    </main>
  </div>
</template>
