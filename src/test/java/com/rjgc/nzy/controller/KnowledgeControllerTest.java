package com.rjgc.nzy.controller;

import com.rjgc.nzy.common.Result;
import com.rjgc.nzy.entity.KnowledgeChunk;
import com.rjgc.nzy.entity.KnowledgeDocument;
import com.rjgc.nzy.service.ChunkSearchResult;
import com.rjgc.nzy.service.KnowledgeDocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KnowledgeControllerTest {

    private final KnowledgeDocumentService service = mock(KnowledgeDocumentService.class);
    private final KnowledgeController controller = new KnowledgeController(service);

    @Test
    void importDocumentDelegatesToDocumentService() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "ai.md",
                "text/markdown",
                "RAG 的完整流程是怎么样的？".getBytes(StandardCharsets.UTF_8));
        KnowledgeDocument document = new KnowledgeDocument();
        document.setOriginalFilename("ai.md");
        document.setChunkCount(1);
        when(service.importDocument(file)).thenReturn(document);

        Result<KnowledgeDocument> result = controller.importDocument(file);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getOriginalFilename()).isEqualTo("ai.md");
        assertThat(result.getData().getChunkCount()).isEqualTo(1);
    }

    @Test
    void deleteDocumentDelegatesToDocumentService() {
        Result<Void> result = controller.deleteDocument(7L);

        assertThat(result.getCode()).isEqualTo(200);
        verify(service).deleteDocument(7L);
    }

    @Test
    void searchChunksDelegatesAndReturnsReadableChinese() {
        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setTitlePath("RAG 的完整流程");
        chunk.setContent("RAG 包括数据切分、检索召回、生成答案。");
        KnowledgeDocument document = new KnowledgeDocument();
        document.setOriginalFilename("AI大模型题库.pdf");
        when(service.searchChunks("RAG 流程", 5))
                .thenReturn(List.of(new ChunkSearchResult(chunk, document, 16)));

        Result<List<ChunkSearchResult>> result = controller.searchChunks("RAG 流程", 5);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getChunk().getContent()).contains("检索召回");
        verify(service).searchChunks("RAG 流程", 5);
    }
}
