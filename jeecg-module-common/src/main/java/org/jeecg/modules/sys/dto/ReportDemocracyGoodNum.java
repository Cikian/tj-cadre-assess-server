package org.jeecg.modules.sys.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Title: ReportDemocracyRanking
 * @Author Cikian
 * @Package org.jeecg.modules.sys.dto
 * @Date 2024/11/17 03:15
 * @description: jeecg-boot-parent: 排名
 */

@Data
public class ReportDemocracyGoodNum {
    private String summaryId;
    private int total;
    private String good;
    private BigDecimal rate;
    private Integer ranking;
}
