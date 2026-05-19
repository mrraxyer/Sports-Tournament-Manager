<script setup lang="ts">
import Navbar from '../components/Navbar.vue'
import AdminMatchManager from '../components/AdminMatchManager.vue'
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />
    <main class="max-w-6xl mx-auto px-6 py-8">
      <AdminMatchManager />
    </main>
  </div>
</template>
