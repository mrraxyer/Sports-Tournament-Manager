<script setup lang="ts">
import { watch, onMounted } from 'vue'
import { usePlayers } from '../composables/usePlayers'

// Props
const props = defineProps<{
  modelValue: boolean
  teamId: number | null
  teamName: string
}>()

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  close: []
}>()

// Composable
const {
  jugadores,
  loading,
  feedback,
  formFeedback,
  deleteFeedback,
  showAddForm,
  showDeleteConfirm,
  deletingJugador,
  form,
  formTitle,
  fetchJugadores,
  openAddForm,
  openEditForm,
  submitForm,
  openDeleteConfirm,
  confirmDelete,
} = usePlayers(() => props.teamId)

const closeModal = () => {
  emit('update:modelValue', false)
  emit('close')
  showAddForm.value = false
  showDeleteConfirm.value = false
}

onMounted(() => {
  if (props.teamId) {
    fetchJugadores()
  }
})

// Watchers
watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal && props.teamId) {
      feedback.type = ''
      feedback.message = ''
      fetchJugadores()
    }
  }
)

watch(
  () => props.teamId,
  () => {
    if (props.modelValue && props.teamId) {
      fetchJugadores()
    }
  }
)
</script>

<template>
  <!-- Main Player Roster Modal -->
  <div v-if="modelValue" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-lg w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col">
      <!-- Header -->
      <div class="flex justify-between items-center p-6 border-b border-gray-200">
        <h2 class="text-2xl font-bold text-gray-900">Gestionar Jugadores - {{ teamName }}</h2>
        <button @click="closeModal" class="text-gray-500 hover:text-gray-700 text-2xl font-bold">
          ✕
        </button>
      </div>

      <!-- Feedback Messages -->
      <div v-if="feedback.message" class="px-6 pt-4">
        <div :class="{
          'bg-green-100 border border-green-400 text-green-700': feedback.type === 'success',
          'bg-red-100 border border-red-400 text-red-700': feedback.type === 'error'
        }" class="px-4 py-3 rounded">
          {{ feedback.message }}
        </div>
      </div>

      <!-- Content -->
      <div class="flex-1 overflow-y-auto p-6">
        <!-- Add Player Button -->
        <button @click="openAddForm" :disabled="loading.fetch || loading.submit || loading.delete"
          class="mb-4 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-bold py-2 px-4 rounded transition">
          Agregar Jugador
        </button>

        <!-- Loading State -->
        <div v-if="loading.fetch" class="text-center py-8">
          <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p class="text-gray-600 mt-2">Cargando...</p>
        </div>

        <!-- Players Table -->
        <div v-else-if="jugadores.length > 0" class="overflow-x-auto">
          <table class="min-w-full border-collapse border border-gray-200 [&_th]:border [&_th]:border-gray-200 [&_th]:px-4 [&_th]:py-2 [&_th]:text-left [&_th]:font-semibold [&_th]:text-gray-900 [&_td]:border [&_td]:border-gray-200 [&_td]:px-4 [&_td]:py-3 [&_td]:text-gray-900">
            <thead class="bg-gray-100">
              <tr>
                <th>Nombre</th>
                <th>Número de Camiseta</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="jugador in jugadores" :key="jugador.jugadoresId" class="hover:bg-gray-50">
                <td>{{ jugador.nombre }}</td>
                <td class="text-center">{{ jugador.numeroCamiseta }}</td>
                <td class="text-center">
                  <button @click="openEditForm(jugador)" :disabled="loading.fetch || loading.submit || loading.delete"
                    class="bg-yellow-500 hover:bg-yellow-600 disabled:bg-gray-400 text-white font-bold py-1 px-3 rounded mr-2 transition text-sm">
                    Editar
                  </button>
                  <button @click="openDeleteConfirm(jugador)"
                    :disabled="loading.fetch || loading.submit || loading.delete"
                    class="bg-red-600 hover:bg-red-700 disabled:bg-gray-400 text-white font-bold py-1 px-3 rounded transition text-sm">
                    Eliminar
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Empty State -->
        <div v-else class="text-center py-8">
          <p class="text-gray-500 text-lg">No hay jugadores en este equipo</p>
        </div>
      </div>

      <!-- Footer -->
      <div class="border-t border-gray-200 p-6 flex justify-end">
        <button @click="closeModal"
          class="bg-gray-500 hover:bg-gray-600 text-white font-bold py-2 px-6 rounded transition">
          Cerrar
        </button>
      </div>
    </div>
  </div>

  <!-- Add/Edit Player Form Modal -->
  <div v-if="showAddForm" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-lg w-full max-w-md">
      <!-- Header -->
      <div class="flex justify-between items-center p-6 border-b border-gray-200">
        <h3 class="text-xl font-bold text-gray-900">{{ formTitle }}</h3>
        <button @click="showAddForm = false" class="text-gray-500 hover:text-gray-700 text-2xl font-bold">
          ✕
        </button>
      </div>

      <!-- Feedback Messages -->
      <div v-if="formFeedback.message" class="px-6 pt-4">
        <div :class="{
          'bg-green-100 border border-green-400 text-green-700': formFeedback.type === 'success',
          'bg-red-100 border border-red-400 text-red-700': formFeedback.type === 'error'
        }" class="px-4 py-3 rounded">
          {{ formFeedback.message }}
        </div>
      </div>

      <!-- Form -->
      <form @submit.prevent="submitForm" class="p-6 [&_input]:w-full [&_input]:px-3 [&_input]:py-2 [&_input]:border [&_input]:border-gray-300 [&_input]:rounded [&_input]:focus:outline-none [&_input]:focus:ring-2 [&_input]:focus:ring-blue-500 [&_input]:disabled:bg-gray-100">
        <!-- Nombre Input -->
        <div class="mb-4">
          <label for="nombre" class="block text-gray-700 font-bold mb-2">Nombre</label>
          <input id="nombre" v-model="form.nombre" type="text" placeholder="Ingrese el nombre del jugador"
            :disabled="loading.submit || loading.delete" />
        </div>

        <!-- Número de Camiseta Input -->
        <div class="mb-6">
          <label for="numeroCamiseta" class="block text-gray-700 font-bold mb-2">Número de Camiseta</label>
          <input id="numeroCamiseta" v-model.number="form.numeroCamiseta" type="number"
            placeholder="Ingrese el número de camiseta" :disabled="loading.submit || loading.delete" />
        </div>

        <!-- Buttons -->
        <div class="flex justify-end gap-3">
          <button type="button" @click="showAddForm = false" :disabled="loading.submit || loading.delete"
            class="bg-gray-500 hover:bg-gray-600 disabled:bg-gray-400 text-white font-bold py-2 px-6 rounded transition">
            Cancelar
          </button>
          <button type="submit" :disabled="loading.submit || loading.delete"
            class="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-bold py-2 px-6 rounded transition">
            {{ loading.submit ? 'Guardando...' : 'Guardar' }}
          </button>
        </div>
      </form>
    </div>
  </div>

  <!-- Delete Confirmation Modal -->
  <div v-if="showDeleteConfirm" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-lg w-full max-w-md">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200">
        <h3 class="text-xl font-bold text-gray-900">Confirmar Eliminación</h3>
      </div>

      <!-- Feedback Messages -->
      <div v-if="deleteFeedback.message" class="px-6 pt-4">
        <div :class="{
          'bg-green-100 border border-green-400 text-green-700': deleteFeedback.type === 'success',
          'bg-red-100 border border-red-400 text-red-700': deleteFeedback.type === 'error'
        }" class="px-4 py-3 rounded">
          {{ deleteFeedback.message }}
        </div>
      </div>

      <!-- Content -->
      <div class="p-6">
        <p class="text-gray-700 mb-2">
          ¿Estás seguro de que deseas eliminar al jugador
          <span class="font-bold">{{ deletingJugador?.nombre }}</span>?
        </p>
        <p class="text-gray-600 text-sm">Esta acción no se puede deshacer.</p>
      </div>

      <!-- Buttons -->
      <div class="border-t border-gray-200 p-6 flex justify-end gap-3">
        <button @click="showDeleteConfirm = false" :disabled="loading.delete"
          class="bg-gray-500 hover:bg-gray-600 disabled:bg-gray-400 text-white font-bold py-2 px-6 rounded transition">
          Cancelar
        </button>
        <button @click="confirmDelete" :disabled="loading.delete"
          class="bg-red-600 hover:bg-red-700 disabled:bg-gray-400 text-white font-bold py-2 px-6 rounded transition">
          {{ loading.delete ? 'Eliminando...' : 'Eliminar' }}
        </button>
      </div>
    </div>
  </div>
</template>
