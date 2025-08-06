package org.jeecg.modules.sys.entity;

import  com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 进行中的考核信息
 * @Author: jeecg-boot
 * @Date: 2024-10-23
 * @Version: V1.0
 */
@Data
@NoArgsConstructor
@TableName("assess_current_assess")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_current_assess对象", description = "进行中的考核信息")
public class AssessCurrentAssess implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    /**
     * 考核
     */
    @Excel(name = "考核", width = 15)
    @ApiModelProperty(value = "考核")
    private String assess;
    /**
     * 考核名称
     */
    @Excel(name = "考核名称", width = 15)
    @ApiModelProperty(value = "考核名称")
    private String assessName;
    /**
     * 年度
     */
    @Excel(name = "年度", width = 15)
    @ApiModelProperty(value = "年度")
    private String currentYear;
    /**
     * 开始日期
     */
    @Excel(name = "开始日期", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "开始日期")
    private Date startDate;
    /**
     * 截日期
     */
    @Excel(name = "截日期", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "截日期")
    private Date deadline;
    /**
     * 季度（平时考核用）
     */
    @Excel(name = "季度（平时考核用）", width = 15)
    @ApiModelProperty(value = "季度（平时考核用）")
    private String quarter;

    private boolean assessing;

    public AssessCurrentAssess(String assess, String assessName, String currentYear, Date startDate, Date deadline, String quarter, boolean assessing) {
        this.assess = assess;
        this.assessName = assessName;
        this.currentYear = currentYear;
        this.startDate = startDate;
        this.deadline = deadline;
        this.quarter = quarter;
        this.assessing = assessing;
    }
}
