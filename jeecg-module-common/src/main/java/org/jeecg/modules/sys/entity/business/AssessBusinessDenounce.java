package org.jeecg.modules.sys.entity.business;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * @Description: 通报批评
 * @Author: jeecg-boot
 * @Date: 2024-08-30
 * @Version: V1.0
 */
@Data
@TableName("assess_business_denounce")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_business_denounce对象", description = "通报批评")
public class AssessBusinessDenounce implements Serializable {
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
     * 年度
     */
    @Excel(name = "年度", width = 15, dictTable = "year_code", dicText = "year_value", dicCode = "year_key")
    @Dict(dictTable = "year_code", dicText = "year_value", dicCode = "year_key")
    @ApiModelProperty(value = "年度")
    private java.lang.String currentYear;
    /**
     * 处室（单位）
     */
    @Excel(name = "处室（单位）", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @ApiModelProperty(value = "处室（单位）")
    private java.lang.String departmentCode;
    /**
     * 通报单位
     */
    @Excel(name = "通报单位", width = 15)
    @ApiModelProperty(value = "通报单位")
    private java.lang.String denounceUnit;
    /**
     * 通报项目
     */
    @Excel(name = "通报项目", width = 15)
    @ApiModelProperty(value = "通报项目")
    private java.lang.String denounceProject;
    /**
     * 通报时间
     */
    @Excel(name = "通报时间", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "通报时间")
    private java.util.Date denounceTime;
    /**
     * 填报人
     */
//    @Excel(name = "填报人", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @ApiModelProperty(value = "填报人")
    private java.lang.String reportBy;
    /**
     * 证明材料
     */
//    @Excel(name = "证明材料", width = 15)
    @ApiModelProperty(value = "证明材料")
    private java.lang.String proof;
    /**
     * 状态
     */
//    @Excel(name = "状态", width = 15)
    @ApiModelProperty(value = "状态")
    private java.lang.Integer status;

    private String remark;

}
