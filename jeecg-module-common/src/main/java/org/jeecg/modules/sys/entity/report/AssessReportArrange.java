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
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Description: 一报告两评议工作安排
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
@Data
@TableName("assess_report_arrange")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_report_arrange对象", description = "一报告两评议工作安排")
public class AssessReportArrange implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
    /**
     * 单位名称
     */
    @Excel(name = "单位名称", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @ApiModelProperty(value = "单位名称")
    private java.lang.String depart;
    /**
     * 年度
     */
    @Excel(name = "年度", width = 15, dicCode = "assess_year")
    @Dict(dicCode = "assess_year")
    @ApiModelProperty(value = "年度")
    private java.lang.String currentYear;
    /**
     * 单位人数
     */
    @Excel(name = "单位人数", width = 15)
    @ApiModelProperty(value = "单位人数")
    private java.lang.Integer num;

    private Integer voteA;
    private Integer voteB;

    private String scope;

    /**
     * 民主测评参加范围
     */
    @Excel(name = "被评议对象", width = 15)
    @ApiModelProperty(value = "被评议对象")
    private java.lang.String partinScope;
    /**
     * 民主测评应道人数
     */
    @Excel(name = "民主测评应道人数", width = 15)
    @ApiModelProperty(value = "民主测评应道人数")
    private java.lang.Integer partinNum;
    /**
     * 需要说明的情况
     */
    @Excel(name = "需要说明的情况", width = 15)
    @ApiModelProperty(value = "需要说明的情况")
    private java.lang.String remark;
    /**
     * 填报id（外键）
     */
    @Excel(name = "填报id（外键）", width = 15)
    @ApiModelProperty(value = "填报id（外键）")
    private java.lang.String fillId;

    /**
     * 联系人
     */
    @Excel(name = "联系人", width = 15)
    @ApiModelProperty(value = "联系人")
    @NotNull(message = "联系人不能为空！")
    private java.lang.String contacts;

    /**
     * 手机
     */
    @Excel(name = "手机", width = 15)
    @ApiModelProperty(value = "手机")
    private java.lang.String contactsPhone;

    /**
     * 办公电话
     */
    @Excel(name = "办公电话", width = 15)
    @ApiModelProperty(value = "办公电话")
    @NotNull(message = "办公电话不能为空！")
    private java.lang.String officeTel;
}
