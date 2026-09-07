package com.agiantii.backend.pojo.homework;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_homework_submission")
public class HomeworkSubmission implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long submissionId;
    private Long homeworkId;
    private Long studentId;
    private LocalDateTime submitTime;
    private Integer isLate;
    private String status;
    private String submitText;
    private String attachmentPath;
    private Double finalScore;
    private Long gradedBy;
    private LocalDateTime gradedAt;
}
