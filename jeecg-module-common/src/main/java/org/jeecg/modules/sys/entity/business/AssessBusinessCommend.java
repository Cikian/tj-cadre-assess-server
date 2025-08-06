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
 * @Description: 表彰
 * @Author: jeecg-boot
 * @Date: 2024-08-30
 * @Version: V1.0
 */
@Data
@TableName("assess_business_commend")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_business_commend对象", description = "表彰")
public class AssessBusinessCommend implements Serializable {
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
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    @ApiModelProperty(value = "处室（单位）")
    private java.lang.String departmentCode;
    /**
     * 表彰单位
     */
    @Excel(name = "表彰单位", width = 15)
    @ApiModelProperty(value = "表彰单位")
    private java.lang.String commendUnit;
    /**
     * 表彰项目
     */
    @Excel(name = "表彰项目", width = 15)
    @ApiModelProperty(value = "表彰项目")
    private java.lang.String commendProject;
    /**
     * 表彰时间
     */
    @Excel(name = "表彰时间", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "表彰时间")
    private java.util.Date commendTime;
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
     * 状态（1：审核中；2：审核通过；3：采用；4：不采用）
     */
//    @Excel(name = "状态（1：审核中；2：审核通过；3：采用；4：不采用）", width = 15)
    @ApiModelProperty(value = "状态（1：审核中；2：审核通过；3：采用；4：不采用）")
    private java.lang.Integer status;

    private String remark;
}
