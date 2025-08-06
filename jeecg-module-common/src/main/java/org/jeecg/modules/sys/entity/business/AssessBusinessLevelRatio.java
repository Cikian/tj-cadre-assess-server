package org.jeecg.modules.sys.entity.business;

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
import java.math.BigDecimal;

/**
 * @Description: 业务考核等次默认比例
 * @Author: jeecg-boot
 * @Date: 2024-10-29
 * @Version: V1.0
 */
@Data
@TableName("assess_business_level_ratio")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_business_level_ratio对象", description = "业务考核等次默认比例")
public class AssessBusinessLevelRatio implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    /**
     * 优秀比例
     */
    @Excel(name = "优秀比例", width = 15)
    @ApiModelProperty(value = "优秀比例")
    private BigDecimal excellent;
    /**
     * 良好比例
     */
    @Excel(name = "良好比例", width = 15)
    @ApiModelProperty(value = "良好比例")
    private BigDecimal fine;
    /**
     * 一般比例
     */
    @Excel(name = "一般比例", width = 15)
    @ApiModelProperty(value = "一般比例")
    private BigDecimal common;
    /**
     * 启用
     */
    @Excel(name = "启用", width = 15)
    @ApiModelProperty(value = "启用")
    private boolean enable;
}
