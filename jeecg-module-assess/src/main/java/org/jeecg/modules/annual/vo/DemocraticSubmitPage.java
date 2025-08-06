package org.jeecg.modules.annual.vo;

import lombok.Data;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluationItem;

import java.util.List;

@Data
public class DemocraticSubmitPage {
    private String appraisee;
    private String currentYear;
    private String depart;
    private String evaluationSummaryId;
    private String overallEvaluation;
    private String type;
    private List<AssessDemocraticEvaluationItem> items;
    private DemocraticSubmitOptionsVO options;
}
