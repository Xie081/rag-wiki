import api from './index'
import type { KnowledgeBase } from '@/types'

export function getKnowledgeBases() {
  return api.get<KnowledgeBase[]>('/knowledge-bases')
}

export function getKnowledgeBase(id: number) {
  return api.get<KnowledgeBase>(`/knowledge-bases/${id}`)
}

export function createKnowledgeBase(data: { name: string; description: string }) {
  return api.post<KnowledgeBase>('/knowledge-bases', data)
}

export function deleteKnowledgeBase(id: number) {
  return api.delete(`/knowledge-bases/${id}`)
}
