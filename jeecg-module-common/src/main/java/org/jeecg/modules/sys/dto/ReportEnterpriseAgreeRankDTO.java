package org.jeecg.modules.sys.dto;


import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;

import java.math.BigDecimal;


/**
 * 本年度新提拔干部民主评议排名
 */
@Data
public class ReportEnterpriseAgreeRankDTO {
    private String id;
    private int sort;
    private String name;
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    private String depart;
    private String year;
    private String rank;
    private String count;
    private double agreeRate;
    private double averageRate;
}
