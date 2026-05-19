<script setup lang="ts">
import { watch, onMounted, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useAdminMatches } from '../composables/useAdminMatches'
import TournamentBracketRoundRobin from './TournamentBracketRoundRobin.vue'
import TournamentBracketElimination from './TournamentBracketElimination.vue'
import FormModal from './modals/FormModal.vue'

const auth = useAuthStore()

/** Verdadero cuando el usuario autenticado tiene el rol ADMIN. */
const isAdmin = computed(() => auth.session?.usuario.rol === 'ADMIN')

/**
 * Verdadero cuando el torneo seleccionado es de formato round-robin o liga.
 * Determina qué componente de bracket se muestra.
 */
const isRoundRobin = computed(() => {
  const fmt = (partidos[0]?.torneo?.tipoFormato ?? '').toLowerCase()
  return fmt.includes('round') || fmt.includes('liga') || fmt.includes('robin')
})

const {
  tournaments,
  selectedTournamentId,
  partidos,
  draftScores,
  loading,
  savingId,
  feedback,
  fetchTournaments,
  fetchTournamentMatches,
  generateTournamentSchedule,
  saveMatchScore,
  showRescheduleModal,
  rescheduleDate,
  openRescheduleModal,
  rescheduleMatch,
  deleteMatch,
} = useAdminMatches()

/**
 * Formatea una cadena de fecha ISO en fecha media y hora corta legibles.
 * @param {string} date - Cadena de fecha ISO 8601 proveniente de la API.
 * @returns {string} Cadena de fecha formateada, o '-' si el valor está vacío.
 */
function formatDate(date: string): string {
  if (!date) return '-'
  return new Intl.DateTimeFormat('en-US', { dateStyle: 'medium', timeStyle: 'short' }).format(
    new Date(date)
  )
}

/** Recarga los partidos cuando cambia el torneo seleccionado. */
watch(selectedTournamentId, (id) => {
  if (id !== null) fetchTournamentMatches(id)
})

onMounted(() => {
  auth.hydrateSession()
  fetchTournaments()
})
</script>

