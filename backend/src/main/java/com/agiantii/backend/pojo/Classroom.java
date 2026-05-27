package com.agiantii.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("t_classroom")
@ApiModel("教室")
public class Classroom {
    @TableId(value = "classroom_id", type = IdType.AUTO)
    @ApiModelProperty("教室ID")
    private Integer classroomId;

    @TableField("building")
    @ApiModelProperty("教学楼")
    private String building;

    @TableField("room_no")
    @ApiModelProperty("教室编号")
    private String roomNo;

    @TableField("capacity")
    @ApiModelProperty("容量")
    private Integer capacity;

    @TableField("status")
    @ApiModelProperty("状态 1=启用 0=停用")
    private Integer status;

    @TableField("remark")
    @ApiModelProperty("备注")
    private String remark;
}
