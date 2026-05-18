import api from './index'

export interface Torneo {
  torneosId: number
  nombre: string
}

export interface Usuario {
  usuariosId: number
  nombre: string
  email: string
  rol: string
}

export interface Equipo {
  equiposId: number
  nombre: string
  torneo: Torneo
  capitan: Usuario
}

export const teamAPI = {
  async list() {
    const response = await api.get<{ data: Equipo[] }>('/equipos')
    return response.data.data || []
  },

  async create(nombre: string, torneosId: number, usuariosId: number) {
    const payload = {
      nombre,
      torneo: { torneosId },
      capitan: { usuariosId },
    }
    const response = await api.post<{ data: Equipo }>('/equipos', payload)
    return response.data.data
  },

  async update(id: number, nombre: string, torneosId: number, usuariosId: number) {
    const payload = {
      nombre,
      torneo: { torneosId },
      capitan: { usuariosId },
    }
    const response = await api.put<{ data: Equipo }>(`/equipos/${id}`, payload)
    return response.data.data
  },

  async delete(id: number) {
    await api.delete(`/equipos/${id}`)
  },
}

export const tournamentAPI = {
  async list() {
    const response = await api.get<{ data: Torneo[] }>('/torneos')
    return response.data.data || []
  },
}

export const userAPI = {
  async list() {
    const response = await api.get<{ data: Usuario[] }>('/usuarios')
    return response.data.data || []
  },
}
