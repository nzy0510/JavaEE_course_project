package com.rjgc.nzy.service;

import com.rjgc.nzy.entity.KnowledgeChunk;
import com.rjgc.nzy.entity.KnowledgeDocument;
import com.rjgc.nzy.mapper.KnowledgeChunkMapper;
import com.rjgc.nzy.mapper.KnowledgeDocumentMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KnowledgeDocumentServiceTest {

    private final KnowledgeDocumentMapper documentMapper = mock(KnowledgeDocumentMapper.class);
    private final KnowledgeChunkMapper chunkMapper = mock(KnowledgeChunkMapper.class);
    private final DocumentConversionService conversionService = mock(DocumentConversionService.class);
    private final MarkdownChunker chunker = mock(MarkdownChunker.class);
    private final KnowledgeDocumentService service = new KnowledgeDocumentService(
            documentMapper, chunkMapper, conversionService, chunker, new KnowledgeCategoryClassifier());

    @Test
    void importDocumentConvertsChunksAndMarksDocumentActive() {
        doAnswer(invocation -> {
            KnowledgeDocument document = invocation.getArgument(0);
            document.setId(7L);
            return 1;
        }).when(documentMapper).insert(any(KnowledgeDocument.class));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "AI大模型题库.pdf",
                "application/pdf",
                "pdf".getBytes(StandardCharsets.UTF_8));
        when(conversionService.convert(file, "pdf"))
                .thenReturn(new DocumentConversionService.ConvertedDocument("markdown", Duration.ofSeconds(1)));
        when(chunker.chunk("markdown")).thenReturn(List.of(
                new MarkdownSection("RAG 的完整流程是怎么样的？", "RAG 分为检索和生成。"),
                new MarkdownSection("什么是 LoRA？", "LoRA 是低秩适配。")));

        KnowledgeDocument document = service.importDocument(file);

        assertThat(document.getStatus()).isEqualTo("ACTIVE");
        assertThat(document.getChunkCount()).isEqualTo(2);
        assertThat(document.getKnowledgeCategory()).isEqualTo("大模型");
        ArgumentCaptor<KnowledgeChunk> captor = ArgumentCaptor.forClass(KnowledgeChunk.class);
        verify(chunkMapper, times(2)).insert(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(chunk -> {
            assertThat(chunk.getDocumentId()).isEqualTo(7L);
            assertThat(chunk.getStatus()).isEqualTo("ACTIVE");
        });
    }

    @Test
    void searchForAiScoresTitleAndContentMatches() {
        KnowledgeChunk rag = chunk(1L, "RAG 的完整流程", "RAG 包括数据切分、检索召回、生成答案。");
        KnowledgeChunk lora = chunk(2L, "LoRA 微调", "低秩矩阵适配模型参数。");
        KnowledgeDocument document = new KnowledgeDocument();
        document.setOriginalFilename("AI大模型.pdf");
        when(chunkMapper.selectList(any())).thenReturn(List.of(lora, rag));
        when(documentMapper.selectById(any())).thenReturn(document);

        List<ChunkSearchResult> results = service.searchForAi(List.of("RAG 流程"), 3);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getChunk().getTitlePath()).isEqualTo("RAG 的完整流程");
        assertThat(results.get(0).getScore()).isGreaterThan(0);
    }

    @Test
    void searchChunksKeepsChineseTextReadable() {
        KnowledgeChunk rag = chunk(1L, "RAG 的完整流程", "RAG 包括数据切分、检索召回、生成答案。");
        KnowledgeDocument document = new KnowledgeDocument();
        document.setId(10L);
        document.setOriginalFilename("AI大模型题库.pdf");
        document.setKnowledgeCategory("大模型");
        when(chunkMapper.selectList(any())).thenReturn(List.of(rag));
        when(documentMapper.selectBatchIds(anyDocumentIdCollection())).thenReturn(List.of(document));

        List<ChunkSearchResult> results = service.searchChunks("RAG 流程", 3);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getChunk().getTitlePath()).isEqualTo("RAG 的完整流程");
        assertThat(results.get(0).getChunk().getContent()).contains("检索召回");
        assertThat(results.get(0).getDocument().getOriginalFilename()).isEqualTo("AI大模型题库.pdf");
    }

    @Test
    void searchForAiLoadsMatchedDocumentsInOneBatch() {
        KnowledgeChunk first = chunk(1L, "RAG 流程", "RAG 包括检索召回。");
        KnowledgeChunk second = chunk(2L, "RAG 应用", "RAG 可以结合大模型生成答案。");
        first.setDocumentId(10L);
        second.setDocumentId(10L);
        KnowledgeDocument document = new KnowledgeDocument();
        document.setId(10L);
        document.setOriginalFilename("AI大模型题库.pdf");
        when(chunkMapper.selectList(any())).thenReturn(List.of(first, second));
        when(documentMapper.selectBatchIds(anyDocumentIdCollection())).thenReturn(List.of(document));

        List<ChunkSearchResult> results = service.searchForAi(List.of("RAG"), 2);

        assertThat(results).hasSize(2);
        verify(documentMapper).selectBatchIds(anyDocumentIdCollection());
        verify(documentMapper, never()).selectById(any());
    }

    @Test
    void statsIncludesActiveKnowledgeCategoryDistribution() {
        when(documentMapper.selectCount(any())).thenReturn(3L, 2L, 1L);
        when(chunkMapper.selectCount(any())).thenReturn(12L);
        when(documentMapper.selectActiveCategoryStats()).thenReturn(List.of(
                new KnowledgeCategoryStats("大模型", 1L, 8L),
                new KnowledgeCategoryStats("Java基础", 1L, 4L)));

        KnowledgeStats stats = service.stats();

        assertThat(stats.getCategoryStats()).containsExactly(
                new KnowledgeCategoryStats("大模型", 1L, 8L),
                new KnowledgeCategoryStats("Java基础", 1L, 4L));
    }

    @Test
    void classifierRecognizesLargeModelMaterialsFromFilenameAndContent() {
        KnowledgeCategoryClassifier classifier = new KnowledgeCategoryClassifier();

        String category = classifier.classify(
                "AI大模型题库.md",
                "RAG、Transformer、LoRA、Prompt Engineering 是大语言模型面试高频知识点。");

        assertThat(category).isEqualTo("大模型");
    }

    @Test
    void classifierDoesNotUseContentForGenericFilenames() {
        KnowledgeCategoryClassifier classifier = new KnowledgeCategoryClassifier();

        String category = classifier.classify(
                "面试刷题资料.pdf",
                """
                        Transformer 自注意力机制、RAG 检索增强生成、Embedding 向量表示、
                        LoRA 参数高效微调、Prompt Engineering 都是大语言模型应用高频知识。
                        """);

        assertThat(category).isEqualTo("通用知识");
    }

    @Test
    void classifierUsesFilenameOnlyForSimpleCategoryAssignment() {
        KnowledgeCategoryClassifier classifier = new KnowledgeCategoryClassifier();

        assertThat(classifier.classify("AI大模型题库.pdf", "普通 Java 基础内容")).isEqualTo("大模型");
        assertThat(classifier.classify("面试刷题资料.pdf", "RAG、Transformer、LoRA 都是大模型内容")).isEqualTo("通用知识");
    }

    @Test
    void deleteDocumentRemovesDocumentById() {
        service.deleteDocument(9L);

        verify(documentMapper).deleteById(9L);
    }

    private KnowledgeChunk chunk(Long id, String title, String content) {
        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setId(id);
        chunk.setDocumentId(10L);
        chunk.setChunkIndex(id.intValue());
        chunk.setTitlePath(title);
        chunk.setContent(content);
        chunk.setStatus("ACTIVE");
        return chunk;
    }

    @SuppressWarnings("unchecked")
    private Collection<Long> anyDocumentIdCollection() {
        return any(Collection.class);
    }
}
