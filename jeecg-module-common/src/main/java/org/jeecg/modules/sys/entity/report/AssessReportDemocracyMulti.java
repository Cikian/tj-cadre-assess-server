package org.jeecg.modules.sys.entity.report;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * @Description: 一报告两评议民主测评填报多选项
 * @Author: jeecg-boot
 * @Date: 2024-10-08
 * @Version: V1.0
 */
@Data
@TableName("assess_report_democracy_multi")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_report_democracy_multi对象", description = "一报告两评议民主测评填报多选项")
public class AssessReportDemocracyMulti implements Serializable {
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
     * 测评项id（外键）
     */
    @Excel(name = "测评项id（外键）", width = 15)
    @ApiModelProperty(value = "测评项id（外键）")
    private java.lang.String itemId;
    /**
     * 选项
     */
    @Excel(name = "选项", width = 15)
    @ApiModelProperty(value = "选项")
    private java.lang.String topic;
    /**
     * 是否选择
     */
    @Excel(name = "是否选择", width = 15)
    @ApiModelProperty(value = "是否选择")
    private Boolean selected;

    private String summaryId;
}
