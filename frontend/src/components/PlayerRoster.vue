<script setup lang="ts">
import { watch, onMounted } from 'vue'
import { usePlayers } from '../composables/usePlayers'
import ConfirmModal from './modals/ConfirmModal.vue'
import FormModal from './modals/FormModal.vue'
import ContentModal from './modals/ContentModal.vue'

const props = defineProps<{
  modelValue: boolean
  teamId: number | null
  teamName: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  close: []
}>()

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

// Observadores
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
  <ContentModal
    :model-value="modelValue"
    :title="`Gestionar Jugadores - ${teamName}`"
    @update:model-value="$emit('update:modelValue', $event)"
    @close="closeModal"
  >
    <template #actions>
      <button
        @click="openAddForm"
        :disabled="loading.fetch || loading.submit || loading.delete"
        class="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-bold py-2 px-4 rounded transition text-sm"
      >
        Agregar Jugador
      </button>
    </template>

    <div v-if="feedback.message" class="mb-4">
      <div
        :class="{
          'bg-green-100 border border-green-400 text-green-700': feedback.type === 'success',
          'bg-red-100 border border-red-400 text-red-700': feedback.type === 'error',
        }"
        class="px-4 py-3 rounded"
      >
        {{ feedback.message }}
      </div>
    </div>

    <div v-if="deleteFeedback.message" class="mb-4">
      <div
        :class="{
          'bg-green-100 border border-green-400 text-green-700': deleteFeedback.type === 'success',
          'bg-red-100 border border-red-400 text-red-700': deleteFeedback.type === 'error',
        }"
        class="px-4 py-3 rounded"
      >
        {{ deleteFeedback.message }}
      </div>
    </div>

    <div v-if="loading.fetch" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      <p class="text-gray-600 mt-2">Cargando...</p>
    </div>

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
              <button
                @click="openEditForm(jugador)"
                :disabled="loading.fetch || loading.submit || loading.delete"
                class="bg-yellow-500 hover:bg-yellow-600 disabled:bg-gray-400 text-white font-bold py-1 px-3 rounded mr-2 transition text-sm"
              >
                Editar
              </button>
              <button
                @click="openDeleteConfirm(jugador)"
                :disabled="loading.fetch || loading.submit || loading.delete"
                class="bg-red-600 hover:bg-red-700 disabled:bg-gray-400 text-white font-bold py-1 px-3 rounded transition text-sm"
              >
                Eliminar
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="text-center py-8">
      <p class="text-gray-500 text-lg">No hay jugadores en este equipo</p>
    </div>
  </ContentModal>

  <FormModal
    v-model="showAddForm"
    :title="formTitle"
    :loading="loading.submit"
    @submit="submitForm"
  >
    <template #feedback>
      <div
        v-if="formFeedback.message"
        :class="{
          'bg-green-100 border border-green-400 text-green-700': formFeedback.type === 'success',
          'bg-red-100 border border-red-400 text-red-700': formFeedback.type === 'error',
        }"
        class="px-4 py-3 rounded mb-4"
      >
        {{ formFeedback.message }}
      </div>
    </template>
    <div class="mb-4">
      <label for="nombre" class="block text-gray-700 font-bold mb-2">Nombre</label>
      <input
        id="nombre"
        v-model="form.nombre"
        type="text"
        placeholder="Ingrese el nombre del jugador"
        :disabled="loading.submit || loading.delete"
        class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
      />
    </div>
    <div>
      <label for="numeroCamiseta" class="block text-gray-700 font-bold mb-2">Número de Camiseta</label>
      <input
        id="numeroCamiseta"
        v-model.number="form.numeroCamiseta"
        type="number"
        placeholder="Ingrese el número de camiseta"
        :disabled="loading.submit || loading.delete"
        class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
      />
    </div>
  </FormModal>

  <ConfirmModal
    v-model="showDeleteConfirm"
    title="Confirmar Eliminación"
    :message="`¿Estás seguro de que deseas eliminar al jugador ${deletingJugador?.nombre}?`"
    detail="Esta acción no se puede deshacer."
    confirm-label="Eliminar"
    :loading="loading.delete"
    @confirm="confirmDelete"
  />
</template>
