package org.jeecg.modules.annual.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;

/**
 * @Title: AnnualGroupSummaryVO
 * @Author Cikian
 * @Package org.jeecg.modules.annual.vo
 * @Date 2024/12/11 23:59
 * @description: jeecg-boot-parent: 年度考核领导班子汇总视图
 */

@Data
public class AnnualGroupSummaryVO {
    private String id;
    private String currentYear;
    @Dict(dictTable = "assess_annual_fill", dicText = "assess_name", dicCode = "id")
    private String fillId;
    @ApiModelProperty(value = "评优建议")
    @Dict(dicCode = "evaluation_suggestion")
    private String evaluation;
    @ApiModelProperty(value = "处室类型（不展示，查询用）")
    @Dict(dicCode = "depart_type")
    private String departType;
    private String list;
    private Boolean leaderRecommend;
    private String dutyReport;
    private String level;
    private String person;
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    private String depart;
    private Integer ranking;
    private String remark;
    private Boolean redoFlag;
}
