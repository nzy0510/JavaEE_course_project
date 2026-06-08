package com.rjgc.nzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rjgc.nzy.entity.KnowledgeDocument;
import com.rjgc.nzy.service.KnowledgeCategoryStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {

    @Select("""
            SELECT
                COALESCE(d.knowledge_category, '通用知识') AS category,
                COUNT(DISTINCT d.id) AS documentCount,
                COALESCE(SUM(CASE WHEN c.status = 'ACTIVE' THEN 1 ELSE 0 END), 0) AS chunkCount
            FROM knowledge_document d
            LEFT JOIN knowledge_chunk c ON c.document_id = d.id
            WHERE d.status = 'ACTIVE'
            GROUP BY COALESCE(d.knowledge_category, '通用知识')
            ORDER BY chunkCount DESC, documentCount DESC
            """)
    List<KnowledgeCategoryStats> selectActiveCategoryStats();
}
