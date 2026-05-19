import { reactive, ref } from 'vue'
import { teamAPI, tournamentAPI, userAPI, type Equipo, type Torneo, type Usuario } from '../api/teams'

export function useTeams() {
  const equipos = reactive<Equipo[]>([])
  const torneos = reactive<Torneo[]>([])
  const usuarios = reactive<Usuario[]>([])
  const loading = reactive({ fetch: false, submit: false, delete: false })
  const feedback = reactive({
    type: '' as 'success' | 'error' | '',
    message: '',
  })

  const deletingEquipo = ref<Equipo | null>(null)
  const showDeleteConfirm = ref(false)

  const form = reactive({
    nombre: '',
    torneoId: null as number | null,
    capitanId: null as number | null,
  })

  const selectedTorneoId = ref<number | null>(null)

  async function fetchTeamsByTorneo(torneoId: number | null) {
    loading.fetch = true
    try {
      let e: Equipo[] = []
      if (torneoId) {
        e = await teamAPI.listByTorneo(torneoId)
      } else {
        e = await teamAPI.list()
      }
      equipos.length = 0
      equipos.push(...e)
    } finally {
      loading.fetch = false
    }
  }

  async function fetchAll() {
    loading.fetch = true
    try {
      const [t, u, e] = await Promise.all([
        tournamentAPI.list(),
        userAPI.list(),
        teamAPI.list(),
      ])
      torneos.length = 0
      torneos.push(...t)
      usuarios.length = 0
      usuarios.push(...u)
      equipos.length = 0
      equipos.push(...e)
    } finally {
      loading.fetch = false
    }
  }


  function openDeleteConfirm(equipo: Equipo) {
    deletingEquipo.value = equipo
    showDeleteConfirm.value = true
  }

  async function confirmDelete() {
    if (!deletingEquipo.value) return
    loading.delete = true
    try {
      await teamAPI.delete(deletingEquipo.value.equiposId)
      feedback.type = 'success'
      feedback.message = 'Equipo eliminado exitosamente'
      showDeleteConfirm.value = false
      deletingEquipo.value = null
      await fetchAll()
    } catch (error) {
      feedback.type = 'error'
      feedback.message = error instanceof Error ? error.message : 'Error al eliminar equipo'
    } finally {
      loading.delete = false
    }
  }

  function resetForm() {
    form.nombre = ''
    form.torneoId = null
    form.capitanId = null
  }

  function clearFeedback() {
    feedback.type = ''
    feedback.message = ''
  }

  async function submitForm() {
    clearFeedback()

    if (!form.nombre.trim()) {
      feedback.type = 'error'
      feedback.message = 'Nombre es requerido'
      return
    }
    if (!form.torneoId) {
      feedback.type = 'error'
      feedback.message = 'Torneo es requerido'
      return
    }
    if (!form.capitanId) {
      feedback.type = 'error'
      feedback.message = 'Capitán es requerido'
      return
    }

    loading.submit = true
    try {
      await teamAPI.create(form.nombre.trim(), form.torneoId, form.capitanId)
      feedback.type = 'success'
      feedback.message = 'Equipo creado exitosamente'
      resetForm()
      setTimeout(() => clearFeedback(), 3000)
      await fetchAll()
    } catch (error) {
      feedback.type = 'error'
      feedback.message = error instanceof Error ? error.message : 'Error al crear equipo'
    } finally {
      loading.submit = false
    }
  }

  return {
    equipos,
    torneos,
    usuarios,
    form,
    loading,
    feedback,
    deletingEquipo,
    showDeleteConfirm,
    fetchAll,
    submitForm,
    resetForm,
    clearFeedback,
    openDeleteConfirm,
    confirmDelete,
    selectedTorneoId,
    fetchTeamsByTorneo,
  }
}
