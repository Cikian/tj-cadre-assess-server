package org.jeecg.modules.report.vo;

import java.util.List;

import org.jeecg.modules.sys.entity.report.AssessReportEvaluationItem;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Description: 一报告两评议民主测评汇总
 * @Author: jeecg-boot
 * @Date:   2024-10-08
 * @Version: V1.0
 */
@Data
@ApiModel(value="assess_report_evaluation_summaryPage对象", description="一报告两评议民主测评汇总")
public class AssessReportEvaluationSummaryPage {

	/**主键*/
	@ApiModelProperty(value = "主键")
    private java.lang.String id;
	/**创建人*/
	@ApiModelProperty(value = "创建人")
    private java.lang.String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "创建日期")
    private java.util.Date createTime;
	/**更新人*/
	@ApiModelProperty(value = "更新人")
    private java.lang.String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "更新日期")
    private java.util.Date updateTime;
	/**所属部门*/
	@ApiModelProperty(value = "所属部门")
    private java.lang.String sysOrgCode;
	/**填报id*/
	@Excel(name = "填报id", width = 15)
	@ApiModelProperty(value = "填报id")
    private java.lang.String reportFillId;
	/**年度*/
	@Excel(name = "年度", width = 15, dicCode = "assess_year")
    @Dict(dicCode = "assess_year")
	@ApiModelProperty(value = "年度")
    private java.lang.String currentYear;
	/**测评名称*/
	@Excel(name = "测评名称", width = 15)
	@ApiModelProperty(value = "测评名称")
    private java.lang.String assessName;
	/**单位*/
	@Excel(name = "单位", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
	@ApiModelProperty(value = "单位")
    private java.lang.String depart;
	/**开始时间*/
	@Excel(name = "开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "开始时间")
    private java.util.Date startDate;
	/**结束时间*/
	@Excel(name = "结束时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "结束时间")
    private java.util.Date endDate;
	/**测评人数*/
	@Excel(name = "测评人数", width = 15)
	@ApiModelProperty(value = "测评人数")
    private java.lang.Integer num;
	/**已完成数量*/
	@Excel(name = "已完成数量", width = 15)
	@ApiModelProperty(value = "已完成数量")
    private java.lang.Integer filledNum;

	@ExcelCollection(name="一报告两评议民主测评项")
	@ApiModelProperty(value = "一报告两评议民主测评项")
	private List<AssessReportEvaluationItem> assessReportEvaluationItemList;

}
