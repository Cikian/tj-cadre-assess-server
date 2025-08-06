package org.jeecg.modules.sys.dto;

import lombok.Data;
import org.jeecg.modules.sys.entity.report.AssessReportArrange;
import org.jeecg.modules.sys.entity.report.AssessReportFill;
import org.jeecg.modules.sys.entity.report.AssessReportNewLeader;
import org.jeecg.modules.sys.entity.report.AssessReportObjectInfo;

import java.util.List;

/**
 * @Title: ReportFillDTO
 * @Author Cikian
 * @Package org.jeecg.modules.report.dto
 * @Date 2024/10/6 22:19
 * @description: jeecg-boot-parent:
 */

@Data
public class ReportFillDTO {
    private AssessReportFill reportFill;
    private AssessReportArrange reportArrange;
    private List<AssessReportNewLeader> reportNewLeader;
    private List<AssessReportObjectInfo> reportObjectInfo;
}
