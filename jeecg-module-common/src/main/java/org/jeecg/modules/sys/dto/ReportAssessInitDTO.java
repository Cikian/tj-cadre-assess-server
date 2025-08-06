package org.jeecg.modules.sys.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @Title: BusinessAssessDepartFillDTO
 * @Author Cikian
 * @Package org.jeecg.modules.dto
 * @Date 2024/8/22 23:47
 * @description: jeecg-boot-parent:
 */

@Data
public class ReportAssessInitDTO {
    private String assessName;
    private String currentYear;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date deadline;
    private String scope;
}
