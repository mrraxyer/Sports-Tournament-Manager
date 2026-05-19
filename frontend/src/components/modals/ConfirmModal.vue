<script setup lang="ts">
import BaseModal from './BaseModal.vue'

const props = defineProps<{
  modelValue: boolean
  title: string
  message: string
  detail?: string
  confirmLabel?: string
  cancelLabel?: string
  variant?: 'danger' | 'primary'
  loading?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: []
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
    <div class="p-6 border-b border-gray-300 shrink-0">
      <h3 class="text-xl font-semibold text-gray-900">{{ title }}</h3>
    </div>
    <div class="p-6">
      <p class="text-gray-700 mb-1">{{ message }}</p>
      <p v-if="detail" class="text-gray-500 text-sm">{{ detail }}</p>
    </div>
    <div class="border-t border-gray-300 p-6 flex justify-end gap-3 shrink-0">
      <button @click="cancel" :disabled="loading"
        class="px-4 py-2 bg-white text-gray-700 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-60 cursor-pointer font-medium text-sm">
        {{ cancelLabel ?? 'Cancelar' }}
      </button>
      <button @click="$emit('confirm')" :disabled="loading" :class="[
        'px-4 py-2 text-white border rounded disabled:opacity-60 cursor-pointer font-medium text-sm',
        (variant ?? 'danger') === 'danger'
          ? 'bg-red-600 border-red-600 hover:bg-red-700 hover:border-red-700'
          : 'bg-blue-600 border-blue-600 hover:bg-blue-700 hover:border-blue-700',
      ]">
        {{ loading ? `${confirmLabel ?? 'Confirmar'}…` : (confirmLabel ?? 'Confirmar') }}
      </button>
    </div>
  </BaseModal>
</template>
