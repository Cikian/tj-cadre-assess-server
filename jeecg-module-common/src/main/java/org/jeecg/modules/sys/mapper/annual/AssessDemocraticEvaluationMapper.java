package org.jeecg.modules.sys.mapper.annual;

import java.math.BigDecimal;
import java.util.List;
import org.jeecg.modules.sys.entity.annual.AssessDemocraticEvaluation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: 民主测评填报
 * @Author: jeecg-boot
 * @Date:   2024-09-12
 * @Version: V1.0
 */
public interface AssessDemocraticEvaluationMapper extends BaseMapper<AssessDemocraticEvaluation> {

  /**
   * 通过主表id删除子表数据
   *
   * @param mainId 主表id
   * @return boolean
   */
	public boolean deleteByMainId(@Param("mainId") String mainId);

  /**
   * 通过主表id查询子表数据
   *
   * @param mainId 主表id
   * @return List<AssessDemocraticEvaluation>
   */
	public List<AssessDemocraticEvaluation> selectByMainId(@Param("mainId") String mainId);

    @Deprecated
    int getCountsBySummaryId(@Param("summaryId") String summaryId);

    @Deprecated
    BigDecimal getSumBySummaryId(@Param("summaryId") String summaryId);
}
