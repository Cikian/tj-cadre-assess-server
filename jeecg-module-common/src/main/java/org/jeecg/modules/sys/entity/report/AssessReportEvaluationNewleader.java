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
 * @Description: 一报告两评议民主评议新提拔任用干部评议
 * @Author: jeecg-boot
 * @Date: 2024-10-09
 * @Version: V1.0
 */
@Data
@TableName("assess_report_evaluation_newleader")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_report_evaluation_newleader对象", description = "一报告两评议民主评议新提拔任用干部评议")
public class AssessReportEvaluationNewleader implements Serializable {
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
     * 新提拔任用领导项（外键）
     */
    @Excel(name = "新提拔任用领导项（外键）", width = 15)
    @ApiModelProperty(value = "新提拔任用领导项（外键）")
    private java.lang.String newLeaderItem;
    /**
     * 填写（A：认同 B：基本认同 C：不认同 D：不了解）
     */
    @Excel(name = "填写（A：认同 B：基本认同 C：不认同 D：不了解）", width = 15)
    @ApiModelProperty(value = "填写（A：认同 B：基本认同 C：不认同 D：不了解）")
    private java.lang.String fill;
    /**
     * 不认同原因
     */
    @Excel(name = "不认同原因", width = 15)
    @ApiModelProperty(value = "不认同原因")
    private java.lang.String reason;

    private String voteType;

    /**
     * 汇总id（外键）
     */
    @Excel(name = "汇总id（外键）", width = 15)
    @ApiModelProperty(value = "汇总id（外键）")
    private java.lang.String summaryId;
}
