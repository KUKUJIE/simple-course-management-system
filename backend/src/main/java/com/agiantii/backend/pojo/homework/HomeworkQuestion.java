package com.agiantii.backend.pojo.homework;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("t_homework_question")
public class HomeworkQuestion implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long questionId;
    private Long homeworkId;
    private Integer seq;
    private String qtype;
    private String content;
    private Double score;
    private Long knowledgePointId;
}
