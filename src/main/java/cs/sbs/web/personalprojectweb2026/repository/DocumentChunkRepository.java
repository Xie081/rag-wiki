package cs.sbs.web.personalprojectweb2026.repository;

import cs.sbs.web.personalprojectweb2026.model.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocumentIdOrderByChunkIndex(Long documentId);

    List<DocumentChunk> findByKbId(Long kbId);

    void deleteByDocumentId(Long documentId);

    void deleteByKbId(Long kbId);

    /**
     * Cosine similarity search using PGVector <=> operator.
     * Returns top-k most similar chunks for a given knowledge base.
     * embedding_str must be a PGVector-compatible string like '[0.1,0.2,...]'
     */
    @Query(value = """
        SELECT dc.*, 1 - (dc.embedding <=> :embedding::vector) AS similarity
        FROM document_chunks dc
        WHERE dc.kb_id = :kbId
        ORDER BY dc.embedding <=> :embedding::vector
        LIMIT :limit
        """, nativeQuery = true)
    List<DocumentChunk> findSimilarChunks(
            @Param("embedding") String embeddingStr,
            @Param("kbId") Long kbId,
            @Param("limit") int limit);
}
