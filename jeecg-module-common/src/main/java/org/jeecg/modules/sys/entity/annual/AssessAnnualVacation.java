package org.jeecg.modules.sys.entity.annual;

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
import java.util.Date;

/**
 * @Description: 年度考核休假情况
 * @Author: jeecg-boot
 * @Date: 2024-09-03
 * @Version: V1.0
 */
@Data
@TableName("assess_annual_vacation")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_annual_vacation对象", description = "年度考核休假情况")
public class AssessAnnualVacation implements Serializable {
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
     * 年度
     */
    @Excel(name = "年度", width = 15, dicCode = "assess_year")
    @Dict(dicCode = "assess_year")
    @ApiModelProperty(value = "年度")
    private java.lang.String currentYear;
    /**
     * 姓名
     */
    @Excel(name = "姓名", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @ApiModelProperty(value = "姓名")
    private java.lang.String name;
    /**
     * 单位（处室）
     */
    @Excel(name = "单位（处室）", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @ApiModelProperty(value = "单位（处室）")
    private java.lang.String depart;

    @TableField(exist = false)
    private String depart_dictText;
    /**
     * 休假类型
     */
    @Excel(name = "休假类型", width = 15, dicCode = "vacation_type")
    @Dict(dicCode = "vacation_type")
    @ApiModelProperty(value = "休假类型")
    private java.lang.Integer type;
    /**
     * 休假开始时间
     */
    @Excel(name = "休假开始时间", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "休假开始时间")
    private java.util.Date startDate;
    /**
     * 休假结束时间
     */
    @Excel(name = "休假结束时间", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "休假结束时间")
    private java.util.Date endDate;
    /**
     * 备注
     */
    @Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private java.lang.String remark;
    /**
     * 申请状态
     */
    @Excel(name = "申请状态", width = 15, dicCode = "apply_status")
    @Dict(dicCode = "adopt_status")
    @ApiModelProperty(value = "申请状态")
    private java.lang.Integer status;

    private String hashId;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyyMMdd")
    private Date birth;
    private String sex;

    private Integer days;


}
