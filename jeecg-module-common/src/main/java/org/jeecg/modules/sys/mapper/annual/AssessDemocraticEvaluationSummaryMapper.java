package org.jeecg.modules.sys.mapper.annual;

import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluationSummary;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @Description: 民主测评汇总
 * @Author: jeecg-boot
 * @Date:   2024-09-12
 * @Version: V1.0
 */
public interface AssessDemocraticEvaluationSummaryMapper extends BaseMapper<AssessDemocraticEvaluationSummary> {
    List<AssessDemocraticEvaluationSummary> selectByGroup(String year);
}
