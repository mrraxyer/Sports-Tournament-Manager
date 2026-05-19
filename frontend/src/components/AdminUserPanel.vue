<script setup lang="ts">
import { onMounted, reactive, computed, ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import api from '../services/api'
import FormModal from './modals/FormModal.vue'

const auth = useAuthStore()

interface Usuario {
  usuariosId: number
  nombre: string
  correo: string
  rol: {
    rolesId: number
    nombre: string
  }
}

interface Rol {
  rolesId: number
  nombre: string
}

interface FormData {
  nombre: string
  correo: string
  contrasena: string
  rolId: number | null
}

const usuarios = reactive<Usuario[]>([])
const roles = reactive<Rol[]>([])
const loading = reactive({ fetch: false, submit: false })
const feedback = reactive({
  type: '' as 'success' | 'error' | '',
  message: '',
})

const form = reactive<FormData>({
  nombre: '',
  correo: '',
  contrasena: '',
  rolId: null,
})

const showModal = ref(false)
const isAdmin = computed(() => auth.session?.usuario.rol?.toUpperCase() === 'ADMIN')

async function fetchUsuarios() {
  loading.fetch = true
  try {
    const response = await api.get<{ data: Usuario[] }>('/usuarios')
    usuarios.length = 0
    usuarios.push(...(response.data.data || []))
  } catch (error) {
    feedback.type = 'error'
    feedback.message = error instanceof Error ? error.message : 'Error al cargar usuarios'
  } finally {
    loading.fetch = false
  }
}

async function fetchRoles() {
  try {
    const response = await api.get<{ data: Rol[] }>('/roles')
    roles.length = 0
    roles.push(...(response.data.data || []))
  } catch (error) {
    feedback.type = 'error'
    feedback.message = error instanceof Error ? error.message : 'Error al cargar roles'
  }
}

async function submitForm() {
  feedback.type = ''
  feedback.message = ''

  if (!form.nombre.trim()) {
    feedback.type = 'error'
    feedback.message = 'Nombre es requerido'
    return
  }

  if (!form.correo.trim()) {
    feedback.type = 'error'
    feedback.message = 'Correo es requerido'
    return
  }

  if (!form.contrasena.trim()) {
    feedback.type = 'error'
    feedback.message = 'Contraseña es requerida'
    return
  }

  if (form.rolId === null) {
    feedback.type = 'error'
    feedback.message = 'Rol es requerido'
    return
  }

  loading.submit = true
  try {
    const payload = {
      nombre: form.nombre.trim(),
      correo: form.correo.trim(),
      contrasena: form.contrasena,
      rolId: form.rolId,
    }

    const response = await api.post('/usuarios/crear', payload)
    const message = response.data.message || 'Usuario creado exitosamente'

    feedback.type = 'success'
    feedback.message = message
    form.nombre = ''
    form.correo = ''
    form.contrasena = ''
    form.rolId = null
    showModal.value = false

    await fetchUsuarios()
  } catch (error) {
    feedback.type = 'error'
    feedback.message = error instanceof Error ? error.message : 'Error al crear usuario'
  } finally {
    loading.submit = false
  }
}

async function eliminarUsuario(usuarioId: number) {
  const currentUserId = auth.session?.usuario.usuariosId
  if (currentUserId === usuarioId) {
    feedback.type = 'error'
    feedback.message = 'No puedes eliminar tu propia cuenta'
    return
  }

  if (!confirm('¿Estás seguro de que deseas eliminar este usuario?')) {
    return
  }

  feedback.type = ''
  feedback.message = ''

  try {
    await api.delete(`/usuarios/${usuarioId}`)
    feedback.type = 'success'
    feedback.message = 'Usuario eliminado exitosamente'
    await fetchUsuarios()
  } catch (error) {
    feedback.type = 'error'
    feedback.message = error instanceof Error ? error.message : 'Error al eliminar usuario'
  }
}

onMounted(() => {
  auth.hydrateSession()
  fetchRoles()
  fetchUsuarios()
})
</script>

<template>
  <div v-if="isAdmin" class="space-y-6">
    <!-- Create User Form -->
    <section class="bg-white border border-gray-300 rounded-lg p-6">
      <header class="mb-6">
        <h2 class="text-2xl font-semibold text-gray-900">Crear Nuevo Usuario</h2>
      </header>

      <button @click="showModal = true"
        class="px-4 py-2.5 bg-blue-600 text-white border border-blue-600 rounded hover:bg-blue-700 disabled:opacity-60 cursor-pointer font-medium text-sm">
        Crear Usuario
      </button>
    </section>

    <!-- Alert -->
    <div v-if="feedback.message" :class="{
      'bg-green-50 border-green-300 text-green-700': feedback.type === 'success',
      'bg-red-50 border-red-300 text-red-700': feedback.type === 'error',
    }" class="px-4 py-3 rounded border text-sm">
      {{ feedback.message }}
    </div>

    <!-- Users Table -->
    <section class="bg-white border border-gray-300 rounded-lg overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-300 bg-gray-50">
        <h3 class="text-xl font-semibold text-gray-900">Usuarios</h3>
      </div>

      <div v-if="loading.fetch" class="px-6 py-8 text-center text-gray-600">
        Cargando usuarios…
      </div>

      <div v-else-if="usuarios.length === 0" class="px-6 py-8 text-center text-gray-600">
        No hay usuarios creados aún
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="bg-gray-50 border-b border-gray-300">
              <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">ID</th>
              <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Nombre</th>
              <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Correo</th>
              <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Rol</th>
              <th class="px-6 py-3 text-center text-sm font-semibold text-gray-900">Acción</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="usuario in usuarios" :key="usuario.usuariosId"
              class="border-b border-gray-200 hover:bg-gray-50 transition-colors">
              <td class="px-6 py-3 text-sm text-gray-900">{{ usuario.usuariosId }}</td>
              <td class="px-6 py-3 text-sm text-gray-900">{{ usuario.nombre }}</td>
              <td class="px-6 py-3 text-sm text-gray-700">{{ usuario.correo }}</td>
              <td class="px-6 py-3 text-sm text-gray-700 font-medium">
                {{ usuario.rol?.nombre ?? 'Sin rol' }}
              </td>
              <td class="px-6 py-3 text-center">
                <button
                  :disabled="usuario.usuariosId === auth.session?.usuario.usuariosId"
                  @click="eliminarUsuario(usuario.usuariosId)"
                  :class="usuario.usuariosId === auth.session?.usuario.usuariosId ? 'opacity-50 cursor-not-allowed' : 'hover:bg-red-700'"
                  class="px-3 py-1.5 bg-red-600 text-white text-sm rounded cursor-pointer disabled:cursor-not-allowed">
                  Eliminar
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- Create User Modal -->
    <FormModal v-model="showModal" title="Crear Usuario" @submit="submitForm">
      <div class="space-y-4">
        <label class="block">
          <span class="text-sm font-medium text-gray-700">Nombre</span>
          <input v-model="form.nombre" type="text" placeholder="Ej: Juan Pérez"
            class="mt-2 w-full px-3 py-2 border border-gray-300 rounded bg-white text-gray-900 focus:outline-2 focus:outline-blue-400 focus:outline-offset-1" />
        </label>

        <label class="block">
          <span class="text-sm font-medium text-gray-700">Correo</span>
          <input v-model="form.correo" type="email" placeholder="Ej: juan@example.com"
            class="mt-2 w-full px-3 py-2 border border-gray-300 rounded bg-white text-gray-900 focus:outline-2 focus:outline-blue-400 focus:outline-offset-1" />
        </label>

        <label class="block">
          <span class="text-sm font-medium text-gray-700">Contraseña</span>
          <input v-model="form.contrasena" type="password"
            class="mt-2 w-full px-3 py-2 border border-gray-300 rounded bg-white text-gray-900 focus:outline-2 focus:outline-blue-400 focus:outline-offset-1" />
        </label>

        <label class="block">
          <span class="text-sm font-medium text-gray-700">Rol</span>
          <select v-model.number="form.rolId"
            class="mt-2 w-full px-3 py-2 border border-gray-300 rounded bg-white text-gray-900 focus:outline-2 focus:outline-blue-400 focus:outline-offset-1">
            <option :value="null" disabled>Selecciona un rol</option>
            <option v-for="rol in roles" :key="rol.rolesId" :value="rol.rolesId">
              {{ rol.nombre }}
            </option>
          </select>
        </label>

        <div v-if="feedback.message" :class="{
          'bg-green-50 border-green-300 text-green-700': feedback.type === 'success',
          'bg-red-50 border-red-300 text-red-700': feedback.type === 'error',
        }" class="px-3 py-2 rounded border text-sm">
          {{ feedback.message }}
        </div>
      </div>
    </FormModal>
  </div>

  <!-- Non-Admin: Access Denied -->
  <div v-else class="px-6 py-8 text-center text-gray-600">
    Acceso restringido a administradores.
  </div>
</template>
