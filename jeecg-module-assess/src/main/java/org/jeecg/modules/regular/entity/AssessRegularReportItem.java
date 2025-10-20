package org.jeecg.modules.regular.entity;

import com.baomidou.mybatisplus.annotation.*;
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
 * @Description: 平时考核填报明细表
 * @Author: admin
 * @Date: 2024-08-30
 * @Version: V1.0
 */
@ApiModel(value = "assess_regular_report_item对象", description = "平时考核填报明细表")
@Data
@TableName("assess_regular_report_item")
public class AssessRegularReportItem implements Serializable {
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
     * 姓名
     */
    @Excel(name = "姓名", width = 15)
    @ApiModelProperty(value = "姓名")
    private String name;
    /**
     * 性别
     */
    @Excel(name = "性别", width = 15, dicCode = "sex")
    @Dict(dicCode = "sex")
    @ApiModelProperty(value = "性别")
    private Integer sex;
    /**
     * 电话
     */
    @Excel(name = "电话", width = 15)
    @ApiModelProperty(value = "电话")
    private String phone;
    /**
     * 职务
     */
    @Excel(name = "职务", width = 15)
    @Dict(dictTable = "sys_position", dicText = "name", dicCode = "code")
    @ApiModelProperty(value = "职务")
    private String post;
    /**
     * 是否处级领导
     */
    @Excel(name = "是否局领导考核", width = 15, dicCode = "yn")
    @Dict(dicCode = "yn")
    @ApiModelProperty(value = "是否局领导考核")
    private String isLeader;
    /**
     * 所在处室
     */
    @Excel(name = "所在处室", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    @ApiModelProperty(value = "所在处室")
    private String departmentCode;
    /**
     * 年度
     */
    @Excel(name = "年度", width = 15)
    @Dict(dicCode = "assess_year")
    @ApiModelProperty(value = "年度")
    private String currentYear;
    /**
     * 第一季度
     */
    @Excel(name = "第一季度", width = 15, dicCode = "regular_assess_level")
    @Dict(dicCode = "regular_assess_level")
    @ApiModelProperty(value = "第一季度")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String quarter1;
    /**
     * 第二季度
     */
    @Excel(name = "第二季度", width = 15, dicCode = "regular_assess_level")
    @Dict(dicCode = "regular_assess_level")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "第二季度")
    private String quarter2;
    /**
     * 第三季度
     */
    @Excel(name = "第三季度", width = 15, dicCode = "regular_assess_level")
    @Dict(dicCode = "regular_assess_level")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "第三季度")
    private String quarter3;
    /**
     * 第四季度
     */
    @Excel(name = "第四季度", width = 15, dicCode = "regular_assess_level")
    @Dict(dicCode = "regular_assess_level")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "第四季度")
    private String quarter4;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String note;
    /**
     * 主表
     */
//	@Excel(name = "主表", width = 15)
    @ApiModelProperty(value = "主表")
    private String mainId;
    /**
     * 哈希值
     */
    @ApiModelProperty(value = "哈希值")
    private String hashId;
    /**
     * 职级
     */
    @Excel(name = "职级", width = 15)
    @ApiModelProperty(value = "职级")
    private String rank;
    /**
     * 类别
     */
    @Excel(name = "类别", width = 15)
    @ApiModelProperty(value = "类别")
    private String positionType;
    /**
     * 管理部门
     */
    @Excel(name = "管理部门", width = 15)
    @ApiModelProperty(value = "管理部门")
    private String adminDepartment;
    /**
     * 出生年月
     */
    @Excel(name = "出生日期", width = 15)
    @ApiModelProperty(value = "出生日期")
    private String birthDate;

    /**
     * 性别别名字段（不与数据库表字段映射）
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "性别别名")
    private String sexAlias;

    /**
     * 部门别名字段（不与数据库表字段映射）
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "部门别名")
    private String departmentCodeAlias;

    private String status;

    /**
     * 删除状态（0，正常，1已删除）
     */
    @Excel(name = "删除状态", width = 15, dicCode = "del_flag")
    private Integer delFlag;
}
