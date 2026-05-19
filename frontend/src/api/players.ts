import api from './index'

export interface Jugador {
  jugadoresId: number
  nombre: string
  numeroCamiseta: number
  equipo?: { equiposId: number }
}

export const playerAPI = {
  async list() {
    const response = await api.get<{ data: Jugador[] }>('/jugadores')
    return response.data.data || []
  },

  async create(nombre: string, numeroCamiseta: number, equiposId: number) {
    const payload = {
      nombre,
      numeroCamiseta,
      equipo: { equiposId },
    }
    const response = await api.post<{ data: Jugador }>('/jugadores', payload)
    return response.data.data
  },

  async update(id: number, nombre: string, numeroCamiseta: number) {
    const payload = {
      nombre,
      numeroCamiseta,
    }
    const response = await api.put<{ data: Jugador }>(`/jugadores/${id}`, payload)
    return response.data.data
  },

  async delete(id: number) {
    await api.delete(`/jugadores/${id}`)
  },
}
