package org.jeecg.modules.sys.entity.annual;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * @Description: 民主测评填报
 * @Author: jeecg-boot
 * @Date: 2024-09-12
 * @Version: V1.0
 */
@ApiModel(value = "assess_democratic_evaluation对象", description = "民主测评填报")
@Data
@TableName("assess_democratic_evaluation")
public class AssessDemocraticEvaluation implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private java.lang.String createBy;
    /**
     * 创建日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private java.util.Date createTime;
    /**
     * 更新人
     */
    @ApiModelProperty(value = "更新人")
    private java.lang.String updateBy;
    /**
     * 更新日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private java.util.Date updateTime;
    /**
     * 所属部门
     */
    @ApiModelProperty(value = "所属部门")
    private java.lang.String sysOrgCode;
    /**
     * 分数
     */
    @Excel(name = "分数", width = 15)
    @ApiModelProperty(value = "分数")
    private java.math.BigDecimal score;
    /**
     * 类型
     */
    @Excel(name = "类型", width = 15, dicCode = "democratic_type")
    @ApiModelProperty(value = "类型")
    private java.lang.String type;
    /**
     * 总体评价
     */
    @Excel(name = "总体评价", width = 15)
    @ApiModelProperty(value = "总体评价")
    private java.lang.String overallEvaluation;
    /**
     * 民主测评汇总id（外键）
     */
    @ApiModelProperty(value = "民主测评汇总id（外键）")
    private java.lang.String evaluationSummaryId;

    private String voteType;
}
