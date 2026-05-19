import api from './index'

export interface Partido {
  partidosId: number
  torneo: { torneosId: number; nombre: string }
  equipoLocal: { equiposId: number; nombre: string }
  equipoVisitante: { equiposId: number; nombre: string }
  golesLocal: number
  golesVisitante: number
  fechaPartido: string
}

export interface TablaPosiciones {
  tablaPosicionesId: number
  equipo: { equiposId: number; nombre: string }
  puntos: number
  partidosJugados: number
  golesAFavor: number
  golesEnContra: number
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
}

export const standingsAPI = {
  async listByTorneo(torneoId: number): Promise<TablaPosiciones[]> {
    const response = await api.get<{ data: TablaPosiciones[] }>(`/tabla-posiciones/torneo/${torneoId}`)
    return response.data.data || []
  },
}
