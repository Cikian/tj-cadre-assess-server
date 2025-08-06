package org.jeecg.modules.regular.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegularExportDTO {

    private String id;
    private String name;
    private String sex;
    private String birthDate;
    private String departmentCode;
    private String post;
    private String positionType;
    private String currentYear;
    private String quarter1;
    private String quarter2;
    private String quarter3;
    private String quarter4;
    private String adminDepartment;
}
