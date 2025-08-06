package org.jeecg.modules.annual.vo;


import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.modules.sys.entity.annual.AssessAnnualSummary;

import java.util.List;

/**
 * @className: RecommendTempVO
 * @author: Cikian
 * @date: 2024/11/18 10:55
 * @Version: 1.0
 * @description:
 */

@Data
public class RecommendTempVO {
    private String id;
    @Dict(dicCode = "id",dictTable = "sys_user",dicText = "realname")
    String leader;
    String bureau;
    @Dict(dicCode = "id",dictTable = "sys_depart",dicText = "alias")
    String group;
    String basic;
    String institution;
}
