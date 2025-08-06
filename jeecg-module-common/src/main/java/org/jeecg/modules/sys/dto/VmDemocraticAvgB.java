package org.jeecg.modules.sys.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 民主测评汇总
 * @Author: jeecg-boot
 * @Date: 2024-09-12
 * @Version: V1.0
 */
@Data
@TableName("vm_democratic_avg_b")
public class VmDemocraticAvgB implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    private String currentYear;
    private String appraisee;
    private String depart;
    private java.math.BigDecimal score;
    private String type;
    private String departType;
    private String voteType;
    @TableField(exist = false)
    private Integer ranking;
    @TableField(exist = false)
    private Integer nums;
}
