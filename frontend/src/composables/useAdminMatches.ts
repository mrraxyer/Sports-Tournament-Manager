import { reactive, ref } from 'vue'
import { matchAPI, type Partido } from '../api/matches'
import api from '../services/api'

/** Estructura mínima de torneo para poblar el selector. */
interface Tournament {
  torneosId: number
  nombre: string
  tipoFormato: string
  fechaInicio: string
}

export function useAdminMatches() {
  const tournaments = reactive<Tournament[]>([])
  const selectedTournamentId = ref<number | null>(null)
  const partidos = reactive<Partido[]>([])
  const loading = reactive({ fetch: false, generate: false })

  const showRescheduleModal = ref(false)
  const rescheduleMatchId = ref<number | null>(null)
  const rescheduleDate = ref<string>('')

  const feedback = reactive({
    type: '' as 'success' | 'error' | '',
    message: '',
  })

  async function fetchTournaments(): Promise<void> {
    try {
      const response = await api.get<{ data: Tournament[] }>('/torneos')
      tournaments.length = 0
      tournaments.push(...(response.data.data || []))
    } catch (error) {
      feedback.type = 'error'
      feedback.message = error instanceof Error ? error.message : 'Error al cargar torneos'
    }
  }

  async function fetchTournamentMatches(tournamentId: number): Promise<void> {
    loading.fetch = true
    feedback.type = ''
    feedback.message = ''
    try {
      const result = await matchAPI.listByTorneo(tournamentId)
      partidos.length = 0
      partidos.push(...result)
    } catch (error) {
      feedback.type = 'error'
      feedback.message = error instanceof Error ? error.message : 'Error al cargar partidos'
    } finally {
      loading.fetch = false
    }
  }

  async function generateTournamentSchedule(tournamentId: number): Promise<void> {
    if (partidos.length > 0) {
      feedback.type = 'error'
      feedback.message = 'Este torneo ya tiene partidos generados. Si necesitas recrearlo, elimina primero los partidos existentes.'
      return
    }

    loading.generate = true
    feedback.type = ''
    feedback.message = ''
    try {
      await matchAPI.generateSchedule(tournamentId)
      feedback.type = 'success'
      feedback.message = 'Calendario generado exitosamente'
      await fetchTournamentMatches(tournamentId)
    } catch (error) {
      feedback.type = 'error'
      feedback.message = error instanceof Error ? error.message : 'Error al generar el calendario'
    } finally {
      loading.generate = false
    }
  }

  async function generateEliminationPhase(tournamentId: number, clasificados = 2): Promise<void> {
    loading.generate = true
    feedback.type = ''
    feedback.message = ''
    try {
      await api.post(`/torneos/${tournamentId}/avanzar-eliminatoria`, null, {
        params: { clasificadosPorGrupo: clasificados }
      })
      feedback.type = 'success'
      feedback.message = 'Fase eliminatoria generada correctamente'
      await fetchTournamentMatches(tournamentId)
    } catch (error) {
      feedback.type = 'error'
      feedback.message = error instanceof Error ? error.message : 'Error al generar eliminatoria'
    } finally {
      loading.generate = false
    }
  }

  async function saveMatchScore(matchId: number, localGoals: number, visitorGoals: number): Promise<void> {
    if (selectedTournamentId.value === null) return
    feedback.type = ''
    feedback.message = ''
    try {
      await matchAPI.saveResult(matchId, localGoals, visitorGoals)
      feedback.type = 'success'
      feedback.message = 'Resultado guardado exitosamente'
      setTimeout(() => {
        feedback.type = ''
        feedback.message = ''
      }, 3000)
      await fetchTournamentMatches(selectedTournamentId.value)
    } catch (error) {
      feedback.type = 'error'
      feedback.message = error instanceof Error ? error.message : 'Error al guardar resultado'
    }
  }

  async function openRescheduleModal(matchId: number, fechaIso: string) {
    rescheduleMatchId.value = matchId
    // Convertir ISO a formato requerido por input[type=datetime-local]
    const dt = new Date(fechaIso)
    const local = new Date(dt.getTime() - dt.getTimezoneOffset() * 60000)
    rescheduleDate.value = local.toISOString().slice(0, 16)
    showRescheduleModal.value = true
  }

  async function rescheduleMatch() {
    if (!rescheduleMatchId.value || selectedTournamentId.value === null) return
    loading.fetch = true
    try {
      await matchAPI.reschedule(rescheduleMatchId.value, new Date(rescheduleDate.value).toISOString())
      showRescheduleModal.value = false
      await fetchTournamentMatches(selectedTournamentId.value)
    } catch (error) {
      feedback.type = 'error'
      feedback.message = error instanceof Error ? error.message : 'Error al reprogramar partido'
    } finally {
      loading.fetch = false
    }
  }

  async function deleteMatch(matchId: number) {
    if (!confirm('¿Confirmas eliminar este partido? Esta acción no puede revertirse.')) return
    if (selectedTournamentId.value === null) return
    loading.fetch = true
    feedback.type = ''
    feedback.message = ''
    try {
      await matchAPI.delete(matchId)
      feedback.type = 'success'
      feedback.message = 'Partido eliminado exitosamente'
      await fetchTournamentMatches(selectedTournamentId.value)
    } catch (error) {
      feedback.type = 'error'
      feedback.message = error instanceof Error ? error.message : 'Error al eliminar partido'
    } finally {
      loading.fetch = false
    }
  }

  return {
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
  }
}
