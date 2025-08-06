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

/**
 * @Description: 年度考核成绩汇总
 * @Author: jeecg-boot
 * @Date: 2024-09-10
 * @Version: V1.0
 */
@Data
@TableName("assess_annual_summary")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_annual_summary对象", description = "年度考核成绩汇总")
public class AssessAnnualSummary implements Serializable {
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
     * 年度
     */
    @Excel(name = "年度", width = 15, dicCode = "assess_year")
    @Dict(dicCode = "assess_year")
    @ApiModelProperty(value = "年度")
    private String currentYear;
    /**
     * 考核名称
     */
    @Excel(name = "考核名称", width = 15, dictTable = "assess_annual_fill", dicText = "assess_name", dicCode = "id")
    @Dict(dictTable = "assess_annual_fill", dicText = "assess_name", dicCode = "id")
    @ApiModelProperty(value = "考核名称")
    private String fillId;
    /**
     * 是否问责
     */
    @Excel(name = "是否问责", width = 15)
    @ApiModelProperty(value = "是否问责")
    private String accountability;
    /**
     * 是否休假
     */
    @Excel(name = "是否休假", width = 15)
    @ApiModelProperty(value = "是否休假")
    private String vacation;
    /**
     * 评优建议
     */
    @Excel(name = "评优建议", width = 15, dicCode = "evaluation_suggestion")
    @Dict(dicCode = "evaluation_suggestion")
    @ApiModelProperty(value = "评优建议")
    private String evaluation;
    /**
     * 优秀副职
     */
    @Excel(name = "优秀副职", width = 15)
    @ApiModelProperty(value = "优秀副职")
    private String excellentDeputy;
    /**
     * 处室类型（不展示，查询用）
     */
    @Excel(name = "处室类型（不展示，查询用）", width = 15, dicCode = "depart_type")
    @Dict(dicCode = "depart_type")
    @ApiModelProperty(value = "处室类型（不展示，查询用）")
    private String departType;
    /**
     * 负面清单
     */
    @Excel(name = "负面清单", width = 15)
    @ApiModelProperty(value = "负面清单")
    private String negativeList;
    /**
     * 证明材料
     */
    @Excel(name = "证明材料", width = 15)
    @ApiModelProperty(value = "证明材料")
    private String negativeProof;
    /**
     * 党建考核
     */
    @Excel(name = "党建考核", width = 15, dicCode = "party_assess")
    @ApiModelProperty(value = "党建考核")
    private String partyAssess;
    /**
     * 三等功资格
     */
    @Excel(name = "三等功资格", width = 15)
    @ApiModelProperty(value = "三等功资格")
    private String thirdClass;
    /**
     * 纪检组推荐
     */
    @Excel(name = "纪检组推荐", width = 15)
    @ApiModelProperty(value = "纪检组推荐")
    private String disciplineRecommend;
    /**
     * 局领导推优
     */
    @Excel(name = "局领导推优", width = 15)
    @ApiModelProperty(value = "局领导推优")
    private Boolean leaderRecommend;
    /**
     * 述职报告
     */
    @Excel(name = "述职报告", width = 15)
    @ApiModelProperty(value = "述职报告")
    private String dutyReport;
    /**
     * 等次
     */
    @Excel(name = "等次", width = 15)
//    @Dict(dictTable = "sys_dict_item", dicText = "item_text", dicCode = "id")
    @ApiModelProperty(value = "等次")
    private String level;

    @ApiModelProperty(value = "推荐类型")
    private String recommendType;

    @Excel(name = "姓名", width = 15)
    @ApiModelProperty(value = "姓名")
    private String person;

    @Excel(name = "性别", width = 15, dicCode = "sex")
    @Dict(dicCode = "sex")
    @ApiModelProperty(value = "性别")
    private String sex;

    @Excel(name = "出生日期", width = 15)
    @ApiModelProperty(value = "出生日期")
    private String birth;

    /**
     * 单位
     */
    @Excel(name = "所在处室（单位）", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    @ApiModelProperty(value = "所在处室（单位）")
    private String depart;

//    @Excel(name = "所在处室（单位）", width = 15)
//    @TableField(exist = false)
//    private String departName;

    @Excel(name = "职务", width = 15)
    @ApiModelProperty(value = "职务")
    private String duties;

    @Excel(name = "职级", width = 15, dicCode = "position_rank")
    @Dict(dicCode = "position_rank")
    @ApiModelProperty(value = "职级")
    private String position;

    /**
     * 述职报告
     * chief：处级正职，deputy：处级副职，group：领导班子
     */
    @Excel(name = "类别", width = 15, dicCode = "annual_type")
    @Dict(dicCode = "annual_type")
    @ApiModelProperty(value = "类别")
    private String type;

    @Excel(name = "是否分管纪检工作", width = 15, dicCode = "true_false")
    @Dict(dicCode = "true_false")
    @ApiModelProperty(value = "是否分管纪检工作")
    private Boolean inspectionWork;

    @Excel(name = "是否党组织书记", width = 15, dicCode = "true_false")
    @Dict(dicCode = "true_false")
    @ApiModelProperty(value = "是否党组织书记")
    private Boolean secretary;

    @Excel(name = "是否党政主要负责人（对应业务考核）", width = 15, dicCode = "true_false")
    @Dict(dicCode = "true_false")
    @ApiModelProperty(value = "是否党政主要负责人（对应业务考核）")
    private Boolean partyHeader;

    @Excel(name = "是否参与平时考核（只针对分局、参公）", width = 15, dicCode = "true_false")
    @Dict(dicCode = "true_false")
    @ApiModelProperty(value = "是否参与平时考核（只针对分局、参公）")
    private Boolean hasRegular;

    @Excel(name = "身份", width = 15)
    @ApiModelProperty(value = "身份")
    private String identityType;

    @TableField(exist = false)
    private int status;

    private String hashId;

    private String remark;

    @TableField(exist = false)
    private String quarter1;
    @TableField(exist = false)
    private String quarter2;
    @TableField(exist = false)
    private String quarter3;
    @TableField(exist = false)
    private String quarter4;

    // 等次补订标记
    private Boolean redoFlag;

    @Excel(name = "序号", width = 15)
    @ApiModelProperty(value = "序号")
    private String personOrder;
}
