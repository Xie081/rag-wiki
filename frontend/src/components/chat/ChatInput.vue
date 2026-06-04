<script setup lang="ts">
import { ref, computed } from 'vue'

const MAX_LENGTH = 2000

const props = defineProps<{
  disabled: boolean
  isStreaming: boolean
}>()

const emit = defineEmits<{
  send: [question: string]
  stop: []
}>()

const input = ref('')
const textareaRef = ref<HTMLTextAreaElement>()

const charCount = computed(() => input.value.length)
const isOverLimit = computed(() => charCount.value > MAX_LENGTH)

function handleSubmit() {
  const text = input.value.trim()
  if (!text || props.disabled || isOverLimit.value) return
  emit('send', text)
  input.value = ''
}

function autoResize() {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 160) + 'px'
}
</script>

<template>
  <div class="chat-input-wrap">
    <div class="chat-input-box">
      <textarea
        ref="textareaRef"
        v-model="input"
        placeholder="向 AI 提问，基于知识库文档获得答案..."
        :disabled="disabled"
        rows="1"
        @input="autoResize"
        @keydown.enter.exact.prevent="handleSubmit"
      />
      <div class="input-footer">
        <span class="char-count" :class="{ warn: charCount > MAX_LENGTH * 0.8, over: isOverLimit }">
          {{ charCount }} / {{ MAX_LENGTH }}
        </span>
        <div class="input-actions">
          <button v-if="isStreaming" class="btn-stop" @click="emit('stop')">
            <span class="stop-icon" /> 停止生成
          </button>
          <button
            v-else
            class="btn-send"
            :disabled="!input.trim() || disabled || isOverLimit"
            @click="handleSubmit"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-input-wrap {
  padding: 16px 20px;
  border-top: 1px solid var(--border-light);
  background: var(--surface);
}
.chat-input-box {
  background: var(--surface-alt);
  border: 1.5px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 12px 16px;
  transition: border-color 0.2s var(--ease);
}
.chat-input-box:focus-within {
  border-color: var(--sage);
  box-shadow: 0 0 0 4px var(--sage-light);
}

textarea {
  width: 100%;
  border: none;
  background: transparent;
  font-size: var(--text-base);
  font-family: var(--font);
  line-height: var(--leading);
  color: var(--text);
  outline: none;
  resize: none;
  max-height: 160px;
}
textarea::placeholder { color: var(--text-muted); }
textarea:disabled { opacity: 0.6; }

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.char-count {
  font-size: var(--text-xs);
  color: var(--text-muted);
  transition: color 0.2s;
}
.char-count.warn { color: var(--warning); }
.char-count.over { color: var(--error); font-weight: 600; }

.input-actions { display: flex; gap: 8px; }

.btn-send {
  width: 38px; height: 38px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--sage), #7a8f74);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.15s var(--ease), box-shadow 0.15s var(--ease), opacity 0.2s;
}
.btn-send:hover:not(:disabled) { transform: scale(1.08); box-shadow: var(--shadow-sm); }
.btn-send:disabled { opacity: 0.3; cursor: not-allowed; }

.btn-stop {
  padding: 8px 18px;
  border: 1.5px solid var(--error);
  border-radius: var(--radius);
  background: var(--rose-light);
  color: var(--error);
  font-size: var(--text-sm);
  font-weight: 500;
  font-family: var(--font);
  cursor: pointer;
  display: inline-flex; align-items: center; gap: 6px;
  transition: all 0.2s var(--ease);
}
.btn-stop:hover { background: #f5ddd8; }
.stop-icon {
  width: 8px; height: 8px;
  border-radius: 2px;
  background: var(--error);
}
</style>
