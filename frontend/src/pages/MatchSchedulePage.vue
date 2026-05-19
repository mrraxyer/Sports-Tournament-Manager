<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useMatches } from '../composables/useMatches'
import Navbar from '../components/Navbar.vue'
import TournamentBracketRoundRobin from '../components/TournamentBracketRoundRobin.vue'
import TournamentBracketElimination from '../components/TournamentBracketElimination.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const torneoId = computed(() => Number(route.params.torneoId))
const isAdmin = computed(() => auth.session?.usuario.rol === 'ADMIN')

const {
  partidos,
  standings,
  loading,
  savingId,
  feedback,
  draftScores,
  fetchAll,
  saveResult,
} = useMatches()

const tournamentName = computed(() =>
  partidos.length > 0 ? partidos[0].torneo.nombre : `Torneo ${torneoId.value}`
)

/** Verdadero cuando el torneo es de formato round-robin o liga. */
const isRoundRobin = computed(() => {
  const fmt = (partidos[0]?.torneo?.tipoFormato ?? '').toLowerCase()
  return fmt.includes('round') || fmt.includes('liga') || fmt.includes('robin')
})

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Intl.DateTimeFormat('es-ES', { dateStyle: 'medium', timeStyle: 'short' }).format(
    new Date(date)
  )
}

onMounted(() => {
  auth.hydrateSession()
  fetchAll(torneoId.value)
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />

    <main class="max-w-6xl mx-auto px-6 py-8 space-y-8">
      <button type="button" @click="router.back()"
        class="flex items-center gap-1 text-sm text-gray-600 hover:text-gray-900 transition-colors">
        ← Volver
      </button>

      <!-- Matches Section -->
      <section class="bg-white border border-gray-300 rounded-lg overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-300 bg-gray-50">
          <h2 class="text-xl font-semibold text-gray-900">Partidos — {{ tournamentName }}</h2>
        </div>

        <div v-if="loading.fetch" class="px-6 py-8 text-center text-gray-600">
          Cargando partidos…
        </div>

        <div v-else-if="partidos.length === 0" class="px-6 py-8 text-center text-gray-600">
          No hay partidos programados para este torneo
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="bg-gray-50 border-b border-gray-300">
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">#</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Local</th>
                <th class="px-6 py-3 text-center text-sm font-semibold text-gray-900">Resultado</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Visitante</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Fecha</th>
                <th v-if="isAdmin" class="px-6 py-3 text-center text-sm font-semibold text-gray-900">
                  Acción
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="partido in partidos" :key="partido.partidosId"
                class="border-b border-gray-200 hover:bg-gray-50 transition-colors">
                <td class="px-6 py-3 text-sm text-gray-500">{{ partido.partidosId }}</td>
                <td class="px-6 py-3 text-sm font-medium text-gray-900">
                  {{ partido.equipoLocal.nombre }}
                </td>
                <td class="px-6 py-3 text-center">
                  <template v-if="isAdmin && draftScores[partido.partidosId]">
                    <div class="flex items-center justify-center gap-2">
                      <input v-model.number="draftScores[partido.partidosId].golesLocal" type="number" min="0"
                        class="w-14 px-2 py-1 border border-gray-300 rounded text-center text-sm focus:outline-2 focus:outline-blue-400 focus:outline-offset-1" />
                      <span class="text-gray-500 font-semibold">-</span>
                      <input v-model.number="draftScores[partido.partidosId].golesVisitante" type="number" min="0"
                        class="w-14 px-2 py-1 border border-gray-300 rounded text-center text-sm focus:outline-2 focus:outline-blue-400 focus:outline-offset-1" />
                    </div>
                  </template>
                  <template v-else>
                    <span class="text-sm font-semibold text-gray-900">
                      {{ partido.golesLocal }} - {{ partido.golesVisitante }}
                    </span>
                  </template>
                </td>
                <td class="px-6 py-3 text-sm text-gray-900">
                  {{ partido.equipoVisitante.nombre }}
                </td>
                <td class="px-6 py-3 text-sm text-gray-700">{{ formatDate(partido.fechaPartido) }}</td>
                <td v-if="isAdmin" class="px-6 py-3 text-center">
                  <button type="button" :disabled="savingId === partido.partidosId"
                    @click="saveResult(partido.partidosId, torneoId)"
                    class="px-3 py-1.5 bg-blue-600 text-white text-sm font-medium rounded hover:bg-blue-700 disabled:opacity-60 cursor-pointer transition">
                    <span v-if="savingId === partido.partidosId">Guardando…</span>
                    <span v-else>Guardar</span>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="feedback.message" :class="{
          'bg-green-50 border-green-300 text-green-700': feedback.type === 'success',
          'bg-red-50 border-red-300 text-red-700': feedback.type === 'error',
        }" class="mx-6 my-4 px-4 py-3 rounded border text-sm">
          {{ feedback.message }}
        </div>
      </section>

      <!-- Standings Section -->
      <section class="bg-white border border-gray-300 rounded-lg overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-300 bg-gray-50">
          <h3 class="text-xl font-semibold text-gray-900">Tabla de Posiciones</h3>
        </div>

        <div v-if="loading.fetch" class="px-6 py-8 text-center text-gray-600">
          Cargando tabla…
        </div>

        <div v-else-if="standings.length === 0" class="px-6 py-8 text-center text-gray-600">
          No hay tabla de posiciones disponible
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="bg-gray-50 border-b border-gray-300">
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Pos</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Equipo</th>
                <th class="px-6 py-3 text-center text-sm font-semibold text-gray-900">PJ</th>
                <th class="px-6 py-3 text-center text-sm font-semibold text-gray-900">GF</th>
                <th class="px-6 py-3 text-center text-sm font-semibold text-gray-900">GC</th>
                <th class="px-6 py-3 text-center text-sm font-semibold text-gray-900">Pts</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(standing, index) in standings" :key="standing.tablaPosicionesId"
                class="border-b border-gray-200 hover:bg-gray-50 transition-colors">
                <td class="px-6 py-3 text-sm font-semibold text-gray-900">{{ index + 1 }}</td>
                <td class="px-6 py-3 text-sm font-medium text-gray-900">
                  {{ standing.equipo.nombre }}
                </td>
                <td class="px-6 py-3 text-center text-sm text-gray-700">
                  {{ standing.partidosJugados }}
                </td>
                <td class="px-6 py-3 text-center text-sm text-gray-700">
                  {{ standing.golesAFavor }}
                </td>
                <td class="px-6 py-3 text-center text-sm text-gray-700">
                  {{ standing.golesEnContra }}
                </td>
                <td class="px-6 py-3 text-center text-sm font-bold text-gray-900">
                  {{ standing.puntos }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- Bracket del torneo -->
      <TournamentBracketRoundRobin
        v-if="partidos.length > 0 && isRoundRobin"
        :partidos="partidos"
      />
      <TournamentBracketElimination
        v-else-if="partidos.length > 0"
        :partidos="partidos"
      />
    </main>
  </div>
</template>
