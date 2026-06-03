package cs.sbs.web.personalprojectweb2026.controller;

import cs.sbs.web.personalprojectweb2026.config.SecurityUtil;
import cs.sbs.web.personalprojectweb2026.service.RagService;
import cs.sbs.web.personalprojectweb2026.service.RagService.RagResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.Executor;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final RagService ragService;
    private final StreamingChatModel streamingChatModel;
    private final SecurityUtil securityUtil;
    private final Executor taskExecutor;
    private final ObjectMapper objectMapper;

    /**
     * Non-streaming RAG Q&A.
     */
    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody Map<String, Object> body) {
        Long kbId = Long.valueOf(body.get("knowledgeBaseId").toString());
        String question = body.get("question").toString();

        RagResult result = ragService.ask(kbId, question);
        return ResponseEntity.ok(Map.of(
                "answer", result.answer(),
                "sources", result.sources()
        ));
    }

    /**
     * SSE streaming RAG Q&A.
     * Sends JSON-enveloped events: {"type":"token","content":"..."} and {"type":"sources","data":[...]}
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody Map<String, Object> body) {
        Long kbId = Long.valueOf(body.get("knowledgeBaseId").toString());
        String question = body.get("question").toString();

        SseEmitter emitter = new SseEmitter(300000L); // 5 min timeout

        taskExecutor.execute(() -> {
            try {
                // Single retrieval: get both messages and sources in one call
                var renderedWithSources = ragService.buildRenderedPrompt(kbId, question);

                // Stream the response
                Flux<ChatResponse> flux = streamingChatModel.stream(
                        new Prompt(renderedWithSources.messages()));

                flux.doOnNext(chunk -> {
                    String content = chunk.getResult().getOutput().getText();
                    if (content != null) {
                        try {
                            String json = objectMapper.writeValueAsString(
                                    Map.of("type", "token", "content", content));
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(json));
                        } catch (Exception e) {
                            // client disconnected, stop the flux
                        }
                    }
                }).doOnComplete(() -> {
                    try {
                        // Send sources (from the single retrieval, no double LLM call)
                        String sourcesJson = objectMapper.writeValueAsString(
                                Map.of("type", "sources", "data", renderedWithSources.sources()));
                        emitter.send(SseEmitter.event()
                                .name("sources")
                                .data(sourcesJson));
                        emitter.complete();
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                }).doOnError(e -> {
                    log.error("Streaming error for question: {}", question, e);
                    try {
                        String errorJson = objectMapper.writeValueAsString(
                                Map.of("type", "error", "message", e.getMessage()));
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data(errorJson));
                    } catch (Exception ignored) {}
                    emitter.completeWithError(e);
                }).subscribe();

            } catch (Exception e) {
                log.error("Failed to build prompt for question: {}", question, e);
                emitter.completeWithError(e);
            }
        });

        // Clean up on timeout or client disconnect
        emitter.onTimeout(() -> {
            log.debug("SSE emitter timed out");
            emitter.complete();
        });
        emitter.onError(emitter::completeWithError);

        return emitter;
    }
}
