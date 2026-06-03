<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getDocumentDetail } from '@/api/document'
import { useToast } from '@/composables/useToast'
import type { Document } from '@/types'
import type { DocumentDetailResponse } from '@/api/document'

const route = useRoute()
const router = useRouter()
const docId = Number(route.params.id)
const toast = useToast()

const loading = ref(true)
const detail = ref<DocumentDetailResponse | null>(null)
const doc = ref<Document | null>(null)

function getStatusText(status: string): string {
  const map: Record<string, string> = {
    UPLOADED: '待处理',
    PROCESSING: '处理中',
    COMPLETED: '已完成',
    FAILED: '失败'
  }
  return map[status] || status
}

function getStatusClass(status: string): string {
  const map: Record<string, string> = {
    UPLOADED: 'status-pending',
    PROCESSING: 'status-processing',
    COMPLETED: 'status-done',
    FAILED: 'status-fail'
  }
  return map[status] || ''
}

function formatFileSize(bytes: number | null): string {
  if (!bytes) return '未知'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

onMounted(async () => {
  loading.value = true
  try {
    const { data } = await getDocumentDetail(docId)
    detail.value = data
    doc.value = data.document
  } catch (err: any) {
    toast.error(err.response?.data?.message || '加载文档失败')
    router.back()
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="doc-page">
    <header>
      <button class="btn-back" @click="router.back()">← 返回</button>
      <div v-if="doc">
        <h1>{{ doc.originalName }}</h1>
        <div class="meta-row">
          <span class="badge" :class="doc.fileType.toLowerCase()">{{ doc.fileType }}</span>
          <span class="status-badge" :class="getStatusClass(doc.status)">
            {{ getStatusText(doc.status) }}
          </span>
          <span class="meta-text">{{ formatFileSize((doc as any).fileSize) }}</span>
          <span class="meta-text">{{ new Date(doc.createdAt).toLocaleString() }}</span>
        </div>
      </div>
    </header>

    <main>
      <div v-if="loading" class="empty">加载中...</div>

      <template v-else-if="doc && detail">
        <!-- AI Summary -->
        <section class="card summary-card">
          <h2>📝 AI 摘要</h2>
          <p v-if="doc.summary">{{ doc.summary }}</p>
          <p v-else-if="doc.status === 'PROCESSING'" class="placeholder">摘要生成中...</p>
          <p v-else-if="doc.status === 'FAILED'" class="placeholder">摘要生成失败</p>
          <p v-else class="placeholder">文档处理完成后将自动生成摘要</p>
        </section>

        <!-- Chunks -->
        <section class="card chunks-card">
          <h2>📄 文档分块 ({{ detail.chunkCount }} 块)</h2>
          <div v-if="detail.chunks.length === 0" class="placeholder">
            文档尚未完成分块处理，请等待处理完成。
          </div>
          <div v-else class="chunk-list">
            <div v-for="(chunk, i) in detail.chunks" :key="i" class="chunk-item">
              <div class="chunk-index">#{{ chunk.chunkIndex + 1 }}</div>
              <div class="chunk-content">{{ chunk.content }}</div>
            </div>
          </div>
        </section>
      </template>
    </main>
  </div>
</template>

<style scoped>
.doc-page { min-height: 100vh; background: #f0f2f5; }
header {
  padding: 20px 32px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
header h1 { font-size: 1.3rem; margin: 8px 0 4px; word-break: break-all; }
.meta-row { display: flex; gap: 10px; align-items: center; margin-top: 8px; flex-wrap: wrap; }
.meta-text { color: #888; font-size: 0.85rem; }

.btn-back {
  padding: 6px 12px;
  background: transparent;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  color: #666;
}

.badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
}
.badge.pdf { background: #fee2e2; color: #dc2626; }
.badge.markdown { background: #dbeafe; color: #2563eb; }

.status-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 0.75rem;
  font-weight: 600;
}
.status-pending { background: #fef3c7; color: #d97706; }
.status-processing { background: #dbeafe; color: #2563eb; }
.status-done { background: #d1fae5; color: #059669; }
.status-fail { background: #fee2e2; color: #dc2626; }

main { padding: 24px 32px; max-width: 900px; margin: 0 auto; }

.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  padding: 24px;
  margin-bottom: 20px;
}
.card h2 { margin: 0 0 12px; font-size: 1.1rem; }

.summary-card p { line-height: 1.7; color: #444; font-size: 0.95rem; margin: 0; }

.placeholder { color: #999; font-style: italic; }

.chunk-list { display: flex; flex-direction: column; gap: 16px; }
.chunk-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}
.chunk-index {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  background: #4f46e5;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
}
.chunk-content {
  flex: 1;
  font-size: 0.9rem;
  line-height: 1.7;
  color: #444;
  white-space: pre-wrap;
  word-break: break-word;
}

.empty {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}
</style>
