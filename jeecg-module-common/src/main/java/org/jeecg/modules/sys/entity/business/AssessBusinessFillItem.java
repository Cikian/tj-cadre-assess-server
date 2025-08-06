package org.jeecg.modules.sys.entity.business;

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
 * @Description: 业务考核填报项目
 * @Author: jeecg-boot
 * @Date: 2024-08-23
 * @Version: V1.0.12
 */
@ApiModel(value = "assess_business_fill_item对象", description = "业务考核填报项目")
@Data
@TableName("assess_business_fill_item")
public class AssessBusinessFillItem implements Serializable {
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
    private java.util.Date createTime;
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
    private java.util.Date updateTime;
    /**
     * 所属部门
     */
    @ApiModelProperty(value = "所属部门")
    private String sysOrgCode;
    /**
     * 被考核单位
     */
    @Excel(name = "被考核单位", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @ApiModelProperty(value = "被考核单位")
    @Dict(dicCode = "id", dictTable = "sys_depart", dicText = "alias")
    private String departCode;
    /**
     * 分数
     */
    @Excel(name = "分数", width = 15, dicCode = "business_score")
    @ApiModelProperty(value = "分数")
    private java.math.BigDecimal score;
    /**
     * 主表ID
     */
    @ApiModelProperty(value = "主表ID")
    private String fillId;
    /**
     * 扣分原因
     */
    @Excel(name = "扣分原因", width = 15)
    @ApiModelProperty(value = "扣分原因")
    private String reasonOfDeduction;
    /**
     * 考核单位
     */
    @Excel(name = "考核单位", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @ApiModelProperty(value = "考核单位")
    @Dict(dicCode = "id", dictTable = "sys_depart", dicText = "depart_name")
    private String reportDepart;

    private String gatherId;

    private boolean mustFill;

    private boolean canEdit;

    @TableField(exist = false)
    private int sortNo;
}
