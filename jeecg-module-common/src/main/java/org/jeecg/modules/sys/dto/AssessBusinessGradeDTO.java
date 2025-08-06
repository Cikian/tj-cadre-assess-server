package org.jeecg.modules.sys.dto;

import lombok.Data;

@Data
public class AssessBusinessGradeDTO {
    String currentYear;
    String departId;
    String score;
    Integer level;
    Integer departType;
    String rankingVirtual;
    Integer departOrder;
    String departName;
}
