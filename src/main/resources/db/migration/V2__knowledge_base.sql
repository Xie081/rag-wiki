-- V2: Knowledge base and document tables

CREATE TABLE knowledge_bases (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_kb_user_id ON knowledge_bases(user_id);

CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    file_type VARCHAR(20) NOT NULL CHECK (file_type IN ('PDF', 'MARKDOWN')),
    original_name VARCHAR(500) NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    file_size BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'UPLOADED' CHECK (status IN ('UPLOADED', 'PROCESSING', 'COMPLETED', 'FAILED')),
    summary TEXT,
    kb_id BIGINT NOT NULL REFERENCES knowledge_bases(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_doc_kb_id ON documents(kb_id);
CREATE INDEX idx_doc_user_id ON documents(user_id);
CREATE INDEX idx_doc_status ON documents(status);

CREATE TABLE document_chunks (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    chunk_index INT NOT NULL,
    embedding vector(1536),
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    kb_id BIGINT NOT NULL REFERENCES knowledge_bases(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chunk_document_id ON document_chunks(document_id);
CREATE INDEX idx_chunk_kb_id ON document_chunks(kb_id);

CREATE TABLE prompt_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    system_prompt TEXT NOT NULL,
    user_prompt_template TEXT NOT NULL,
    variables JSONB DEFAULT '[]',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Insert default RAG QA template
INSERT INTO prompt_templates (name, description, system_prompt, user_prompt_template, variables)
VALUES (
    'rag-qa',
    'RAG 问答默认模板',
    '你是一个专业的知识库助手。你的任务是严格根据提供的文档内容回答用户问题。\n\n规则：\n1. 只使用下面【参考文档】中的信息回答问题\n2. 如果文档中没有相关信息，请明确告知用户"该知识库中未找到相关信息"\n3. 回答时引用具体的文档名称和段落\n4. 保持回答简洁、准确、结构化\n5. 使用 Markdown 格式组织回答',
    '【参考文档】\n{context}\n\n【用户问题】\n{question}\n\n请根据以上参考文档回答用户的问题。',
    '["context", "question"]'::jsonb
);

-- Insert default summarize template
INSERT INTO prompt_templates (name, description, system_prompt, user_prompt_template, variables)
VALUES (
    'summarize',
    '文档摘要生成模板',
    '你是一个文档摘要专家。请为以下文档内容生成一个简洁的摘要。\n\n要求：\n1. 摘要长度控制在 200 字以内\n2. 突出文档的核心主题和关键结论\n3. 使用清晰的中文表达',
    '【文档标题】{document_title}\n【文档内容】{content}\n\n请生成摘要：',
    '["document_title", "content"]'::jsonb
);
