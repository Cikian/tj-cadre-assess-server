package org.jeecg.modules.sys.dto;


import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;


/**
 * 干部选拔任用工作民主评议结果排名
 */
@Data
public class ReportLeaderWorkRankDTO {
    private String id;
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    private String depart;
    private String year;
    private String rank;
    private double rate;
    private String rateText;
}
