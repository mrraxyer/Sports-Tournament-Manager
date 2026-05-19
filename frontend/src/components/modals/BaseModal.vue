<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  modelValue: boolean
  size?: 'sm' | 'md' | 'lg'
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  close: []
}>()

const sizeClass: Record<string, string> = {
  sm: 'max-w-sm',
  md: 'max-w-md',
  lg: 'max-w-2xl',
}

function close() {
  emit('update:modelValue', false)
  emit('close')
}

function onOverlayClick(e: MouseEvent) {
  if (e.target === e.currentTarget) close()
}

function onKeydown(e: KeyboardEvent) {
  if (props.modelValue && e.key === 'Escape') close()
}

onMounted(() => document.addEventListener('keydown', onKeydown))
onUnmounted(() => document.removeEventListener('keydown', onKeydown))
</script>

<template>
  <Teleport to="body">
    <div v-if="modelValue" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click="onOverlayClick">
      <div :class="[
        'bg-white rounded-lg shadow-lg w-full mx-4 flex flex-col max-h-[90vh] overflow-hidden',
        sizeClass[size ?? 'md'],
      ]">
        <slot />
      </div>
    </div>
  </Teleport>
</template>
