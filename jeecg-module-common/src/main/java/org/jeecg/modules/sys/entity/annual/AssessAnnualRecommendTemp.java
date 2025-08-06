package org.jeecg.modules.sys.entity.annual;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * @Description: 局领导推优临时
 * @Author: jeecg-boot
 * @Date: 2024-11-05
 * @Version: V1.0
 */
@Data
@TableName("assess_annual_recommend_temp")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_annual_recommend_temp对象", description = "局领导推优临时")
public class AssessAnnualRecommendTemp implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    /**
     * 局领导
     */
    @Excel(name = "局领导", width = 15)
    @ApiModelProperty(value = "局领导")
    private String leader;
    /**
     * 领导班子推优
     */
    @Excel(name = "领导班子推优", width = 15)
    @ApiModelProperty(value = "领导班子推优")
    private String groupRec;
    /**
     * 局机关推优
     */
    @Excel(name = "局机关推优", width = 15)
    @ApiModelProperty(value = "局机关推优")
    private String bureauRec;
    /**
     * 基层单位推优
     */
    @Excel(name = "基层单位推优", width = 15)
    @ApiModelProperty(value = "基层单位推优")
    private String basicRec;
    /**
     * 事业单位推优
     */
    @Excel(name = "事业单位推优", width = 15)
    @ApiModelProperty(value = "事业单位推优")
    private String institutionRec;
    /**
     * 是否通过
     */
    @Excel(name = "是否通过", width = 15)
    @ApiModelProperty(value = "是否通过")
    private boolean pass;
    /**
     * 状态
     */
    @Excel(name = "状态", width = 15)
    @ApiModelProperty(value = "状态")
    private String status;
    /**
     * 备注
     */
    @Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private String remark;

    private String currentYear;
}
