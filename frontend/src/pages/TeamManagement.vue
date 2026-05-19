<script setup lang="ts">
import { onMounted, computed, ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useTeams } from '../composables/useTeams'
import Navbar from '../components/Navbar.vue'
import PlayerRoster from '../components/PlayerRoster.vue'
import ConfirmModal from '../components/modals/ConfirmModal.vue'

const auth = useAuthStore()
const {
  equipos,
  torneos,
  usuarios,
  form,
  loading,
  feedback,
  deletingEquipo,
  showDeleteConfirm,
  fetchAll,
  submitForm,
  openDeleteConfirm,
  confirmDelete,
  selectedTorneoId,
  fetchTeamsByTorneo,
} = useTeams()

const showPlayerRoster = ref(false)
const selectedTeamId = ref<number | null>(null)
const selectedTeamName = ref<string>('')

const isAdmin = computed(() => auth.session?.usuario.rol === 'ADMIN')

function openPlayerRoster(equipo: any) {
  selectedTeamId.value = equipo.equiposId
  selectedTeamName.value = equipo.nombre
  showPlayerRoster.value = true
}

function closePlayerRoster() {
  showPlayerRoster.value = false
  selectedTeamId.value = null
  selectedTeamName.value = ''
}

onMounted(() => {
  auth.hydrateSession()
  if (isAdmin.value) {
    fetchAll()
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />

    <main v-if="isAdmin" class="max-w-6xl mx-auto px-6 py-8">
      <!-- Create Team Form -->
      <section class="bg-white border border-gray-300 rounded-lg p-6 mb-8">
        <header class="mb-6">
          <h2 class="text-2xl font-semibold text-gray-900">Crear Nuevo Equipo</h2>
        </header>

        <form class="space-y-4" @submit.prevent="submitForm">
          <div
            class="grid grid-cols-1 md:grid-cols-3 gap-4 [&>label]:block [&>label]:space-y-1.5 [&_input]:w-full [&_input]:px-3 [&_input]:py-2.5 [&_input]:border [&_input]:border-gray-300 [&_input]:rounded [&_input]:bg-white [&_input]:text-gray-900 [&_input]:focus:outline-2 [&_input]:focus:outline-blue-400 [&_input]:focus:outline-offset-1 [&_select]:w-full [&_select]:px-3 [&_select]:py-2.5 [&_select]:border [&_select]:border-gray-300 [&_select]:rounded [&_select]:bg-white [&_select]:text-gray-900 [&_select]:focus:outline-2 [&_select]:focus:outline-blue-400 [&_select]:focus:outline-offset-1">
            <label for="team-name">
              <span class="text-sm font-medium text-gray-700">Nombre</span>
              <input id="team-name" v-model="form.nombre" type="text" placeholder="Ej: Equipo Azul" required />
            </label>

            <label for="team-torneo">
              <span class="text-sm font-medium text-gray-700">Torneo</span>
              <select id="team-torneo" v-model.number="form.torneoId" required>
                <option value="0" disabled>Selecciona un torneo</option>
                <option v-for="torneo in torneos" :key="torneo.torneosId" :value="torneo.torneosId">
                  {{ torneo.nombre }}
                </option>
              </select>
            </label>

            <label for="team-capitan">
              <span class="text-sm font-medium text-gray-700">Capitán</span>
              <select id="team-capitan" v-model.number="form.capitanId" required>
                <option value="0" disabled>Selecciona un capitán</option>
                <option v-for="usuario in usuarios" :key="usuario.usuariosId" :value="usuario.usuariosId">
                  {{ usuario.nombre }}
                </option>
              </select>
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
            <span v-else>Crear Equipo</span>
          </button>
        </form>
      </section>

      <!-- Teams Table -->
      <section class="bg-white border border-gray-300 rounded-lg overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-300 bg-gray-50 flex items-center justify-between">
          <h3 class="text-xl font-semibold text-gray-900">Equipos Existentes</h3>
          <div class="flex items-center gap-3">
            <label class="text-sm text-gray-700">Filtrar por torneo</label>
            <select v-model.number="selectedTorneoId" @change="fetchTeamsByTorneo(selectedTorneoId || null)"
              class="px-3 py-2 border border-gray-300 rounded bg-white">
              <option :value="null">Todos</option>
              <option v-for="torneo in torneos" :key="torneo.torneosId" :value="torneo.torneosId">{{ torneo.nombre }}
              </option>
            </select>
          </div>
        </div>

        <div v-if="loading.fetch" class="px-6 py-8 text-center text-gray-600">
          Cargando equipos…
        </div>

        <div v-else-if="equipos.length === 0" class="px-6 py-8 text-center text-gray-600">
          No hay equipos creados aún
        </div>

        <div v-else class="overflow-x-auto">
          <table
            class="w-full [&_th]:px-6 [&_th]:py-3 [&_th]:text-left [&_th]:text-sm [&_th]:font-semibold [&_th]:text-gray-900 [&_td]:px-6 [&_td]:py-3 [&_td]:text-sm">
            <thead>
              <tr class="bg-gray-50 border-b border-gray-300">
                <th>ID</th>
                <th>Nombre</th>
                <th>Capitán</th>
                <th>Torneo</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="equipo in equipos" :key="equipo.equiposId"
                class="border-b border-gray-200 hover:bg-gray-50 transition-colors">
                <td class="text-gray-900">{{ equipo.equiposId }}</td>
                <td class="text-gray-900">{{ equipo.nombre }}</td>
                <td class="text-gray-700">{{ equipo.capitan?.nombre || '-' }}</td>
                <td class="text-gray-700">{{ equipo.torneo?.nombre || '-' }}</td>
                <td class="flex gap-2">
                  <button @click="openPlayerRoster(equipo)"
                    class="px-3 py-1.5 bg-blue-500 text-white border border-blue-500 rounded hover:bg-blue-600 hover:border-blue-600 cursor-pointer font-medium text-xs">
                    Gestionar Jugadores
                  </button>
                  <button @click="openDeleteConfirm(equipo)"
                    class="px-3 py-1.5 bg-red-600 text-white border border-red-600 rounded hover:bg-red-700 hover:border-red-700 cursor-pointer font-medium text-xs">
                    Eliminar
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>

    <!-- Non-Admin Message -->
    <main v-else class="max-w-2xl mx-auto px-6 py-8">
      <section class="bg-white border border-gray-300 rounded-lg p-6">
        <div class="text-center py-8">
          <h2 class="text-xl font-semibold text-gray-900 mb-2">Acceso Denegado</h2>
          <p class="text-gray-600">Esta sección es solo para administradores. Tu rol actual es: <strong>{{
            auth.session?.usuario.rol || 'Sin rol' }}</strong></p>
        </div>
      </section>
    </main>

    <!-- Player Roster Modal -->
    <PlayerRoster v-if="selectedTeamId" v-model="showPlayerRoster" :team-id="selectedTeamId"
      :team-name="selectedTeamName" @close="closePlayerRoster" />

    <!-- Delete Confirmation Modal -->
    <ConfirmModal v-model="showDeleteConfirm" title="Confirmar Eliminación"
      :message="`¿Eliminar el equipo ${deletingEquipo?.nombre}?`"
      detail="Se eliminarán también todos sus jugadores y partidos asociados." confirm-label="Eliminar"
      :loading="loading.delete" @confirm="confirmDelete" />
  </div>
</template>
