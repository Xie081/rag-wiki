<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getKnowledgeBases, createKnowledgeBase, deleteKnowledgeBase } from '@/api/knowledgeBase'
import type { KnowledgeBase } from '@/types'

const authStore = useAuthStore()
const router = useRouter()

const knowledgeBases = ref<KnowledgeBase[]>([])
const loading = ref(true)
const showCreate = ref(false)
const newName = ref('')
const newDesc = ref('')

async function loadKBs() {
  loading.value = true
  try {
    const { data } = await getKnowledgeBases()
    knowledgeBases.value = data
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!newName.value.trim()) return
  await createKnowledgeBase({ name: newName.value, description: newDesc.value })
  newName.value = ''
  newDesc.value = ''
  showCreate.value = false
  await loadKBs()
}

async function handleDelete(id: number) {
  if (!confirm('确定删除该知识库及其所有文档？')) return
  await deleteKnowledgeBase(id)
  await loadKBs()
}

function goToKB(id: number) {
  router.push(`/knowledge-base/${id}`)
}

onMounted(loadKBs)
</script>

<template>
  <div class="dashboard">
    <header>
      <h1>📚 我的知识库</h1>
      <div class="header-right">
        <button class="btn-primary" @click="showCreate = true">+ 新建知识库</button>
        <button class="btn-logout" @click="authStore.logout()">退出</button>
      </div>
    </header>

    <main>
      <!-- Create modal -->
      <div v-if="showCreate" class="modal-overlay" @click.self="showCreate = false">
        <div class="modal">
          <h3>新建知识库</h3>
          <input v-model="newName" placeholder="知识库名称" @keyup.enter="handleCreate" />
          <textarea v-model="newDesc" placeholder="描述（可选）" rows="3" />
          <div class="modal-actions">
            <button class="btn-cancel" @click="showCreate = false">取消</button>
            <button class="btn-primary" @click="handleCreate" :disabled="!newName.trim()">创建</button>
          </div>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="empty">加载中...</div>

      <!-- Empty -->
      <div v-else-if="knowledgeBases.length === 0" class="empty">
        <p>📭 还没有知识库</p>
        <button class="btn-primary" @click="showCreate = true">创建第一个知识库</button>
      </div>

      <!-- KB Grid -->
      <div v-else class="kb-grid">
        <div
          v-for="kb in knowledgeBases"
          :key="kb.id"
          class="kb-card"
          @click="goToKB(kb.id)"
        >
          <div class="kb-icon">📁</div>
          <div class="kb-info">
            <h3>{{ kb.name }}</h3>
            <p>{{ kb.description || '暂无描述' }}</p>
            <span class="kb-date">{{ new Date(kb.createdAt).toLocaleDateString() }}</span>
          </div>
          <button class="btn-delete" @click.stop="handleDelete(kb.id)" title="删除">✕</button>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.dashboard { min-height: 100vh; background: #f0f2f5; }
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 32px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
header h1 { font-size: 1.4rem; margin: 0; }
.header-right { display: flex; gap: 12px; }

.btn-primary {
  padding: 8px 20px;
  background: #4f46e5;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  cursor: pointer;
}
.btn-primary:hover { background: #4338ca; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-logout {
  padding: 8px 16px;
  background: transparent;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
}
.btn-delete {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  background: #fee2e2;
  color: #ef4444;
  border: none;
  border-radius: 50%;
  font-size: 0.75rem;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}
.kb-card:hover .btn-delete { opacity: 1; }

main { padding: 32px; max-width: 1200px; margin: 0 auto; }

.kb-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}
.kb-card {
  position: relative;
  display: flex;
  gap: 16px;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}
.kb-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); transform: translateY(-2px); }
.kb-icon { font-size: 2rem; }
.kb-info { flex: 1; min-width: 0; }
.kb-info h3 { margin: 0 0 6px; font-size: 1.1rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.kb-info p { margin: 0 0 8px; color: #888; font-size: 0.85rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.kb-date { color: #aaa; font-size: 0.8rem; }

.empty {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}
.empty p { font-size: 1.2rem; margin-bottom: 16px; }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal {
  background: #fff;
  padding: 32px;
  border-radius: 12px;
  width: 100%;
  max-width: 440px;
}
.modal h3 { margin: 0 0 16px; }
.modal input, .modal textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.95rem;
  margin-bottom: 12px;
  box-sizing: border-box;
  font-family: inherit;
}
.modal-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 8px; }
.btn-cancel {
  padding: 8px 20px;
  background: #f3f4f6;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
}
</style>
