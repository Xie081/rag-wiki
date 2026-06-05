package cs.sbs.web.personalprojectweb2026.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Generate embedding vectors via SiliconFlow API (OpenAI-compatible).
 * Bypasses Spring AI auto-configuration to avoid base-url conflicts with DeepSeek chat.
 */
@Service
@Slf4j
public class EmbeddingService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String model;

    public EmbeddingService(
            @Value("${spring.ai.openai.embedding.base-url:https://api.siliconflow.cn/v1}") String baseUrl,
            @Value("${spring.ai.openai.embedding.api-key:}") String apiKey,
            @Value("${spring.ai.openai.embedding.options.model:BAAI/bge-m3}") String model) {
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl + "/embeddings")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        log.info("EmbeddingService initialized: baseUrl={}, model={}", baseUrl, model);
    }

    /**
     * Generate embedding vector for a single text via SiliconFlow API.
     */
    public float[] embed(String text) {
        try {
            String body = restClient.post()
                    .body(Map.of("model", model, "input", text))
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(body);
            JsonNode embeddingNode = root.path("data").get(0).path("embedding");
            float[] embedding = new float[embeddingNode.size()];
            for (int i = 0; i < embedding.length; i++) {
                embedding[i] = (float) embeddingNode.get(i).asDouble();
            }
            log.debug("Generated embedding via {}: {} dimensions", model, embedding.length);
            return embedding;
        } catch (Exception e) {
            log.error("Embedding API call failed: {}", e.getMessage());
            throw new RuntimeException("Embedding 生成失败: " + e.getMessage(), e);
        }
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
