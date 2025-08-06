package org.jeecg.modules.sys.dto;

import lombok.Data;

@Data
public class AssessBusinessDepartGradeDTO {
    String currentYear;
    String departId;
    String assessScore;
    String score;
    String addPoints;
    String subtractPoints;
    Integer level;
    Integer departType;
    String ranking;
    String addReason;
    String subtractReason;
    String assessSubReason;
}
