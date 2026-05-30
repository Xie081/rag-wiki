package cs.sbs.web.personalprojectweb2026.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    /**
     * Generate embedding vector for a single text.
     * @return float array of embedding dimensions
     */
    public float[] embed(String text) {
        EmbeddingRequest request = new EmbeddingRequest(Collections.singletonList(text), null);
        EmbeddingResponse response = embeddingModel.call(request);
        float[] embedding = response.getResult().getOutput();
        log.debug("Generated embedding: {} dimensions", embedding.length);
        return embedding;
    }

    /**
     * Convert embedding float array to PGVector-compatible string: [0.1,0.2,...]
     */
    public String toPgVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
