package org.jeecg.modules.report.vo;

import lombok.Data;
import org.jeecg.modules.sys.entity.report.AssessReportEvaluationItem;
import org.jeecg.modules.sys.entity.report.AssessReportEvaluationNewleader;

import java.util.List;

@Data
public class AssessReportDemocracyFillVO {
    private String summaryId;
    private List<AssessReportEvaluationItem> items;
    private List<AssessReportEvaluationNewleader> newLeadersItems;
    private String reason;
}
