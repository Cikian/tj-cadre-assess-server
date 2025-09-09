package org.jeecg.modules.sys.vo;

import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;

import java.util.Date;

@Data
public class DemocraticFillListVO {
    private String summaryId;

    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    private String departId;

    @Dict(dicCode = "assess_uear")
    private String currentYear;

    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    private String appraisee;

    private String assessName;

    @Dict(dicCode = "democratic_type")
    private String type;

    private Date endDate;
}
