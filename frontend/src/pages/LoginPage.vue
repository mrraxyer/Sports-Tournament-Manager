<script setup lang="ts">
import { reactive, ref, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

const dismissTimeoutId = ref<ReturnType<typeof setTimeout> | null>(null)

const form = reactive({
  correo: '',
  contrasena: '',
})

const feedback = reactive({
  type: '' as 'success' | 'error' | '',
  message: '',
})

async function submitLogin() {
  feedback.type = ''
  feedback.message = ''

  try {
    await auth.login({
      correo: form.correo.trim(),
      contrasena: form.contrasena,
    })

    feedback.type = 'success'
    feedback.message = 'Sesión iniciada correctamente. Redirigiendo…'
    form.contrasena = ''

    setTimeout(() => {
      router.push('/panel')
    }, 500)
  } catch (error) {
    feedback.type = 'error'
    feedback.message = error instanceof Error ? error.message : 'No fue posible iniciar sesión.'
    form.contrasena = ''

    // Cancel any pending timeout
    if (dismissTimeoutId.value) {
      clearTimeout(dismissTimeoutId.value)
      // When retrying after an error, extend the timeout duration
      dismissTimeoutId.value = setTimeout(() => {
        feedback.type = ''
        feedback.message = ''
      }, 8000)
    } else {
      // Auto-dismiss error after 5 seconds on initial error
      dismissTimeoutId.value = setTimeout(() => {
        feedback.type = ''
        feedback.message = ''
      }, 5000)
    }
  }
}

// Cleanup timeout on component unmount
onBeforeUnmount(() => {
  if (dismissTimeoutId.value) {
    clearTimeout(dismissTimeoutId.value)
  }
})
</script>

<template>
  <main class="min-h-screen grid place-items-center w-full px-3 py-6">
    <section class="w-full max-w-md bg-white border border-gray-300 rounded-lg p-6">
      <header class="mb-4">
        <h2 class="text-xl mb-1">Iniciar sesión</h2>
      </header>

      <form class="space-y-4" @submit.prevent="submitLogin">
        <label class="block space-y-1.5">
          <span class="text-sm text-gray-600">Correo electrónico</span>
          <input v-model="form.correo" type="email" placeholder="usuario@correo.com" autocomplete="username"
            class="w-full px-3 py-2.5 border border-gray-300 rounded bg-white text-gray-900 focus:outline-2 focus:outline-blue-300 focus:outline-offset-1"
            required />
        </label>

        <label class="block space-y-1.5">
          <span class="text-sm text-gray-600">Contraseña</span>
          <input v-model="form.contrasena" type="password" placeholder="Ingresa tu contraseña"
            autocomplete="current-password"
            class="w-full px-3 py-2.5 border border-gray-300 rounded bg-white text-gray-900 focus:outline-2 focus:outline-blue-300 focus:outline-offset-1"
            required />
        </label>

        <p v-if="feedback.message" :class="{
          'px-3 py-2.5 rounded border bg-green-50 border-green-300 text-green-700': feedback.type === 'success',
          'px-3 py-2.5 rounded border bg-red-50 border-red-300 text-red-700': feedback.type === 'error'
        }">
          {{ feedback.message }}
        </p>

        <button
          class="w-full px-3.5 py-2.5 bg-blue-500 text-white border border-blue-500 rounded hover:bg-blue-700 hover:border-blue-700 disabled:opacity-70 cursor-pointer font-medium"
          type="submit" :disabled="auth.loading">
          <span v-if="auth.loading">Validando credenciales…</span>
          <span v-else>Entrar</span>
        </button>
      </form>
    </section>
  </main>
</template>
