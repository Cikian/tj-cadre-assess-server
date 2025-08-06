package org.jeecg.modules.regular.vo;

import java.util.List;
import org.jeecg.modules.regular.entity.AssessRegularReport;
import org.jeecg.modules.regular.entity.AssessRegularReportItem;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelEntity;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Description: 平时考核填报
 * @Author: admin
 * @Date:   2024-08-30
 * @Version: V1.0
 */
@Data
@ApiModel(value="assess_regular_reportPage对象", description="平时考核填报")
public class AssessRegularReportPage {

	/**主键*/
	@ApiModelProperty(value = "主键")
    private String id;
	/**创建人*/
	@ApiModelProperty(value = "创建人")
    private String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "创建日期")
    private Date createTime;
	/**更新人*/
	@ApiModelProperty(value = "更新人")
    private String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "更新日期")
    private Date updateTime;
	/**所属部门*/
	@ApiModelProperty(value = "所属部门")
    private String sysOrgCode;
	/**考核名称*/
	@Excel(name = "考核名称", width = 15)
	@ApiModelProperty(value = "考核名称")
    private String assessName;
	/**年度*/
	@Excel(name = "年度", width = 15, dicCode = "assess_year")
    @Dict(dicCode = "assess_year")
	@ApiModelProperty(value = "年度")
    private String currentYear;
	/**处室*/
	@Excel(name = "处室", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
	@ApiModelProperty(value = "处室")
    private String departmentCode;
	/**截止日期*/
	@Excel(name = "截止日期", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
	@ApiModelProperty(value = "截止日期")
    private Date deadline;
	/**预警状态*/
	@Excel(name = "预警状态", width = 15, dicCode = "warn_status")
    @Dict(dicCode = "warn_status")
	@ApiModelProperty(value = "预警状态")
    private String warnStatus;
	/**填报状态*/
	@Excel(name = "填报状态", width = 15, dicCode = "report_status")
    @Dict(dicCode = "report_status")
	@ApiModelProperty(value = "填报状态")
    private String status;
	/**处室审核人*/
	@Excel(name = "处室审核人", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
	@ApiModelProperty(value = "处室审核人")
    private String leader;
	/**填报人*/
	@Excel(name = "填报人", width = 15)
	@ApiModelProperty(value = "填报人")
    private String reportPerson;
	/**填报时间*/
	@Excel(name = "填报时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "填报时间")
    private Date reportTime;
	/**备注*/
	@Excel(name = "备注", width = 15)
	@ApiModelProperty(value = "备注")
    private String note;

	@ExcelCollection(name="平时考核填报明细表")
	@ApiModelProperty(value = "平时考核填报明细表")
	private List<AssessRegularReportItem> assessRegularReportItemList;

}
