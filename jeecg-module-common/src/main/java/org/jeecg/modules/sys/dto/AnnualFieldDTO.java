package org.jeecg.modules.sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class AnnualFieldDTO {

    private String currentYear;

    private String assessName;

    private String depart;

    private Integer status;

    private Date deadline;
}
