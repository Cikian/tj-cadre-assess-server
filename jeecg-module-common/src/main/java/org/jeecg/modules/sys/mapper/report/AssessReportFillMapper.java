package org.jeecg.modules.sys.mapper.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.sys.dto.ReportFieldDTO;
import org.jeecg.modules.sys.entity.report.AssessReportFill;

import java.util.List;

/**
 * @Description: 一报告两评议填报
 * @Author: jeecg-boot
 * @Date: 2024-09-24
 * @Version: V1.0
 */
public interface AssessReportFillMapper extends BaseMapper<AssessReportFill> {

    List<ReportFieldDTO> getAllItems(@Param("currentYear") String currentYear);
}
