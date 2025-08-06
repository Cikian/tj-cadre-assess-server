package org.jeecg.modules.sys.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AssessAnnualVacationDTO {
    String currentYear;
    String name;
    String depart;
    Integer duty;
    Integer type;
    Date startDate;
    Date endDate;
    String remark;
}
