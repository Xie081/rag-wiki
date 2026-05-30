-- V3: Add IVFFlat index on document_chunks embedding column
-- Requires some data first, so we create it with a small number of lists
-- For production, tune lists = number of rows / 1000

-- IVFFlat index for cosine similarity (vector_l2_ops for L2 distance, we use cosine via <=>)
-- PGVector's <=> operator returns cosine distance
CREATE INDEX IF NOT EXISTS idx_chunks_embedding
    ON document_chunks
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 10);
