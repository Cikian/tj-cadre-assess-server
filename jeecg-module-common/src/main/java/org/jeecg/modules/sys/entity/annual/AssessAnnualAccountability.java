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
 * @Description: 年度考核受问责情况
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
@Data
@TableName("assess_annual_accountability")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_annual_accountability对象", description = "年度考核受问责情况")
public class AssessAnnualAccountability implements Serializable {
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
     * 姓名
     */
    @Excel(name = "姓名", width = 15)
    @ApiModelProperty(value = "姓名")
    private java.lang.String personnel;
    /**
     * 年度
     */
    @Excel(name = "年度", width = 15)
    @ApiModelProperty(value = "年度")
    private java.lang.String currentYear;
    /**
     * 单位（处室）
     */
    @Excel(name = "单位（处室）", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    @ApiModelProperty(value = "单位（处室）")
    private java.lang.String depart;

    @TableField(exist = false)
    private java.lang.String depart_dictText;
    /**
     * 问责类型
     */
    @Excel(name = "问责类型", width = 15)
    @ApiModelProperty(value = "问责类型")
    private java.lang.String type;
    /**
     * 问责类型
     */
    @Excel(name = "问责类型", width = 15)
    @ApiModelProperty(value = "问责类型")
    private java.lang.String typeText;
    /**
     * 文号
     */
    @Excel(name = "文号", width = 15)
    @ApiModelProperty(value = "文号")
    private java.lang.String referenceNo;
    /**
     * 事由
     */
    @Excel(name = "事由", width = 15)
    @ApiModelProperty(value = "事由")
    private java.lang.String reason;
    /**
     * 给予处理部门
     */
    @Excel(name = "给予处理部门", width = 15)
    @ApiModelProperty(value = "给予处理部门")
    private java.lang.String processingDepart;
    /**
     * 生效日期
     */
    @Excel(name = "生效日期", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "生效日期")
    private java.util.Date effectiveDate;
    /**
     * 影响期结束
     */
    @Excel(name = "影响期结束", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "影响期结束")
    private java.util.Date endDate;
    /**
     * 证明材料
     */
    @Excel(name = "证明材料", width = 15)
    @ApiModelProperty(value = "证明材料")
    private java.lang.String proof;
    /**
     * 备注
     */
    @Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private java.lang.String remark;
    /**
     * 状态
     */
    @Excel(name = "状态", width = 15)
    @ApiModelProperty(value = "状态")
    @Dict(dicCode = "adopt_status")
    private java.lang.Integer status;

    private String hashId;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyyMMdd")
    private Date birth;
    private String sex;

}
