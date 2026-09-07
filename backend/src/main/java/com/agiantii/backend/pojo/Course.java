package com.agiantii.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("t_course")
@Data
public class Course {
    @TableId(value = "course_id", type = IdType.AUTO)
    private Long courseId;
    private String courseCode;
    private String name;
    private String description;
    private Long teacherId;
    private LocalDateTime createdAt;
}
