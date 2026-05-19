package com.agiantii.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("t_student")
@Data
public class Student {
    @TableId(value = "student_id", type = IdType.AUTO)
    private Integer studentId;

    @TableField("user_id")
    private Integer userId;

    @TableField("major_id")
    private Integer majorId;

    @TableField("student_no")
    private String studentNo;

    @TableField("student_name")
    private String studentName;

    @TableField("gender")
    private String gender;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("enrollment_year")
    private Integer enrollmentYear;

    @TableField("status")
    private Integer status;
}
