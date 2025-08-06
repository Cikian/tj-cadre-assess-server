package org.jeecg.modules.sys.entity.report;

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

/**
 * @Description: 评议对象有关信息汇总表
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
@Data
@TableName("assess_report_object_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_report_object_info对象", description = "评议对象有关信息汇总表")
public class AssessReportObjectInfo implements Serializable {
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
    @ApiModelProperty(value = "姓名")
    private java.lang.String name;

    @TableField(exist = false)
    private java.lang.String name_dictText;
    /**
     * 出生年月
     */
    @Excel(name = "出生年月", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "出生年月")
    private java.util.Date birthday;
    /**
     * 现任职务
     */
    @Excel(name = "现任职务", width = 15)
    @ApiModelProperty(value = "现任职务")
    private java.lang.String presentPosition;
    /**
     * 入党时间
     */
    @Excel(name = "入党时间", width = 15)
    @ApiModelProperty(value = "入党时间")
    private String joinPartyDate;
    /**
     * 参加工作时间
     */
    @Excel(name = "参加工作时间", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "参加工作时间")
    private java.util.Date joinWorkDate;
    /**
     * 是否进行谈话推荐
     */
    @Excel(name = "是否进行谈话推荐", width = 15)
    @ApiModelProperty(value = "是否进行谈话推荐")
    private Boolean talkRecommend;
    /**
     * 是否进行会议推荐
     */
    @Excel(name = "是否进行会议推荐", width = 15)
    @ApiModelProperty(value = "是否进行会议推荐")
    private Boolean meetRecommend;
    /**
     * 是否进行考察
     */
    @Excel(name = "是否进行考察", width = 15)
    @ApiModelProperty(value = "是否进行考察")
    private Boolean inspect;
    /**
     * 是否进行干部人事档案审核
     */
    @Excel(name = "是否进行干部人事档案审核", width = 15)
    @ApiModelProperty(value = "是否进行干部人事档案审核")
    private Boolean fileReview;
    /**
     * 是否进行个人有关事项查核
     */
    @Excel(name = "是否进行个人有关事项查核", width = 15)
    @ApiModelProperty(value = "是否进行个人有关事项查核")
    private Boolean personalReview;
    /**
     * 是否征求纪检监察机关意见
     */
    @Excel(name = "是否征求纪检监察机关意见", width = 15)
    @ApiModelProperty(value = "是否征求纪检监察机关意见")
    private Boolean disciplineOpinion;
    /**
     * 是否有不得列为考察对象的情形
     */
    @Excel(name = "是否有不得列为考察对象的情形", width = 15)
    @ApiModelProperty(value = "是否有不得列为考察对象的情形")
    private Boolean nonAssessSituation;
    /**
     * 是否经会议集体讨论决定
     */
    @Excel(name = "是否经会议集体讨论决定", width = 15)
    @ApiModelProperty(value = "是否经会议集体讨论决定")
    private Boolean discuss;
    /**
     * 参会人员是否符合规定要求
     */
    @Excel(name = "参会人员是否符合规定要求", width = 15)
    @ApiModelProperty(value = "参会人员是否符合规定要求")
    private Boolean accord;
    /**
     * 是否进行公示
     */
    @Excel(name = "是否进行公示", width = 15)
    @ApiModelProperty(value = "是否进行公示")
    private Boolean publicity;
    /**
     * 是否有举报反映，有无进行查核
     */
    @Excel(name = "是否有举报反映，有无进行查核", width = 15, dicCode = "hasReport")
    @Dict(dicCode = "hasReport")
    @ApiModelProperty(value = "是否有举报反映，有无进行查核")
    private String expose;
    /**
     * 填报id（外键）
     */
    @Excel(name = "填报id（外键）", width = 15)
    @ApiModelProperty(value = "填报id（外键）")
    private java.lang.String fillId;
}