<template>
  <div v-if="isAdmin" class="space-y-6">

    <!-- Selección de torneo y generación de calendario -->
    <section class="bg-white border border-gray-300 rounded p-4">
      <div class="flex flex-wrap items-end gap-4">
        <label class="block space-y-1.5 flex-1 min-w-48">
          <span class="text-sm font-medium text-gray-700">Torneo</span>
          <select v-model="selectedTournamentId"
            class="w-full px-3 py-2.5 border border-gray-300 rounded bg-white text-gray-900 focus:outline-2 focus:outline-blue-400 focus:outline-offset-1">
            <option :value="null" disabled>Selecciona un torneo</option>
            <option v-for="t in tournaments" :key="t.torneosId" :value="t.torneosId">
              {{ t.nombre }}
            </option>
          </select>
        </label>

        <button type="button" :disabled="selectedTournamentId === null || loading.generate"
          @click="generateTournamentSchedule(selectedTournamentId!)"
          class="px-4 py-2.5 bg-blue-600 text-white border border-blue-600 rounded hover:bg-blue-700 disabled:opacity-60 font-medium text-sm cursor-pointer">
          <span v-if="loading.generate">Generando…</span>
          <span v-else>Generar Calendario</span>
        </button>
      </div>
    </section>

    <!-- Alerta de retroalimentación -->
    <div v-if="feedback.message" :class="{
      'bg-green-50 border-green-300 text-green-700': feedback.type === 'success',
      'bg-red-50 border-red-300 text-red-700': feedback.type === 'error',
    }" class="px-4 py-3 rounded border text-sm">
      {{ feedback.message }}
    </div>

    <!-- Lista de partidos -->
    <section v-if="selectedTournamentId !== null" class="bg-white border border-gray-300 rounded overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-300 bg-gray-50">
        <h2 class="text-xl font-semibold text-gray-900">Partidos</h2>
      </div>

      <div v-if="loading.fetch" class="px-6 py-8 text-center text-gray-600">
        Cargando partidos…
      </div>

      <div v-else-if="partidos.length === 0" class="px-6 py-8 text-center text-gray-600">
        No hay partidos programados. Genera el calendario primero.
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="bg-gray-50 border-b border-gray-300">
              <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Fecha</th>
              <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Equipo Local</th>
              <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Equipo Visitante</th>
              <th class="px-6 py-3 text-center text-sm font-semibold text-gray-900">Goles Local</th>
              <th class="px-6 py-3 text-center text-sm font-semibold text-gray-900">Goles Visitante</th>
              <th class="px-6 py-3 text-center text-sm font-semibold text-gray-900">Acción</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="partido in partidos" :key="partido.partidosId" class="border-b border-gray-200 transition-colors"
              :class="partido.jugado ? 'bg-green-50' : 'hover:bg-gray-50'">
              <td class="px-6 py-3 text-sm text-gray-700">{{ formatDate(partido.fechaPartido) }}</td>
              <td class="px-6 py-3 text-sm font-medium text-gray-900">
                {{ partido.equipoLocal.nombre }}
              </td>
              <td class="px-6 py-3 text-sm text-gray-900">
                {{ partido.equipoVisitante.nombre }}
              </td>

              <!-- Goles Local -->
              <td class="px-6 py-3 text-center">
                <span v-if="partido.jugado"
                  class="inline-block w-16 px-2 py-1 bg-green-100 border border-green-200 rounded text-center text-sm font-semibold text-green-800">
                  {{ partido.golesLocal }}
                </span>
                <input v-else-if="draftScores[partido.partidosId]"
                  v-model.number="draftScores[partido.partidosId].golesLocal" type="number" min="0"
                  class="w-16 px-2 py-1 border border-gray-300 rounded text-center text-sm focus:outline-2 focus:outline-blue-400 focus:outline-offset-1" />
              </td>

              <!-- Goles Visitante -->
              <td class="px-6 py-3 text-center">
                <span v-if="partido.jugado"
                  class="inline-block w-16 px-2 py-1 bg-green-100 border border-green-200 rounded text-center text-sm font-semibold text-green-800">
                  {{ partido.golesVisitante }}
                </span>
                <input v-else-if="draftScores[partido.partidosId]"
                  v-model.number="draftScores[partido.partidosId].golesVisitante" type="number" min="0"
                  class="w-16 px-2 py-1 border border-gray-300 rounded text-center text-sm focus:outline-2 focus:outline-blue-400 focus:outline-offset-1" />
              </td>

              <!-- Acción -->
              <td class="px-6 py-3 text-center">
                <span v-if="partido.jugado" class="text-sm font-medium text-green-600">
                  ✓ Guardado
                </span>
                <div v-else class="flex gap-2 justify-center">
                  <button type="button" :disabled="savingId === partido.partidosId" @click="draftScores[partido.partidosId] && saveMatchScore(
                    partido.partidosId,
                    draftScores[partido.partidosId].golesLocal,
                    draftScores[partido.partidosId].golesVisitante
                  )"
                    class="px-3 py-1.5 bg-blue-600 text-white text-sm font-medium rounded hover:bg-blue-700 disabled:opacity-60 cursor-pointer transition">
                    <span v-if="savingId === partido.partidosId">Guardando…</span>
                    <span v-else>Guardar</span>
                  </button>

                  <button type="button" @click="openRescheduleModal(partido.partidosId, partido.fechaPartido)"
                    class="px-3 py-1.5 bg-amber-500 text-white text-sm font-medium rounded hover:bg-amber-600 cursor-pointer">
                    Reprogramar
                  </button>
                  <button type="button" @click="deleteMatch(partido.partidosId)"
                    class="px-3 py-1.5 bg-red-600 text-white text-sm font-medium rounded hover:bg-red-700 cursor-pointer">
                    Eliminar
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- Bracket del torneo seleccionado -->
    <TournamentBracketRoundRobin v-if="selectedTournamentId !== null && partidos.length > 0 && isRoundRobin"
      :partidos="partidos" />
    <TournamentBracketElimination v-else-if="selectedTournamentId !== null && partidos.length > 0"
      :partidos="partidos" />

    <FormModal v-model="showRescheduleModal" title="Reprogramar Partido" @submit="rescheduleMatch">
      <div class="space-y-4">
        <label class="block">
          <span class="text-sm font-medium text-gray-700">Nueva fecha y hora</span>
          <input type="datetime-local" v-model="rescheduleDate" class="mt-2 w-full px-3 py-2 border rounded" />
        </label>
      </div>
    </FormModal>
  </div>

  <!-- Restricción para no administradores -->
  <div v-else class="px-6 py-8 text-center text-gray-600">
    Acceso restringido a administradores.
  </div>
</template>
