package org.jeecg.modules.sys.mapper.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.sys.dto.ReportNewLeaderDTO;
import org.jeecg.modules.sys.entity.report.AssessReportNewLeader;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

/**
 * @Description: 新提拔干部名册
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
public interface AssessReportNewLeaderMapper extends BaseMapper<AssessReportNewLeader> {
    List<ReportNewLeaderDTO> getNewLeaderSummaryByYear(String year, String column, String sort);
}
