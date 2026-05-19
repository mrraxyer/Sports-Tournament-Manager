<script setup lang="ts">
import { watch, onMounted, computed, ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useAdminMatches } from '../composables/useAdminMatches'
import TournamentBracketRoundRobin from './TournamentBracketRoundRobin.vue'
import TournamentBracketElimination from './TournamentBracketElimination.vue'
import TournamentBracketGroups from './TournamentBracketGroups.vue'
import FormModal from './modals/FormModal.vue'

const auth = useAuthStore()

const isAdmin = computed(() => auth.session?.usuario.rol?.toUpperCase() === 'ADMIN')

const isRoundRobin = computed(() => {
  const fmt = (partidos[0]?.torneo?.tipoFormato ?? '').toLowerCase()
  return fmt.includes('round') || fmt.includes('liga') || fmt.includes('robin')
})

const isGroupFormat = computed(() => {
  const fmt = (partidos[0]?.torneo?.tipoFormato ?? '').toLowerCase()
  return fmt.includes('grupo') || fmt.includes('grupos')
})

const hasGroupMatches = computed(() => {
  return partidos.some(p => p.grupo != null)
})

const eliminationMatches = computed(() => partidos.filter(p => p.bracketIndex != null))
const regularMatches = computed(() => partidos.filter(p => p.bracketIndex == null))
const hasEliminationMatches = computed(() => eliminationMatches.value.length > 0)

const {
  tournaments,
  selectedTournamentId,
  partidos,
  loading,
  feedback,
  fetchTournaments,
  fetchTournamentMatches,
  generateTournamentSchedule,
  generateEliminationPhase,
  saveMatchScore,
  showRescheduleModal,
  rescheduleDate,
  openRescheduleModal,
  rescheduleMatch,
  deleteMatch,
} = useAdminMatches()

const clasificadosInput = ref(2)
const showAdvanceModal = ref(false)

const showResultModal = ref(false)
const resultMatchId = ref<number | null>(null)
const resultLocal = ref(0)
const resultVisitante = ref(0)

function openResultModal(partidoId: number, local: number, visitante: number) {
  resultMatchId.value = partidoId
  resultLocal.value = local || 0
  resultVisitante.value = visitante || 0
  showResultModal.value = true
}

