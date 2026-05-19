<script setup lang="ts">
import { computed } from 'vue'
import type { Partido } from '../api/matches'

const props = defineProps<{ partidos: Partido[] }>()

/**
 * Agrupa los partidos por fecha (YYYY-MM-DD) y los ordena cronológicamente.
 * Cada grupo representa una jornada.
 * @returns {{ date: string; index: number; matches: Partido[] }[]}
 */
const jornadas = computed(() => {
  const map = new Map<string, Partido[]>()
  for (const p of props.partidos) {
    const date = p.fechaPartido.slice(0, 10)
    if (!map.has(date)) map.set(date, [])
    map.get(date)!.push(p)
  }
  return Array.from(map.entries())
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([date, matches], index) => ({ date, matches, index }))
})
</script>

<template>
  <section class="bg-white border border-gray-300 rounded overflow-hidden">
    <div class="px-6 py-4 border-b border-gray-300 bg-gray-50">
      <h3 class="text-xl font-semibold text-gray-900">Calendario</h3>
    </div>

    <div class="p-4 overflow-x-auto">
      <div class="flex gap-4 min-w-max">
        <div
          v-for="jornada in jornadas"
          :key="jornada.date"
          class="w-44"
        >
          <div
            class="text-xs font-bold uppercase tracking-wide mb-2"
            :class="jornada.matches.every(m => m.jugado) ? 'text-blue-600' : 'text-gray-400'"
          >
            Jornada {{ jornada.index + 1 }}
          </div>

          <div class="space-y-2">
            <div
              v-for="partido in jornada.matches"
              :key="partido.partidosId"
              class="rounded px-3 py-2 text-xs"
              :class="partido.jugado
                ? 'bg-blue-50 border border-blue-200'
                : 'bg-white border border-dashed border-gray-300'"
            >
              <div class="flex items-center justify-between gap-1">
                <span
                  class="truncate font-medium"
                  :class="partido.jugado ? 'text-blue-900' : 'text-gray-400'"
                >{{ partido.equipoLocal.nombre }}</span>
                <span
                  class="shrink-0 font-bold text-xs"
                  :class="partido.jugado ? 'text-blue-700' : 'text-gray-300'"
                >
                  {{ partido.jugado ? `${partido.golesLocal}–${partido.golesVisitante}` : 'vs' }}
                </span>
                <span
                  class="truncate text-right"
                  :class="partido.jugado ? 'text-blue-800' : 'text-gray-400'"
                >{{ partido.equipoVisitante.nombre }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
