<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useMatches } from '../composables/useMatches'
import Navbar from '../components/Navbar.vue'
import TournamentBracketRoundRobin from '../components/TournamentBracketRoundRobin.vue'
import TournamentBracketElimination from '../components/TournamentBracketElimination.vue'
import TournamentBracketGroups from '../components/TournamentBracketGroups.vue'
import { tournamentAPI } from '../api/matches'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const torneoId = computed(() => Number(route.params.torneoId))
const isAdmin = computed(() => auth.session?.usuario.rol === 'ADMIN')

const {
  partidos,
  standings,
  loading,
  feedback,
  fetchAll,
} = useMatches()

const selectedTab = ref<'matches' | 'groups'>('matches')

const clasificadosInput = ref<number>(2)

const tournamentName = computed(() =>
  partidos.length > 0 ? partidos[0].torneo.nombre : `Torneo ${torneoId.value}`
)

/** Verdadero cuando el torneo es de formato round-robin o liga. */
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

const groupedStandings = computed(() => {
  const map = new Map<string, typeof standings>()
  standings.forEach((s) => {
    const g = s.grupo ?? 'A'
    if (!map.has(g)) map.set(g, [])
    map.get(g)!.push(s)
  })
  // convert to array of [group, rows]
  return Array.from(map.entries()).sort((a, b) => a[0].localeCompare(b[0]))
})

const allGroupMatchesPlayed = computed(() => {
  const groupMatches = partidos.filter((p) => p.grupo != null)
  if (groupMatches.length === 0) return false
  return groupMatches.every((p) => p.jugado === true)
})

async function advanceToElimination() {
  try {
    loading.fetch = true
    await tournamentAPI.advanceToElimination(torneoId.value, clasificadosInput.value)
    await fetchAll(torneoId.value)
    // switch to matches view after advancing
    selectedTab.value = 'matches'
  } catch (e) {
    // bubble to feedback in useMatches via fetchAll
  } finally {
    loading.fetch = false
  }
}

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
              </tr>
            </thead>
            <tbody>
              <tr v-for="partido in partidos" :key="partido.partidosId"
                class="border-b border-gray-200 hover:bg-gray-50 transition-colors">
                <td class="px-6 py-3 text-sm text-gray-500">{{ partido.partidosId }}</td>
                <td class="px-6 py-3 text-sm font-medium text-gray-900">
                  {{ partido.equipoLocal?.nombre ?? 'TBD' }}
                </td>
                <td class="px-6 py-3 text-center">
                  <span class="text-sm font-semibold text-gray-900">{{ partido.golesLocal }} - {{ partido.golesVisitante
                  }}</span>
                </td>
                <td class="px-6 py-3 text-sm text-gray-900">
                  {{ partido.equipoVisitante?.nombre ?? 'TBD' }}
                </td>
                <td class="px-6 py-3 text-sm text-gray-700">{{ formatDate(partido.fechaPartido) }}</td>
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

      <!-- Standings / Groups Section -->
      <section class="bg-white border border-gray-300 rounded-lg overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-300 bg-gray-50 flex items-center justify-between">
          <h3 class="text-xl font-semibold text-gray-900">Tabla de Posiciones</h3>
          <div class="flex items-center gap-3">
            <button @click="selectedTab = 'matches'"
              :class="selectedTab === 'matches' ? 'bg-blue-600 text-white' : 'bg-white text-gray-700'"
              class="px-3 py-1 text-sm rounded border">
              Partidos
            </button>
            <button v-if="isGroupFormat" @click="selectedTab = 'groups'"
              :class="selectedTab === 'groups' ? 'bg-blue-600 text-white' : 'bg-white text-gray-700'"
              class="px-3 py-1 text-sm rounded border">
              Grupos
            </button>
          </div>
        </div>

        <div v-if="loading.fetch" class="px-6 py-8 text-center text-gray-600">
          Cargando tabla…
        </div>

        <div v-else>
          <div v-if="selectedTab === 'matches'">
            <div v-if="standings.length === 0" class="px-6 py-8 text-center text-gray-600">
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
          </div>

          <div v-else-if="selectedTab === 'groups'">
            <div v-if="groupedStandings.length === 0" class="px-6 py-8 text-center text-gray-600">
              No hay grupos disponibles
            </div>

            <div v-else class="space-y-6 px-6 py-6">
              <div v-for="([group, rows]) in groupedStandings" :key="group" class="">
                <h4 class="text-lg font-semibold mb-2">Grupo {{ group }}</h4>
                <div class="overflow-x-auto">
                  <table class="w-full">
                    <thead>
                      <tr class="bg-gray-50 border-b border-gray-300">
                        <th class="px-4 py-2 text-left text-sm font-semibold text-gray-900">Pos</th>
                        <th class="px-4 py-2 text-left text-sm font-semibold text-gray-900">Equipo</th>
                        <th class="px-4 py-2 text-center text-sm font-semibold text-gray-900">PJ</th>
                        <th class="px-4 py-2 text-center text-sm font-semibold text-gray-900">V</th>
                        <th class="px-4 py-2 text-center text-sm font-semibold text-gray-900">E</th>
                        <th class="px-4 py-2 text-center text-sm font-semibold text-gray-900">D</th>
                        <th class="px-4 py-2 text-center text-sm font-semibold text-gray-900">GF</th>
                        <th class="px-4 py-2 text-center text-sm font-semibold text-gray-900">GC</th>
                        <th class="px-4 py-2 text-center text-sm font-semibold text-gray-900">Pts</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(row, idx) in rows" :key="row.tablaPosicionesId" class="border-b border-gray-200">
                        <td class="px-4 py-2 text-sm font-semibold">{{ idx + 1 }}</td>
                        <td class="px-4 py-2 text-sm">{{ row.equipo.nombre }}</td>
                        <td class="px-4 py-2 text-center text-sm">{{ row.partidosJugados }}</td>
                        <td class="px-4 py-2 text-center text-sm">{{ row.victorias ?? '-' }}</td>
                        <td class="px-4 py-2 text-center text-sm">{{ row.empates ?? '-' }}</td>
                        <td class="px-4 py-2 text-center text-sm">{{ row.derrotas ?? '-' }}</td>
                        <td class="px-4 py-2 text-center text-sm">{{ row.golesAFavor }}</td>
                        <td class="px-4 py-2 text-center text-sm">{{ row.golesEnContra }}</td>
                        <td class="px-4 py-2 text-center text-sm font-bold">{{ row.puntos }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <div v-if="isAdmin" class="flex items-center gap-3">
                <label class="flex items-center gap-2">
                  <span class="text-sm text-gray-700">Clasificados por grupo</span>
                  <input v-model.number="clasificadosInput" type="number" min="1"
                    class="ml-2 w-20 px-2 py-1 border rounded" />
                </label>
                <button :disabled="!allGroupMatchesPlayed" @click.prevent="advanceToElimination"
                  class="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 disabled:opacity-60">
                  Avanzar a eliminatoria
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Bracket del torneo -->
      <TournamentBracketGroups v-if="partidos.length > 0 && isGroupFormat && hasGroupMatches" :partidos="partidos" />
      <TournamentBracketRoundRobin v-else-if="partidos.length > 0 && isRoundRobin" :partidos="partidos" />
      <TournamentBracketElimination v-else-if="partidos.length > 0" :partidos="partidos" />
    </main>
  </div>
</template>
