package com.rjgc.nzy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_atom")
public class KnowledgeAtom {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String subject;
    private String category;
    private String difficulty;
    private String tags;
    private String principles;
    private String pitfalls;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
