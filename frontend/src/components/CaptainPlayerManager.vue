<script setup lang="ts">
import { onMounted, reactive, computed, ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import api from '../services/api'
import FormModal from './modals/FormModal.vue'

const auth = useAuthStore()

interface Jugador {
  jugadoresId: number
  nombre: string
  numeroCamiseta: number | null
}

interface Equipo {
  equiposId: number
  nombre: string
  torneo: {
    torneosId: number
    nombre: string
  }
  jugadores: Jugador[]
}

interface FormData {
  nombre: string
  numeroCamiseta: number | null
}

const equipos = reactive<Equipo[]>([])
const loading = reactive({ fetch: false, submit: false })
const feedback = reactive({
  type: '' as 'success' | 'error' | '',
  message: '',
})

const form = reactive<FormData>({
  nombre: '',
  numeroCamiseta: null,
})

const showModal = ref(false)
const selectedEquipoId = ref<number | null>(null)
const editingJugadorId = ref<number | null>(null)
const isCaptain = computed(() => auth.session?.usuario.rol?.toUpperCase() === 'TEAM_CAPTAIN')
const usuarioId = computed(() => auth.session?.usuario.usuariosId)

async function fetchEquipos() {
  if (!usuarioId.value) return

  loading.fetch = true
  try {
    const response = await api.get<{ data: Equipo[] }>(`/equipos/capitan/${usuarioId.value}`)
    equipos.length = 0
    equipos.push(...(response.data.data || []))
  } catch (error) {
    feedback.type = 'error'
    feedback.message = error instanceof Error ? error.message : 'Error al cargar equipos'
  } finally {
    loading.fetch = false
  }
}

async function submitForm() {
  feedback.type = ''
  feedback.message = ''

  if (!form.nombre.trim()) {
    feedback.type = 'error'
    feedback.message = 'Nombre del jugador es requerido'
    return
  }

  if (selectedEquipoId.value === null) {
    feedback.type = 'error'
    feedback.message = 'Debe seleccionar un equipo'
    return
  }

  loading.submit = true
  try {
    if (editingJugadorId.value) {
      const payload = {
        nombre: form.nombre.trim(),
        numeroCamiseta: form.numeroCamiseta,
      }
      await api.put(`/jugadores/${editingJugadorId.value}`, payload)
      feedback.type = 'success'
      feedback.message = 'Jugador actualizado exitosamente'
    } else {
      const payload = {
        nombre: form.nombre.trim(),
        numeroCamiseta: form.numeroCamiseta,
        equipo: {
          equiposId: selectedEquipoId.value,
        },
      }
      await api.post('/jugadores', payload)
      feedback.type = 'success'
      feedback.message = 'Jugador creado exitosamente'
    }

    form.nombre = ''
    form.numeroCamiseta = null
    showModal.value = false
    editingJugadorId.value = null

    await fetchEquipos()
  } catch (error) {
    feedback.type = 'error'
    feedback.message = error instanceof Error ? error.message : 'Error al guardar jugador'
  } finally {
    loading.submit = false
  }
}

function openCreateModal(equipoId: number) {
  selectedEquipoId.value = equipoId
  editingJugadorId.value = null
  form.nombre = ''
  form.numeroCamiseta = null
  showModal.value = true
}

function openEditModal(jugador: Jugador) {
  editingJugadorId.value = jugador.jugadoresId
  form.nombre = jugador.nombre
  form.numeroCamiseta = jugador.numeroCamiseta
  showModal.value = true
}

onMounted(() => {
  auth.hydrateSession()
  fetchEquipos()
})
</script>

<template>
  <div v-if="isCaptain" class="space-y-6">
    <!-- Header -->
    <section class="bg-white border border-gray-300 rounded-lg p-6">
      <header class="mb-4">
        <h2 class="text-2xl font-semibold text-gray-900">Mis Equipos</h2>
        <p class="text-gray-600 text-sm mt-1">Gestiona los jugadores de tus equipos</p>
      </header>
    </section>

    <!-- Alert -->
    <div v-if="feedback.message" :class="{
      'bg-green-50 border-green-300 text-green-700': feedback.type === 'success',
      'bg-red-50 border-red-300 text-red-700': feedback.type === 'error',
    }" class="px-4 py-3 rounded border text-sm">
      {{ feedback.message }}
    </div>

    <!-- Teams Section -->
    <div v-if="loading.fetch" class="px-6 py-8 text-center text-gray-600 bg-white rounded border border-gray-300">
      Cargando equipos…
    </div>

    <div v-else-if="equipos.length === 0"
      class="px-6 py-8 text-center text-gray-600 bg-white rounded border border-gray-300">
      No tienes equipos asignados aún
    </div>

    <div v-else class="space-y-6">
      <section v-for="equipo in equipos" :key="equipo.equiposId"
        class="bg-white border border-gray-300 rounded-lg overflow-hidden">
        <!-- Team Header -->
        <div class="px-6 py-4 border-b border-gray-300 bg-gray-50">
          <div class="flex justify-between items-center">
            <div>
              <h3 class="text-xl font-semibold text-gray-900">{{ equipo.nombre }}</h3>
              <p class="text-sm text-gray-600 mt-1">Torneo: {{ equipo.torneo?.nombre }}</p>
            </div>
            <button @click="openCreateModal(equipo.equiposId)"
              class="px-4 py-2 bg-blue-600 text-white border border-blue-600 rounded hover:bg-blue-700 cursor-pointer font-medium text-sm">
              Agregar Jugador
            </button>
          </div>
        </div>

        <!-- Players Table -->
        <div v-if="equipo.jugadores && equipo.jugadores.length > 0" class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="bg-gray-50 border-b border-gray-300">
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">ID</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Nombre</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Número de Camiseta</th>
                <th class="px-6 py-3 text-right text-sm font-semibold text-gray-900">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="jugador in equipo.jugadores" :key="jugador.jugadoresId"
                class="border-b border-gray-200 hover:bg-gray-50 transition-colors">
                <td class="px-6 py-3 text-sm text-gray-900">{{ jugador.jugadoresId }}</td>
                <td class="px-6 py-3 text-sm text-gray-900">{{ jugador.nombre }}</td>
                <td class="px-6 py-3 text-sm text-gray-700">
                  {{ jugador.numeroCamiseta ?? '-' }}
                </td>
                <td class="px-6 py-3 text-right text-sm font-medium">
                  <button @click="openEditModal(jugador)" class="text-blue-600 hover:text-blue-900 cursor-pointer">Editar</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Empty State -->
        <div v-else class="px-6 py-8 text-center text-gray-600">
          No hay jugadores en este equipo aún
        </div>
      </section>
    </div>

    <!-- Create / Edit Player Modal -->
    <FormModal v-model="showModal" :title="editingJugadorId ? 'Editar Jugador' : 'Agregar Jugador'" @submit="submitForm">
      <div class="space-y-4">
        <label class="block">
          <span class="text-sm font-medium text-gray-700">Nombre del Jugador</span>
          <input v-model="form.nombre" type="text" placeholder="Ej: Carlos López"
            class="mt-2 w-full px-3 py-2 border border-gray-300 rounded bg-white text-gray-900 focus:outline-2 focus:outline-blue-400 focus:outline-offset-1" />
        </label>

        <label class="block">
          <span class="text-sm font-medium text-gray-700">Número de Camiseta</span>
          <input v-model.number="form.numeroCamiseta" type="number" min="1" max="99" placeholder="Ej: 10"
            class="mt-2 w-full px-3 py-2 border border-gray-300 rounded bg-white text-gray-900 focus:outline-2 focus:outline-blue-400 focus:outline-offset-1" />
        </label>

        <div v-if="feedback.message" :class="{
          'bg-green-50 border-green-300 text-green-700': feedback.type === 'success',
          'bg-red-50 border-red-300 text-red-700': feedback.type === 'error',
        }" class="px-3 py-2 rounded border text-sm">
          {{ feedback.message }}
        </div>
      </div>
    </FormModal>
  </div>

  <!-- Non-Captain: Access Denied -->
  <div v-else class="px-6 py-8 text-center text-gray-600">
    Acceso restringido a capitanes.
  </div>
</template>
