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
 * @Description: 年度考核负面清单
 * @Author: jeecg-boot
 * @Date:   2024-11-03
 * @Version: V1.0
 */
@Data
@TableName("assess_annual_negative_list")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="assess_annual_negative_list对象", description="年度考核负面清单")
public class AssessAnnualNegativeList implements Serializable {
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

	/**单位*/
	@Excel(name = "单位", width = 15)
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    @ApiModelProperty(value = "单位")
    private String depart;
    private String person;
    private String hashId;
	/**内容*/
	@Excel(name = "内容", width = 15)
    @ApiModelProperty(value = "内容")
    private String content;
	/**填写单位*/
	@Excel(name = "填写单位", width = 15)
    @ApiModelProperty(value = "填写单位")
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    private String reportDepart;
	/**证明文件*/
	@Excel(name = "证明文件", width = 15)
    @ApiModelProperty(value = "证明文件")
    private String files;
	/**状态*/
	@Excel(name = "状态", width = 15)
    @ApiModelProperty(value = "状态")
    @Dict(dicCode = "report_status")
    private String status;
	/**年度*/
	@Excel(name = "年度", width = 15)
    @ApiModelProperty(value = "年度")
    private String currentYear;
    private String type;

    private String reportBy;
    private String auditBy;
}