async function submitResult() {
  if (resultMatchId.value === null) return
  await saveMatchScore(resultMatchId.value, resultLocal.value, resultVisitante.value)
  showResultModal.value = false
}

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

        <!-- Botón de avanzar a eliminatoria: solo si es grupos o RR, hay partidos y aún no existe bracket -->
        <button v-if="(isGroupFormat || isRoundRobin) && partidos.length > 0 && !hasEliminationMatches"
          type="button" :disabled="loading.generate"
          @click="showAdvanceModal = true"
          class="px-4 py-2.5 bg-purple-600 text-white border border-purple-600 rounded hover:bg-purple-700 disabled:opacity-60 font-medium text-sm cursor-pointer">
          Avanzar a Eliminatoria
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
              <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Fase</th>
              <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Equipo Local</th>
              <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Equipo Visitante</th>
              <th class="px-6 py-3 text-center text-sm font-semibold text-gray-900">Resultado</th>
              <th class="px-6 py-3 text-center text-sm font-semibold text-gray-900">Acción</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="partido in partidos" :key="partido.partidosId" class="border-b border-gray-200 transition-colors"
              :class="partido.jugado ? 'bg-green-50' : 'hover:bg-gray-50'">
              <td class="px-6 py-3 text-sm text-gray-700">{{ formatDate(partido.fechaPartido) }}</td>
              <td class="px-6 py-3 text-sm text-gray-700 font-medium">{{ partido.fase ?? '-' }}</td>
              <td class="px-6 py-3 text-sm font-medium text-gray-900">
                {{ partido.equipoLocal?.nombre ?? 'TBD' }}
              </td>
              <td class="px-6 py-3 text-sm text-gray-900">
                {{ partido.equipoVisitante?.nombre ?? 'TBD' }}
              </td>

              <!-- Resultado -->
              <td class="px-6 py-3 text-center text-sm font-semibold text-gray-900">
                <span v-if="partido.jugado" class="px-2 py-1 bg-green-100 text-green-800 rounded">
                  {{ partido.golesLocal }} - {{ partido.golesVisitante }}
                </span>
                <span v-else class="text-gray-400">Sin jugar</span>
              </td>

              <!-- Acción -->
              <td class="px-6 py-3 text-center">
                <div class="flex gap-2 justify-center flex-wrap">
                  <button type="button"
                    @click="openResultModal(partido.partidosId, partido.golesLocal, partido.golesVisitante)"
                    class="px-3 py-1.5 bg-green-600 text-white text-sm font-medium rounded hover:bg-green-700 cursor-pointer">
                    {{ partido.jugado ? 'Editar Resultado' : 'Registrar Resultado' }}
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

    <!-- Fase regular: grupos o round robin -->
    <TournamentBracketGroups
      v-if="selectedTournamentId !== null && regularMatches.length > 0 && isGroupFormat && hasGroupMatches"
      :partidos="regularMatches" />
    <TournamentBracketRoundRobin
      v-if="selectedTournamentId !== null && regularMatches.length > 0 && isRoundRobin"
      :partidos="regularMatches" />

    <!-- Bracket de eliminatoria: siempre que haya partidos con bracketIndex -->
    <TournamentBracketElimination
      v-if="selectedTournamentId !== null && eliminationMatches.length > 0"
      :partidos="eliminationMatches" />

    <!-- Bracket para torneos de eliminación directa pura (sin fase previa) -->
    <TournamentBracketElimination
      v-if="selectedTournamentId !== null && partidos.length > 0 && !isGroupFormat && !isRoundRobin"
      :partidos="partidos" />

    <FormModal v-model="showRescheduleModal" title="Reprogramar Partido" @submit="rescheduleMatch">
      <div class="space-y-4">
        <label class="block">
          <span class="text-sm font-medium text-gray-700">Nueva fecha y hora</span>
          <input type="datetime-local" v-model="rescheduleDate" class="mt-2 w-full px-3 py-2 border rounded" />
        </label>
      </div>
    </FormModal>

    <FormModal v-model="showResultModal" title="Registrar Resultado" @submit="submitResult">
      <div class="space-y-4 flex gap-4 items-center">
        <label class="block flex-1">
          <span class="text-sm font-medium text-gray-700">Goles Local</span>
          <input type="number" min="0" v-model="resultLocal" class="mt-2 w-full px-3 py-2 border rounded text-center" />
        </label>
        <span class="text-xl font-bold pt-6">-</span>
        <label class="block flex-1">
          <span class="text-sm font-medium text-gray-700">Goles Visitante</span>
          <input type="number" min="0" v-model="resultVisitante"
            class="mt-2 w-full px-3 py-2 border rounded text-center" />
        </label>
      </div>
    </FormModal>

    <!-- Modal de avanzar a eliminatoria -->
    <FormModal v-model="showAdvanceModal" title="Avanzar a Eliminatoria"
      @submit="generateEliminationPhase(selectedTournamentId!, clasificadosInput); showAdvanceModal = false">
      <div class="space-y-4">
        <p class="text-sm text-gray-600">
          <template v-if="isGroupFormat">
            Selecciona cuántos equipos clasifican de cada grupo.
          </template>
          <template v-else>
            Selecciona el total de equipos que clasifican a la eliminatoria (ej. 4 para semifinales, 8 para cuartos).
          </template>
        </p>
        <label class="block">
          <span class="text-sm font-medium text-gray-700">
            {{ isGroupFormat ? 'Clasificados por grupo' : 'Total de clasificados' }}
          </span>
          <input type="number" min="2" v-model.number="clasificadosInput"
            class="mt-2 w-full px-3 py-2 border rounded text-center" />
        </label>
      </div>
    </FormModal>
  </div>

  <!-- Restricción para no administradores -->
  <div v-else class="px-6 py-8 text-center text-gray-600">
    Acceso restringido a administradores.
  </div>
</template>
