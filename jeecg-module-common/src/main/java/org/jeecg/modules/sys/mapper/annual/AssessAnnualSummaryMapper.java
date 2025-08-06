package org.jeecg.modules.sys.mapper.annual;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.sys.entity.annual.AssessAnnualSummary;

/**
 * @Description: 年度考核成绩汇总
 * @Author: jeecg-boot
 * @Date: 2024-09-10
 * @Version: V1.0
 */
public interface AssessAnnualSummaryMapper extends BaseMapper<AssessAnnualSummary> {
    @Deprecated
    String getAssessLastYear();
}
