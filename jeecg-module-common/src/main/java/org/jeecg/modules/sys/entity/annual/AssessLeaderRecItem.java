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
@TableName("assess_leader_rec_item")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "assess_leader_rec_item对象", description = "领导推优详细")
public class AssessLeaderRecItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    private String recommendType;
    private String name;
    // 职务
    private String duties;
    // 职级
    private String post;
    private String hashId;
    private String suggestion;
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    private String depart;
    private String type;
    private Integer democraticRanking;
    private String remark;
    private String negativeList;

    private String twoYear;
    private String excellentDeputy;
    private Boolean leaderRecommend;
    private Boolean canExcellent;
    private Boolean disciplineRecommend;
    private String info;
    private String mainId;
    private String personOrder;
    private String departName;
}
