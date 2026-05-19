import api from './index'

export interface Partido {
  partidosId: number
  torneo: { torneosId: number; nombre: string; tipoFormato: string }
  equipoLocal: { equiposId: number; nombre: string }
  equipoVisitante: { equiposId: number; nombre: string }
  golesLocal: number
  golesVisitante: number
  fechaPartido: string
  jugado: boolean
  grupo?: string | null
}

export interface TablaPosiciones {
  tablaPosicionesId: number
  equipo: { equiposId: number; nombre: string }
  puntos: number
  partidosJugados: number
  golesAFavor: number
  golesEnContra: number
  grupo?: string | null
  victorias?: number
  empates?: number
  derrotas?: number
}

export const matchAPI = {
  async listByTorneo(torneoId: number): Promise<Partido[]> {
    const response = await api.get<{ data: Partido[] }>(`/partidos/torneo/${torneoId}`)
    return response.data.data || []
  },

  async saveResult(id: number, golesLocal: number, golesVisitante: number): Promise<Partido> {
    const response = await api.put<{ data: Partido }>(
      `/partidos/${id}/resultado`,
      null,
      { params: { golesLocal, golesVisitante } }
    )
    return response.data.data
  },

  async generateSchedule(tournamentId: number): Promise<Partido[]> {
    const response = await api.post<{ data: Partido[] }>(
      `/torneos/${tournamentId}/generar-calendario`
    )
    return response.data.data || []
  },

  async reschedule(id: number, fechaPartidoIso: string): Promise<Partido> {
    const payload = { fechaPartido: fechaPartidoIso }
    const response = await api.put<{ data: Partido }>(`/partidos/${id}`, payload)
    return response.data.data
  },
  async delete(id: number): Promise<void> {
    await api.delete(`/partidos/${id}`)
  },
}

export const tournamentAPI = {
  async advanceToElimination(torneoId: number, clasificadosPorGrupo = 2) {
    const response = await api.post<{ data: Partido[] }>(`/torneos/${torneoId}/avanzar-eliminatoria`, null, {
      params: { clasificadosPorGrupo },
    })
    return response.data.data || []
  },
}

export const standingsAPI = {
  async listByTorneo(torneoId: number): Promise<TablaPosiciones[]> {
    const response = await api.get<{ data: TablaPosiciones[] }>(`/tabla-posiciones/torneo/${torneoId}`)
    return response.data.data || []
  },
}
