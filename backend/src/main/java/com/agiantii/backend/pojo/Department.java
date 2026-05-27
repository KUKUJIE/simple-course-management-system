package com.agiantii.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("t_department")
@ApiModel("院系")
public class Department {
    @TableId(value = "department_id", type = IdType.AUTO)
    @ApiModelProperty("院系ID")
    private Integer departmentId;

    @TableField("department_code")
    @ApiModelProperty("院系编号")
    private String departmentCode;

    @TableField("department_name")
    @ApiModelProperty("院系名称")
    private String departmentName;

    @TableField("office_phone")
    @ApiModelProperty("联系电话")
    private String officePhone;

    @TableField("status")
    @ApiModelProperty("状态 1=启用 0=停用")
    private Integer status;
}
