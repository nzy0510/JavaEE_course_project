package com.rjgc.nzy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_document")
public class KnowledgeDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String originalFilename;
    private String storedFilename;
    private String fileExtension;
    private Long fileSize;
    private String knowledgeCategory;
    private String status;
    private Integer chunkCount;
    private String errorMessage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
