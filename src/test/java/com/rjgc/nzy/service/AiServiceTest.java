package com.rjgc.nzy.service;

import com.rjgc.nzy.entity.KnowledgeChunk;
import com.rjgc.nzy.entity.KnowledgeDocument;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiServiceTest {

    @Test
    void askReturnsConfigurationHintAndTopChunksWhenModelIsUnavailable() {
        KnowledgeDocumentService documentService = mock(KnowledgeDocumentService.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<OpenAiChatModel> provider = mock(ObjectProvider.class);
        KnowledgeDocument document = new KnowledgeDocument();
        document.setOriginalFilename("AI大模型.pdf");
        document.setKnowledgeCategory("大模型");
        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setChunkIndex(1);
        chunk.setTitlePath("RAG 的完整流程");
        chunk.setContent("RAG 包括准备数据、检索资料、生成答案。");
        when(documentService.searchForAi(eq(List.of("RAG 的流程是什么？")), eq(3)))
                .thenReturn(List.of(new ChunkSearchResult(chunk, document, 18)));
        when(provider.getIfAvailable()).thenReturn(null);

        AiService service = new AiService(documentService, provider);

        String answer = service.ask("RAG 的流程是什么？");

        assertThat(answer).contains("DEEPSEEK_API_KEY");
        assertThat(answer).contains("AI大模型.pdf");
        assertThat(answer).contains("分类】大模型");
        assertThat(answer).contains("匹配分】18");
        assertThat(answer).contains("RAG 包括准备数据、检索资料、生成答案");
    }
}
