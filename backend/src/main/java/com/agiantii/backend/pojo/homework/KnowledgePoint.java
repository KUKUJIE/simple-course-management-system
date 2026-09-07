package com.agiantii.backend.pojo.homework;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("t_knowledge_point")
public class KnowledgePoint implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long knowledgePointId;
    private String name;
    private String description;
}
