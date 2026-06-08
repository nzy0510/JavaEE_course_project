package com.rjgc.nzy.service;

import com.rjgc.nzy.entity.KnowledgeChunk;
import com.rjgc.nzy.entity.KnowledgeDocument;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChunkSearchResult {

    private KnowledgeChunk chunk;
    private KnowledgeDocument document;
    private int score;
}
