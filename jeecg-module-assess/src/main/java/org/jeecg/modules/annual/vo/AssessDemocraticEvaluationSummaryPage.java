package org.jeecg.modules.annual.vo;

import java.util.List;

import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluation;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Description: 民主测评汇总
 * @Author: jeecg-boot
 * @Date:   2024-09-12
 * @Version: V1.0
 */
@Data
@ApiModel(value="assess_democratic_evaluation_summaryPage对象", description="民主测评汇总")
public class AssessDemocraticEvaluationSummaryPage {

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
	/**年度考核汇总id（外键）*/
	@Excel(name = "年度考核汇总id（外键）", width = 15)
	@ApiModelProperty(value = "年度考核汇总id（外键）")
    private java.lang.String annualSummaryId;
	/**年度*/
	@Excel(name = "年度", width = 15, dicCode = "assess_year")
    @Dict(dicCode = "assess_year")
	@ApiModelProperty(value = "年度")
    private java.lang.String currentYear;
	/**测评对象*/
	@Excel(name = "测评对象", width = 15)
	@ApiModelProperty(value = "测评对象")
    private java.lang.String appraisee;
	/**测评名称*/
	@Excel(name = "测评名称", width = 15)
	@ApiModelProperty(value = "测评名称")
    private java.lang.String assessName;
	/**单位*/
	@Excel(name = "单位", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
	@ApiModelProperty(value = "单位")
    private java.lang.String depart;
	/**分数*/
	@Excel(name = "分数", width = 15)
	@ApiModelProperty(value = "分数")
    private java.math.BigDecimal score;
	/**测评类型*/
	@Excel(name = "测评类型", width = 15, dicCode = "democratic_type")
    @Dict(dicCode = "democratic_type")
	@ApiModelProperty(value = "测评类型")
    private java.lang.String type;
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

	@ExcelCollection(name="民主测评填报")
	@ApiModelProperty(value = "民主测评填报")
	private List<AssessDemocraticEvaluation> assessDemocraticEvaluationList;

}
