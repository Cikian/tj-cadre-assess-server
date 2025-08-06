package org.jeecg.modules.report.vo;

import java.util.List;

import org.jeecg.modules.sys.entity.report.AssessReportDemocracyConfigItem;
import lombok.Data;
import org.jeecg.modules.sys.entity.report.AssessReportMultiItem;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Description: 一报告两评议民主测评配置
 * @Author: jeecg-boot
 * @Date:   2024-10-07
 * @Version: V1.0
 */
@Data
@ApiModel(value="assess_report_democracy_configPage对象", description="一报告两评议民主测评配置")
public class AssessReportDemocracyConfigPage {

	/**主键*/
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
	/**测评名称*/
	@Excel(name = "测评名称", width = 15)
	@ApiModelProperty(value = "测评名称")
    private String democracyName;

	@ExcelCollection(name="一报告两评议民主测评项")
	@ApiModelProperty(value = "一报告两评议民主测评项")
	private List<AssessReportDemocracyConfigItem> configItems;

	private List<AssessReportMultiItem> multiItems;

	private Boolean enable;

}
