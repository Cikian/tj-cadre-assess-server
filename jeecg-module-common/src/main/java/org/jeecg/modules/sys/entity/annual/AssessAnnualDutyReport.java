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

/**
 * @Description: 述职报告
 * @Author: jeecg-boot
 * @Date: 2024-09-04
 * @Version: V1.0
 */
@ApiModel(value = "assess_annual_duty_report对象", description = "述职报告")
@Data
@TableName("assess_annual_duty_report")
public class AssessAnnualDutyReport implements Serializable {
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
     * 考核填报id
     */
    @ApiModelProperty(value = "考核填报id")
    private java.lang.String annualFillId;
    /**
     * 人员id
     */
    @Excel(name = "人员id", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @ApiModelProperty(value = "人员id")
    private java.lang.String personId;
    /**
     * 述职报告
     */
    @Excel(name = "述职报告", width = 15)
    @ApiModelProperty(value = "述职报告")
    private java.lang.String dutyReport;

    private String excellentDeputy; // 是否推荐优秀副职

    // chief：处级正职，deputy：处级副职，group：领导班子
    @TableField(exist = false)
    private String type;
}
