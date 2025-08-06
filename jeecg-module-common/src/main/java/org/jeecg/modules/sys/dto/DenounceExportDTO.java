package org.jeecg.modules.sys.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DenounceExportDTO {

    private java.lang.String id;
    private java.lang.String currentYear;
    private java.lang.String alias;
    private java.lang.String denounceUnit;
    private java.lang.String denounceProject;
    private java.util.Date denounceTime;
}
