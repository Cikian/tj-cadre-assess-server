package org.jeecg.modules.annual.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Title: AssessAnnualExcellentConfigVO
 * @Author Cikian
 * @Package org.jeecg.modules.annual.vo
 * @Date 2024/9/17 15:19
 * @description: jeecg-boot-parent:
 */

@Data
public class AssessAnnualExcellentConfigVO {
    private String id;
    private BigDecimal groupRatio;
    private BigDecimal bureauRatio;
    private BigDecimal basicRatio;
    private BigDecimal institutionRatio;
}
