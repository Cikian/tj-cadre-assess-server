package org.jeecg.modules.report.vo;

import lombok.Data;

import java.util.Date;

@Data
public class AssessReportDemocracyNewLeaderVO {
    private String newLeaderItem;
    private String name;
    private Date birthday;
    private String originalPosition;
    private String currentPosition;
    private String fill;
    private String reason;
}
