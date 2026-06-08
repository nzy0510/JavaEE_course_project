package com.rjgc.nzy.service;

import com.rjgc.nzy.config.MarkitdownProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownChunkerTest {

    @Test
    void chunkUsesQuestionLinesAsBoundariesAndRemovesPromoNoise() {
        MarkitdownProperties properties = new MarkitdownProperties();
        properties.setChunkTargetChars(1000);
        MarkdownChunker chunker = new MarkdownChunker(properties);

        List<MarkdownSection> chunks = chunker.chunk("""
                mianshiya.com
                本资源来自面试鸭：https://www.mianshiya.com
                RAG 的完整流程是怎么样的？
                RAG 分为准备数据、检索资料、生成答案。

                什么是 LoRA 微调？
                LoRA 通过低秩矩阵适配模型参数。
                """);

        assertThat(chunks).hasSize(2);
        assertThat(chunks).extracting(MarkdownSection::getTitlePath)
                .containsExactly("RAG 的完整流程是怎么样的?", "什么是 LoRA 微调?");
        assertThat(chunks.get(0).getContent()).doesNotContain("mianshiya.com");
    }

    @Test
    void chunkSplitsLongSectionsWithOverlap() {
        MarkitdownProperties properties = new MarkitdownProperties();
        properties.setChunkTargetChars(60);
        properties.setChunkOverlapChars(10);
        MarkdownChunker chunker = new MarkdownChunker(properties);

        String body = "A".repeat(700);
        List<MarkdownSection> chunks = chunker.chunk("长文档标题？\n" + body);

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks).allSatisfy(chunk -> assertThat(chunk.getTitlePath()).startsWith("长文档标题?"));
    }
}
