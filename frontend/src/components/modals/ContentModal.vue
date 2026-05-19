<script setup lang="ts">
import BaseModal from './BaseModal.vue'

const props = defineProps<{
  modelValue: boolean
  title: string
  closeLabel?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  close: []
}>()

function close() {
  emit('update:modelValue', false)
  emit('close')
}
</script>

<template>
  <BaseModal :model-value="modelValue" size="lg" @update:model-value="$emit('update:modelValue', $event)"
    @close="close">
    <div class="flex justify-between items-center p-6 border-b border-gray-200 shrink-0">
      <h2 class="text-2xl font-bold text-gray-900">{{ title }}</h2>
      <div class="flex items-center gap-3">
        <slot name="actions" />
        <button type="button" @click="close" aria-label="Close"
          class="text-gray-500 hover:text-gray-700 text-2xl font-bold leading-none">
          ✕
        </button>
      </div>
    </div>
    <div class="flex-1 overflow-y-auto p-6">
      <slot />
    </div>
    <div class="border-t border-gray-200 p-6 flex justify-end shrink-0">
      <button type="button" @click="close"
        class="bg-gray-500 hover:bg-gray-600 text-white font-bold py-2 px-6 rounded transition">
        {{ closeLabel ?? 'Cerrar' }}
      </button>
    </div>
  </BaseModal>
</template>
