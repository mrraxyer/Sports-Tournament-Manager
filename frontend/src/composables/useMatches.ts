import { reactive, ref } from 'vue'
import { matchAPI, standingsAPI, type Partido, type TablaPosiciones } from '../api/matches'

export function useMatches() {
  const partidos = reactive<Partido[]>([])
  const standings = reactive<TablaPosiciones[]>([])
  const loading = reactive({ fetch: false })
  const savingId = ref<number | null>(null)
  const feedback = reactive({
    type: '' as 'success' | 'error' | '',
    message: '',
  })
  const draftScores = reactive<Record<number, { golesLocal: number; golesVisitante: number }>>({})

  function initDrafts(list: Partido[]) {
    list.forEach((p) => {
      draftScores[p.partidosId] = {
        golesLocal: p.golesLocal,
        golesVisitante: p.golesVisitante,
      }
    })
  }

  async function fetchAll(torneoId: number) {
    loading.fetch = true
    try {
      const [newPartidos, newStandings] = await Promise.all([
        matchAPI.listByTorneo(torneoId),
        standingsAPI.listByTorneo(torneoId),
      ])
      partidos.length = 0
      partidos.push(...newPartidos)
      standings.length = 0
      standings.push(...newStandings)
      initDrafts(newPartidos)
    } catch (error) {
      feedback.type = 'error'
      feedback.message = error instanceof Error ? error.message : 'Error al cargar partidos'
    } finally {
      loading.fetch = false
    }
  }

  async function saveResult(partidoId: number, torneoId: number) {
    const draft = draftScores[partidoId]
    if (!draft) return

    savingId.value = partidoId
    feedback.type = ''
    feedback.message = ''

    try {
      await matchAPI.saveResult(partidoId, draft.golesLocal, draft.golesVisitante)
      feedback.type = 'success'
      feedback.message = 'Resultado guardado exitosamente'
      setTimeout(() => {
        feedback.type = ''
        feedback.message = ''
      }, 3000)
      await fetchAll(torneoId)
    } catch (error) {
      feedback.type = 'error'
      feedback.message = error instanceof Error ? error.message : 'Error al guardar resultado'
    } finally {
      savingId.value = null
    }
  }

  return {
    partidos,
    standings,
    loading,
    savingId,
    feedback,
    draftScores,
    fetchAll,
    saveResult,
  }
}
