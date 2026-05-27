package com.agiantii.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("t_major")
@ApiModel("专业")
public class Major {
    @TableId(value = "major_id", type = IdType.AUTO)
    @ApiModelProperty("专业ID")
    private Integer majorId;

    @TableField("department_id")
    @ApiModelProperty("所属院系ID")
    private Integer departmentId;

    @TableField("major_code")
    @ApiModelProperty("专业编号")
    private String majorCode;

    @TableField("major_name")
    @ApiModelProperty("专业名称")
    private String majorName;

    @TableField("status")
    @ApiModelProperty("状态 1=启用 0=停用")
    private Integer status;
}
