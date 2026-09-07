package com.agiantii.backend.pojo.homework;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("t_homework_answer")
public class HomeworkAnswer implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long answerId;
    private Long submissionId;
    private Long questionId;
    private String answerText;
    private String attachmentPath;
    private Double scoreGiven;
    private String comment;
}
