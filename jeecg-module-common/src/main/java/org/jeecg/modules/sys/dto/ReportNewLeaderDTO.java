package org.jeecg.modules.sys.dto;


import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;

import java.math.BigDecimal;

/**
 * @className: ReportNewLeaderDTO
 * @author: Cikian
 * @date: 2024/11/26 14:41
 * @Version: 1.0
 * @description: 新提拔干部汇总DTO
 */

@Data
public class ReportNewLeaderDTO {
    private String id;
    private String currentYear;
    private int sort;
    private String name;
    @Dict(dictTable = "sys_depart", dicText = "alias", dicCode = "id")
    private String depart;
    private String originalPosition;
    private String originalDate;
    private String currentPosition;
    private String curDate;
    private String hashId;
    private BigDecimal recognitionRate;
    private BigDecimal disagreementRate;
    private BigDecimal identityRate;
    private BigDecimal unknownRate;
}
