package org.jeecg.modules.sys.entity.business;

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

/**
 * @Description: 业务考核成绩汇总
 * @Author: jeecg-boot
 * @Date: 2024-08-28
 * @Version: V1.0
 */
@Data
@TableName("assess_business_grade")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_business_grade对象", description = "业务考核成绩汇总")
public class AssessBusinessGrade implements Serializable {
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
     * 排名
     */
    @Excel(name = "排名", width = 15)
    @ApiModelProperty(value = "排名")
    private java.lang.Integer ranking;
    /**
     * 单位（处室）
     */
    @Excel(name = "单位（处室）", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @ApiModelProperty(value = "单位（处室）")
    private java.lang.String departId;
    /**
     * 分数
     */
    @Excel(name = "分数", width = 15)
    @ApiModelProperty(value = "分数")
    private java.math.BigDecimal score;
    /**
     * 测评得分
     */
    @Excel(name = "测评得分", width = 15)
    @ApiModelProperty(value = "测评得分")
    private java.math.BigDecimal assessScore;
    /**
     * 加分
     */
    @Excel(name = "加分", width = 15)
    @ApiModelProperty(value = "加分")
    private java.math.BigDecimal addPoints;
    /**
     * 加分
     */
    @Excel(name = "减分", width = 15)
    @ApiModelProperty(value = "减分")
    private java.math.BigDecimal subtractPoints;
    /**
     * 加分原因
     */
    @Excel(name = "加分原因", width = 15)
    @ApiModelProperty(value = "加分原因")
    private java.lang.String addReason;
    /**
     * 减分原因
     */
    @Excel(name = "减分原因", width = 15)
    @ApiModelProperty(value = "减分原因")
    private java.lang.String subtractReason;
    /**
     * 等次
     */
    @Excel(name = "等次", width = 15, dicCode = "assess_level")
    @Dict(dicCode = "assess_level")
    @ApiModelProperty(value = "等次")
    private java.lang.String level;
    /**
     * 是否人为修改
     */
    @Excel(name = "是否人为修改", width = 15)
    @ApiModelProperty(value = "是否人为修改")
    private java.lang.Integer manEdit;
    /**
     * 单位类型
     */
    @Excel(name = "单位类型", width = 15, dicCode = "score_depart_type")
    @Dict(dicCode = "score_depart_type")
    @ApiModelProperty(value = "单位类型")
    private java.lang.String departType;

    /**
     * 单位顺序
     */
    @Excel(name = "单位类型", width = 15)
    @ApiModelProperty(value = "单位顺序")
    @TableField(exist = false)
    private java.lang.Integer departOrder;
}
