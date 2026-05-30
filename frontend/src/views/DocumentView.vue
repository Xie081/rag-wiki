<script setup lang="ts">
import { useRoute } from 'vue-router'
const route = useRoute()
</script>

<template>
  <div class="doc-view">
    <h1>文档 #{{ route.params.id }}</h1>
    <p>文档内容即将上线...</p>
  </div>
</template>
