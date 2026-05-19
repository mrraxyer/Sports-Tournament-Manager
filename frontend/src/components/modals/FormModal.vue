<script setup lang="ts">
import BaseModal from './BaseModal.vue'

const props = defineProps<{
  modelValue: boolean
  title: string
  submitLabel?: string
  cancelLabel?: string
  loading?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: []
  cancel: []
}>()

function cancel() {
  emit('update:modelValue', false)
  emit('cancel')
}
</script>

<template>
  <BaseModal :model-value="modelValue" size="md" @update:model-value="$emit('update:modelValue', $event)"
    @close="cancel">
    <div class="flex justify-between items-center p-6 border-b border-gray-200 shrink-0">
      <h3 class="text-xl font-bold text-gray-900">{{ title }}</h3>
      <button type="button" @click="cancel" class="text-gray-500 hover:text-gray-700 text-2xl font-bold leading-none">
        ✕
      </button>
    </div>
    <form @submit.prevent="$emit('submit')" class="p-6 overflow-y-auto">
      <slot name="feedback" />
      <slot />
      <div class="flex justify-end gap-3 mt-6">
        <button type="button" @click="cancel" :disabled="loading"
          class="bg-gray-500 hover:bg-gray-600 disabled:bg-gray-400 text-white font-bold py-2 px-6 rounded transition">
          {{ cancelLabel ?? 'Cancelar' }}
        </button>
        <button type="submit" :disabled="loading"
          class="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-bold py-2 px-6 rounded transition">
          {{ loading ? 'Guardando...' : (submitLabel ?? 'Guardar') }}
        </button>
      </div>
    </form>
  </BaseModal>
</template>
