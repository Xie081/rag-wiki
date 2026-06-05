package cs.sbs.web.personalprojectweb2026.service;

import cs.sbs.web.personalprojectweb2026.model.entity.KnowledgeBase;
import cs.sbs.web.personalprojectweb2026.repository.DocumentChunkRepository;
import cs.sbs.web.personalprojectweb2026.repository.DocumentRepository;
import cs.sbs.web.personalprojectweb2026.repository.KnowledgeBaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeBaseServiceTest {

    @Mock
    private KnowledgeBaseRepository kbRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentChunkRepository chunkRepository;

    private KnowledgeBaseService kbService;

    @BeforeEach
    void setUp() {
        kbService = new KnowledgeBaseService(kbRepository, documentRepository, chunkRepository);
    }

    @Test
    void shouldCreateKnowledgeBase() {
        KnowledgeBase kb = KnowledgeBase.builder()
                .name("测试知识库")
                .description("测试描述")
                .userId(1L)
                .build();
        kb.setCreatedAt(Instant.now());

        when(kbRepository.save(any(KnowledgeBase.class))).thenReturn(kb);

        KnowledgeBase created = kbService.create("测试知识库", "测试描述", 1L);

        assertNotNull(created);
        assertEquals("测试知识库", created.getName());
        assertEquals("测试描述", created.getDescription());
        assertEquals(1L, created.getUserId());
        verify(kbRepository).save(any(KnowledgeBase.class));
    }

    @Test
    void shouldListByUser() {
        KnowledgeBase kb1 = KnowledgeBase.builder().name("KB1").userId(1L).build();
        KnowledgeBase kb2 = KnowledgeBase.builder().name("KB2").userId(1L).build();
        when(kbRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(kb1, kb2));

        List<KnowledgeBase> result = kbService.listByUser(1L);

        assertEquals(2, result.size());
        assertEquals("KB1", result.get(0).getName());
    }

    @Test
    void shouldGetByIdWithValidUser() {
        KnowledgeBase kb = KnowledgeBase.builder().name("我的知识库").userId(1L).build();
        when(kbRepository.findById(1L)).thenReturn(Optional.of(kb));

        KnowledgeBase result = kbService.getById(1L, 1L);

        assertNotNull(result);
        assertEquals("我的知识库", result.getName());
    }

    @Test
    void shouldThrowWhenAccessingOtherUserKnowledgeBase() {
        KnowledgeBase kb = KnowledgeBase.builder().name("别人的知识库").userId(2L).build();
        when(kbRepository.findById(1L)).thenReturn(Optional.of(kb));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> kbService.getById(1L, 1L));
        assertEquals("无权访问该知识库", ex.getMessage());
    }

    @Test
    void shouldThrowWhenKnowledgeBaseNotFound() {
        when(kbRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> kbService.getById(999L, 1L));
        assertEquals("知识库不存在", ex.getMessage());
    }

    @Test
    void shouldDeleteKnowledgeBaseAndCascade() {
        KnowledgeBase kb = KnowledgeBase.builder().name("待删除").userId(1L).build();
        when(kbRepository.findById(1L)).thenReturn(Optional.of(kb));

        kbService.delete(1L, 1L);

        verify(documentRepository).deleteByKbId(1L);
        verify(kbRepository).delete(kb);
    }
}
