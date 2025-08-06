package org.jeecg.modules.regular.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class RegularFieldDTO {

    private String assessName;

    private String currentYear;

    private String departmentCode;

    private Date deadline;

    private String status;
}
