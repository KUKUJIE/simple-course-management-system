package com.agiantii.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_teacher")
public class Teacher {
    @TableId(value = "teacher_id", type = IdType.AUTO)
    private Integer teacherId;

    @TableField("user_id")
    private Integer userId;

    @TableField("department_id")
    private Integer departmentId;

    @TableField("teacher_no")
    private String teacherNo;

    @TableField("teacher_name")
    private String teacherName;

    @TableField("title")
    private String title;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("status")
    private Integer status;
}
