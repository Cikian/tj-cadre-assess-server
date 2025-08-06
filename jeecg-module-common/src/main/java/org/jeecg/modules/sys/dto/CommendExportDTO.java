package org.jeecg.modules.sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommendExportDTO {

    private java.lang.String id;
    private java.lang.String currentYear;
    private java.lang.String alias;
    private java.lang.String commendUnit;
    private java.lang.String commendProject;
    private java.util.Date commendTime;

}
