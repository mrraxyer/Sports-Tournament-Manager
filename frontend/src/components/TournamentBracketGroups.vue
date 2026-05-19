<script setup lang="ts">
import { computed } from 'vue'
import type { Partido } from '../api/matches'

const props = defineProps<{ partidos: Partido[] }>()

const groupedMatches = computed(() => {
  const groupMap = new Map<string, Partido[]>()

  for (const p of props.partidos) {
    if (!p.grupo) continue
    if (!groupMap.has(p.grupo)) groupMap.set(p.grupo, [])
    groupMap.get(p.grupo)!.push(p)
  }

  return Array.from(groupMap.entries())
    .sort((a, b) => a[0].localeCompare(b[0]))
    .map(([grupo, matches]) => {
      const jornadas = new Map<string, Partido[]>()
      for (const m of matches) {
        const date = m.fechaPartido.slice(0, 10)
        if (!jornadas.has(date)) jornadas.set(date, [])
        jornadas.get(date)!.push(m)
      }
      const sortedJornadas = Array.from(jornadas.entries())
        .sort(([a], [b]) => a.localeCompare(b))
        .map(([date, jMatches], idx) => ({ date, jMatches, index: idx }))

      return { grupo, jornadas: sortedJornadas }
    })
})

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
      <h3 class="text-xl font-semibold text-gray-900">Fase de Grupos</h3>
    </div>

    <div class="p-6 space-y-8">
      <div v-for="group in groupedMatches" :key="group.grupo" class="">
        <h4 class="text-lg font-bold text-gray-800 mb-4">Grupo {{ group.grupo }}</h4>

        <div class="flex gap-6 overflow-x-auto pb-4">
          <div v-for="jornada in group.jornadas" :key="jornada.date" class="flex flex-col shrink-0">
            <div class="text-xs font-bold uppercase tracking-wide text-center mb-3"
              :class="jornada.jMatches.every(m => m.jugado) ? 'text-blue-600' : 'text-gray-400'">
              Jornada {{ jornada.index + 1 }}
            </div>

            <div class="flex flex-col gap-3">
              <div v-for="partido in jornada.jMatches" :key="partido.partidosId"
                class="w-44 rounded border overflow-hidden text-xs"
                :class="partido.jugado ? 'border-blue-200' : 'border-dashed border-gray-300'">
                <!-- Fila equipo local -->
                <div class="px-3 py-2 flex justify-between items-center border-b" :class="partido.jugado && winner(partido) === 'local'
                  ? 'bg-blue-600 text-white font-bold border-blue-600'
                  : partido.jugado
                    ? 'bg-blue-50 text-blue-900 border-blue-100'
                    : 'bg-white text-gray-400 border-gray-100'">
                  <span class="truncate max-w-30">{{ partido.equipoLocal?.nombre ?? 'TBD' }}</span>
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
                  <span class="truncate max-w-30">{{ partido.equipoVisitante?.nombre ?? 'TBD' }}</span>
                  <span class="font-bold ml-2 shrink-0">
                    {{ partido.jugado ? partido.golesVisitante : '–' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
