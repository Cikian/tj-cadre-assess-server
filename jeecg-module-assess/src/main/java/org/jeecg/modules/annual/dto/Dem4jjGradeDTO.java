package org.jeecg.modules.annual.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Title: DemGradeDTO
 * @Author Cikian
 * @Package org.jeecg.modules.annual.vo
 * @Date 2025/2/6 10:31
 * @description: jeecg-boot-parent: 年度民主测评成绩导出
 */

@Data
public class Dem4jjGradeDTO {
    private int no;
    private String name;
    private String depart;
    private BigDecimal grade;
    private Integer ranking;
    private String scopeRanking;
    private Integer scopeRanking1;
    private Integer scopeRanking2;
    private String level;
}
