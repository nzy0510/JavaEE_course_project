package com.rjgc.nzy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_chunk")
public class KnowledgeChunk {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long documentId;
    private Integer chunkIndex;
    private String titlePath;
    private String content;
    private String contentHash;
    private Integer tokenEstimate;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
