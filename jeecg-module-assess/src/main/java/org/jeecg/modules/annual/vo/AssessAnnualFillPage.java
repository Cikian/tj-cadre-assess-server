package org.jeecg.modules.annual.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.modules.sys.entity.annual.AssessAnnualArrange;
import org.jeecg.modules.sys.entity.annual.AssessAnnualDutyReport;
import org.jeecg.modules.sys.entity.annual.AssessAnnualSummary;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description: 年度考核填报列表
 * @Author: jeecg-boot
 * @Date: 2024-09-04
 * @Version: V1.0
 */
@Data
@ApiModel(value = "assess_annual_fillPage对象", description = "年度考核填报列表")
public class AssessAnnualFillPage {

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
     * 年度
     */
    @Excel(name = "年度", width = 15, dicCode = "assess_year")
    @Dict(dicCode = "assess_year")
    @ApiModelProperty(value = "年度")
    private java.lang.String currentYear;
    /**
     * 名称
     */
    @Excel(name = "名称", width = 15)
    @ApiModelProperty(value = "名称")
    private java.lang.String assessName;
    /**
     * 处室
     */
    @Excel(name = "处室", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @ApiModelProperty(value = "处室")
    private java.lang.String depart;
    /**
     * 领导干部
     */
    @Excel(name = "领导干部", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "username")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "username")
    @ApiModelProperty(value = "领导干部")
    @NotNull(message = "领导干部不能为空")
    private java.lang.String leader;
    /**
     * 状态
     */
    @Excel(name = "状态", width = 15, dicCode = "report_status")
    @Dict(dicCode = "report_status")
    @ApiModelProperty(value = "状态")
    private java.lang.Integer status;
    /**
     * 填报人
     */
    @Excel(name = "填报人", width = 15)
    @ApiModelProperty(value = "填报人")
    private java.lang.String reportBy;

    /**
     * 备注
     */
    @Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private java.lang.String remark;

//    @ExcelCollection(name = "述职报告")
//    @ApiModelProperty(value = "述职报告")
//    @Valid
//    @NotNull(message = "述职报告不能为空")
//    private List<AssessAnnualDutyReport> assessAnnualDutyReportList;

    @ExcelCollection(name = "年度考核工作安排")
    @ApiModelProperty(value = "年度考核工作安排")
    @Valid
    private List<AssessAnnualArrange> assessAnnualArrangeList;

    @ExcelCollection(name = "年度考核汇总列表")
    @ApiModelProperty(value = "汇总列表")
    private List<AssessAnnualSummary> summaryList;

    private String departType;

    private String duty;

    private Boolean nonAccountability;

    private Boolean nonVacation;

}
