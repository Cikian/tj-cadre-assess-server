package org.jeecg.modules.annual.vo;


import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.modules.sys.entity.annual.AssessAnnualNegativeList;

import java.util.List;

/**
 * @className: LeaderRecommendListVO
 * @author: Cikian
 * @date: 2024/11/17 21:25
 * @Version: 1.0
 * @description:
 */

@Data
public class LeaderRecommendListVO {
    private String hashId;
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    private String depart;
    private String name;
    // 职务
    private String duties;
    // 职级
    private String post;
    // 类别
    private String type;
    // 民主测评考核单元内排名
    private Integer democraticRanking;
    // 评优建议
    private String suggestion;
    // 不能评优情况汇总
    private String remark;
    // 参考信息
    private String negativeList;
    // 能否评优
    private Boolean canExcellent;
    // 纪检组推荐
    private Boolean disciplineRecommend;
    // 推荐类型
    private String recommendType;
    // 局领导推荐
    private Boolean leaderRecommend;
    // 连续两年评为优秀
    private String twoYear;
    // 优秀副职
    private String excellentDeputy;
    // 其他信息
    private String info;

}
