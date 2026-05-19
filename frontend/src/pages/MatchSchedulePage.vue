<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useMatches } from '../composables/useMatches'
import { useTournamentPdf } from '../composables/useTournamentPdf'
import Navbar from '../components/Navbar.vue'
import TournamentBracketRoundRobin from '../components/TournamentBracketRoundRobin.vue'
import TournamentBracketElimination from '../components/TournamentBracketElimination.vue'
import TournamentBracketGroups from '../components/TournamentBracketGroups.vue'
import api from '../services/api'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const torneoId = computed(() => Number(route.params.torneoId))

const { partidos, standings, loading, fetchAll } = useMatches()
const { downloadPdf } = useTournamentPdf()

const exportLoading = ref(false)

async function exportPDF() {
  exportLoading.value = true
  try {
    downloadPdf(
      tournamentName.value,
      partidos[0]?.torneo?.tipoFormato ?? '',
      partidos,
      standings,
    )
  } finally {
    exportLoading.value = false
  }
}

const openMatches = ref(true)
const openStandings = ref(true)
const openBracket = ref(true)
const standingsTab = ref<'global' | 'groups'>('global')

const tournamentName = computed(() =>
  partidos.length > 0 ? partidos[0].torneo.nombre : `Torneo ${torneoId.value}`
)

const isRoundRobin = computed(() => {
  const f = (partidos[0]?.torneo?.tipoFormato ?? '').toLowerCase()
  return f.includes('round') || f.includes('liga') || f.includes('robin')
})
const isGroupFormat = computed(() => {
  const f = (partidos[0]?.torneo?.tipoFormato ?? '').toLowerCase()
  return f.includes('grupo') || f.includes('grupos')
})
const hasGroupMatches = computed(() => partidos.some(p => p.grupo != null))
const eliminationMatches = computed(() => partidos.filter(p => p.bracketIndex != null))
const regularMatches = computed(() => partidos.filter(p => p.bracketIndex == null))

const groupedStandings = computed(() => {
  const map = new Map<string, typeof standings>()
  standings.forEach(s => {
    const g = s.grupo ?? 'General'
    if (!map.has(g)) map.set(g, [])
    map.get(g)!.push(s)
  })
  return Array.from(map.entries()).sort(([a], [b]) => a.localeCompare(b))
})

const sortedStandings = computed(() =>
  [...standings].sort((a, b) =>
    b.puntos - a.puntos ||
    (b.golesAFavor - b.golesEnContra) - (a.golesAFavor - a.golesEnContra)
  )
)

const totalPlayed = computed(() => partidos.filter(p => p.jugado).length)
const totalPending = computed(() => partidos.filter(p => !p.jugado).length)

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Intl.DateTimeFormat('es-ES', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(date))
}

onMounted(async () => {
  auth.hydrateSession()
  // Redirigir si torneo es borrador y usuario no es admin
  try {
    const res = await api.get<{ data: { estado: string } }>(`/torneos/${torneoId.value}`)
    const estado = (res.data.data?.estado ?? '').toUpperCase()
    const isAdmin = auth.session?.usuario?.rol === 'ADMIN'
    if (estado === 'BORRADOR' && !isAdmin) {
      router.replace('/')
      return
    }
  } catch {
    // si falla la consulta, continuar y dejar que la página lo maneje
  }
  fetchAll(torneoId.value)
})
</script>

