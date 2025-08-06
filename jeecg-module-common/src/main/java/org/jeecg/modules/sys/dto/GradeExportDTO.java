package org.jeecg.modules.sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GradeExportDTO {

    private java.lang.String id;
    private java.lang.String currentYear;
    private java.lang.String departId;
    private java.lang.String alias;
    private java.math.BigDecimal addPoints;
    private java.lang.String addReason;
    private java.math.BigDecimal subtractPoints;
    private java.lang.String subtractReason;
    private java.math.BigDecimal assessScore;
    private java.math.BigDecimal score;
    private java.lang.Integer ranking;
    private java.lang.String level;
    private java.lang.String commend;
    private java.lang.String denounce;
    private java.lang.Integer departOrder;
}