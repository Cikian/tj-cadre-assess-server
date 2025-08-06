package org.jeecg.modules.sys.mapper.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.sys.dto.ArrangeDTO;
import org.jeecg.modules.sys.entity.report.AssessReportArrange;

import java.util.List;

/**
 * @Description: 一报告两评议工作安排
 * @Author: jeecg-boot
 * @Date:   2024-09-24
 * @Version: V1.0
 */
public interface AssessReportArrangeMapper extends BaseMapper<AssessReportArrange> {
    Integer selectEvaluationNumByDepartId(String departId, String fillId);

    List<ArrangeDTO> queryAllArrangeByYear(@Param("year") String year);

}
