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
 * @Description: 年度考核优秀比例设置
 * @Author: jeecg-boot
 * @Date:   2024-11-04
 * @Version: V1.0
 */
@Data
@TableName("assess_annual_excellent_ratio")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="assess_annual_excellent_ratio对象", description="年度考核优秀比例设置")
public class AssessAnnualExcellentRatio implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private Date createTime;
	/**更新人*/
    @ApiModelProperty(value = "更新人")
    private String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private Date updateTime;
	/**领导班子优秀比例*/
	@Excel(name = "领导班子优秀比例", width = 15)
    @ApiModelProperty(value = "领导班子优秀比例")
    private BigDecimal groupRatio;
	/**局机关优秀比例*/
	@Excel(name = "局机关优秀比例", width = 15)
    @ApiModelProperty(value = "局机关优秀比例")
    private BigDecimal bureauRatio;
	/**基层单位优秀比例*/
	@Excel(name = "基层单位优秀比例", width = 15)
    @ApiModelProperty(value = "基层单位优秀比例")
    private BigDecimal basicRatio;
	/**事业单位优秀比例*/
	@Excel(name = "事业单位优秀比例", width = 15)
    @ApiModelProperty(value = "事业单位优秀比例")
    private BigDecimal institutionRatio;
	/**启用*/
	@Excel(name = "启用", width = 15)
    @ApiModelProperty(value = "启用")
    private Integer enable;
}
