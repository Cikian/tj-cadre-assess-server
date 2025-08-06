package org.jeecg.modules.sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class BusinessFieldDTO {

    private String assessName;

    private String currentYear;

    private Date deadline;

    private String reportDepart;

    private String assessDepart;

    private Integer status;
}
