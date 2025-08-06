package org.jeecg.modules.annual.vo;

import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;

import java.math.BigDecimal;

/**
 * @Title: InspectionVO
 * @Author Cikian
 * @Package org.jeecg.modules.annual.vo
 * @Date 2024/12/8 03:26
 * @description: jeecg-boot-parent: 纪检组推荐
 */

@Data
public class InspectionVO {
    private String id;
    private String person;
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    private String depart;
    private BigDecimal score;
    private Integer scopeRanking;
    private Integer ranking;
    private Boolean inspection;
    private String hashId;
}
