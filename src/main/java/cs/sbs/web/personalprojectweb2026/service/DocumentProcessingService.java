package cs.sbs.web.personalprojectweb2026.service;

import cs.sbs.web.personalprojectweb2026.model.entity.Document;
import cs.sbs.web.personalprojectweb2026.model.entity.DocumentChunk;
import cs.sbs.web.personalprojectweb2026.repository.DocumentChunkRepository;
import cs.sbs.web.personalprojectweb2026.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessingService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final DocumentParserService parserService;
    private final TextChunker textChunker;
    private final EmbeddingService embeddingService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Process a document asynchronously: parse → chunk → embed → store.
     */
    @Async
    @Transactional
    public void processDocument(Long documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在: " + documentId));

        try {
            // Update status to PROCESSING
            doc.setStatus(Document.DocumentStatus.PROCESSING);
            documentRepository.save(doc);

            // Step 1: Parse
            Path filePath = Path.of(doc.getStoragePath());
            String text = parserService.parse(filePath, doc.getFileType().name());

            // Step 2: Chunk
            List<String> chunks = textChunker.chunk(text);
            log.info("Document {} split into {} chunks", documentId, chunks.size());

            if (chunks.isEmpty()) {
                doc.setStatus(Document.DocumentStatus.COMPLETED);
                documentRepository.save(doc);
                return;
            }

            // Step 3 & 4: Embed + store each chunk
            for (int i = 0; i < chunks.size(); i++) {
                String chunkText = chunks.get(i);
                float[] embedding = embeddingService.embed(chunkText);
                String vectorStr = embeddingService.toPgVectorString(embedding);

                // Insert chunk with embedding using native SQL for vector type
                jdbcTemplate.update("""
                    INSERT INTO document_chunks (content, chunk_index, embedding, document_id, kb_id, created_at)
                    VALUES (?, ?, ?::vector, ?, ?, NOW())
                    """, chunkText, i, vectorStr, doc.getId(), doc.getKbId());
            }

            // Update status to COMPLETED
            doc.setStatus(Document.DocumentStatus.COMPLETED);
            documentRepository.save(doc);
            log.info("Document {} processing completed, {} chunks stored", documentId, chunks.size());

        } catch (Exception e) {
            log.error("Failed to process document {}: {}", documentId, e.getMessage(), e);
            doc.setStatus(Document.DocumentStatus.FAILED);
            documentRepository.save(doc);
        }
    }
}
