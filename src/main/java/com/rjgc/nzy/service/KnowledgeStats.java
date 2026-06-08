package com.rjgc.nzy.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class KnowledgeStats {

    private long documentCount;
    private long activeDocumentCount;
    private long archivedDocumentCount;
    private long chunkCount;
    private List<KnowledgeCategoryStats> categoryStats;
}
