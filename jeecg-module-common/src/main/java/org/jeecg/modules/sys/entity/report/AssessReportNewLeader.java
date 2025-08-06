package org.jeecg.modules.sys.entity.report;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
import java.math.BigDecimal;

/**
 * @Description: 新提拔干部名册
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
@Data
@TableName("assess_report_new_leader")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_report_new_leader对象", description = "新提拔干部名册")
public class AssessReportNewLeader implements Serializable {
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
     * 姓名
     */
    @Excel(name = "姓名", width = 15)
    @ApiModelProperty(value = "姓名")
    private java.lang.String name;

    @TableField(exist = false)
    private java.lang.String name_dictText;

    private String sex;

    /**
     * 出生年月
     */
    @Excel(name = "出生年月", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING,timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "出生年月")
    private java.util.Date birthday;
    /**
     * 原任职务及任职时间
     */
    @Excel(name = "原任职务及任职时间", width = 15)
    @ApiModelProperty(value = "原任职务及任职时间")
    private java.lang.String originalPosition;
    private java.lang.String originalDate;
    /**
     * 现任职务及任职时间
     */
    @Excel(name = "现任职务及任职时间", width = 15)
    @ApiModelProperty(value = "现任职务及任职时间")
    private java.lang.String currentPosition;
    private java.lang.String curDate;
    /**
     * 填报id（外键）
     */
    @Excel(name = "填报id（外键）", width = 15)
    @ApiModelProperty(value = "填报id（外键）")
    private java.lang.String fillId;

    /**
     * 年度（提拔年度）
     */
    @Excel(name = "年度", width = 15)
    @ApiModelProperty(value = "年度")
    private java.lang.String currentYear;

    /**
     * 处室（单位）
     */
    @TableField(exist = false)
    @Excel(name = "单位（处室）", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @ApiModelProperty(value = "单位（处室）")
    private String depart;

    private String hashId;

    private BigDecimal recognitionRate;

    private BigDecimal disagreementRate;

    private BigDecimal identityRate;

    private BigDecimal unknownRate;

    @TableField(exist = false)
    private String strBirthday;
}
