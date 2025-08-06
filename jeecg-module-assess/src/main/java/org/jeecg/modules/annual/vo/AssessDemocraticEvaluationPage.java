package org.jeecg.modules.annual.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluationItem;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

/**
 * @Description: 民主测评填报
 * @Author: jeecg-boot
 * @Date: 2024-09-11
 * @Version: V1.0
 */
@Data
@ApiModel(value = "assess_democratic_evaluationPage对象", description = "民主测评填报")
public class AssessDemocraticEvaluationPage {

    /**
     * 主键
     */
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
     * 测评对象
     */
    @Excel(name = "测评对象", width = 15)
    @ApiModelProperty(value = "测评对象")
    private java.lang.String appraisee;
    /**
     * 年度
     */
    @Excel(name = "年度", width = 15)
    @ApiModelProperty(value = "年度")
    private java.lang.String currentYear;
    /**
     * 考核名称
     */
    @Excel(name = "考核名称", width = 15)
    @ApiModelProperty(value = "考核名称")
    private java.lang.String assessName;
    /**
     * 单位（处室）
     */
    @Excel(name = "单位（处室）", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @ApiModelProperty(value = "单位（处室）")
    private java.lang.String depart;
    /**
     * 分数
     */
    @Excel(name = "分数", width = 15)
    @ApiModelProperty(value = "分数")
    private java.lang.String score;
    /**
     * 考核填报id（外键）
     */
    @Excel(name = "考核填报id（外键）", width = 15)
    @ApiModelProperty(value = "考核填报id（外键）")
    private java.lang.String assessFillId;
    /**
     * 类型
     */
    @Excel(name = "类型", width = 15, dicCode = "democratic_type")
    @Dict(dicCode = "democratic_type")
    @ApiModelProperty(value = "类型")
    private java.lang.String type;
    /**
     * 开始时间
     */
    @Excel(name = "开始时间", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "开始时间")
    private java.util.Date startDate;
    /**
     * 结束时间
     */
    @Excel(name = "结束时间", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "结束时间")
    private java.util.Date endDate;

    @ExcelCollection(name = "民主测评项")
    @ApiModelProperty(value = "民主测评项")
    private List<AssessDemocraticEvaluationItem> assessDemocraticEvaluationItemList;

}
