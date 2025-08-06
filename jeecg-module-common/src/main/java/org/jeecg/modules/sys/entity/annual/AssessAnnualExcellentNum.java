package org.jeecg.modules.sys.entity.annual;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 年度考核分管领导优秀名额
 * @Author: jeecg-boot
 * @Date: 2024-11-04
 * @Version: V1.0
 */
@Data
@TableName("assess_annual_excellent_num")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_annual_excellent_num对象", description = "年度考核分管领导优秀名额")
public class AssessAnnualExcellentNum implements Serializable {
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
     * 年度
     */
    @Excel(name = "年度", width = 15, dicCode = "assess_year")
    @Dict(dicCode = "assess_year")
    @ApiModelProperty(value = "年度")
    private String currentYear;
    /**
     * 领导
     */
    @Excel(name = "领导", width = 15)
    @ApiModelProperty(value = "领导")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    private String leader;
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    private String depart;
    /**
     * 领导班子优秀比例
     */
    @Excel(name = "领导班子优秀比例", width = 15)
    @ApiModelProperty(value = "领导班子优秀比例")
    private BigDecimal groupRatio;
    private BigDecimal groupNum;
    private BigDecimal groupTheoryNum;
    private BigDecimal groupTrueNum;
    /**
     * 局机关优秀比例
     */
    @Excel(name = "局机关优秀比例", width = 15)
    @ApiModelProperty(value = "局机关优秀比例")
    private BigDecimal bureauRatio;
    private BigDecimal bureauNum;
    private BigDecimal bureauTheoryNum;
    private BigDecimal bureauTrueNum;
    /**
     * 基层单位优秀比例
     */
    @Excel(name = "基层单位优秀比例", width = 15)
    @ApiModelProperty(value = "基层单位优秀比例")
    private BigDecimal basicRatio;
    private BigDecimal basicNum;
    private BigDecimal basicTheoryNum;
    private BigDecimal basicTrueNum;
    /**
     * 事业单位优秀比例
     */
    @Excel(name = "事业单位优秀比例", width = 15)
    @ApiModelProperty(value = "事业单位优秀比例")
    private BigDecimal institutionRatio;
    private BigDecimal institutionNum;
    private BigDecimal institutionTheoryNum;
    private BigDecimal institutionTrueNum;

    private Integer inspectionNum;
}
