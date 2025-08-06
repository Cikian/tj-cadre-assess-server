package org.jeecg.modules.regular.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 平时考核填报
 * @Author: admin
 * @Date:   2024-08-30
 * @Version: V1.0
 */
@ApiModel(value="assess_regular_report对象", description="平时考核填报")
@Data
@TableName("assess_regular_report")
public class AssessRegularReport implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
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
    /**季度*/
    @Excel(name = "季度", width = 15, dicCode = "assess_quarter")
    @Dict(dicCode = "assess_quarter")
    @ApiModelProperty(value = "季度")
    private String currentQuarter;
	/**处室*/
	@Excel(name = "处室", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
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
    private String auditBy;
	/**填报人*/
    @Excel(name = "填报人", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @ApiModelProperty(value = "填报人")
    private String reportBy;
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
}
