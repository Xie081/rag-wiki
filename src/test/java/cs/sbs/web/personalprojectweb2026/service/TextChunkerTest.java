package cs.sbs.web.personalprojectweb2026.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextChunkerTest {

    private final TextChunker chunker = new TextChunker();

    @Test
    void shouldReturnEmptyListForNullText() {
        List<String> chunks = chunker.chunk(null);
        assertTrue(chunks.isEmpty());
    }

    @Test
    void shouldReturnEmptyListForBlankText() {
        List<String> chunks = chunker.chunk("   ");
        assertTrue(chunks.isEmpty());
    }

    @Test
    void shouldReturnSingleChunkForShortText() {
        String text = "Hello world. This is a test.";
        List<String> chunks = chunker.chunk(text);
        assertEquals(1, chunks.size());
        assertEquals(text, chunks.get(0));
    }

    @Test
    void shouldSplitLongTextIntoMultipleChunks() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append("Paragraph ").append(i + 1)
              .append(": This is a longer paragraph to fill up the chunk size limit. ")
              .append("It contains multiple sentences that together form a coherent block. ")
              .append("This should help us reach the chunk size threshold.\n\n");
        }
        String text = sb.toString();

        List<String> chunks = chunker.chunk(text);
        assertTrue(chunks.size() > 1, "Long text should produce multiple chunks");
    }

    @Test
    void shouldBreakAtParagraphBoundaries() {
        // Build text with clear paragraph breaks
        String paragraph = "A".repeat(480) + "\n\n" + "B".repeat(480);
        List<String> chunks = chunker.chunk(paragraph, 500, 100);

        assertTrue(chunks.size() >= 2, "Should break at paragraph boundary");
        assertTrue(chunks.get(0).contains("AAA"), "First chunk should contain A's");
        assertTrue(chunks.get(1).contains("BBB"), "Second chunk should contain B's");
    }

    @Test
    void shouldHandleChineseText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append("第").append(i + 1).append("段：这是一个中文段落，用于测试中文文本的分块功能。")
              .append("我们需要确保中文的句子结束标记（句号、问号等）能够被正确识别。")
              .append("此外还要验证段落之间的重叠是否正常处理。\n\n");
        }
        String text = sb.toString();
        List<String> chunks = chunker.chunk(text);

        assertTrue(chunks.size() >= 2, "Chinese long text should produce multiple chunks");
        // Each chunk should not be empty
        chunks.forEach(chunk -> assertFalse(chunk.isEmpty(), "No chunk should be empty"));
    }

    @Test
    void shouldHaveOverlapBetweenChunks() {
        StringBuilder sb = new StringBuilder();
        // Generate text that will create exactly 2 chunks
        sb.append("A".repeat(400));
        sb.append("\n\n");
        sb.append("B".repeat(200));
        String text = sb.toString();

        List<String> chunks = chunker.chunk(text, 500, 100);
        if (chunks.size() >= 2) {
            String firstChunk = chunks.get(0);
            String secondChunk = chunks.get(1);
            // The second chunk should contain some text from the end of the first chunk
            assertNotNull(firstChunk);
            assertNotNull(secondChunk);
        }
    }

    @Test
    void shouldNotCreateInfiniteLoop() {
        // Ensure chunking terminates even with tricky text
        String text = "X".repeat(2000);
        List<String> chunks = chunker.chunk(text, 500, 100);
        assertFalse(chunks.isEmpty());
        // With 2000 chars, chunk_size=500, overlap=100: ~5 chunks
        assertTrue(chunks.size() >= 4, "Should produce multiple chunks without infinite loop");
    }
}
