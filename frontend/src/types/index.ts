// User
export interface User {
  id: number
  username: string
  email: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email: string
}

export interface AuthResponse {
  token: string
  user: User
}

// Knowledge Base
export interface KnowledgeBase {
  id: number
  name: string
  description: string
  createdAt: string
}

// Document
export interface Document {
  id: number
  title: string
  fileType: 'PDF' | 'MARKDOWN'
  originalName: string
  status: 'UPLOADED' | 'PROCESSING' | 'COMPLETED' | 'FAILED'
  summary: string | null
  createdAt: string
}

// Chat
export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  sources?: CitationSource[]
  timestamp: string
}

export interface CitationSource {
  documentTitle: string
  snippet: string
}

export interface ChatRequest {
  knowledgeBaseId: number
  question: string
}