<style>
@media print {

  nav,
  .no-print {
    display: none !important;
  }

  body {
    background: white !important;
  }

  .print-section {
    break-inside: avoid;
  }
}
</style>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar class="no-print" />

    <main class="max-w-6xl mx-auto px-6 py-8 space-y-6">

      <!-- Encabezado -->
      <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
          <button type="button" @click="router.back()"
            class="no-print flex items-center gap-1 text-sm text-gray-500 hover:text-gray-800 transition-colors mb-3">
            ← Volver
          </button>
          <h1 class="text-2xl font-semibold text-gray-900">{{ tournamentName }}</h1>
          <p class="text-sm text-gray-500 mt-0.5">{{ partidos[0]?.torneo?.tipoFormato ?? '' }}</p>
        </div>

        <div class="no-print flex items-center gap-6">
          <div class="text-center">
            <div class="text-xl font-bold text-gray-900">{{ totalPlayed }}</div>
            <div class="text-xs text-gray-500">Jugados</div>
          </div>
          <div class="text-center">
            <div class="text-xl font-bold text-gray-900">{{ totalPending }}</div>
            <div class="text-xs text-gray-500">Pendientes</div>
          </div>
          <button v-if="partidos.length > 0" @click="exportPDF" :disabled="exportLoading"
            class="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-blue-600 rounded hover:bg-blue-700 disabled:opacity-60 transition-colors">
            {{ exportLoading ? 'Generando…' : 'Exportar PDF' }}
          </button>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading.fetch" class="py-16 text-center text-gray-600">
        Cargando partidos…
      </div>

      <template v-else>

        <!-- PARTIDOS -->
        <section class="print-section bg-white border border-gray-300 rounded overflow-hidden">
          <button @click="openMatches = !openMatches"
            class="no-print w-full flex items-center justify-between px-6 py-4 border-b border-gray-300 bg-gray-50 hover:bg-gray-100 transition-colors text-left">
            <h2 class="text-lg font-semibold text-gray-900">
              Partidos
              <span class="ml-2 text-sm font-normal text-gray-500">({{ partidos.length }})</span>
            </h2>
            <span class="text-gray-500 text-sm">{{ openMatches ? '▲' : '▼' }}</span>
          </button>
          <div class="px-6 py-3 border-b border-gray-300 bg-gray-50 print:block hidden">
            <h2 class="text-lg font-semibold text-gray-900">Partidos</h2>
          </div>

          <div v-show="openMatches">
            <div v-if="partidos.length === 0" class="px-6 py-8 text-center text-gray-600">
              No hay partidos programados para este torneo.
            </div>

            <div v-else class="overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="bg-gray-50 border-b border-gray-300">
                    <th class="px-6 py-3 text-left font-semibold text-gray-900">Fase</th>
                    <th class="px-6 py-3 text-right font-semibold text-gray-900">Local</th>
                    <th class="px-6 py-3 text-center font-semibold text-gray-900">Resultado</th>
                    <th class="px-6 py-3 text-left font-semibold text-gray-900">Visitante</th>
                    <th class="px-6 py-3 text-left font-semibold text-gray-900">Fecha</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="partido in partidos" :key="partido.partidosId"
                    class="border-b border-gray-200 hover:bg-gray-50 transition-colors"
                    :class="partido.jugado ? 'bg-green-50 hover:bg-green-100' : ''">
                    <td class="px-6 py-3 text-gray-500">
                      {{ partido.fase ?? (partido.grupo ? `Grupo ${partido.grupo}` : '-') }}
                    </td>
                    <td class="px-6 py-3 text-right font-medium text-gray-900">
                      {{ partido.equipoLocal?.nombre ?? 'TBD' }}
                    </td>
                    <td class="px-6 py-3 text-center">
                      <span v-if="partido.jugado" class="font-bold text-gray-900">
                        {{ partido.golesLocal }} - {{ partido.golesVisitante }}
                      </span>
                      <span v-else class="text-gray-400">vs</span>
                    </td>
                    <td class="px-6 py-3 font-medium text-gray-900">
                      {{ partido.equipoVisitante?.nombre ?? 'TBD' }}
                    </td>
                    <td class="px-6 py-3 text-gray-600">{{ formatDate(partido.fechaPartido) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </section>

        <!-- TABLA DE POSICIONES -->
        <section v-if="standings.length > 0"
          class="print-section bg-white border border-gray-300 rounded overflow-hidden">
          <button @click="openStandings = !openStandings"
            class="no-print w-full flex items-center justify-between px-6 py-4 border-b border-gray-300 bg-gray-50 hover:bg-gray-100 transition-colors text-left">
            <h2 class="text-lg font-semibold text-gray-900">Tabla de Posiciones</h2>
            <span class="text-gray-500 text-sm">{{ openStandings ? '▲' : '▼' }}</span>
          </button>
          <div class="px-6 py-3 border-b border-gray-300 bg-gray-50 print:block hidden">
            <h2 class="text-lg font-semibold text-gray-900">Tabla de Posiciones</h2>
          </div>

          <div v-show="openStandings">
            <!-- Tab switcher -->
            <div v-if="isGroupFormat" class="no-print flex border-b border-gray-200">
              <button v-for="tab in [{ key: 'global', label: 'General' }, { key: 'groups', label: 'Por Grupos' }]"
                :key="tab.key" @click="standingsTab = tab.key as 'global' | 'groups'"
                class="px-5 py-3 text-sm font-medium border-b-2 transition-colors" :class="standingsTab === tab.key
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'">
                {{ tab.label }}
              </button>
            </div>

            <!-- Global -->
            <div v-if="!isGroupFormat || standingsTab === 'global'" class="overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="bg-gray-50 border-b border-gray-300">
                    <th class="px-6 py-3 text-left font-semibold text-gray-900 w-14">Pos</th>
                    <th class="px-6 py-3 text-left font-semibold text-gray-900">Equipo</th>
                    <th class="px-4 py-3 text-center font-semibold text-gray-900">PJ</th>
                    <th class="px-4 py-3 text-center font-semibold text-gray-900">V</th>
                    <th class="px-4 py-3 text-center font-semibold text-gray-900">E</th>
                    <th class="px-4 py-3 text-center font-semibold text-gray-900">D</th>
                    <th class="px-4 py-3 text-center font-semibold text-gray-900">GF</th>
                    <th class="px-4 py-3 text-center font-semibold text-gray-900">GC</th>
                    <th class="px-4 py-3 text-center font-semibold text-gray-900">Pts</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(s, i) in sortedStandings" :key="s.tablaPosicionesId"
                    class="border-b border-gray-200 hover:bg-gray-50 transition-colors">
                    <td class="px-6 py-3 text-sm text-gray-500">{{ i + 1 }}</td>
                    <td class="px-6 py-3 font-medium text-gray-900">{{ s.equipo.nombre }}</td>
                    <td class="px-4 py-3 text-center text-gray-700">{{ s.partidosJugados }}</td>
                    <td class="px-4 py-3 text-center text-gray-700">{{ s.victorias ?? '-' }}</td>
                    <td class="px-4 py-3 text-center text-gray-700">{{ s.empates ?? '-' }}</td>
                    <td class="px-4 py-3 text-center text-gray-700">{{ s.derrotas ?? '-' }}</td>
                    <td class="px-4 py-3 text-center text-gray-700">{{ s.golesAFavor }}</td>
                    <td class="px-4 py-3 text-center text-gray-700">{{ s.golesEnContra }}</td>
                    <td class="px-4 py-3 text-center font-bold text-gray-900">{{ s.puntos }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Por grupos -->
            <div v-if="isGroupFormat && standingsTab === 'groups'" class="space-y-6 px-6 py-6">
              <div v-for="([group, rows]) in groupedStandings" :key="group">
                <h3 class="text-sm font-semibold text-gray-700 mb-2">Grupo {{ group }}</h3>
                <div class="overflow-x-auto border border-gray-200 rounded">
                  <table class="w-full text-sm">
                    <thead>
                      <tr class="bg-gray-50 border-b border-gray-200">
                        <th class="px-4 py-2 text-left font-semibold text-gray-900">Pos</th>
                        <th class="px-4 py-2 text-left font-semibold text-gray-900">Equipo</th>
                        <th class="px-3 py-2 text-center font-semibold text-gray-900">PJ</th>
                        <th class="px-3 py-2 text-center font-semibold text-gray-900">V</th>
                        <th class="px-3 py-2 text-center font-semibold text-gray-900">E</th>
                        <th class="px-3 py-2 text-center font-semibold text-gray-900">D</th>
                        <th class="px-3 py-2 text-center font-semibold text-gray-900">GF</th>
                        <th class="px-3 py-2 text-center font-semibold text-gray-900">GC</th>
                        <th class="px-3 py-2 text-center font-semibold text-gray-900">Pts</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(row, idx) in rows" :key="row.tablaPosicionesId"
                        class="border-b border-gray-200 hover:bg-gray-50 transition-colors">
                        <td class="px-4 py-2.5 text-gray-500">{{ idx + 1 }}</td>
                        <td class="px-4 py-2.5 font-medium text-gray-900">{{ row.equipo.nombre }}</td>
                        <td class="px-3 py-2.5 text-center text-gray-700">{{ row.partidosJugados }}</td>
                        <td class="px-3 py-2.5 text-center text-gray-700">{{ row.victorias ?? '-' }}</td>
                        <td class="px-3 py-2.5 text-center text-gray-700">{{ row.empates ?? '-' }}</td>
                        <td class="px-3 py-2.5 text-center text-gray-700">{{ row.derrotas ?? '-' }}</td>
                        <td class="px-3 py-2.5 text-center text-gray-700">{{ row.golesAFavor }}</td>
                        <td class="px-3 py-2.5 text-center text-gray-700">{{ row.golesEnContra }}</td>
                        <td class="px-3 py-2.5 text-center font-bold text-gray-900">{{ row.puntos }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- BRACKET -->
        <section v-if="partidos.length > 0"
          class="print-section bg-white border border-gray-300 rounded overflow-hidden">
          <button @click="openBracket = !openBracket"
            class="no-print w-full flex items-center justify-between px-6 py-4 border-b border-gray-300 bg-gray-50 hover:bg-gray-100 transition-colors text-left">
            <h2 class="text-lg font-semibold text-gray-900">Llaves</h2>
            <span class="text-gray-500 text-sm">{{ openBracket ? '▲' : '▼' }}</span>
          </button>
          <div class="px-6 py-3 border-b border-gray-300 bg-gray-50 print:block hidden">
            <h2 class="text-lg font-semibold text-gray-900">Llave de Eliminación</h2>
          </div>

          <div v-show="openBracket">
            <TournamentBracketGroups v-if="regularMatches.length > 0 && isGroupFormat && hasGroupMatches"
              :partidos="regularMatches" />
            <TournamentBracketRoundRobin v-if="regularMatches.length > 0 && isRoundRobin" :partidos="regularMatches" />
            <TournamentBracketElimination v-if="eliminationMatches.length > 0" :partidos="eliminationMatches" />
            <TournamentBracketElimination v-if="partidos.length > 0 && !isGroupFormat && !isRoundRobin"
              :partidos="partidos" />
          </div>
        </section>

      </template>
    </main>
  </div>
</template>
