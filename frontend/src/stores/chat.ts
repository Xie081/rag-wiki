import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChatMessage } from '@/types'
import { loadHistory, syncHistory, type RemoteMessage } from '@/api/chat'

export const useChatStore = defineStore('chat', () => {
  const messages = ref<ChatMessage[]>([])
  let currentKbId: number | null = null
  let syncTimer: ReturnType<typeof setTimeout> | null = null

  function toRemote(): RemoteMessage[] {
    return messages.value.map(m => ({
      role: m.role,
      content: m.content,
      sources: m.sources ? JSON.stringify(m.sources) : undefined,
      timestamp: m.timestamp
    }))
  }

  function doSync() {
    if (syncTimer) { clearTimeout(syncTimer); syncTimer = null }
    if (currentKbId != null && messages.value.length > 0) {
      syncHistory(currentKbId, toRemote()).catch(e => {
        console.error('Chat sync failed:', e)
      })
    }
  }

  async function setKbId(kbId: number) {
    currentKbId = kbId
    try {
      const { data } = await loadHistory(kbId)
      messages.value = data.map((m: RemoteMessage, i: number) => ({
        id: `${kbId}_${i}`,
        role: m.role as 'user' | 'assistant',
        content: m.content,
        sources: m.sources ? JSON.parse(m.sources) : undefined,
        timestamp: m.timestamp
      }))
    } catch {
      messages.value = []
    }
  }

  function addMessage(message: ChatMessage) {
    messages.value.push(message)
    // User message: sync immediately
    if (syncTimer) clearTimeout(syncTimer)
    syncTimer = setTimeout(doSync, 500)
  }

  function updateLastAssistantMessage(content: string) {
    const last = messages.value[messages.value.length - 1]
    if (last && last.role === 'assistant') {
      last.content = content
      // Debounce stream tokens, force sync when done
      if (syncTimer) clearTimeout(syncTimer)
      syncTimer = setTimeout(doSync, 300)
    }
  }

  /** Call when streaming completes — force immediate sync */
  function flushSync() {
    doSync()
  }

  function clearMessages() {
    messages.value = []
    if (currentKbId != null) {
      syncHistory(currentKbId, []).catch(() => {})
    }
  }

  // Sync on page unload (refresh / close tab)
  if (typeof window !== 'undefined') {
    window.addEventListener('beforeunload', doSync)
  }

  return { messages, setKbId, addMessage, updateLastAssistantMessage, flushSync, clearMessages }
})
