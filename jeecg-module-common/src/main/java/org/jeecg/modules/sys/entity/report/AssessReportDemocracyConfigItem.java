package org.jeecg.modules.sys.entity.report;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Description: 一报告两评议民主测评项
 * @Author: jeecg-boot
 * @Date:   2024-10-07
 * @Version: V1.0
 */
@ApiModel(value="assess_report_democracy_config_item对象", description="一报告两评议民主测评项")
@Data
@TableName("assess_report_democracy_config_item")
public class AssessReportDemocracyConfigItem implements Serializable {
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
	/**所属部门*/
    @ApiModelProperty(value = "所属部门")
    private String sysOrgCode;
	/**描述*/
	@Excel(name = "描述", width = 15)
    @ApiModelProperty(value = "描述")
    private String title;
	/**类型*/
	@Excel(name = "类型", width = 15, dicCode = "report_democracy_type")
    @ApiModelProperty(value = "类型")
    private String type;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private String remark;
	/**外键*/
    @ApiModelProperty(value = "外键")
    private String configId;

    private int itemOrder;
}
