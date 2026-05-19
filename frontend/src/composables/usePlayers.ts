import { reactive, ref, computed } from 'vue'
import { playerAPI, type Jugador } from '../api/players'

export function usePlayers(teamId: () => number | null) {
  const jugadores = reactive<Jugador[]>([])
  const loading = reactive({ fetch: false, submit: false, delete: false })
  const feedback = reactive({
    type: '' as 'success' | 'error' | '',
    message: '',
  })
  const formFeedback = reactive({
    type: '' as 'success' | 'error' | '',
    message: '',
  })
  const deleteFeedback = reactive({
    type: '' as 'success' | 'error' | '',
    message: '',
  })

  const showAddForm = ref(false)
  const showDeleteConfirm = ref(false)
  const editingJugador = ref<Jugador | null>(null)
  const deletingJugador = ref<Jugador | null>(null)

  const form = reactive({
    nombre: '',
    numeroCamiseta: null as number | null,
  })

  const formTitle = computed(() =>
    editingJugador.value ? 'Editar Jugador' : 'Agregar Jugador'
  )

  async function fetchJugadores() {
    if (!teamId()) return
    loading.fetch = true
    try {
      const all = await playerAPI.list()
      jugadores.length = 0
      jugadores.push(...all.filter(j => j.equipo?.equiposId === teamId()))
    } catch (error) {
      feedback.type = 'error'
      feedback.message = error instanceof Error ? error.message : 'Error al cargar jugadores'
    } finally {
      loading.fetch = false
    }
  }

  function openAddForm() {
    editingJugador.value = null
    form.nombre = ''
    form.numeroCamiseta = null
    showAddForm.value = true
  }

  function openEditForm(jugador: Jugador) {
    editingJugador.value = jugador
    form.nombre = jugador.nombre
    form.numeroCamiseta = jugador.numeroCamiseta
    showAddForm.value = true
  }

  async function submitForm() {
    formFeedback.type = ''
    formFeedback.message = ''

    if (!form.nombre.trim()) {
      formFeedback.type = 'error'
      formFeedback.message = 'Nombre es requerido'
      return
    }
    if (form.numeroCamiseta === null) {
      formFeedback.type = 'error'
      formFeedback.message = 'Número de camiseta es requerido'
      return
    }

    loading.submit = true
    try {
      if (editingJugador.value) {
        await playerAPI.update(editingJugador.value.jugadoresId, form.nombre.trim(), form.numeroCamiseta)
        feedback.message = 'Jugador actualizado exitosamente'
      } else {
        await playerAPI.create(form.nombre.trim(), form.numeroCamiseta, teamId()!)
        feedback.message = 'Jugador creado exitosamente'
      }
      feedback.type = 'success'
      showAddForm.value = false
      await fetchJugadores()
    } catch (error) {
      formFeedback.type = 'error'
      formFeedback.message = error instanceof Error ? error.message : 'Error al guardar jugador'
    } finally {
      loading.submit = false
    }
  }

  function openDeleteConfirm(jugador: Jugador) {
    deletingJugador.value = jugador
    showDeleteConfirm.value = true
  }

  async function confirmDelete() {
    if (!deletingJugador.value) return
    loading.delete = true
    try {
      await playerAPI.delete(deletingJugador.value.jugadoresId)
      deleteFeedback.type = 'success'
      deleteFeedback.message = 'Jugador eliminado exitosamente'
      showDeleteConfirm.value = false
      deletingJugador.value = null
      await fetchJugadores()
    } catch (error) {
      deleteFeedback.type = 'error'
      deleteFeedback.message = error instanceof Error ? error.message : 'Error al eliminar jugador'
    } finally {
      loading.delete = false
    }
  }

  return {
    jugadores,
    loading,
    feedback,
    formFeedback,
    deleteFeedback,
    showAddForm,
    showDeleteConfirm,
    editingJugador,
    deletingJugador,
    form,
    formTitle,
    fetchJugadores,
    openAddForm,
    openEditForm,
    submitForm,
    openDeleteConfirm,
    confirmDelete,
  }
}
