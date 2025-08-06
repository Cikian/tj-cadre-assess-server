package org.jeecg.modules.sys.entity.report;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 一报告两评议民主测评项
 * @Author: jeecg-boot
 * @Date: 2024-10-08
 * @Version: V1.0
 */
@ApiModel(value = "assess_report_evaluation_item对象", description = "一报告两评议民主测评项")
@Data
@TableName("assess_report_evaluation_item")
public class AssessReportEvaluationItem implements Serializable {
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
     * 测评项描述
     */
    @Excel(name = "测评项描述", width = 15)
    @ApiModelProperty(value = "测评项描述")
    private java.lang.String topic;
    /**
     * 输入类型
     */
    @Excel(name = "输入类型", width = 15, dicCode = "report_democracy_type")
    @ApiModelProperty(value = "输入类型")
    private java.lang.String inputType;
    /**
     * 填写
     */
    @Excel(name = "填写", width = 15)
    @ApiModelProperty(value = "填写")
    private java.lang.String fill;

    private String voteType;
    /**
     * 汇总id（外键）
     */
    @ApiModelProperty(value = "汇总id（外键）")
    private java.lang.String summaryId;

    @TableField(exist = false)
    private List<AssessReportDemocracyMulti> multiList;

    private int itemOrder;
}
