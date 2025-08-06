package org.jeecg.modules.sys.entity.annual;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * @Description: 民主测评汇总
 * @Author: jeecg-boot
 * @Date: 2024-09-12
 * @Version: V1.0
 */
@ApiModel(value = "assess_democratic_evaluation_summary对象", description = "民主测评汇总")
@Data
@TableName("assess_democratic_evaluation_summary")
public class AssessDemocraticEvaluationSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createBy;
    /**
     * 创建日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private Date createTime;
    /**
     * 更新人
     */
    @ApiModelProperty(value = "更新人")
    private String updateBy;
    /**
     * 更新日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private Date updateTime;
    /**
     * 所属部门
     */
    @ApiModelProperty(value = "所属部门")
    private String sysOrgCode;
    /**
     * 年度考核汇总id（外键）
     */
    @Excel(name = "年度考核汇总id（外键）", width = 15)
    @ApiModelProperty(value = "年度考核汇总id（外键）")
    private String annualSummaryId;
    /**
     * 年度
     */
    @Excel(name = "年度", width = 15, dicCode = "assess_year")
    @Dict(dicCode = "assess_year")
    @ApiModelProperty(value = "年度")
    private String currentYear;
    /**
     * 测评对象
     */
    @Excel(name = "测评对象", width = 15)
    @ApiModelProperty(value = "测评对象")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    private String appraisee;
    /**
     * 测评名称
     */
    @Excel(name = "测评名称", width = 15)
    @ApiModelProperty(value = "测评名称")
    private String assessName;
    /**
     * 单位
     */
    @Excel(name = "单位", width = 15, dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    @ApiModelProperty(value = "单位")
    private String depart;
    /**
     * 分数
     */
    @Excel(name = "分数", width = 15)
    @ApiModelProperty(value = "分数")
    private java.math.BigDecimal score;
    /**
     * 测评类型
     */
    @Excel(name = "测评类型", width = 15, dicCode = "democratic_type")
    @Dict(dicCode = "democratic_type")
    @ApiModelProperty(value = "测评类型")
    private String type;
    /**
     * 开始时间
     */
    @Excel(name = "开始时间", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "开始时间")
    private Date startDate;
    /**
     * 结束时间
     */
    @Excel(name = "结束时间", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "结束时间")
    private Date endDate;
    /**
     * 测评人数
     */
    @Excel(name = "测评人数", width = 15)
    @ApiModelProperty(value = "测评人数")
    private Integer num;
    /**
     * 已测评数量
     */
    @ApiModelProperty(value = "已测评数量")
    private Integer filledNum;

    @Dict(dicCode = "depart_type")
    @ApiModelProperty(value = "处室类型（不展示，查询用）")
    private String departType;

    @TableField(exist = false)
    @ApiModelProperty(value = "总体评价，计算得出，不存储")
    private String democraticLevel;

    private Integer scopeRanking;
    private Integer ranking;

    @TableField(exist = false)
    private Integer nums;

    @TableField(exist = false)
    private Boolean trial;
}
