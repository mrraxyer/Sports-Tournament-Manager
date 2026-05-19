<script setup lang="ts">
import { computed } from 'vue'
import type { Partido } from '../api/matches'

const props = defineProps<{ partidos: Partido[] }>()

/**
 * Agrupa los partidos por fecha (YYYY-MM-DD), los ordena cronológicamente
 * y asigna etiquetas de ronda (Ronda N, Cuartos, Semifinal, Final).
 * @returns {{ label: string; matches: Partido[] }[]}
 */
const rondas = computed(() => {
  const map = new Map<string, Partido[]>()
  for (const p of props.partidos) {
    const date = p.fechaPartido.slice(0, 10)
    if (!map.has(date)) map.set(date, [])
    map.get(date)!.push(p)
  }
  const sorted = Array.from(map.entries()).sort(([a], [b]) => a.localeCompare(b))
  const total = sorted.length
  return sorted.map(([date, matches], index) => {
    const fromEnd = total - 1 - index
    let label: string
    if (fromEnd === 0) label = 'Final'
    else if (fromEnd === 1) label = 'Semifinal'
    else if (fromEnd === 2) label = 'Cuartos'
    else label = `Ronda ${index + 1}`
    return { date, matches, label }
  })
})

/**
 * Determina el ganador de un partido jugado.
 * @param {Partido} partido
 * @returns {'local' | 'visitante' | 'empate'}
 */
function winner(partido: Partido): 'local' | 'visitante' | 'empate' {
  if (!partido.jugado) return 'empate'
  if (partido.golesLocal > partido.golesVisitante) return 'local'
  if (partido.golesVisitante > partido.golesLocal) return 'visitante'
  return 'empate'
}
</script>

<template>
  <section class="bg-white border border-gray-300 rounded overflow-hidden">
    <div class="px-6 py-4 border-b border-gray-300 bg-gray-50">
      <h3 class="text-xl font-semibold text-gray-900">Bracket</h3>
    </div>

    <div class="p-6 overflow-x-auto">
      <div class="flex gap-6 items-stretch min-w-max">
        <div v-for="ronda in rondas" :key="ronda.date" class="flex flex-col">
          <div class="text-xs font-bold uppercase tracking-wide text-center mb-3"
            :class="ronda.matches.every(m => m.jugado) ? 'text-blue-600' : 'text-gray-400'">
            {{ ronda.label }}
          </div>

          <div class="flex flex-col flex-1 justify-around gap-3">
            <div v-for="partido in ronda.matches" :key="partido.partidosId"
              class="w-44 rounded border overflow-hidden text-xs"
              :class="partido.jugado ? 'border-blue-200' : 'border-dashed border-gray-300'">
              <!-- Fila equipo local -->
              <div class="px-3 py-2 flex justify-between items-center border-b" :class="partido.jugado && winner(partido) === 'local'
                ? 'bg-blue-600 text-white font-bold border-blue-600'
                : partido.jugado
                  ? 'bg-blue-50 text-blue-900 border-blue-100'
                  : 'bg-white text-gray-400 border-gray-100'">
                <span class="truncate max-w-30">{{ partido.equipoLocal.nombre }}</span>
                <span class="font-bold ml-2 shrink-0">
                  {{ partido.jugado ? partido.golesLocal : '–' }}
                </span>
              </div>

              <!-- Fila equipo visitante -->
              <div class="px-3 py-2 flex justify-between items-center" :class="partido.jugado && winner(partido) === 'visitante'
                ? 'bg-blue-600 text-white font-bold'
                : partido.jugado
                  ? 'bg-blue-50 text-blue-800'
                  : 'bg-white text-gray-400'">
                <span class="truncate max-w-30">{{ partido.equipoVisitante.nombre }}</span>
                <span class="font-bold ml-2 shrink-0">
                  {{ partido.jugado ? partido.golesVisitante : '–' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
