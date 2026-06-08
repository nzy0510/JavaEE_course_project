package com.rjgc.nzy.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeCategoryStats {

    private String category;
    private long documentCount;
    private long chunkCount;
}
