package org.jeecg.modules.sys.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AssessAnnualAccountabilityDTO {
    String personnel;
    String currentYear;
    String depart;
    String typeText;
    String referenceNo;
    String reason;
    String processingDepart;
    Date effectiveDate;
    Date endDate;
    String remark;
}
