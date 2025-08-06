package org.jeecg.modules.sys.entity.annual;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * @className: AssessLeaderRec
 * @author: Cikian
 * @date: 2024/12/26 19:28
 * @Version: 1.0
 * @description:
 */

@Data
@TableName("assess_leader_rec")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_leader_rec对象", description = "领导推优")
public class AssessLeaderRec implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    /**
     * 领导
     */
    @Excel(name = "领导", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @ApiModelProperty(value = "领导")
    private String leader;
    /**
     * 年度
     */
    @Excel(name = "年度", width = 15, dicCode = "assess_year")
    @Dict(dicCode = "assess_year")
    @ApiModelProperty(value = "年度")
    private String currentYear;
    /**
     * 机关推优情况
     */
    @Excel(name = "机关推优情况", width = 15)
    @ApiModelProperty(value = "机关推优情况")
    private String bureauRec;
    /**
     * 班子推优情况
     */
    @Excel(name = "班子推优情况", width = 15)
    @ApiModelProperty(value = "班子推优情况")
    private String groupRec;
    /**
     * 分局参公推优情况
     */
    @Excel(name = "分局参公推优情况", width = 15)
    @ApiModelProperty(value = "分局参公推优情况")
    private String basicRec;
    /**
     * 分局参公推优情况
     */
    @Excel(name = "事业单位推优情况", width = 15)
    @ApiModelProperty(value = "事业单位推优情况")
    private String institutionRec;
    /**
     * 状态
     */
    @Excel(name = "状态", width = 15)
    @ApiModelProperty(value = "状态")
    private String status;

    private Integer bureauTrueNum;
    private Integer groupTrueNum;
    private Integer basicTrueNum;
    private Integer institutionTrueNum;
}
