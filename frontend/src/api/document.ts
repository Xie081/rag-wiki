import api from './index'
import type { Document } from '@/types'

export function getDocuments(kbId: number) {
  return api.get<Document[]>(`/knowledge-bases/${kbId}/documents`)
}

export function getDocument(id: number) {
  return api.get<Document>(`/documents/${id}`)
}

export function uploadDocument(kbId: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return api.post<Document>(`/knowledge-bases/${kbId}/documents`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function deleteDocument(id: number) {
  return api.delete(`/documents/${id}`)
}

export interface DocumentDetailResponse {
  document: Document
  chunks: Array<{ chunkIndex: number; content: string }>
  chunkCount: number
}

export function getDocumentDetail(id: number) {
  return api.get<DocumentDetailResponse>(`/documents/${id}/detail`)
}
