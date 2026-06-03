<script setup lang="ts">
import { computed, ref } from 'vue'
import { marked } from 'marked'
import { useToast } from '@/composables/useToast'
import type { ChatMessage as ChatMessageType, CitationSource } from '@/types'

const props = defineProps<{
  message: ChatMessageType
  sources?: CitationSource[]
}>()

const toast = useToast()
const copied = ref(false)

const isUser = computed(() => props.message.role === 'user')

// Configure marked for safe rendering
marked.setOptions({
  breaks: true,
  gfm: true
})

function renderMarkdown(text: string): string {
  if (!text) return isUser.value ? '' : '思考中...'
  // Simple XSS sanitization: strip script tags
  const sanitized = text.replace(/<script[^>]*>[\s\S]*?<\/script>/gi, '')
  return marked.parse(sanitized) as string
}

async function copyContent() {
  try {
    await navigator.clipboard.writeText(props.message.content)
    copied.value = true
    toast.success('已复制到剪贴板')
    setTimeout(() => { copied.value = false }, 2000)
  } catch {
    toast.error('复制失败')
  }
}
</script>

<template>
  <div class="message" :class="{ user: isUser, assistant: !isUser }">
    <div class="avatar">{{ isUser ? '👤' : '🤖' }}</div>
    <div class="content">
      <div
        v-if="isUser"
        class="text user-text"
      >{{ message.content }}</div>
      <div
        v-else
        class="text assistant-text markdown-body"
        v-html="renderMarkdown(message.content)"
      />
      <div v-if="message.role === 'assistant' && message.content" class="msg-actions">
        <button class="btn-copy" :class="{ copied }" @click="copyContent" :title="copied ? '已复制' : '复制回答'">
          {{ copied ? '✓ 已复制' : '📋 复制' }}
        </button>
      </div>
      <div v-if="sources && sources.length > 0 && message.role === 'assistant'" class="sources">
        <div class="sources-title">📎 参考来源：</div>
        <div v-for="(src, i) in sources" :key="i" class="source-item">
          <span class="source-doc">{{ src.documentTitle }}</span>
          <span class="source-snippet">{{ src.snippet }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.message.user { flex-direction: row-reverse; }
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  flex-shrink: 0;
}
.user .avatar { background: #e0e7ff; }
.assistant .avatar { background: #f0fdf4; }
.content { max-width: 80%; }
.user .content { text-align: right; }
.text {
  padding: 10px 16px;
  border-radius: 12px;
  font-size: 0.9rem;
  line-height: 1.6;
  word-break: break-word;
}
.user-text { background: #4f46e5; color: #fff; border-bottom-right-radius: 4px; }
.assistant-text { background: #f3f4f6; color: #333; border-bottom-left-radius: 4px; }

.msg-actions { margin-top: 4px; }
.btn-copy {
  padding: 2px 10px;
  font-size: 0.75rem;
  background: transparent;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  color: #888;
  transition: all 0.2s;
}
.btn-copy:hover { color: #4f46e5; border-color: #4f46e5; }
.btn-copy.copied { color: #059669; border-color: #a7f3d0; background: #d1fae5; }

/* Markdown body styles */
.markdown-body h1, .markdown-body h2, .markdown-body h3 {
  margin: 8px 0 4px;
  font-size: 1.05rem;
}
.markdown-body p { margin: 4px 0; }
.markdown-body ul, .markdown-body ol { margin: 4px 0; padding-left: 20px; }
.markdown-body li { margin: 2px 0; }
.markdown-body code {
  background: #e5e7eb;
  padding: 1px 5px;
  border-radius: 3px;
  font-size: 0.85rem;
}
.markdown-body pre {
  background: #1f2937;
  color: #f9fafb;
  padding: 12px 16px;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 0.85rem;
  margin: 8px 0;
}
.markdown-body pre code {
  background: transparent;
  padding: 0;
  color: inherit;
}
.markdown-body blockquote {
  border-left: 3px solid #d1d5db;
  padding-left: 12px;
  color: #6b7280;
  margin: 8px 0;
}
.markdown-body table {
  border-collapse: collapse;
  width: 100%;
  margin: 8px 0;
}
.markdown-body th, .markdown-body td {
  border: 1px solid #d1d5db;
  padding: 6px 10px;
  text-align: left;
}
.markdown-body th { background: #f3f4f6; }
.markdown-body strong { font-weight: 600; }
.markdown-body a { color: #4f46e5; }

.sources {
  margin-top: 8px;
  padding: 12px;
  background: #fffbeb;
  border-radius: 8px;
  font-size: 0.8rem;
}
.sources-title { font-weight: 600; margin-bottom: 6px; color: #92400e; }
.source-item {
  padding: 4px 0;
  border-bottom: 1px dashed #fde68a;
}
.source-item:last-child { border-bottom: none; }
.source-doc { color: #b45309; font-weight: 500; display: block; }
.source-snippet { color: #78716c; font-size: 0.75rem; }
</style>
