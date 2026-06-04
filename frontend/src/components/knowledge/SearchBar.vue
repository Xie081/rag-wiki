<script setup lang="ts">
import { ref, watch } from 'vue'
import api from '@/api'

const props = defineProps<{ knowledgeBaseId: number }>()

const query = ref('')
const results = ref<any[]>([])
const searching = ref(false)
const showResults = ref(false)

let debounceTimer: ReturnType<typeof setTimeout>

watch(query, (val) => {
  clearTimeout(debounceTimer)
  if (!val.trim()) { results.value = []; showResults.value = false; return }
  debounceTimer = setTimeout(() => doSearch(), 400)
})

async function doSearch() {
  if (!query.value.trim()) return
  searching.value = true
  try {
    const { data } = await api.get(`/search/${props.knowledgeBaseId}`, { params: { q: query.value } })
    results.value = data.results
    showResults.value = true
  } finally { searching.value = false }
}

function highlightText(text: string, q: string): string {
  const escaped = q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return text.replace(new RegExp(`(${escaped})`, 'gi'), '<mark>$1</mark>')
}
</script>

<template>
  <div class="search-bar">
    <div class="search-input-wrap">
      <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
      <input v-model="query" type="text" placeholder="搜索知识库内容..." />
      <span v-if="searching" class="search-spin" />
    </div>

    <div v-if="showResults && results.length > 0" class="search-dropdown" @mouseleave="showResults = false">
      <div class="results-count">{{ results.length }} 条结果</div>
      <div v-for="r in results" :key="r.chunkId" class="result-item">
        <div class="result-doc">📄 {{ r.documentTitle }}</div>
        <div class="result-snippet" v-html="highlightText(r.content, query)" />
      </div>
    </div>
    <div v-else-if="showResults && query.trim()" class="search-dropdown">
      <div class="results-empty">未找到相关内容</div>
    </div>
  </div>
</template>

<style scoped>
.search-bar { position: relative; }
.search-input-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius);
  background: var(--surface);
  transition: border-color 0.2s var(--ease);
}
.search-input-wrap:focus-within { border-color: var(--sage); }
.search-icon { color: var(--text-muted); flex-shrink: 0; }
.search-input-wrap input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: var(--text-sm);
  font-family: var(--font);
  color: var(--text);
  outline: none;
}
.search-input-wrap input::placeholder { color: var(--text-muted); }
.search-spin {
  width: 14px; height: 14px;
  border: 2px solid var(--border);
  border-top-color: var(--sage);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  flex-shrink: 0;
}
@keyframes spin { to { transform: rotate(360deg); } }

.search-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0; right: 0;
  background: var(--surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-light);
  z-index: 50;
  max-height: 400px;
  overflow-y: auto;
}
.results-count {
  padding: 10px 16px;
  font-size: var(--text-xs);
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  border-bottom: 1px solid var(--border-light);
}
.results-empty { padding: 24px; text-align: center; color: var(--text-muted); font-size: var(--text-sm); }
.result-item { padding: 14px 16px; cursor: pointer; border-bottom: 1px solid var(--border-light); transition: background 0.15s; }
.result-item:hover { background: var(--surface-alt); }
.result-item:last-child { border-bottom: none; }
.result-doc { font-size: var(--text-xs); color: var(--sage); margin-bottom: 4px; font-weight: 600; }
.result-snippet { font-size: var(--text-sm); color: var(--text-secondary); line-height: 1.5; }
.result-snippet :deep(mark) {
  background: #fdf2d0;
  color: var(--text);
  padding: 0 3px;
  border-radius: 2px;
}
</style>
