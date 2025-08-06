package org.jeecg.modules.annual.vo;

import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class AssessAnnualListVo {
    private String id;

    private String createBy;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String updateBy;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String sysOrgCode;

    @Dict(dicCode = "assess_year")
    private String currentYear;

    private String assessName;

    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    private String depart;

    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    private String leader;

    @Dict(dicCode = "report_status")
    private Integer status;

    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    private String reportBy;

    private String remark;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date deadline;

    private boolean accountability;

    private boolean vacation;

    private boolean recommend;

    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    private String evaluation;
}
