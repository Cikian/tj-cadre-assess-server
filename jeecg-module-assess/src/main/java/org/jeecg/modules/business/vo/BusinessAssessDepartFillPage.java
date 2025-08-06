package org.jeecg.modules.business.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.modules.sys.entity.business.AssessBusinessFillItem;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

/**
 * @Description: 业务考核填报列表
 * @Author: jeecg-boot
 * @Date: 2024-08-22
 * @Version: V1.0
 */
@Data
@ApiModel(value = "business_assess_depart_fillPage对象", description = "业务考核填报列表")
public class BusinessAssessDepartFillPage {

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
     * 考核ID
     */
    @Excel(name = "考核ID", width = 15)
    @ApiModelProperty(value = "考核ID")
    private java.lang.String businessAssessId;
    /**
     * 考核名称
     */
    @Excel(name = "考核名称", width = 15)
    @ApiModelProperty(value = "考核名称")
    private java.lang.String assessName;
    /**
     * 年度
     */
    @Excel(name = "年度", width = 15)
    @ApiModelProperty(value = "年度")
    private java.lang.String currentYear;
    /**
     * 截止日期
     */
    @Excel(name = "截止日期", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "截止日期")
    private java.util.Date deadline;
    /**
     * 考核处室
     */
    @Excel(name = "考核处室", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @ApiModelProperty(value = "考核处室")
    private java.lang.String reportDepart;
    /**
     * 被考核处室
     */
    @Excel(name = "被考核处室", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @ApiModelProperty(value = "被考核处室")
    private java.lang.String assessDepart;
    /**
     * 填报人
     */
    @Excel(name = "填报人", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @ApiModelProperty(value = "填报人")
    private java.lang.String reportBy;
    /**
     * 预警状态
     */
    @Excel(name = "预警状态", width = 15)
    @ApiModelProperty(value = "预警状态")
    private java.lang.Integer warnStatus;
    /**
     * 填报状态
     */
    @Excel(name = "填报状态", width = 15, dicCode = "report_status")
    @Dict(dicCode = "report_status")
    @ApiModelProperty(value = "填报状态")
    private java.lang.Integer status;

    @ExcelCollection(name = "业务考核填报项目")
    @ApiModelProperty(value = "业务考核填报项目")
    private List<AssessBusinessFillItem> assessBusinessFillItemList;

}
