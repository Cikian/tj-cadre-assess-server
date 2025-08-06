package org.jeecg.modules.sys.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.modules.sys.entity.annual.AssessAnnualSummary;

import java.util.Date;
import java.util.List;

/**
 * @Title: BusinessAssessDepartFillDTO
 * @Author Cikian
 * @Package org.jeecg.modules.dto
 * @Date 2024/8/22 23:47
 * @description: jeecg-boot-parent:
 */

@Data
public class AnnualAssessDepartFillDTO {
    private String assessName;
    private String currentYear;
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date deadline;

    List<AssessAnnualSummary> summaryList;


}
