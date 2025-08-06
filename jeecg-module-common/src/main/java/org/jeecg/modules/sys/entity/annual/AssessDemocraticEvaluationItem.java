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
 * @Description: 民主测评项
 * @Author: jeecg-boot
 * @Date: 2024-09-11
 * @Version: V1.0
 */
@ApiModel(value = "assess_democratic_evaluation_item对象", description = "民主测评项")
@Data
@TableName("assess_democratic_evaluation_item")
public class AssessDemocraticEvaluationItem implements Serializable {
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
     * 外键
     */
    @ApiModelProperty(value = "外键")
    private java.lang.String democraticId;
    /**
     * 项目
     */
    @Excel(name = "项目", width = 15)
    @ApiModelProperty(value = "项目")
    private java.lang.String itemName;
    /**
     * 项目分数
     */
    @Excel(name = "项目分数", width = 15)
    @ApiModelProperty(value = "项目分数")
    private java.math.BigDecimal itemScore;
    /**
     * 得分
     */
    @Excel(name = "得分", width = 15)
    @ApiModelProperty(value = "得分")
    private java.math.BigDecimal score;
    /**
     * 选项
     */
    @Excel(name = "选项", width = 15)
    @ApiModelProperty(value = "选项")
    private java.lang.String itemOption;
    /**
     * 选项权重
     */
    @Excel(name = "选项权重", width = 15)
    @ApiModelProperty(value = "选项权重")
    private java.math.BigDecimal optionWeight;

    private String voteType;
}
