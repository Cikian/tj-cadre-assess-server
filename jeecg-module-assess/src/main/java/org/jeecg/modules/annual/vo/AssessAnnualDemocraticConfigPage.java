package org.jeecg.modules.annual.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.modules.sys.entity.annual.AssessAnnualDemocraticItem;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

/**
 * @Description: 民主测评指标
 * @Author: jeecg-boot
 * @Date: 2024-09-11
 * @Version: V1.0
 */
@Data
@ApiModel(value = "assess_annual_democratic_configPage对象", description = "民主测评指标")
public class AssessAnnualDemocraticConfigPage {

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
    @Excel(name = "测评对象", width = 15, dicCode = "democratic_type")
    @Dict(dicCode = "democratic_type")
    @ApiModelProperty(value = "测评对象")
    private java.lang.String type;
    /**
     * 指标名称
     */
    @Excel(name = "指标名称", width = 15)
    @ApiModelProperty(value = "指标名称")
    private java.lang.String name;
    /**
     * A项权重
     */
    @Excel(name = "A项权重", width = 15)
    @ApiModelProperty(value = "A项权重")
    private java.math.BigDecimal weightA;
    /**
     * B项权重
     */
    @Excel(name = "B项权重", width = 15)
    @ApiModelProperty(value = "B项权重")
    private java.math.BigDecimal weightB;
    /**
     * C项权重
     */
    @Excel(name = "C项权重", width = 15)
    @ApiModelProperty(value = "C项权重")
    private java.math.BigDecimal weightC;
    /**
     * D项权重
     */
    @Excel(name = "D项权重", width = 15)
    @ApiModelProperty(value = "D项权重")
    private java.math.BigDecimal weightD;

    @ExcelCollection(name = "民主测评指标项")
    @ApiModelProperty(value = "民主测评指标项")
    private List<AssessAnnualDemocraticItem> assessAnnualDemocraticItemList;

}
