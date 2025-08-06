package org.jeecg.modules.sys.dto;

import lombok.Data;

@Data
public class AssessAnnualArrangeDTO {
    String depart;
    Integer peopleNum;
    String assessScope;
    Integer assessPartinNum;
    String leaders;
    String nonAssess;
    String remark;
    String contacts;
    String contactsPhone;
    String officeTel;
}
