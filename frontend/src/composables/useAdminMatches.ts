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
  /** Lista de torneos obtenida de GET /torneos para poblar el selector. */
  const tournaments = reactive<Tournament[]>([])

  /** ID del torneo seleccionado actualmente. Null indica que no hay torneo seleccionado. */
  const selectedTournamentId = ref<number | null>(null)

  /** Partidos del torneo seleccionado. Se reemplaza en cada consulta. */
  const partidos = reactive<Partido[]>([])

  /**
   * Borradores de marcadores editables indexados por ID de partido.
   * Se inicializan con los valores del servidor en cada consulta y se modifican con los inputs.
   */
  const draftScores = reactive<Record<number, { golesLocal: number; golesVisitante: number }>>({})

  /** Indicadores de carga para las operaciones asíncronas. */
  const loading = reactive({ fetch: false, generate: false })

  /** ID del partido cuyo resultado se está guardando. Null si no hay guardado en curso. */
  const savingId = ref<number | null>(null)

  /** Mensaje de retroalimentación de la última operación API mostrado como alerta. */
  const feedback = reactive({
    type: '' as 'success' | 'error' | '',
    message: '',
  })

  /**
   * Inicializa los borradores de marcadores a partir de la lista de partidos recién obtenida.
   * @param {Partido[]} list - Partidos para inicializar los borradores.
   */
  function initDrafts(list: Partido[]): void {
    list.forEach((p) => {
      draftScores[p.partidosId] = {
        golesLocal: p.golesLocal,
        golesVisitante: p.golesVisitante,
      }
    })
  }

  /**
   * Obtiene todos los torneos del backend para poblar el selector.
   * @returns {Promise<void>}
   */
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

  /**
   * Obtiene todos los partidos del torneo indicado y reinicia los borradores de marcadores.
   * @param {number} tournamentId - ID del torneo cuyos partidos se van a obtener.
   * @returns {Promise<void>}
   */
  async function fetchTournamentMatches(tournamentId: number): Promise<void> {
    loading.fetch = true
    feedback.type = ''
    feedback.message = ''
    try {
      const result = await matchAPI.listByTorneo(tournamentId)
      partidos.length = 0
      partidos.push(...result)
      initDrafts(result)
    } catch (error) {
      feedback.type = 'error'
      feedback.message = error instanceof Error ? error.message : 'Error al cargar partidos'
    } finally {
      loading.fetch = false
    }
  }

  /**
   * Solicita al backend ejecutar el algoritmo de programación para el torneo indicado.
   * Recarga la lista de partidos al completarse. Retorna error 409 si ya existen partidos.
   * @param {number} tournamentId - ID del torneo para el que se generará el calendario.
   * @returns {Promise<void>}
   */
  async function generateTournamentSchedule(tournamentId: number): Promise<void> {
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

  /**
   * Guarda el resultado de un partido y recarga la lista del torneo seleccionado.
   * @param {number} matchId - ID del partido a actualizar.
   * @param {number} localGoals - Goles anotados por el equipo local.
   * @param {number} visitorGoals - Goles anotados por el equipo visitante.
   * @returns {Promise<void>}
   */
  async function saveMatchScore(matchId: number, localGoals: number, visitorGoals: number): Promise<void> {
    if (selectedTournamentId.value === null) return
    savingId.value = matchId
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
    } finally {
      savingId.value = null
    }
  }

  return {
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
  }
}
