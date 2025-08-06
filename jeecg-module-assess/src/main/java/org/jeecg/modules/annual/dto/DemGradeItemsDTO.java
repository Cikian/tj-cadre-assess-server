package org.jeecg.modules.annual.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Title: DemGradeDTO
 * @Author Cikian
 * @Package org.jeecg.modules.annual.vo
 * @Date 2025/1/20 11:21
 * @description: jeecg-boot-parent: 年度民主测评成绩导出
 */

@Data
public class DemGradeItemsDTO {
    private int no;
    private String name;
    private String depart;
    private BigDecimal grade;
    private BigDecimal graderA;
    private BigDecimal graderB;
    private BigDecimal graderC;
    private BigDecimal graderD;
    private BigDecimal graderE;
}
