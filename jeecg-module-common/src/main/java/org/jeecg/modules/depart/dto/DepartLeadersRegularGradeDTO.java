package org.jeecg.modules.depart.dto;

import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;

@Data
public class DepartLeadersRegularGradeDTO {
    private String hashId;
    private String realname;
    private String roleCode;
    private String roleName;
    @Dict(dicCode = "regular_assess_level")
    private String quarter1;
    @Dict(dicCode = "regular_assess_level")
    private String quarter2;
    @Dict(dicCode = "regular_assess_level")
    private String quarter3;
    @Dict(dicCode = "regular_assess_level")
    private String quarter4;
    private String dutyReport;
    // chief：处级正职，deputy：处级副职，group：领导班子
    @Dict(dicCode = "annual_type")
    private String type;
}
