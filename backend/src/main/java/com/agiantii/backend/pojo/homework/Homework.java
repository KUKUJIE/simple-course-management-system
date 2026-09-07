package com.agiantii.backend.pojo.homework;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_homework")
public class Homework implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long homeworkId;
    private Long sectionId;
    private Long teacherId;
    private String title;
    private String description;
    private LocalDateTime publishTime;
    private LocalDateTime deadline;
    private Integer allowLate;
    private Double totalScore;
    private String allowSubmissionTypes;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
